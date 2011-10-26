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
package org.eclipse.skalli.testutil.feeds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;

import org.eclipse.skalli.api.java.feeds.Content;
import org.eclipse.skalli.api.java.feeds.Entry;
import org.eclipse.skalli.api.java.feeds.FeedEntry;
import org.eclipse.skalli.api.java.feeds.FeedPersistenceService;
import org.eclipse.skalli.api.java.feeds.FeedService;
import org.eclipse.skalli.api.java.feeds.FeedServiceException;
import org.eclipse.skalli.api.java.feeds.Link;
import org.eclipse.skalli.api.java.feeds.Person;

/**
 * A simple HashMap Service implementing FeedService and FeedPersistenceService.
 * e.g use in Tests where a simple FeedService, FeedPersistenceService is needed.
 */
public class HashMapFeedService implements FeedService, FeedPersistenceService {

    static public class SimpleEntry implements FeedEntry {
        private String id;
        private String title;
        private Link link;
        private Content content;
        private Date published;
        private Person author;
        private UUID projectId;
        private String source;

        @Override
        public String getId() {
            return id;
        }

        @Override
        public void setId(String id) {
            this.id = id;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public void setTitle(String title) {
            this.title = title;
        }

        @Override
        public Link getLink() {
            return link;
        }

        public void setLink(Link link) {
            this.link = link;
        }

        @Override
        public Content getContent() {
            return content;
        }

        public void setContent(Content content) {
            this.content = content;
        }

        @Override
        public Date getPublished() {
            return published;
        }

        @Override
        public void setPublished(Date published) {
            this.published = published;
        }

        @Override
        public Person getAuthor() {
            return author;
        }

        public void setAuthor(Person author) {
            this.author = author;
        }

        @Override
        public UUID getProjectId() {
            return projectId;
        }

        @Override
        public void setProjectId(UUID projectId) {
            this.projectId = projectId;
        }

        @Override
        public String getSource() {
            return source;
        }

        @Override
        public void setSource(String source) {
            this.source = source;
        }

        @Override
        public String toString() {
            return "SimpleEntry [id=" + id + ", title=" + title + ", link=" + link + ", content=" + content
                    + ", published=" + published + ", author=" + author + ", projectId=" + projectId + ", source="
                    + source + "]";
        }

    }

    Map<String, Entry> entries = new HashMap<String, Entry>();

    public Entry getEntry(String id) {
        return entries.get(id);
    }

    public Collection<Entry> getEntries() {
        return entries.values();
    }

    /* (non-Javadoc)
     * @see org.eclipse.skalli.api.java.feeds.FeedPersistenceService#createEntry()
     */
    @Override
    public FeedEntry createEntry() {
        return new SimpleEntry();
    }

    /* (non-Javadoc)
     * @see org.eclipse.skalli.api.java.feeds.FeedPersistenceService#merge(java.util.Collection)
     */
    @Override
    public void merge(Collection<FeedEntry> newEntries) throws FeedServiceException {
        for (Entry newEntry : newEntries) {
            this.entries.put(newEntry.getId(), newEntry);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.skalli.api.java.feeds.FeedService#findEntries(java.util.UUID, int)
     */
    @Override
    public List<Entry> findEntries(UUID projectId, int maxResults) throws FeedServiceException {
        List<Entry> result = new ArrayList<Entry>();
        Collection<Entry> values = entries.values();
        for (Entry entry : values) {
            if (projectId.equals(entry.getProjectId())) {
                result.add(entry);
            }
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.eclipse.skalli.api.java.feeds.FeedService#findEntries(java.util.UUID, java.util.Collection, int)
     */
    @Override
    public List<Entry> findEntries(UUID projectId, Collection<String> sources, int maxResults)
            throws FeedServiceException {
        List<Entry> result = new ArrayList<Entry>();
        Collection<Entry> values = entries.values();
        for (Entry entry : values) {
            if (result.size() < maxResults && projectId.equals(entry.getProjectId())
                    && sources.contains(entry.getSource())) {
                result.add(entry);
            }
        }
        return result;

    }

    /* (non-Javadoc)
     * @see org.eclipse.skalli.api.java.feeds.FeedService#findSources(java.util.UUID)
     */
    @Override
    public List<String> findSources(UUID projectId) throws FeedServiceException {
        TreeSet<String> sources = new TreeSet<String>();
        List<Entry> projectEntys = findEntries(projectId, -1);
        for (Entry entry : projectEntys) {
            sources.add(entry.getSource());
        }
        return new ArrayList<String>(sources);
    }


}
