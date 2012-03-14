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
package org.eclipse.skalli.core.internal.project;

import static org.easymock.EasyMock.*;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;

import org.eclipse.skalli.core.internal.templates.DefaultProjectTemplate;
import org.eclipse.skalli.model.Member;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.ProjectNature;
import org.eclipse.skalli.model.ext.commons.PeopleExtension;
import org.eclipse.skalli.model.ext.commons.TagsExtension;
import org.eclipse.skalli.model.ext.people.internal.CoreRoleProvider;
import org.eclipse.skalli.services.persistence.PersistenceService;
import org.eclipse.skalli.services.template.ProjectTemplateService;
import org.eclipse.skalli.testutil.BundleManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleException;

@SuppressWarnings("nls")
public class ProjectServiceImplTest {

    private static final UUID[] EMPTY_UUID_LIST = new UUID[0];

    private static class TestProjectTemplate1 extends DefaultProjectTemplate {
        @Override
        public String getId() {
            return "projecttemplate1";
        }

        @Override
        public ProjectNature getProjectNature() {
            return ProjectNature.PROJECT;
        }
    }

    private static class TestComponentTemplate1 extends DefaultProjectTemplate {
        @Override
        public String getId() {
            return "comptemplate1";
        }

        @Override
        public ProjectNature getProjectNature() {
            return ProjectNature.COMPONENT;
        }
    }

    private static class TestComponentTemplate2 extends DefaultProjectTemplate {
        @Override
        public String getId() {
            return "comptemplate2";
        }

        @Override
        public ProjectNature getProjectNature() {
            return ProjectNature.COMPONENT;
        }
    }

    private List<Project> projects;
    private List<Project> deletedprojects;
    protected UUID[] uuids = new UUID[9];
    protected Object[] mocks;
    protected PersistenceService mockIPS;
    protected ProjectTemplateService mockTS;
    private ProjectServiceImpl ps;
    private Member m1;
    private Member m2;
    private Member l1;
    private Member l2;

    private Project createProject(UUID uuid, String projectId, Project parent, String[] tags) {
        Project ret = new Project();
        ret.setProjectId(projectId);
        ret.setUuid(uuid);
        if (parent != null) {
            ret.setParentEntity(parent);
        }
        if (tags != null) {
            TagsExtension tagsExt = new TagsExtension();
            for (String tag : tags) {
                tagsExt.addTag(tag);
            }
            ret.addExtension(tagsExt);
        }
        return ret;
    }

    private Project createDeletedProject(UUID uuid, String projectId, Project parent, String[] tags) {
        Project ret = createProject(uuid, projectId, parent, tags);
        ret.setDeleted(true);
        return ret;
    }

    @Before
    public void setup() throws BundleException {
        BundleManager.startBundles();

        for (int i = 0; i < uuids.length; ++i) {
            uuids[i] = UUID.randomUUID();
        }

        projects = new LinkedList<Project>();
        projects.add(createProject(uuids[1], "project1", null, new String[] {})); //$NON-NLS-1$
        projects.add(createProject(uuids[2], "project2", projects.get(0), new String[] { "tagBoth", "tag2" })); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        projects.get(1).setProjectTemplateId("projecttemplate1");
        projects.add(createProject(uuids[3], "project3", projects.get(1), new String[] { "tagBoth", "tag3" })); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        deletedprojects = new LinkedList<Project>();
        deletedprojects.add(createDeletedProject(uuids[4], "project4", null, new String[] { "tag2" })); //$NON-NLS-1$
        deletedprojects.add(createDeletedProject(uuids[5], "project5", null, new String[] { "tag4" })); //$NON-NLS-1$

        projects.add(createProject(uuids[6], "comp1", projects.get(2), new String[] {}));
        projects.get(3).setProjectTemplateId("comptemplate1");
        projects.add(createProject(uuids[7], "comp2", projects.get(3), new String[] {}));
        projects.get(4).setProjectTemplateId("comptemplate2");
        projects.add(createProject(uuids[8], "comp3", projects.get(2), new String[] {}));
        projects.get(5).setProjectTemplateId("comptemplate1");

        mockIPS = createNiceMock(PersistenceService.class);
        mockTS = createNiceMock(ProjectTemplateService.class);
        mocks = new Object[] { mockIPS, mockTS };
        ps = new ProjectServiceImpl();
        ps.bindPersistenceService(mockIPS);
        ps.bindProjectTemplateService(mockTS);
        ps.bindRoleProvider(new CoreRoleProvider());

        reset(mocks);
        recordMocks();
        replay(mocks);

        m1 = new Member("M1");
        m2 = new Member("M2");
        l1 = new Member("L1");
        l2 = new Member("L2");
    }

