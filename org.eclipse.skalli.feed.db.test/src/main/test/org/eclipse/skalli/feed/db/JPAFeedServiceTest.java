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
package org.eclipse.skalli.feed.db;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.skalli.api.java.feeds.Entry;
import org.eclipse.skalli.api.java.feeds.FeedPersistenceService;
import org.eclipse.skalli.api.java.feeds.FeedService;
import org.eclipse.skalli.api.java.feeds.FeedServiceException;
import org.eclipse.skalli.feed.db.entities.ContentJPA;
import org.eclipse.skalli.feed.db.entities.EntryJPA;
import org.eclipse.skalli.feed.db.entities.LinkJPA;
import org.eclipse.skalli.feed.db.entities.PersonJPA;
import org.eclipse.skalli.testutil.PropertyHelperUtils;
import org.junit.Test;

public class JPAFeedServiceTest {
    final private static UUID defaultProjectUuid = PropertyHelperUtils.TEST_UUIDS[0];
    final private static UUID allFieldsProjectUuid = PropertyHelperUtils.TEST_UUIDS[1];
    final private static UUID notPersistedProjectUuid = PropertyHelperUtils.TEST_UUIDS[2];
    final private static UUID testFindProjectUuid = PropertyHelperUtils.TEST_UUIDS[3];

    @Test
    public void testMergeWithFind() throws FeedServiceException {
        final String source = ("source-" + JPAFeedPersistenceServiceTest.class.getSimpleName()).substring(0,
                EntryJPA.SOURCE_LENGTH);
        final String title = "TestAllFields";
        final Date testDate = new Date(1318946441120L);
        final String author_name = "Jon Smith";
        final String author_email = "JonSmith@somehwere";
        final String content_value = "test content";
        final String link_title = "link title";
        final String link_href = "href info";

        JPAFeedPersistenceService fps = new JPAFeedPersistenceService();
        EntryJPA newEntry = fps.createEntry();
        newEntry.setSource(source);
        newEntry.setProjectId(allFieldsProjectUuid);
        newEntry.setTitle(title);
        newEntry.setPublished(testDate);

        newEntry.setAuthor(new PersonJPA(author_name, author_email));
        newEntry.setContent(new ContentJPA("text", content_value));

        LinkJPA newLink = new LinkJPA();
        newLink.setTitle(link_title);
        newLink.setHref(link_href);
        newEntry.setLink(newLink);

        final String id = DigestUtils.shaHex(newEntry.getProjectId().toString() + newEntry.getTitle()
                + newEntry.getSource());
        newEntry.setId(id);

        Collection<Entry> entries = new ArrayList<Entry>();
        entries.add(newEntry);
        fps.merge(entries);

        JPAFeedService fs = new JPAFeedService();
        List<Entry> foundEntries = fs.findEntries(allFieldsProjectUuid, 10);
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
        entries = new ArrayList<Entry>();
        entries.add(newEntry);
        fps.merge(entries);
        foundEntries = fs.findEntries(allFieldsProjectUuid, 10);
        assertThat(foundEntries.size(), is(1));
        foundEntry = foundEntries.get(0);
        assertNotNull(foundEntry);
        assertThat(foundEntry.getId(), is(id));
        assertThat(foundEntry.getTitle(), is(newTitle));
    }

    @Test
    public void testMerge() throws FeedServiceException {
        FeedPersistenceService fps = new JPAFeedPersistenceService();

        Entry entry1 = createDummyTestEntry(fps, "1");
        Entry entry2 = createDummyTestEntry(fps, "2");
        Entry entry3 = createDummyTestEntry(fps, "3");

        Collection<Entry> entries = new ArrayList<Entry>();
        entries.add(entry1);
        entries.add(entry2);
        fps.merge(entries);
        assertEntriesExist(new String[] { entry1.getId(), entry2.getId() });

        // update again and add a third entry
        entries = new ArrayList<Entry>();
        //entry1 is not added on purpose, so we can check that the old entries are not deleted.
        entries.add(entry2);
        entries.add(entry3);
        fps.merge(entries);

        assertEntriesExist(new String[] { entry1.getId(), entry2.getId(), entry3.getId() });
    }

