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
package org.eclipse.skalli.feed;

import java.text.MessageFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.api.java.ProjectService;
import org.eclipse.skalli.api.java.feeds.Entry;
import org.eclipse.skalli.api.java.feeds.FeedManager;
import org.eclipse.skalli.api.java.feeds.FeedPersistenceService;
import org.eclipse.skalli.api.java.feeds.FeedProvider;
import org.eclipse.skalli.api.java.feeds.FeedServiceException;
import org.eclipse.skalli.api.java.feeds.FeedUpdater;
import org.eclipse.skalli.model.core.Project;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FeedManagerImpl implements FeedManager {

    private static final Logger LOG = LoggerFactory.getLogger(FeedManagerImpl.class);
    private boolean doSleep = true;

    private ProjectService projectService;
    private Set<FeedProvider> feedProviders = new HashSet<FeedProvider>();

    private FeedPersistenceService feedPersistenceService;

    public FeedManagerImpl() {
        super();
    }

    /**
     * Constructor to avoid sleeping, eg. to speed up the test.
     */
    FeedManagerImpl(boolean doSleep) {
        super();
        this.doSleep = doSleep;
    }

    protected void activate(ComponentContext context) {
        LOG.info(MessageFormat.format("[FeedManager] {0} : activated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void deactivate(ComponentContext context) {
        LOG.info(MessageFormat.format("[FeedManager] {0} : deactivated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void bindProjectService(ProjectService projectService) {
        this.projectService = projectService;
    }

    protected void unbindProjectService(ProjectService projectService) {
        this.projectService = null;
    }

    protected void bindFeedProvider(FeedProvider feedProvider) {
        feedProviders.add(feedProvider);
    }

    protected void unbindFeedProvider(FeedProvider feedProvider) {
        feedProviders.remove(feedProvider);
    }

    protected void bindFeedPersistenceService(FeedPersistenceService feedPersistenceService) {
        this.feedPersistenceService = feedPersistenceService;
    }

    protected void unbindFeedPersistenceService(FeedPersistenceService feedPersistenceService) {
        this.feedPersistenceService = null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.skalli.api.java.feeds.FeedManager#updateAllFeeds()
     */
    @Override
    public void updateAllFeeds() {
        LOG.info("Updating all project feeds...");
        List<Project> projects = projectService.getAll();
        for (Project project : projects) {

            if (LOG.isDebugEnabled()) {
                LOG.debug("updating feeds for project: " + project.getProjectId());
            }
            for (FeedProvider feedProvider : feedProviders) {
                List<FeedUpdater> feedUpdaters = feedProvider.getFeedUpdaters(project);
                for (FeedUpdater feedUpdater : feedUpdaters) {
                    try {
                        List<Entry> entries = feedUpdater.updateFeed();
                        if (!entries.isEmpty()) {
                            try {
                                for (Entry entry : entries) {
                                    entry.setSource(feedUpdater.getSource());
                                    entry.setProjectId(project.getUuid());
                                    if (StringUtils.isBlank(entry.getId())) {
                                        setDefaultEntryId(entry);
                                    }
                                }
                                feedPersistenceService.merge(entries);
                            } catch (FeedServiceException e) {
                                LOG.error("Failed to merge feed entries", e);
                            }
                        }
                    } catch (RuntimeException e) {
                        LOG.error("Failed to update the feed", e);
                    }
                }
            }

            if (doSleep) {
                try {
                    // delay the execution for 10 seconds, otherwise we may overload the remote systems
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
        LOG.info("Updating all project feeds: done");
    }

    private void setDefaultEntryId(Entry entry) {
        Date published = entry.getPublished();
        String publishedString = (published == null) ? "" : Long.toString(published.getTime());
        String id = entry.getProjectId().toString() + publishedString + entry.getSource();
        entry.setId(DigestUtils.shaHex(id));
    }
}