    protected void recordMocks() {
        mockIPS.getEntities(eq(Project.class));
        expectLastCall().andReturn(projects).anyTimes();

        mockIPS.getEntity(eq(Project.class), eq(uuids[1]));
        expectLastCall().andReturn(projects.get(0)).anyTimes();

        mockIPS.getEntity(eq(Project.class), eq(uuids[2]));
        expectLastCall().andReturn(projects.get(1)).anyTimes();

        mockIPS.getEntity(eq(Project.class), eq(uuids[3]));
        expectLastCall().andReturn(projects.get(2)).anyTimes();

        mockIPS.getEntity(eq(Project.class), eq(uuids[6]));
        expectLastCall().andReturn(projects.get(3)).anyTimes();

        mockIPS.getEntity(eq(Project.class), eq(uuids[7]));
        expectLastCall().andReturn(projects.get(4)).anyTimes();

        mockIPS.getEntity(eq(Project.class), eq(uuids[8]));
        expectLastCall().andReturn(projects.get(5)).anyTimes();

        mockIPS.getDeletedEntity(eq(Project.class), eq(uuids[4]));
        expectLastCall().andReturn(deletedprojects.get(0)).anyTimes();

        mockIPS.getDeletedEntity(eq(Project.class), eq(uuids[5]));
        expectLastCall().andReturn(deletedprojects.get(1)).anyTimes();

        mockIPS.getDeletedEntities(eq(Project.class));
        expectLastCall().andReturn(deletedprojects).anyTimes();

        mockTS.getProjectTemplateById(eq("default"));
        expectLastCall().andReturn(new DefaultProjectTemplate()).anyTimes();

        mockTS.getProjectTemplateById(eq("projecttemplate1"));
        expectLastCall().andReturn(new TestProjectTemplate1()).anyTimes();

        mockTS.getProjectTemplateById(eq("comptemplate1"));
        expectLastCall().andReturn(new TestComponentTemplate1()).anyTimes();

        mockTS.getProjectTemplateById(eq("comptemplate2"));
        expectLastCall().andReturn(new TestComponentTemplate2()).anyTimes();
    }

    @Test
    public void testGetProjects() {
        List<Project> res = ps.getAll();
        Assert.assertNotNull(res);
        Assert.assertEquals(6, res.size());

        verify(mocks);
    }

    @Test
    public void testGetSortedProjects() {
        List<Project> res = ps.getProjects(new Comparator<Project>() {
            @Override
            public int compare(Project p1, Project p2) {
                // reverse ordering by project id!
                return p2.getProjectId().compareTo(p1.getProjectId());
            }
        });
        Assert.assertNotNull(res);
        Assert.assertEquals(6, res.size());

        Assert.assertEquals(uuids[3], res.get(0).getUuid());
        Assert.assertEquals(uuids[2], res.get(1).getUuid());
        Assert.assertEquals(uuids[1], res.get(2).getUuid());
        Assert.assertEquals(uuids[8], res.get(3).getUuid());
        Assert.assertEquals(uuids[7], res.get(4).getUuid());
        Assert.assertEquals(uuids[6], res.get(5).getUuid());
        verify(mocks);
    }

    @Test
    public void testGetSortedDeletedProjects() {
        List<Project> res = ps.getDeletedProjects(new Comparator<Project>() {
            @Override
            public int compare(Project p1, Project p2) {
                // reverse ordering by project id!
                return p2.getProjectId().compareTo(p1.getProjectId());
            }
        });
        Assert.assertNotNull(res);
        Assert.assertEquals(2, res.size());

        Assert.assertEquals(uuids[5], res.get(0).getUuid());
        Assert.assertEquals(uuids[4], res.get(1).getUuid());
        verify(mocks);
    }

