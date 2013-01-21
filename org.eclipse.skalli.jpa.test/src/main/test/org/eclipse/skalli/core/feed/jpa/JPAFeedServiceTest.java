/*******************************************************************************
 * Copyright (c) 2010 - 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.core.feed.jpa;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.skalli.services.feed.Entry;
import org.eclipse.skalli.services.feed.FeedEntry;
import org.eclipse.skalli.services.feed.FeedPersistenceService;
import org.eclipse.skalli.services.feed.FeedService;
import org.eclipse.skalli.testutil.BundleManager;
import org.eclipse.skalli.testutil.TestUUIDs;
import org.junit.Test;

public class JPAFeedServiceTest {
    final private static UUID defaultProjectUuid = TestUUIDs.TEST_UUIDS[0];
    final private static UUID allFieldsProjectUuid = TestUUIDs.TEST_UUIDS[1];
    final private static UUID notPersistedProjectUuid = TestUUIDs.TEST_UUIDS[2];
    final private static UUID testFindProjectUuid = TestUUIDs.TEST_UUIDS[3];

    protected FeedPersistenceService getFeedPersistenceService() throws Exception {
        FeedPersistenceService service = BundleManager.waitService(FeedPersistenceService.class,
                JPAFeedPersistenceComponent.class, 1000);
        if (service == null) {
            fail(JPAFeedPersistenceComponent.class.getName() + " is not active");
        }
        return service;
    }

    protected FeedService getFeedService() throws Exception {
        FeedService service = BundleManager.waitService(FeedService.class,
                JPAFeedComponent.class, 1000);
        if (service == null) {
            fail(JPAFeedComponent.class.getName() + " is not active");
        }
        return service;
    }

    @Test
    public void testMergeWithFind() throws Exception {
        final String source = ("source-" + JPAFeedPersistenceServiceTest.class.getSimpleName()).substring(0,
                EntryJPA.SOURCE_LENGTH);
        final String title = "TestAllFields";
        final Date testDate = new Date(1318946441120L);
        final String author_name = "Jon Smith";
        final String author_email = "JonSmith@somehwere";
        final String content_value = "test content";
        final String link_title = "link title";
        final String link_href = "href info";

        FeedPersistenceService jPAFeedPersistenceService = getFeedPersistenceService();
        FeedEntry newEntry = jPAFeedPersistenceService.createEntry();
        newEntry.setSource(source);
        newEntry.setProjectId(allFieldsProjectUuid);
        newEntry.setTitle(title);
        newEntry.setPublished(testDate);
        newEntry.getAuthor().setName(author_name);
        newEntry.getAuthor().setEmail(author_email);
        newEntry.getContent().setType("text");
        newEntry.getContent().setValue(content_value);
        newEntry.getLink().setTitle(link_title);
        newEntry.getLink().setHref(link_href);

        final String id = DigestUtils.shaHex(newEntry.getProjectId().toString() + newEntry.getTitle()
                + newEntry.getSource());
        newEntry.setId(id);

        Collection<FeedEntry> entries = new ArrayList<FeedEntry>();
        entries.add(newEntry);
        jPAFeedPersistenceService.merge(entries);

        FeedService jPAFeedService =  getFeedService();
        List<Entry> foundEntries = jPAFeedService.findEntries(allFieldsProjectUuid, 10);
        assertThat(foundEntries.size(), is(1));

        Entry foundEntry = foundEntries.get(0);
        assertNotNull(foundEntry);
        assertThat(foundEntry.getId(), is(id));
        assertThat(foundEntry.getSource(), is(source));
        assertThat(foundEntry.getProjectId(), is(allFieldsProjectUuid));
        assertThat(foundEntry.getTitle(), is(title));
        assertThat(foundEntry.getPublished(), is(testDate));
        assertThat(foundEntry.getAuthor().getName(), is(author_name));
        assertThat(foundEntry.getAuthor().getEmail(), is(author_email));
        assertThat(foundEntry.getContent().getType(), is("text"));
        assertThat(foundEntry.getContent().getValue(), is(content_value));
        assertThat(foundEntry.getLink().getTitle(), is(link_title));
        assertThat(foundEntry.getLink().getHref(), is(link_href));

        // now update the entry and check that it updated
        final String newTitle = "new changed Title";
        newEntry.setTitle(newTitle);
        entries = new ArrayList<FeedEntry>();
        entries.add(newEntry);
        jPAFeedPersistenceService.merge(entries);
        foundEntries = jPAFeedService.findEntries(allFieldsProjectUuid, 10);
        assertThat(foundEntries.size(), is(1));
        foundEntry = foundEntries.get(0);
        assertNotNull(foundEntry);
        assertThat(foundEntry.getId(), is(id));
        assertThat(foundEntry.getTitle(), is(newTitle));
    }



    @Test
    public void testMerge() throws Exception {
        FeedPersistenceService jPAFeedPersistenceService =  getFeedPersistenceService();
        FeedEntry entry1 = createDummyTestEntry(jPAFeedPersistenceService, "1");
        FeedEntry entry2 = createDummyTestEntry(jPAFeedPersistenceService, "2");
        FeedEntry entry3 = createDummyTestEntry(jPAFeedPersistenceService, "3");

        Collection<FeedEntry> entries = new ArrayList<FeedEntry>();
        entries.add(entry1);
        entries.add(entry2);
        jPAFeedPersistenceService.merge(entries);
        assertEntriesExist(new String[] { entry1.getId(), entry2.getId() });

        // update again and add a third entry
        entries = new ArrayList<FeedEntry>();
        // entries.add(entry1); // do not set all entries, only some, and check
        // that the old once are not deleted.
        entries.add(entry2);
        entries.add(entry3);
        jPAFeedPersistenceService.merge(entries);

        assertEntriesExist(new String[] { entry1.getId(), entry2.getId(), entry3.getId() });
    }

    private void assertEntriesExist(String[] expectedEntryIds) throws Exception {
        FeedService jPAFeedService =  getFeedService();
        List<Entry> foundEntries = jPAFeedService.findEntries(defaultProjectUuid, expectedEntryIds.length + 100);
        assertThat(foundEntries.size(), is(expectedEntryIds.length));
        for (String expectedId : expectedEntryIds) {
            assertHasEntry(foundEntries, expectedId);
        }
    }

    private void assertHasEntry(List<Entry> foundEntries, String expectedId) {
        boolean hasEntry = false;
        for (Entry entry : foundEntries) {
            if (expectedId.equals(entry.getId())) {
                hasEntry = true;
            }
        }
        assertThat(hasEntry, is(true));
    }

    private FeedEntry createDummyTestEntry(FeedPersistenceService s, String title) {
        FeedEntry newEntry = s.createEntry();
        newEntry.setSource(("source-" + JPAFeedPersistenceServiceTest.class.getSimpleName()).substring(0,
                EntryJPA.SOURCE_LENGTH));
        newEntry.setProjectId(defaultProjectUuid);
        newEntry.setTitle(title);
        newEntry.setPublished(new Date());
        updateId(newEntry);
        return newEntry;
    }

    @Test
    public void testFindEntries_not_exisiting() throws Exception {
        FeedService jPAFeedService =  getFeedService();
        List<Entry> foundEntries = jPAFeedService.findEntries(notPersistedProjectUuid, 10);
        assertThat(foundEntries.size(), is(0));
    }

    @Test
    public void testFindCalls() throws Exception {

        final Date testDate = new Date(1318946441120L);

        FeedPersistenceService jPAFeedPersistenceService =  getFeedPersistenceService();
        FeedEntry e1 = jPAFeedPersistenceService.createEntry();
        e1.setSource("source-a");
        e1.setProjectId(testFindProjectUuid);
        e1.setTitle("t1");
        e1.setPublished(testDate);
        updateId(e1);

        FeedEntry e2 = jPAFeedPersistenceService.createEntry();
        e2.setSource("source-a");
        e2.setProjectId(testFindProjectUuid);
        e2.setTitle("t2");
        e2.setPublished(new Date(testDate.getTime() + 1));
        updateId(e2);

        FeedEntry e3 = jPAFeedPersistenceService.createEntry();
        e3.setSource("source-a");
        e3.setProjectId(testFindProjectUuid);
        e3.setTitle("t3");
        e3.setPublished(new Date(testDate.getTime() + 2));
        updateId(e3);

        FeedEntry e4 = jPAFeedPersistenceService.createEntry();
        e4.setSource("source-b");
        e4.setProjectId(testFindProjectUuid);
        e4.setTitle("t4");
        e4.setPublished(new Date(testDate.getTime() + 3));
        updateId(e4);

        Collection<FeedEntry> entries = new ArrayList<FeedEntry>();
        entries.add(e1);
        entries.add(e2);
        entries.add(e3);
        entries.add(e4);
        jPAFeedPersistenceService.merge(entries);

        // findEntries: check that the maxResult parameter of findEnties works:
        FeedService jPAFeedService = getFeedService();
        for (int maxResults = 0; maxResults < 10; maxResults++) {
            List<Entry> foundEntries = jPAFeedService.findEntries(testFindProjectUuid, maxResults);
            assertThat(foundEntries.size(), is(Math.min(maxResults, 4)));

            if (maxResults > 0) {
                // check that the entries are ordered desc by published
                for (int i = 1; i < foundEntries.size(); i++) {
                    Date date0 = foundEntries.get(i - 1).getPublished();
                    Date date1 = foundEntries.get(i).getPublished();
                    assertTrue("expected: " + date0.getTime() + ">" + date1.getTime(), date0.compareTo(date1) > 0);
                }
            }
        }

        // findEntries: check find with 1 source
        List<Entry> foundEntries = jPAFeedService.findEntries(testFindProjectUuid, Collections.singleton("source-a"), 10);
        assertThat(foundEntries.size(), is(3));
        for (Entry entry : foundEntries) {
            assertThat(entry.getSource(), is("source-a"));
        }

        // findEntries:: check find with 2 different sources
        Collection<String> sources = new ArrayList<String>();
        sources.add("source-a");
        sources.add("source-b");
        foundEntries = jPAFeedService.findEntries(testFindProjectUuid, sources, 10);
        assertThat(foundEntries.size(), is(4));
        for (Entry entry : foundEntries) {
            assertThat(entry.getSource(), isIn(sources));
        }

        // findEntries: check find with 1 sources and second one not existing
        sources = new ArrayList<String>();
        sources.add("source-a");
        sources.add("notExistingSource");
        foundEntries = jPAFeedService.findEntries(testFindProjectUuid, sources, 10);
        assertThat(foundEntries.size(), is(3));
        for (Entry entry : foundEntries) {
            assertThat(entry.getSource(), is("source-a"));
        }

        // findEntries: check find with 1 sources not persisted source
        sources = new ArrayList<String>();
        sources.add("notExistingSource");
        foundEntries = jPAFeedService.findEntries(testFindProjectUuid, sources, 10);
        assertThat(foundEntries.size(), is(0));

        // findSources
        List<String> foundSources = jPAFeedService.findSources(testFindProjectUuid);
        assertThat(foundSources.size(), is(2));
        // sources are expected to be order by there name
        assertThat(foundSources.get(0), is("source-a"));
        assertThat(foundSources.get(1), is("source-b"));
    }

    private void updateId(FeedEntry newEntry) {
        // in this test we calculate the id via project, title and source.
        String id = newEntry.getProjectId().toString() + newEntry.getTitle() + newEntry.getSource();
        newEntry.setId(DigestUtils.shaHex(id));
    }

    @Test
    public void testfindEntriesIllegalParameters() throws Exception {

        FeedService jPAFeedService =  getFeedService();
        try {
            jPAFeedService.findEntries(null, 4711);
            fail("IllegalArgumentException was expected.");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("projectId"));
        }

        try {
            jPAFeedService.findEntries(defaultProjectUuid, -2);
            fail("IllegalArgumentException was expected.");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("maxResults"));
        }

        @SuppressWarnings("unchecked")
        Collection<String> empty_sources = Collections.EMPTY_LIST;
        try {
            jPAFeedService.findEntries(null, empty_sources, 4711);
            fail("IllegalArgumentException was expected.");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("projectId"));
        }

        List<Entry> result = jPAFeedService.findEntries(defaultProjectUuid, null, 4711);
        assertThat(result.size(), is(0));

        try {
            jPAFeedService.findEntries(defaultProjectUuid, null, -2);
            fail("IllegalArgumentException was expected.");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("maxResults"));
        }
    }

    @Test
    public void testFindSourcesIllegalParameters() throws Exception {
        FeedService jPAFeedService = getFeedService();
        try {
            jPAFeedService.findSources(null);
            fail("IllegalArgumentException was expected.");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("projectId"));
        }
    }
}
