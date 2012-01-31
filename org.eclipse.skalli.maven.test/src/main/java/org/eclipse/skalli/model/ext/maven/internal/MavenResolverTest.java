/*******************************************************************************
 * Copyright (c) 2010, 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.model.ext.maven.internal;

import static org.apache.http.HttpStatus.*;
import static org.easymock.EasyMock.*;
import static org.eclipse.skalli.model.ext.maven.MavenCoordinateUtil.*;
import static org.eclipse.skalli.model.ext.maven.MavenPomUtility.*;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.model.ext.maven.MavenModule;
import org.eclipse.skalli.model.ext.maven.MavenPathResolver;
import org.eclipse.skalli.model.ext.maven.MavenPomUtility;
import org.eclipse.skalli.model.ext.maven.MavenProjectExt;
import org.eclipse.skalli.model.ext.maven.MavenReactor;
import org.eclipse.skalli.model.ext.maven.MavenReactorProjectExt;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.destination.DestinationService;
import org.eclipse.skalli.testutil.BundleManager;
import org.eclipse.skalli.testutil.HttpServerMock;
import org.eclipse.skalli.testutil.PropertyHelperUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

@SuppressWarnings("nls")
public class MavenResolverTest {

    private static final String SCM_LOCATION = "scm";

    private class MavenResolverMock extends MavenResolver {

        private HashMap<String, InputStream> testContent = new HashMap<String, InputStream>();

        public MavenResolverMock(MavenPomParser parser, MavenPathResolver pathResolver,
                DestinationService destinationService) {
            super(PropertyHelperUtils.TEST_UUIDS[0], parser, pathResolver, destinationService);
        }

        @Override
        protected MavenPom getMavenPom(String scmLocation, String relativePath)
                throws IOException, MavenValidationException {
            return parser.parse(getStream(relativePath));
        }

        private InputStream getStream(String relativePath) {
            InputStream result = testContent.get(relativePath);
            if (result == null) {
                result = new ByteArrayInputStream(relativePath.getBytes());
                testContent.put(relativePath, result);
            }
            return result;
        }
    }

    private MavenPomParser parserMock;
    private MavenPathResolver pathResolver;
    private MavenReactor expectedMavenProject;
    private MavenPom reactorPom;
    private String reactorPath;
    private static HttpServerMock mmus;
    private static DestinationService destinationService;

    @BeforeClass
    public static void setUpOnce() throws Exception {
        BundleManager.startBundles();
        mmus = new HttpServerMock();
        mmus.start();
        destinationService = Services.getRequiredService(DestinationService.class);
    }

    @AfterClass
    public static void tearDownOnce() throws Exception {
        mmus.stop();
    }

    @Before
    public void setUp() {
        parserMock = createNiceMock(MavenPomParser.class);
        pathResolver = createNiceMock(MavenPathResolver.class);
        expect(pathResolver.canResolve(SCM_LOCATION)).andReturn(true).anyTimes();
        replay(pathResolver);
        expectedMavenProject = new MavenReactor();
        expectedMavenProject.setCoordinate(TEST_PARENT_COORD);
        reactorPom = new MavenPom();
        reactorPath = "";
    }

    @Test
    public void testPomNoParent() throws Exception {
        MavenResolverMock mavenResolver = new MavenResolverMock(parserMock, pathResolver, destinationService);
        InputStream in = mavenResolver.getStream(reactorPath);
        reactorPom.setSelf(TEST_PARENT_COORD);
        expect(parserMock.parse(in)).andReturn(reactorPom);
        replay(parserMock);

        assertMavenProject(mavenResolver);
    }

    @Test
    public void testPomWithParent() throws Exception {
        MavenResolverMock mavenResolver = new MavenResolverMock(parserMock, pathResolver, destinationService);
        InputStream in = mavenResolver.getStream(reactorPath);
        reactorPom.setSelf(new MavenModule(null, PARENT_ARTIFACT, PARENT_PACKAGING));
        reactorPom.setParent(getParentCoordinates());
        expect(parserMock.parse(in)).andReturn(reactorPom);
        replay(parserMock);

        assertMavenProject(mavenResolver);
    }

    @Test
    public void testPomWithModules() throws Exception {
        MavenResolverMock mavenResolver = new MavenResolverMock(parserMock, pathResolver, destinationService);
        InputStream parentPom = mavenResolver.getStream(reactorPath);
        InputStream modulePom1 = mavenResolver.getStream(MODULE1);
        InputStream modulePom2 = mavenResolver.getStream(MODULE2);

        reactorPom.setSelf(TEST_PARENT_COORD);
        reactorPom.getModuleTags().add(MODULE1);
        reactorPom.getModuleTags().add(MODULE2);
        expect(parserMock.parse(parentPom)).andReturn(reactorPom);
        expect(parserMock.parse(modulePom1)).andReturn(asModulePom(TEST_PARENT_COORD, MODULE1));
        expect(parserMock.parse(modulePom2)).andReturn(asModulePom(TEST_PARENT_COORD, MODULE2));
        replay(parserMock);

        expectedMavenProject.addModule(getModuleCoordinate(MODULE1));
        expectedMavenProject.addModule(getModuleCoordinate(MODULE2));

        assertMavenProject(mavenResolver);
    }

    @Test
    public void testPomWithModulesContainingModules() throws Exception {
        String module2Path = MODULE1 + "/" + MODULE2;
        MavenResolverMock mavenResolver = new MavenResolverMock(parserMock, pathResolver, destinationService);
        InputStream reactorPomStream = mavenResolver.getStream(reactorPath);
        InputStream modulePomStream1 = mavenResolver.getStream(MODULE1);
        InputStream modulePomStream2 = mavenResolver.getStream(module2Path);

        reactorPom.setSelf(TEST_PARENT_COORD);
        reactorPom.getModuleTags().add(MODULE1);
        expect(parserMock.parse(reactorPomStream)).andReturn(reactorPom);

        MavenPom module1 = asModulePom(TEST_PARENT_COORD, MODULE1);
        module1.getModuleTags().add(MODULE2);
        expect(parserMock.parse(modulePomStream1)).andReturn(module1);

        MavenPom module2 = asModulePom(module1.getSelf(), MODULE2);
        expect(parserMock.parse(modulePomStream2)).andReturn(module2);

        replay(parserMock);

        expectedMavenProject.addModule(getModuleCoordinate(MODULE1));
        expectedMavenProject.addModule(getModuleCoordinate(MODULE2));

        assertMavenProject(mavenResolver);
    }

    @Test
    public void testGetMavenPom() throws Exception {
        mmus.addContent("testGetMaven", MavenPomUtility.getPomWithParentAndModules());
        URL url = new URL("http://" + mmus.getHost() + ":" + mmus.getPort() + "/testGetMaven/" + SC_OK);
        MavenResolverMock mavenResolver = new MavenResolverMock(new MavenPomParserImpl(), pathResolver,
                destinationService);

        MavenPom expectedPom = new MavenPom();
        expectedPom.setSelf(getCoordinatesWithoutGroupId());
        expectedPom.setParent(getParentCoordinates());
        expectedPom.setParentRelativePath(PARENT_RELATIVE_PATH);
        addModules(expectedPom);

        MavenPom mavenPom = mavenResolver.getMavenPom(url);
        assertEquals(expectedPom, mavenPom);
    }

    @Test
    public void testGetMavenPomThrowsMavenValidationException() throws Exception {
        assertIssues(SC_NOT_FOUND, SC_UNAUTHORIZED, SC_INTERNAL_SERVER_ERROR,
                SC_SERVICE_UNAVAILABLE, SC_GATEWAY_TIMEOUT, SC_INSUFFICIENT_STORAGE,
                SC_MOVED_PERMANENTLY);
    }

    private void assertIssues(int... expectedResponseCodes) throws Exception {
        for (int expectedResponseCode : expectedResponseCodes) {
            URL url = new URL("http://" + mmus.getHost() + ":" + mmus.getPort() + "/" + expectedResponseCode);
            MavenResolverMock mavenResolver = new MavenResolverMock(parserMock, pathResolver, destinationService);
            try {
                mavenResolver.getMavenPom(url);
            } catch (MavenValidationException e) {
                assertEquals(1, e.getIssues().size());
                Issue issue = e.getIssues().first();
                switch (expectedResponseCode) {
                case SC_UNAUTHORIZED:
                case SC_INTERNAL_SERVER_ERROR:
                case SC_SERVICE_UNAVAILABLE:
                case SC_GATEWAY_TIMEOUT:
                case SC_INSUFFICIENT_STORAGE:
                case SC_MOVED_PERMANENTLY:
                    assertEquals(Severity.WARNING, issue.getSeverity());
                    break;
                default:
                    assertEquals(Severity.ERROR, issue.getSeverity());
                    break;
                }
                assertEquals(PropertyHelperUtils.TEST_UUIDS[0], issue.getEntityId());
                assertEquals(MavenProjectExt.class, issue.getExtension());
                assertEquals(MavenReactorProjectExt.PROPERTY_MAVEN_REACTOR, issue.getPropertyId());
            }
        }
    }

    private void assertMavenProject(MavenResolverMock mavenResolver) throws Exception {
        MavenReactor actual = mavenResolver.resolve(SCM_LOCATION, reactorPath);
        assertEquals(expectedMavenProject, actual);
    }

    private MavenModule getModuleCoordinate(String moduleName) {
        return new MavenModule(GROUPID, moduleName, PACKAGING);
    }

    private MavenPom asModulePom(MavenModule parent, String moduleName) {
        MavenPom pom = new MavenPom();
        pom.setSelf(getModuleCoordinate(moduleName));
        pom.setParent(parent);
        return pom;
    }
}
