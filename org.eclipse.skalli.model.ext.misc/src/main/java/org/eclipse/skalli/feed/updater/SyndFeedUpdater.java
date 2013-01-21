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
package org.eclipse.skalli.feed.updater;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.eclipse.skalli.commons.HttpUtils;
import org.eclipse.skalli.services.destination.Destinations;
import org.eclipse.skalli.services.feed.FeedEntry;
import org.eclipse.skalli.services.feed.FeedFactory;
import org.eclipse.skalli.services.feed.FeedUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;

public class SyndFeedUpdater implements FeedUpdater {

    private static final Logger LOG = LoggerFactory.getLogger(SyndFeedUpdater.class);

    private URL url;
    private String projectName;
    private String source;
    private String caption;

    public SyndFeedUpdater(URL url, String projectName, String source, String caption) {
        this.url = url;
        this.projectName = projectName;
        this.source = source;
        this.caption = caption;
    }

    private List<FeedEntry> getEntries(FeedFactory factory) throws IOException, FeedException {
        if (LOG.isInfoEnabled()) {
            LOG.info(MessageFormat.format("Updating ''{0}'' feed for project ''{1}'' from {2}", source, projectName,
                    url.toString()));
        }
        return (new Converter(factory)).syndFeed2Entry(getSyndFeed());
    }

    private SyndFeed getSyndFeed() throws IOException, FeedException {
        SyndFeed syndFeed = null;
        HttpClient client = Destinations.getClient(url);
        if (client != null) {
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
        return syndFeed;
    }

    @Override
    public List<FeedEntry> updateFeed(FeedFactory factory) {
        try {
            return getEntries(factory);
        } catch (Exception e) {
            LOG.error(MessageFormat.format("Failed to update the feed {0}:\n{1}", url.toString(), e.getMessage()), e);
        }
        return Collections.emptyList();
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public String getCaption() {
        return caption;
    }
}
