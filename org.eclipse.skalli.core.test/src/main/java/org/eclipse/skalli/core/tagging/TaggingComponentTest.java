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
package org.eclipse.skalli.core.tagging;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.UUID;

import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.ext.commons.TagsExtension;
import org.eclipse.skalli.services.entity.EventEntityUpdate;
import org.eclipse.skalli.services.issues.Issues;
import org.eclipse.skalli.services.tagging.TagCount;
import org.eclipse.skalli.testutil.AssertUtils;
import org.eclipse.skalli.testutil.HashMapEntityService;
import org.eclipse.skalli.testutil.TestUUIDs;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("nls")
public class TaggingComponentTest {

    private List<Project> projects;
    private TaggingComponent ts;

    @Before
    public void setup() {
        projects = new LinkedList<Project>();
        projects.add(createProject(TestUUIDs.TEST_UUIDS[0], "project1", new String[] {}));
        projects.add(createProject(TestUUIDs.TEST_UUIDS[1], "project2", new String[] { "most", "aaa", "tag3" }));
        projects.add(createProject(TestUUIDs.TEST_UUIDS[2], "project3", new String[] { "tag2", "most" }));
        projects.add(createProject(TestUUIDs.TEST_UUIDS[3], "project4", new String[] { "aaa" }));
        projects.add(createProject(TestUUIDs.TEST_UUIDS[4], "project5", new String[] { "tag1", "most" }));
        ts = new TaggingComponent();
        for (Project project: projects) {
            ts.onEvent(new EventEntityUpdate(Project.class, project, "homer"));
        }
    }

    @Test
    public void testGetTags() {
        assertAllTagsByName(ts.getTags(Project.class));
    }

    @Test
    public void testGetMostPopular() {
        assertAllTagsSortedByCount(ts.getMostPopular(Project.class));
    }

    @Test
    public void testGetMostPopularNegativeCount() {
        assertAllTagsSortedByCount(ts.getMostPopular(Project.class, -1));
    }

    @Test
    public void testGetMostPopularLargeCount() {
        assertAllTagsSortedByCount(ts.getMostPopular(Project.class, 4711));
    }

    @Test
    public void testGetMostPopularZeroCount() {
        SortedSet<TagCount> mostPopluar = ts.getMostPopular(Project.class, 0);
        assertNotNull(mostPopluar);
        assertTrue(mostPopluar.isEmpty());
    }

    @Test
    public void testOnEvent() {
        SortedSet<TagCount> mostPopluar = ts.getMostPopular(Project.class, 3);
        assertNotNull(mostPopluar);
        assertEquals(3, mostPopluar.size());
        Iterator<TagCount> it = mostPopluar.iterator();
        assertTagCount(it.next(), "most", 3);
        assertTagCount(it.next(), "aaa", 2);
        assertTagCount(it.next(), "tag1", 1);

        // now add "tag1" to all projects!
        for (Project project: projects) {
            project.getExtension(TagsExtension.class).addTag("tag1");
            ts.onEvent(new EventEntityUpdate(Project.class, project, "homer"));
        }
        mostPopluar = ts.getMostPopular(Project.class, 3);
        it = mostPopluar.iterator();
        assertNotNull(mostPopluar);
        assertEquals(3, mostPopluar.size());
        assertTagCount(it.next(), "tag1", 5);
        assertTagCount(it.next(), "most", 3);
        assertTagCount(it.next(), "aaa", 2);

        // remove "most" from project2
        projects.get(1).getExtension(TagsExtension.class).removeTag("most");
        ts.onEvent(new EventEntityUpdate(Project.class, projects.get(1), "homer"));
        mostPopluar = ts.getMostPopular(Project.class, 3);
        it = mostPopluar.iterator();
        assertNotNull(mostPopluar);
        assertEquals(3, mostPopluar.size());
        assertTagCount(it.next(), "tag1", 5);
        assertTagCount(it.next(), "aaa", 2);
        assertTagCount(it.next(), "most", 2);

        // set the deleted flag on project2
        projects.get(1).setDeleted(true);
        ts.onEvent(new EventEntityUpdate(Project.class, projects.get(1), "homer"));
        mostPopluar = ts.getMostPopular(Project.class, 3);
        it = mostPopluar.iterator();
        assertNotNull(mostPopluar);
        assertEquals(3, mostPopluar.size());
        assertTagCount(it.next(), "tag1", 4);
        assertTagCount(it.next(), "most", 2);
        assertTagCount(it.next(), "aaa", 1);

        // assert that "tag3" has vanished from the set of all tags
        SortedMap<String,Integer> tags = ts.getTags(Project.class);
        AssertUtils.assertEquals("getTags(Project.class)", tags.keySet(), "aaa", "most", "tag1", "tag2");
        assertEquals(1, (int)tags.get("aaa"));
        assertEquals(2, (int)tags.get("most"));
        assertEquals(4, (int)tags.get("tag1"));
        assertEquals(1, (int)tags.get("tag2"));

        // exchange TagsExtension of project1: removes "tag1", adds "aaa"
        TagsExtension ext = new TagsExtension();
        ext.addTag("aaa");
        projects.get(0).addExtension(ext);
        ts.onEvent(new EventEntityUpdate(Project.class, projects.get(0), "homer"));
        mostPopluar = ts.getMostPopular(Project.class, 3);
        it = mostPopluar.iterator();
        assertNotNull(mostPopluar);
        assertEquals(3, mostPopluar.size());
        assertTagCount(it.next(), "tag1", 3);
        assertTagCount(it.next(), "aaa", 2);
        assertTagCount(it.next(), "most", 2);

        // un-delete project2
        projects.get(1).setDeleted(false);
        ts.onEvent(new EventEntityUpdate(Project.class, projects.get(1), "homer"));
        mostPopluar = ts.getMostPopular(Project.class, -1);
        it = mostPopluar.iterator();
        assertNotNull(mostPopluar);
        assertEquals(5, mostPopluar.size());
        assertTagCount(it.next(), "tag1", 4);
        assertTagCount(it.next(), "aaa", 3);
        assertTagCount(it.next(), "most", 2);
        assertTagCount(it.next(), "tag2", 1);
        assertTagCount(it.next(), "tag3", 1);
    }

