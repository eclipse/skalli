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
package org.eclipse.skalli.view.internal.filter.ext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.model.ext.commons.InfoExtension;
import org.eclipse.skalli.model.ext.devinf.DevInfProjectExt;
import org.eclipse.skalli.services.gerrit.GerritServerConfig;
import org.eclipse.skalli.testutil.AssertUtils;
import org.eclipse.skalli.testutil.TestUUIDs;
import org.junit.Test;

@SuppressWarnings("nls")
public class GitGerritFilterTest {

    private static final String SCM_PATTERN_WITH_ALL_VARIABLES =
            "${protocol}:${host}:${port}:${parent}:${branch}:" +
            "${userId}:${user}:${user.userId}:${user.firstname}:${user.lastname}:${user.email}:${user.location}:" +
            "${uuid}:${projectId}:${name}:${description}:${info.pageUrl}";

    private static final String EXPECTED_SCM_LOCATION = "ssh:example.org:29418:foo/bar:branch:" +
            "homer:Homer Simpson:homer:Homer:Simpson:homer@example.org:Springfield:"+
            TestUUIDs.TEST_UUIDS[0].toString() +
            ":technology.skalli:Skalli:Extensible system for organizing project:http://example.org";

    private static final String DESCRIPTION_WITH_ALL_VARIABLES =
            "${userId}:${user}:${user.userId}:${user.firstname}:${user.lastname}:${user.email}:${user.location}:" +
            "${uuid}:${projectId}:${name}:${description}:${info.pageUrl}:" +
            "${link}:${foo}";

    private static final String EXPECTED_DESCRIPTION =
            "homer:Homer Simpson:homer:Homer:Simpson:homer@example.org:Springfield:"+
            TestUUIDs.TEST_UUIDS[0].toString() +
            ":technology.skalli:Skalli:Extensible system for organizing project:http://example.org" +
            ":http://example.org/projects/technology.skalli:bar";

    private static final String MATCHING_SCM_PATTERN = "scm:git:ssh://example.org:29418/skalli";
    private static final String NOT_MATCHING_SCM_PATTERN = "scm:git:git://example.net:1234/skalli";

    @Test
    public void testGetDefaultScmLocation() throws Exception {
        assertDefaultScmLocation(GitGerritFilter.DEFAULT_SCM_TEMPLATE);
        assertDefaultScmLocation(null);
        assertDefaultScmLocation("");
    }

    private void assertDefaultScmLocation(String scmPattern) {
        GerritServerConfig gerritConfig = createGerritConfig(scmPattern);
        Project project = createProject();
        User user = createUser();
        GitGerritFilter filter = new GitGerritFilter();
        assertEquals("scm:git:ssh://example.org:29418/skalli.git",
                filter.getScmLocation(gerritConfig, "skalli", project, user));
    }

    @Test
    public void testGetScmLocationAllProps() throws Exception {
        GerritServerConfig gerritConfig = createGerritConfig(SCM_PATTERN_WITH_ALL_VARIABLES);
        Project project = createProject();
        User user = createUser();
        GitGerritFilter filter = new GitGerritFilter();
        assertEquals(EXPECTED_SCM_LOCATION, filter.getScmLocation(gerritConfig, "skallicore", project, user));
    }

    @Test
    public void testGetDefaultDescription() throws Exception {
        assertDefaultDescription(GitGerritFilter.DEFAULT_DESCRIPTION);
        assertDefaultDescription(null);
        assertDefaultDescription("");
    }

    private void assertDefaultDescription(String description) {
        Project project = createProject();
        User user = createUser();
        Map<String, String> properties = new HashMap<String, String>();
        GitGerritFilter filter = new GitGerritFilter();
        assertEquals("Created by Homer Simpson. More details: http://example.org/projects/technology.skalli",
                filter.getDescription(description, "http://example.org", project, user, properties));
    }

    @Test
    public void testGetDescriptionAllProps() throws Exception {
        Project project = createProject();
        User user = createUser();
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("foo", "bar");
        GitGerritFilter filter = new GitGerritFilter();
        assertEquals(EXPECTED_DESCRIPTION,
                filter.getDescription(DESCRIPTION_WITH_ALL_VARIABLES, "http://example.org", project, user, properties));
    }

    @Test
    public void testGetRepositoryNames() throws Exception {
        Pattern scmPattern = Pattern.compile("scm:git:ssh://example.org:29418/(.*).git");
        List<Project> projects = createProjectsWithScmLocations();
        GitGerritFilter filter = new GitGerritFilter();
        Set<String> names = filter.getRepositoryNames(projects, scmPattern);
        assertNotNull(names);
        AssertUtils.assertEquals("getRepositoryNames", names,
                "skalli" + TestUUIDs.TEST_UUIDS[0].toString(),
                "skalli" + TestUUIDs.TEST_UUIDS[3].toString());
    }

    private GerritServerConfig createGerritConfig(String scmPattern) {
        GerritServerConfig gerritConfig = new GerritServerConfig();
        gerritConfig.setProtocol("ssh");
        gerritConfig.setHost("example.org");
        gerritConfig.setPort("29418");
        gerritConfig.setParent("foo/bar");
        gerritConfig.setBranch("branch");
        gerritConfig.setScmTemplate(scmPattern);
        return gerritConfig;
    }

    private User createUser() {
        User user = new User("homer");
        user.setFirstname("Homer");
        user.setLastname("Simpson");
        user.setEmail("homer@example.org");
        user.setLocation("Springfield");
        return user;
    }

    private Project createProject() {
        Project project = new Project();
        project.setUuid(TestUUIDs.TEST_UUIDS[0]);
        project.setProjectId("technology.skalli");
        project.setName("Skalli");
        project.setDescription("Extensible system for organizing project");
        InfoExtension ext = new InfoExtension();
        ext.setPageUrl("http://example.org");
        project.addExtension(ext);
        return project;
    }

    private List<Project> createProjectsWithScmLocations() {
        List<Project> projects = new ArrayList<Project>();
        projects.add(createProjectWithScmLocation(TestUUIDs.TEST_UUIDS[0], MATCHING_SCM_PATTERN, false));
        projects.add(createProjectWithScmLocation(TestUUIDs.TEST_UUIDS[1], NOT_MATCHING_SCM_PATTERN, false));
        projects.add(createProjectWithScmLocation(TestUUIDs.TEST_UUIDS[2], NOT_MATCHING_SCM_PATTERN, false));
        projects.add(createProjectWithScmLocation(TestUUIDs.TEST_UUIDS[3], MATCHING_SCM_PATTERN, false));
        projects.add(createProjectWithScmLocation(TestUUIDs.TEST_UUIDS[4], null, false));
        projects.add(createProjectWithScmLocation(TestUUIDs.TEST_UUIDS[5], null, true));
        projects.add(createProjectWithScmLocation(TestUUIDs.TEST_UUIDS[0], MATCHING_SCM_PATTERN, false));
        return projects;
    }

    private Project createProjectWithScmLocation(UUID uuid, String scmPrefix, boolean inherited) {
        Project project = new Project();
        project.setUuid(uuid);
        project.setProjectId("skalli" + uuid.toString());
        if (inherited) {
            project.setInherited(DevInfProjectExt.class, true);
        } else if (scmPrefix != null) {
            DevInfProjectExt ext = new DevInfProjectExt();
            ext.addScmLocation(scmPrefix + uuid.toString() + ".git");
            project.addExtension(ext);
        }
        return project;
    }
}
