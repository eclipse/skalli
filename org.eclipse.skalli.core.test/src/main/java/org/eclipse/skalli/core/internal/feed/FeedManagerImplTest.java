/*******************************************************************************
 * Copyright (c) 2010 - 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.core.internal.feed;

import static org.easymock.EasyMock.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.services.feed.Entry;
import org.eclipse.skalli.services.feed.FeedEntry;
import org.eclipse.skalli.services.feed.FeedProvider;
import org.eclipse.skalli.services.feed.FeedUpdater;
import org.eclipse.skalli.services.project.ProjectService;
import org.eclipse.skalli.testutil.TestUUIDs;
import org.eclipse.skalli.testutil.feeds.HashMapFeedService;
import org.junit.Test;

public class FeedManagerImplTest {

    private static final UUID PROJECT1_UUID = TestUUIDs.TEST_UUIDS[0];
    private static final UUID PROJECT2_UUID = TestUUIDs.TEST_UUIDS[1];

    @Test
    public void testUpdateAllFeeds() throws Exception {

        final Date aTestDate = new Date(1318946441120L);
        HashMapFeedService hashMapfeedService = new HashMapFeedService();


        FeedManagerImpl feedManagerImpl = new FeedManagerImpl(false);
        feedManagerImpl.bindFeedPersistenceService(hashMapfeedService);

        //bindProjectService
        ProjectService projectServiceMock = createMock(ProjectService.class);
        Set<UUID> projectIds = new HashSet<UUID>();
        projectIds.add(PROJECT1_UUID);
        projectIds.add(PROJECT2_UUID);
        Project p1 = new Project();
        p1.setUuid(PROJECT1_UUID);
        Project p2 = new Project();
        p2.setUuid(PROJECT2_UUID);
        expect(projectServiceMock.keySet()).andReturn(projectIds);
        expect(projectServiceMock.getByUUID(PROJECT1_UUID)).andReturn(p1);
        expect(projectServiceMock.getByUUID(PROJECT2_UUID)).andReturn(p2);
        replay(projectServiceMock);
        feedManagerImpl.bindProjectService(projectServiceMock);

        //bindFeedProvider
        FeedProvider feedProviderMock = createMock(FeedProvider.class);
        expect(feedProviderMock.getFeedUpdaters(p1)).andReturn(
                Collections.singletonList(getFeedUpdaterMock(hashMapfeedService,aTestDate, "source-a")));
        expect(feedProviderMock.getFeedUpdaters(p2)).andReturn(
                Collections.singletonList(getFeedUpdaterMock(hashMapfeedService,aTestDate, "source-b")));
        replay(feedProviderMock);
        feedManagerImpl.bindFeedProvider(feedProviderMock);

        feedManagerImpl.updateAllFeeds();

        assertThat(hashMapfeedService.getEntries().size(), is(3));

        Entry foundEntry1 = findEntryByTitle(hashMapfeedService.getEntries(), "title1-source-a");
        Entry foundEntry2 = findEntryByTitle(hashMapfeedService.getEntries(), "title2-source-a");
        Entry foundEntry3 = findEntryByTitle(hashMapfeedService.getEntries(), "title1-source-b");

        assertNotNull(foundEntry1);
        assertNotNull(foundEntry2);
        assertNotNull(foundEntry3);

        assertNotNull(foundEntry1.getId());

        assertThat(foundEntry1.getId(),
                is(DigestUtils.shaHex(PROJECT1_UUID.toString() + aTestDate.getTime() + "source-a")));
        assertThat(foundEntry1.getSource(), is("source-a"));
        assertThat(foundEntry1.getProjectId(), is(PROJECT1_UUID));
        assertThat(foundEntry1.getPublished(), is(aTestDate));

        assertNotNull(foundEntry2.getId());
        assertThat(foundEntry2.getId(), is("a4e8621588347aa9192f95708bdb48c8e4fb2b68")); //the original + alwasGeneratedPart as shaHex
        assertThat(foundEntry2.getSource(), is("source-a"));
        assertThat(foundEntry2.getProjectId(), is(PROJECT1_UUID));
        assertThat(foundEntry2.getPublished(), is(aTestDate));

        assertNotNull(foundEntry3.getId());
        assertThat(foundEntry3.getId(),
                is(DigestUtils.shaHex(PROJECT2_UUID.toString() + aTestDate.getTime() + "source-b")));
        assertThat(foundEntry3.getSource(), is("source-b"));
        assertThat(foundEntry3.getProjectId(), is(PROJECT2_UUID));
        assertThat(foundEntry3.getPublished(), is(aTestDate));
    }

    private Entry findEntryByTitle(Collection<Entry> entries, final String title) {
        for (Entry entry : entries) {
            if (entry.getTitle().equals(title)) {
                return entry;
            }
        }
        return null;
    }

    private FeedUpdater getFeedUpdaterMock(HashMapFeedService hashMapfeedService, Date date, String source) {
        FeedUpdater feedUpdaterProjectMock = createMock(FeedUpdater.class);
        expect(feedUpdaterProjectMock.getSource()).andReturn(source);
        expect(feedUpdaterProjectMock.getSource()).andReturn(source);
        List<FeedEntry> entries = new ArrayList<FeedEntry>();

        FeedEntry e1 = new HashMapFeedService.SimpleEntry();
        e1.setTitle("title1-" + source);
        e1.setPublished(date);
        entries.add(e1);

        if ("source-a".equals(source)) {
            FeedEntry e2 = new HashMapFeedService.SimpleEntry();
            e2.setTitle("title2-" + source);
            e2.setPublished(date);
            e2.setId("idTitle2");
            entries.add(e2);
        }

        expect(feedUpdaterProjectMock.updateFeed(hashMapfeedService)).andReturn(entries);
        replay(feedUpdaterProjectMock);
        return feedUpdaterProjectMock;
    }

}
