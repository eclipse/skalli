/*******************************************************************************
 * Copyright (c) 2010-2014 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.model.ext.maven.internal;

import static org.eclipse.skalli.model.ext.maven.MavenCoordinateUtil.*;
import static org.eclipse.skalli.model.ext.maven.MavenPomUtility.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import org.eclipse.skalli.model.ValidationException;
import org.eclipse.skalli.model.ext.maven.MavenModule;
import org.eclipse.skalli.model.ext.maven.MavenPomResolver;
import org.eclipse.skalli.model.ext.maven.MavenReactor;
import org.eclipse.skalli.testutil.TestUUIDs;
import org.junit.Test;

@SuppressWarnings("nls")
public class MavenResolverTest {

    private static final String SCM_LOCATION = "scm";
    private static final String RELATIVE_ROOT_POM_PATH = "";

    /**
     * use {@link MavenPomResolverMock#addExpectedPom(String, MavenPom)} to add poms,
     * which should be returned when {@link MavenPomResolverMock#getMavenPom(UUID, String, String)}
     * is called.
     */
    class MavenPomResolverMock implements MavenPomResolver {

        private String scmLocation;

        public MavenPomResolverMock(String scmLocation) {
            super();
            this.scmLocation = scmLocation;
        }

        void addExpectedPom(String relativePath, MavenPom toReturnPom) {
            pomMap.put(relativePath, toReturnPom);
        }

        private HashMap<String, MavenPom> pomMap = new HashMap<String, MavenPom>();

        @Override
        public MavenPom getMavenPom(UUID project, String scmLocation, String relativePath) throws IOException,
                ValidationException {
            if (this.scmLocation.equals(scmLocation)) {
                return pomMap.get(relativePath);
            } else {
                return null;
            }
        }

        @Override
        public boolean canResolve(String scmLocation) {
            return true;
        }
    }

    @Test
    public void testPomNoParent() throws Exception {
        MavenPomResolverMock pomReslover = new MavenPomResolverMock(SCM_LOCATION);
        pomReslover.addExpectedPom(RELATIVE_ROOT_POM_PATH, getParentPom());

        MavenReactor expected = new MavenReactor();
        expected.setCoordinate(TEST_PARENT_COORD);

        MavenResolver resolver = new MavenResolver(TestUUIDs.TEST_UUIDS[0], pomReslover);
        assertThat(resolver.resolve(SCM_LOCATION, RELATIVE_ROOT_POM_PATH), is(expected));
    }

    @Test
    public void testPomWithParent() throws Exception {
        MavenPom mavenParentPom = new MavenPom();
        mavenParentPom.setSelf(new MavenModule(null, PARENT_ARTIFACT, PARENT_PACKAGING)); //group is null! it has to be calculated via parent.GroupId
        mavenParentPom.setParent(getParentCoordinates());

        MavenPomResolverMock pomReslover = new MavenPomResolverMock(SCM_LOCATION);
        pomReslover.addExpectedPom(RELATIVE_ROOT_POM_PATH, mavenParentPom);

        MavenReactor expected = new MavenReactor();
        expected.setCoordinate(TEST_PARENT_COORD);

        MavenResolver resolver = new MavenResolver(TestUUIDs.TEST_UUIDS[0], pomReslover);
        assertThat(resolver.resolve(SCM_LOCATION, RELATIVE_ROOT_POM_PATH), is(expected));
    }

    @Test
    public void testPomWithModules() throws Exception {
        MavenPom parentPom = getParentPom();
        MavenPom module1 = asModulePom(TEST_PARENT_COORD, MODULE1);
        MavenPom module2 = asModulePom(TEST_PARENT_COORD, MODULE2);
        parentPom.getModuleTags().add(MODULE1);
        parentPom.getModuleTags().add(MODULE2);

        MavenPomResolverMock pomReslover = new MavenPomResolverMock(SCM_LOCATION);
        pomReslover.addExpectedPom(RELATIVE_ROOT_POM_PATH, parentPom);
        pomReslover.addExpectedPom(MODULE1, module1);
        pomReslover.addExpectedPom(MODULE2, module2);

        MavenReactor expected = new MavenReactor();
        expected.setCoordinate(TEST_PARENT_COORD);
        expected.addModule(getModuleCoordinate(MODULE1));
        expected.addModule(getModuleCoordinate(MODULE2));

        MavenResolver resolver = new MavenResolver(TestUUIDs.TEST_UUIDS[0], pomReslover);
        assertThat(resolver.resolve(SCM_LOCATION, RELATIVE_ROOT_POM_PATH), is(expected));
    }

    @Test
    public void testPomWithModulesContainingModules() throws Exception {
        MavenPom parentPom = getParentPom();
        MavenPom module1 = asModulePom(TEST_PARENT_COORD, MODULE1);
        MavenPom module2 = asModulePom(TEST_PARENT_COORD, MODULE2);
        parentPom.getModuleTags().add(MODULE1);
        module1.getModuleTags().add(MODULE2);

        MavenPomResolverMock pomReslover = new MavenPomResolverMock(SCM_LOCATION);
        pomReslover.addExpectedPom(RELATIVE_ROOT_POM_PATH, parentPom);
        pomReslover.addExpectedPom(MODULE1, module1);
        pomReslover.addExpectedPom(MODULE1 + "/" + MODULE2, module2);

        MavenReactor expected = new MavenReactor();
        expected.setCoordinate(TEST_PARENT_COORD);
        expected.addModule(getModuleCoordinate(MODULE1));
        expected.addModule(getModuleCoordinate(MODULE2));

        MavenResolver resolver = new MavenResolver(TestUUIDs.TEST_UUIDS[0], pomReslover);
        assertThat(resolver.resolve(SCM_LOCATION, RELATIVE_ROOT_POM_PATH), is(expected));

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

    private MavenPom getParentPom() {
        MavenPom mavenParentPom = new MavenPom();
        mavenParentPom.setSelf(new MavenModule(PARENT_GROUPID, PARENT_ARTIFACT, PARENT_PACKAGING));
        return mavenParentPom;
    }
}