    private void assertEntriesExist(String[] expectedEntryIds) throws FeedServiceException {
        FeedService fs = new JPAFeedService();
        List<Entry> foundEntries = fs.findEntries(defaultProjectUuid, expectedEntryIds.length + 100);
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

    private Entry createDummyTestEntry(FeedPersistenceService s, String title) {
        Entry newEntry = s.createEntry();
        newEntry.setSource(("source-" + JPAFeedPersistenceServiceTest.class.getSimpleName()).substring(0,
                EntryJPA.SOURCE_LENGTH));
        newEntry.setProjectId(defaultProjectUuid);
        newEntry.setTitle(title);
        newEntry.setPublished(new Date());
        updateId(newEntry);
        return newEntry;
    }

    @Test
    public void testFindEntries_not_exisiting() throws FeedServiceException {
        FeedService fps = new JPAFeedService();
        List<Entry> foundEntries = fps.findEntries(notPersistedProjectUuid, 10);
        assertThat(foundEntries.size(), is(0));
    }

    @Test
    public void testFindCalls() throws FeedServiceException, InterruptedException {
        JPAFeedPersistenceService fps = new JPAFeedPersistenceService();
        JPAFeedService fs = new JPAFeedService();

        final Date testDate = new Date(1318946441120L);

        Entry e1 = fps.createEntry();
        e1.setSource("source-a");
        e1.setProjectId(testFindProjectUuid);
        e1.setTitle("t1");
        e1.setPublished(testDate);
        updateId(e1);

        Entry e2 = fps.createEntry();
        e2.setSource("source-a");
        e2.setProjectId(testFindProjectUuid);
        e2.setTitle("t2");
        e2.setPublished(new Date(testDate.getTime() + 1));
        updateId(e2);

        Entry e3 = fps.createEntry();
        e3.setSource("source-a");
        e3.setProjectId(testFindProjectUuid);
        e3.setTitle("t3");
        e3.setPublished(new Date(testDate.getTime() + 2));
        updateId(e3);

        Entry e4 = fps.createEntry();
        e4.setSource("source-b");
        e4.setProjectId(testFindProjectUuid);
        e4.setTitle("t4");
        e4.setPublished(new Date(testDate.getTime() + 3));
        updateId(e4);

        Collection<Entry> entries = new ArrayList<Entry>();
        entries.add(e1);
        entries.add(e2);
        entries.add(e3);
        entries.add(e4);
        fps.merge(entries);

        // findEntries: check that the maxResult parameter of findEnties works:
        for (int maxResults = 0; maxResults < 10; maxResults++) {
            List<Entry> foundEntries = fs.findEntries(testFindProjectUuid, maxResults);
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
        List<Entry> foundEntries = fs.findEntries(testFindProjectUuid, Collections.singleton("source-a"), 10);
        assertThat(foundEntries.size(), is(3));
        for (Entry entry : foundEntries) {
            assertThat(entry.getSource(), is("source-a"));
        }

        // findEntries:: check find with 2 different sources
        Collection<String> sources = new ArrayList<String>();
        sources.add("source-a");
        sources.add("source-b");
        foundEntries = fs.findEntries(testFindProjectUuid, sources, 10);
        assertThat(foundEntries.size(), is(4));
        for (Entry entry : foundEntries) {
            assertThat(entry.getSource(), isIn(sources));
        }

        // findEntries: check find with 1 sources and second one not existing
        sources = new ArrayList<String>();
        sources.add("source-a");
        sources.add("notExistingSource");
        foundEntries = fs.findEntries(testFindProjectUuid, sources, 10);
        assertThat(foundEntries.size(), is(3));
        for (Entry entry : foundEntries) {
            assertThat(entry.getSource(), is("source-a"));
        }

        // findEntries: check find with 1 sources not persisted source
        sources = new ArrayList<String>();
        sources.add("notExistingSource");
        foundEntries = fs.findEntries(testFindProjectUuid, sources, 10);
        assertThat(foundEntries.size(), is(0));

        // findSources
        List<String> foundSources = fs.findSources(testFindProjectUuid);
        assertThat(foundSources.size(), is(2));
        // sources are expected to be order by there name
        assertThat(foundSources.get(0), is("source-a"));
        assertThat(foundSources.get(1), is("source-b"));
    }

    private void updateId(Entry newEntry) {
        // in this test we calculate the id via project, title and source.
        String id = newEntry.getProjectId().toString() + newEntry.getTitle() + newEntry.getSource();
        newEntry.setId(DigestUtils.shaHex(id));
    }

    @Test
    public void testfindEntriesIllegalParameters() throws FeedServiceException {
        JPAFeedService fps = new JPAFeedService();

        try {
            fps.findEntries(null, 4711);
            fail("IllegalArgumentException was expected.");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("projectId"));
        }

        try {
            fps.findEntries(defaultProjectUuid, -2);
            fail("IllegalArgumentException was expected.");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("maxResults"));
        }

        @SuppressWarnings("unchecked")
        Collection<String> empty_sources = Collections.EMPTY_LIST;
        try {
            fps.findEntries(null, empty_sources, 4711);
            fail("IllegalArgumentException was expected.");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("projectId"));
        }

        List<Entry> result = fps.findEntries(defaultProjectUuid, null, 4711);
        assertThat(result.size(), is(0));

        try {
            fps.findEntries(defaultProjectUuid, null, -2);
            fail("IllegalArgumentException was expected.");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("maxResults"));
        }
    }

    @Test
    public void testFindSourcesIllegalParameters() throws FeedServiceException {
        JPAFeedService fps = new JPAFeedService();
        try {
            fps.findSources(null);
            fail("IllegalArgumentException was expected.");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("projectId"));
        }
    }
}
