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
package org.eclipse.skalli.testutil.feeds;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;

import org.eclipse.skalli.services.feed.Content;
import org.eclipse.skalli.services.feed.Entry;
import org.eclipse.skalli.services.feed.FeedEntry;
import org.eclipse.skalli.services.feed.FeedPersistenceService;
import org.eclipse.skalli.services.feed.FeedService;
import org.eclipse.skalli.services.feed.Link;
import org.eclipse.skalli.services.feed.Person;

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

    @Override
    public FeedEntry createEntry() {
        return new SimpleEntry();
    }

    @Override
    public void merge(Collection<FeedEntry> newEntries) throws IOException {
        for (Entry newEntry : newEntries) {
            this.entries.put(newEntry.getId(), newEntry);
        }
    }

    @Override
    public List<Entry> findEntries(UUID projectId, int maxResults) throws IOException {
        List<Entry> result = new ArrayList<Entry>();
        Collection<Entry> values = entries.values();
        for (Entry entry : values) {
            if (projectId.equals(entry.getProjectId())) {
                result.add(entry);
            }
        }
        return result;
    }

    @Override
    public List<Entry> findEntries(UUID projectId, Collection<String> sources, int maxResults)
            throws IOException {
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

    @Override
    public List<String> findSources(UUID projectId) throws IOException {
        TreeSet<String> sources = new TreeSet<String>();
        List<Entry> projectEntys = findEntries(projectId, -1);
        for (Entry entry : projectEntys) {
            sources.add(entry.getSource());
        }
        return new ArrayList<String>(sources);
    }
}