    @Test
    public void testEntityClassNotTaggable() {
        SortedSet<TagCount> mostPopluar = ts.getMostPopular(Issues.class, 0);
        assertNotNull(mostPopluar);
        assertTrue(mostPopluar.isEmpty());
    }

    private static class TestEntityService extends HashMapEntityService<Project> {
        @Override
        public Class<Project> getEntityClass() {
            return Project.class;
        }
    }

    @Test
    public void testInitialize() throws Exception {
        TestEntityService entityService = new TestEntityService();
        for (Project project: projects) {
            entityService.persist(project, "foobar");
        }
        TaggingComponent ts = new TaggingComponent();
        ts.initialize(entityService);
        assertAllTagsByName(ts.getTags(Project.class));
        assertAllTagsSortedByCount(ts.getMostPopular(Project.class));
        assertAllTagsSortedByCount(ts.getMostPopular(Project.class, -1));
    }

    private void assertAllTagsSortedByCount(SortedSet<TagCount> tags) {
        assertNotNull(tags);
        assertEquals(5, tags.size());
        Iterator<TagCount> it = tags.iterator();
        assertTagCount(it.next(), "most", 3);
        assertTagCount(it.next(), "aaa", 2);
        assertTagCount(it.next(), "tag1", 1);
        assertTagCount(it.next(), "tag2", 1);
        assertTagCount(it.next(), "tag3", 1);
    }

    private void assertTagCount(TagCount next, String name, int count) {
        assertEquals(name, next.getName());
        assertEquals(count, next.getCount());
    }

    private void assertAllTagsByName(SortedMap<String,Integer> tags) {
        assertNotNull(tags);
        AssertUtils.assertEquals("getTags(Project.class)", tags.keySet(), "aaa", "most", "tag1", "tag2", "tag3");
        assertEquals(2, (int)tags.get("aaa"));
        assertEquals(3, (int)tags.get("most"));
        assertEquals(1, (int)tags.get("tag1"));
        assertEquals(1, (int)tags.get("tag2"));
        assertEquals(1, (int)tags.get("tag3"));
    }

    private Project createProject(UUID uuid, String projectId, String[] tags) {
        Project ret = new Project();
        ret.setProjectId(projectId);
        ret.setUuid(uuid);
        if (tags != null) {
            TagsExtension ext = new TagsExtension();
            for (String tag : tags) {
                ext.addTag(tag);
            }
            ret.addExtension(ext);
        }
        return ret;
    }
}
