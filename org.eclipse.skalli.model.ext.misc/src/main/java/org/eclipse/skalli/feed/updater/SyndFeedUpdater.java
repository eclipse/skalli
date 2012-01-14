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

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.destination.DestinationService;
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

    private List<FeedEntry> getEntries(FeedFactory factory) throws FeedException {
        if (LOG.isInfoEnabled()) {
            LOG.info(MessageFormat.format("Updating ''{0}'' feed for project ''{1}'' from {2}", source, projectName, url.toString()));
        }
        return (new Converter(factory)).syndFeed2Entry(getSyndFeed());
    }

    private SyndFeed getSyndFeed() throws FeedException {
        SyndFeed syndFeed = null;
        DestinationService destinationService = Services.getService(DestinationService.class);
        if (destinationService != null) {
            Reader reader = null;
            try {
                LOG.info("GET " + url); //$NON-NLS-1$
                GetMethod method = new GetMethod(url.toExternalForm());
                method.setFollowRedirects(true);
                int status = destinationService.getClient(url).executeMethod(method);
                LOG.info(status + " " + HttpStatus.getStatusText(status)); //$NON-NLS-1$
                if (status != HttpStatus.SC_OK) {
                    throw new FeedException(MessageFormat.format("Failed to retrieve feed {0}", url));
                }
                reader = new InputStreamReader(method.getResponseBodyAsStream(), "UTF-8"); //$NON-NLS-1$
                syndFeed = new SyndFeedInput().build(reader);
                return syndFeed;
            } catch (IOException e) {
                throw new FeedException(MessageFormat.format("Failed to retrieve feed {0}", url), e);
            } finally {
                IOUtils.closeQuietly(reader);
            }
        }
        return syndFeed;
    }

    @Override
    public List<FeedEntry> updateFeed(FeedFactory factory) {
        try {
            return getEntries(factory);
        } catch (Exception e) {
            LOG.error("Problems updating the Feed (" + url.toString() + ":" + e.getMessage(), e);
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
