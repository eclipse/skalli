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
package org.eclipse.skalli.services.feed;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.commons.HttpUtils;
import org.eclipse.skalli.services.destination.DestinationService;
import org.eclipse.skalli.services.destination.Destinations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndLink;
import com.sun.syndication.feed.synd.SyndPerson;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;

/**
 * Generic feed updater for a given, usually project specific URL.
 * This feed updater is based on {@link com.sun.syndication.feed.synd.SyndFeed}.
 * It handles all RSS versions and Atom 0.3.
 * <p>
 * Note, this feed updater tries to retrieve a suitable HTTP client from
 * one of the registered {@link DestinationService destination services}.
 * It will fail if no suitable destination for the RSS feed is available.
 */
public class URLFeedUpdater implements FeedUpdater {

    private static final Logger LOG = LoggerFactory.getLogger(URLFeedUpdater.class);

    private URL url;
    private String source;
    private String caption;
    private UUID projectId;

    /**
     * Returns a feed updater for the given URL.
     *
     * @param url  the URL of the feed.
     * @param source  the source identifier of the feed to assign to feed entries.
     * @param caption  the human readable caption of the feed. If no caption is specified,
     * the value of <code>source</code> is used.
     * @param projectId  the unique identifier of the project to assign to feed entries, or
     * <code>null</code> if the feed is not related to an individual project.
     */
    public URLFeedUpdater(URL url, String source, String caption, UUID projectId) {
        if (url == null) {
            throw new IllegalArgumentException("parameter 'url' must not be null");
        }
        if (StringUtils.isBlank(source)) {
            throw new IllegalArgumentException("parameter 'source' must not be null or blank");
        }
        this.url = url;
        this.source = source;
        this.caption = StringUtils.isNotBlank(caption)? caption : source;
        this.projectId = projectId;
    }

    /**
     * Reads entries from the feed assuming that a matching destination is available
     * from one of the registered {@link DestinationService destination services}.
     */
    @Override
    public List<FeedEntry> updateFeed(FeedFactory feedFactory) {
        List<FeedEntry> entries = new ArrayList<FeedEntry>();
        try {
            SyndFeed syndFeed = getSyndFeed();
            for (Object o : syndFeed.getEntries()) {
                entries.add(toFeedEntry(feedFactory, (SyndEntry)o));
            }
        } catch (Exception e) {
            LOG.error(MessageFormat.format("Failed to update feed {0}:\n{1}", url.toString(), e.getMessage()), e);
        }
        return entries;
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public String getCaption() {
        return caption;
    }

    /**
     * Returns the unique identifier of the project associated with this feed updater.
     *
     * @return the unique identifier of a project,
     * or <code>null</code> if the feed is not associated with an individual project.
     */
    public UUID getProjectId() {
        return projectId;
    }

    /**
     * Returns the remote feed. This implementation retrieves a suitable
     * {@link HttpClient} from {@link Destinations} and issues a GET request
     * to the feed's URL.
     *
     * @return a {@link SyndFeed} instance from which the feed content can
     * be read, never <code>null</code>.
     *
     * @throws IOException if an i/o error occured.
     * @throws FeedException  if the feed updater failed to read data from the remote feed, e.g. because
     * no suitable HTTP client could be created or the remote feed reported an error.
     */
    protected SyndFeed getSyndFeed() throws IOException, FeedException {
        HttpClient client = Destinations.getClient(url);
        if (client == null) {
            throw new FeedException(MessageFormat.format("Failed to create HTTP connection to feed {0}", url));
        }
        Reader reader = null;
        HttpResponse response = null;
        try {
            LOG.info("GET " + url); //$NON-NLS-1$
            HttpGet method = new HttpGet(url.toExternalForm());
            response = client.execute(method);
            int status = response.getStatusLine().getStatusCode();
            LOG.info(status + " " + response.getStatusLine().getReasonPhrase()); //$NON-NLS-1$
            if (status != HttpStatus.SC_OK) {
                throw new FeedException(MessageFormat.format("Failed to retrieve feed {0}", url));
            }
            reader = new InputStreamReader(response.getEntity().getContent(), "UTF-8"); //$NON-NLS-1$
            return new SyndFeedInput().build(reader);
        } finally {
            IOUtils.closeQuietly(reader);
            HttpUtils.consumeQuietly(response);
        }
    }

    /**
     * Converts a {@link SyndEntry} to a corresponding {@link FeedEntry}.
     *
     * @param factory  the feed factory to use for creating feed entries.
     * @param syndEntry  the {@link SyndEntry} to convert.
     *
     * @return a new {@link FeedEntry} instance, or <code>null</code> if
     * <code>syndEntry</code> was <code>null</code>.
     */
    protected FeedEntry toFeedEntry(FeedFactory factory, SyndEntry syndEntry) {
        if (syndEntry == null) {
            return null;
        }
        FeedEntry entry = factory.createEntry();
        entry.setSource(source);
        entry.setProjectId(projectId);

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

        Date publishedDate = syndEntry.getPublishedDate();
        if (publishedDate == null) {
            publishedDate = syndEntry.getUpdatedDate();
            if (publishedDate == null) {
                publishedDate = new Date();
            }
        }
        entry.setPublished(publishedDate);

        return entry;
    }
}