    @Test
    public void testGetProjectByUUID() {
        Project res1 = ps.getByUUID(uuids[2]);
        Assert.assertNotNull(res1);
        Assert.assertEquals(uuids[2], res1.getUuid());

        Project res2 = ps.getByUUID(uuids[3]);
        Assert.assertNotNull(res2);
        Assert.assertEquals(uuids[3], res2.getUuid());

        Project res3 = ps.getByUUID(uuids[4]);
        Assert.assertNull(res3);

        verify(mocks);
    }

    @Test
    public void testGetProjectByProjectId() {
        Project res1 = ps.getProjectByProjectId("project2"); //$NON-NLS-1$
        Assert.assertNotNull(res1);
        Assert.assertEquals(projects.get(1), res1);

        Project res2 = ps.getProjectByProjectId("project_nonExisting"); //$NON-NLS-1$
        Assert.assertNull(res2);

        // try to retrieve deleted project
        Project res3 = ps.getProjectByProjectId("project5"); //$NON-NLS-1$
        Assert.assertNull(res3);

        verify(mocks);
    }

    @Test
    public void testGetSubProjects() {
        assertEquals(ps.getSubProjects(uuids[1]), uuids[2]);
        assertEquals(ps.getSubProjects(uuids[2]), uuids[3]);
        assertEquals(ps.getSubProjects(uuids[3]), uuids[6], uuids[8]);
        assertEquals(ps.getSubProjects(uuids[6]), uuids[7]);
        assertEquals(ps.getSubProjects(uuids[7]), EMPTY_UUID_LIST);
        verify(mocks);
    }

    @Test
    public void testGetSubProjectsWithDepth() {
        assertEquals(ps.getSubProjects(uuids[1], null, 0), EMPTY_UUID_LIST);
        assertEquals(ps.getSubProjects(uuids[1], null, 1), uuids[2]);
        assertEquals(ps.getSubProjects(uuids[1], null, 2), uuids[2], uuids[3]);
        assertEquals(ps.getSubProjects(uuids[1], null, 3), uuids[2], uuids[3], uuids[6], uuids[8]);
        assertEquals(ps.getSubProjects(uuids[1], null, Integer.MAX_VALUE), uuids[2], uuids[3], uuids[6], uuids[7], uuids[8]);
        assertEquals(ps.getSubProjects(uuids[1], null, -1), uuids[2], uuids[3], uuids[6], uuids[7], uuids[8]);
        assertEquals(ps.getSubProjects(uuids[8], null, -1), EMPTY_UUID_LIST);
        verify(mocks);
    }

    @Test
    public void testGetSubProjectsSorted() {
        Comparator<Project> c = new Comparator<Project>() {
            @Override
            public int compare(Project o1, Project o2) {
                return o1.getProjectId().compareTo(o2.getProjectId());
            }
        };
        assertEquals(ps.getSubProjects(uuids[1], c, -1), uuids[6], uuids[7], uuids[8], uuids[2], uuids[3]);
        verify(mocks);
    }

    @Test
    public void testGetParentChain() {
        assertEquals(ps.getParentChain(uuids[1]), uuids[1]);
        assertEquals(ps.getParentChain(uuids[2]), uuids[2], uuids[1]);
        assertEquals(ps.getParentChain(uuids[3]), uuids[3], uuids[2], uuids[1]);
        assertEquals(ps.getParentChain(uuids[6]), uuids[6], uuids[3], uuids[2], uuids[1]);
        assertEquals(ps.getParentChain(uuids[7]), uuids[7], uuids[6], uuids[3], uuids[2], uuids[1]);
        assertEquals(ps.getParentChain(uuids[8]), uuids[8], uuids[3], uuids[2], uuids[1]);
        verify(mocks);
    }


    private void assertEquals(Collection<Project> projects, UUID...uuids) {
        Assert.assertNotNull(projects);
        Assert.assertEquals(uuids.length, projects.size());
        Iterator<Project> it = projects.iterator();
        for (UUID uuid: uuids) {
            Assert.assertEquals(uuid, it.next().getUuid());
        }
    }

