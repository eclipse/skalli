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
package org.eclipse.skalli.core.feed;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.services.feed.FeedEntry;
import org.eclipse.skalli.services.feed.FeedManager;
import org.eclipse.skalli.services.feed.FeedPersistenceService;
import org.eclipse.skalli.services.feed.FeedProvider;
import org.eclipse.skalli.services.feed.FeedUpdater;
import org.eclipse.skalli.services.persistence.StorageException;
import org.eclipse.skalli.services.project.ProjectService;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FeedManagerComponent implements FeedManager {

    private static final Logger LOG = LoggerFactory.getLogger(FeedManagerComponent.class);

    private static final String UNKNOWN_SOURCE = "unknown"; //$NON-NLS-1$

    private ProjectService projectService;
    private FeedPersistenceService feedPersistenceService;
    private Set<FeedProvider> feedProviders = new HashSet<FeedProvider>();

    private boolean doSleep = true;

    public FeedManagerComponent() {
        super();
    }

    /**
     * Constructor to avoid sleeping, e.g. to speed up the test.
     */
    FeedManagerComponent(boolean doSleep) {
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

    @Override
    public void updateAllFeeds() {
        if (projectService == null) {
            LOG.error("Failed to update feeds since no project service is available");
            return;
        }
        if (feedPersistenceService== null) {
            LOG.warn("Failed to update feeds since no feed persistence service is available");
            return;
        }
        LOG.info("Updating all project feeds...");
        List<UUID> projectIds = new ArrayList<UUID>(projectService.keySet());
        for (UUID projectId : projectIds) {
            try {
                Project project = projectService.getByUUID(projectId);
                if (project == null) {
                    LOG.warn(MessageFormat.format(
                            "Failed to update feeds: A project with unique identifier ''{0}'' does not exist",
                            projectId));
                    return;
                }
                updateFeeds(project);
                if (doSleep) {
                    try {
                        // delay the execution for 10 seconds, otherwise we may overload the remote systems
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        LOG.error("Feed updater job has been interrupted", e);
                        break;
                    }
                }
            } catch (Exception e) {
                // ensure that the loop never breaks!
                LOG.error(MessageFormat.format(
                        "Unexpected Exception: Failed to update the feed for project {0}",
                        projectId), e);
            }
        }
        LOG.info("Updated all project feeds");
    }

    @Override
    public void updateFeeds(UUID projectId) {
        if (projectService == null) {
            LOG.error(MessageFormat.format(
                    "Failed to update feeds for project {0} since no project service is available",
                    projectId));
            return;
        }
        Project project = projectService.getByUUID(projectId);
        if (project == null) {
            LOG.warn(MessageFormat.format(
                    "Failed to update feeds: A project with unique identifier ''{0}'' does not exist",
                    projectId));
            return;
        }
        updateFeeds(project);
    }

    private void updateFeeds(Project project) {
        if (feedPersistenceService == null) {
            LOG.warn(MessageFormat.format(
                    "Failed to update feeds for project {0} since no project service is available",
                    project.getProjectId()));
            return;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format("updating feeds for project: {0}", project.getProjectId()));
        }
        for (FeedProvider feedProvider : feedProviders) {
            List<FeedUpdater> feedUpdaters = feedProvider.getFeedUpdaters(project);
            for (FeedUpdater feedUpdater : feedUpdaters) {
                try {
                    List<FeedEntry> entries = feedUpdater.updateFeed(feedPersistenceService);
                    if (!entries.isEmpty()) {
                        try {
                            for (FeedEntry entry : entries) {
                                setSource(entry, feedUpdater);
                                entry.setProjectId(project.getUuid());
                                setEntryId(entry);
                            }
                            feedPersistenceService.merge(entries);
                        } catch (StorageException e) {
                            LOG.error(MessageFormat.format(
                                    "Failed to merge feed entries for project {0}",
                                    project.getProjectId()), e);
                        }
                    }
                } catch (Exception e) {
                    LOG.error(MessageFormat.format(
                            "Unexpected Exception: Failed to update the feed for project {0}",
                            project.getProjectId()), e);
                }
            }
        }
    }

    private void setSource(FeedEntry entry, FeedUpdater feedUpdater) {
        String source = entry.getSource();
        if (StringUtils.isBlank(source)) {
            source = feedUpdater.getSource();
            if (StringUtils.isBlank(source)) {
                source = UNKNOWN_SOURCE;
            }
            entry.setSource(source);
        }
    }

    private void setEntryId(FeedEntry entry) {
        StringBuilder newId = new StringBuilder(entry.getProjectId().toString());
        Date published = entry.getPublished();
        if (published != null) {
            newId.append(Long.toString(published.getTime()));
        }
        newId.append(entry.getSource());
        if (StringUtils.isNotBlank(entry.getId())) {
            newId.append(entry.getId());
        }
        entry.setId(DigestUtils.shaHex(newId.toString()));
    }
}
