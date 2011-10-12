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
import org.eclipse.skalli.api.java.feeds.Entry;
import org.eclipse.skalli.api.java.feeds.FeedUpdater;
import org.eclipse.skalli.common.util.HttpUtils;
import org.eclipse.skalli.feed.db.entities.Converter;
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

    private List<Entry> getEntries() throws FeedException {
        if (LOG.isInfoEnabled()) {
            LOG.info(MessageFormat.format("Updating ''{0}'' feed for project ''{1}'' from {2}", source, projectName, url.toString()));
        }
        return Converter.syndFeed2Feed(getSyndFeed(), url.toString());
    }

    private SyndFeed getSyndFeed() throws FeedException {
        Reader reader = null;
        try {
            LOG.info("GET " + url); //$NON-NLS-1$
            GetMethod method = new GetMethod(url.toExternalForm());
            method.setFollowRedirects(true);
            int status = HttpUtils.getClient(url).executeMethod(method);
            LOG.info(status + " " + HttpStatus.getStatusText(status)); //$NON-NLS-1$
            if (status != HttpStatus.SC_OK) {
                throw new FeedException(MessageFormat.format("Failed to retrieve feed {0}", url));
            }
            reader = new InputStreamReader(method.getResponseBodyAsStream(), "UTF-8"); //$NON-NLS-1$
            SyndFeed syndFeed = new SyndFeedInput().build(reader);
            return syndFeed;
        } catch (IOException e) {
            throw new FeedException(MessageFormat.format("Failed to retrieve feed {0}", url), e);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    @Override
    public List<Entry> updateFeed() {
        try {
            return getEntries();
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
