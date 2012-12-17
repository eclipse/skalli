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

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

import org.easymock.Capture;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.ValidationException;
import org.eclipse.skalli.model.ext.devinf.DevInfProjectExt;
import org.eclipse.skalli.model.ext.maven.MavenCoordinateUtil;
import org.eclipse.skalli.model.ext.maven.MavenPomResolver;
import org.eclipse.skalli.model.ext.maven.MavenProjectExt;
import org.eclipse.skalli.model.ext.maven.MavenReactor;
import org.eclipse.skalli.model.ext.maven.MavenReactorProjectExt;
import org.eclipse.skalli.services.project.ProjectService;
import org.eclipse.skalli.testutil.TestUUIDs;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("nls")
public class MavenResolverRunnableTest {

    private static final UUID PROJECT_UUID = UUID.fromString("e4d78581-08da-4f04-8a90-a7dac41f6247");

    private static final String GIT_SCM_LOCATION = "scm:git:git://git.example.org/myproject";

    private static final String REACTOR_POM_PATH = "pom.xml";
    private static final String USERID = "USERID";

    private ProjectService projectServiceMock;
    private MavenPomResolver pathResolverMock;
    private MavenResolverMock resolverMock;

    private MavenResolverRunnableMock classUnderTest;

    private class MavenResolverRunnableMock extends MavenResolverRunnable {

        private MavenResolverMock resolverMock;

        public MavenResolverRunnableMock(MavenResolverMock resolverMock) {
            super(null, USERID);
            this.resolverMock = resolverMock;
        }

        @Override
        protected MavenPomResolver getMavenPomResolver(String scmLocation) {
            return pathResolverMock;
        }

        @Override
        protected MavenResolver getMavenResolver(UUID entityId, MavenPomResolver pathResolver) {
            return resolverMock;
        }

        @Override
        protected ProjectService getProjectService() {
            return projectServiceMock;
        }
    }

    private class MavenResolverMock extends MavenResolver {

        private boolean isResolveCalled = false;
        private MavenReactor mavenReactor;

        public MavenResolverMock(MavenPomResolver pathResolver) {
            super(TestUUIDs.TEST_UUIDS[0], pathResolver);
        }

        @Override
        public MavenReactor resolve(String scmLocation, String relativePath)
                throws IOException, ValidationException {
            isResolveCalled = true;
            return mavenReactor;
        }

        public boolean isResolveCalled() {
            return isResolveCalled;
        }

        public void setMavenReactor(MavenReactor reactor) {
            this.mavenReactor = reactor;
        }
    }

    @Before
    public void setUp() {
        pathResolverMock = createNiceMock(MavenPomResolver.class);
        replay(pathResolverMock);
        resolverMock = new MavenResolverMock(pathResolverMock);
        classUnderTest = new MavenResolverRunnableMock(resolverMock);
    }

    @Test
    public void testResolveProject() throws Exception {
        MavenReactor mavenReactor = createReactor();
        resolverMock.setMavenReactor(mavenReactor);

        Project project = new Project();
        addScmLocation(project, GIT_SCM_LOCATION);
        addReactorPath(project, REACTOR_POM_PATH);

        assertEquals(mavenReactor, classUnderTest.resolveProject(project, null, null));
        assertTrue(resolverMock.isResolveCalled());
    }

    @Test
    public void testResolveProjectNoExtensions() throws Exception {
        Project project = new Project();
        assertNull(classUnderTest.resolveProject(project, null, null));
        assertFalse(resolverMock.isResolveCalled());
    }

    @Test
    public void testResolveProjectBlankScmLocation() throws Exception {
        Project project = new Project();
        addScmLocation(project, null);
        assertNull(classUnderTest.resolveProject(project, null, null));
        assertFalse(resolverMock.isResolveCalled());
    }

    @Test
    public void testResolveProjectBlankReactorPath() throws Exception {
        Project project = new Project();
        addReactorPath(project, null);
        assertNull(classUnderTest.resolveProject(project, null, null));
        assertFalse(resolverMock.isResolveCalled());
    }


    @Test
    public void testRunSingleProject() throws Exception {
        MavenReactor mavenReactor = createReactor();
        resolverMock.setMavenReactor(mavenReactor);

        Project project = new Project();
        project.setUuid(PROJECT_UUID);
        addScmLocation(project, GIT_SCM_LOCATION);
        addReactorPath(project, REACTOR_POM_PATH);
        Capture<Project> capturedProject = new Capture<Project>();
        setupProjectService(project, capturedProject);

        classUnderTest.run();

        Project persistedProject = capturedProject.getValue();
        assertEquals(project, persistedProject);
        assertEquals(mavenReactor, getReactor(persistedProject));
        assertTrue(resolverMock.isResolveCalled());
    }

    private void setupProjectService(Project projectToPersist, Capture<Project> capturedProject) throws Exception {
        projectServiceMock = createNiceMock(ProjectService.class);
        projectServiceMock.keySet();
        expectLastCall().andReturn(Collections.singleton(PROJECT_UUID)).anyTimes();
        projectServiceMock.getByUUID(eq(PROJECT_UUID));
        expectLastCall().andReturn(projectToPersist).anyTimes();
        projectServiceMock.persist(capture(capturedProject), eq(USERID));
        replay(projectServiceMock);
    }

    private void addScmLocation(Project project, String scmLocation) {
        DevInfProjectExt devinfExt = new DevInfProjectExt();
        devinfExt.addScmLocation(scmLocation);
        project.addExtension(devinfExt);
    }

    private void addReactorPath(Project project, String reactorPath) {
        MavenProjectExt mavenExt = new MavenProjectExt();
        mavenExt.setReactorPOM(reactorPath);
        project.addExtension(mavenExt);
    }

    private MavenReactor getReactor(Project project) {
        MavenReactorProjectExt ext = project.getExtension(MavenReactorProjectExt.class);
        return ext != null ? ext.getMavenReactor() : null;
    }

    private MavenReactor createReactor() {
        MavenReactor reactor = new MavenReactor();
        reactor.setCoordinate(MavenCoordinateUtil.TEST_PARENT_COORD);
        reactor.addModules(MavenCoordinateUtil.TEST_MODULES);
        return reactor;
    }
}