    @Test
    public void testGetNearestParent() {
        Project nearest = ps.getNearestParent(uuids[6], ProjectNature.PROJECT);
        Assert.assertEquals(uuids[3], nearest.getUuid());

        nearest = ps.getNearestParent(uuids[7], ProjectNature.PROJECT);
        Assert.assertEquals(uuids[3], nearest.getUuid());

        nearest = ps.getNearestParent(uuids[8], ProjectNature.PROJECT);
        Assert.assertEquals(uuids[3], nearest.getUuid());

        nearest = ps.getNearestParent(uuids[6], ProjectNature.COMPONENT);
        Assert.assertEquals(uuids[6], nearest.getUuid());

        nearest = ps.getNearestParent(uuids[7], ProjectNature.COMPONENT);
        Assert.assertEquals(uuids[7], nearest.getUuid());

        nearest = ps.getNearestParent(uuids[8], ProjectNature.COMPONENT);
        Assert.assertEquals(uuids[8], nearest.getUuid());

        nearest = ps.getNearestParent(uuids[1], ProjectNature.COMPONENT);
        Assert.assertNull(nearest);

        nearest = ps.getNearestParent(uuids[2], ProjectNature.COMPONENT);
        Assert.assertNull(nearest);

        nearest = ps.getNearestParent(uuids[3], ProjectNature.COMPONENT);
        Assert.assertNull(nearest);

        // top-level projects have no parent
        nearest = ps.getNearestParent(uuids[1], ProjectNature.PROJECT);
        Assert.assertEquals(uuids[1], nearest.getUuid());

        nearest = ps.getNearestParent(uuids[2], ProjectNature.PROJECT);
        Assert.assertEquals(uuids[2], nearest.getUuid());

        nearest = ps.getNearestParent(uuids[3], ProjectNature.PROJECT);
        Assert.assertEquals(uuids[3], nearest.getUuid());
    }

    @Test
    public void testGetDeletedProjects() {
        List<Project> res = ps.getDeletedProjects();
        Assert.assertNotNull(res);
        Assert.assertEquals(2, res.size());

        verify(mocks);
    }

    @Test
    public void testGetDeletedProject() {
        Project res1 = ps.getDeletedProject(uuids[4]);
        Assert.assertNotNull(res1);
        Assert.assertEquals(uuids[4], res1.getUuid());

        Project res2 = ps.getDeletedProject(uuids[5]);
        Assert.assertNotNull(res2);
        Assert.assertEquals(uuids[5], res2.getUuid());

        Project res3 = ps.getDeletedProject(uuids[1]);
        Assert.assertNull(res3);

        verify(mocks);
    }

    @Test
    public void testGetAllPeople() {
        Project p = new Project();
        PeopleExtension ext = new PeopleExtension();
        ext.addMember(m1);
        ext.addMember(m2);
        ext.addMember(l1);
        ext.addLead(l1);
        ext.addLead(l2);
        p.addExtension(ext);

        Set<Member> res1 = ps.getMembers(p);
        Assert.assertEquals(4, res1.size());
        Assert.assertTrue(res1.contains(m1));
        Assert.assertTrue(res1.contains(m2));
        Assert.assertTrue(res1.contains(l1));
        Assert.assertTrue(res1.contains(l2));

    }

    @Test
    public void testGetAllPeopleByRole() {
        Project p = new Project();
        PeopleExtension ext = new PeopleExtension();
        ext.addMember(m1);
        ext.addMember(m2);
        ext.addMember(l1);
        ext.addLead(l1);
        ext.addLead(l2);
        p.addExtension(ext);

        Map<String, SortedSet<Member>> res2 = ps.getMembersByRole(p);
        Assert.assertEquals(2, res2.size());

        Set<Member> res2members = res2.get("projectmember");
        Assert.assertNotNull(res2members);
        Assert.assertTrue(res2members.contains(m1));
        Assert.assertTrue(res2members.contains(m2));
        Assert.assertTrue(res2members.contains(l1));
        Assert.assertFalse(res2members.contains(l2));

        Set<Member> res2leads = res2.get("projectlead");
        Assert.assertNotNull(res2leads);
        Assert.assertFalse(res2leads.contains(m1));
        Assert.assertFalse(res2leads.contains(m2));
        Assert.assertTrue(res2leads.contains(l1));
        Assert.assertTrue(res2leads.contains(l2));
    }
}
