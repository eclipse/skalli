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
package org.eclipse.skalli.feed.updater;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.skalli.api.java.feeds.FeedEntry;
import org.eclipse.skalli.api.java.feeds.FeedFactory;
import org.eclipse.skalli.common.util.CollectionUtils;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndLink;
import com.sun.syndication.feed.synd.SyndPerson;

class Converter {

    private FeedFactory feedFactory;

    Converter(FeedFactory feedFactory) {
        this.feedFactory = feedFactory;
    }

    public List<FeedEntry> syndFeed2Entry(SyndFeed syndFeed) {
        if (syndFeed == null) {
            return Collections.emptyList();
        }
        List<FeedEntry> entries = new ArrayList<FeedEntry>();
        for (Object o : syndFeed.getEntries()) {
            entries.add(syndEntry2Entry((SyndEntry) o));
        }
        return entries;
    }

    private FeedEntry syndEntry2Entry(SyndEntry syndEntry) {
        if (syndEntry == null) {
            return null;
        }
        FeedEntry entry = feedFactory.createEntry();

        entry.setTitle(syndEntry.getTitle());

        if (CollectionUtils.isNotBlank(syndEntry.getLinks())) {
            SyndLink syndLink = (SyndLink) syndEntry.getLinks().get(0);
            entry.getLink().setHref(syndLink.getHref());
            entry.getLink().setTitle(syndLink.getTitle());
        }

        if (CollectionUtils.isNotBlank(syndEntry.getContents())) {
            SyndContent syndContent = (SyndContent) syndEntry.getContents().get(0);
            entry.getContent().setType(syndContent.getType());
            entry.getContent().setValue(syndContent.getValue());
        }

        if (CollectionUtils.isNotBlank(syndEntry.getAuthors())) {
            SyndPerson syndAuthor = (SyndPerson) syndEntry.getAuthors().get(0);
            entry.getAuthor().setName(syndAuthor.getName());
            entry.getAuthor().setEmail(syndAuthor.getEmail());
        }

        entry.setPublished(syndEntry.getPublishedDate());
        return entry;
    }

}
