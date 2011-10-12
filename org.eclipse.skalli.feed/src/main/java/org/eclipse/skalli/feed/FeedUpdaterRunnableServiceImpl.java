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
import java.util.UUID;

import org.eclipse.skalli.api.java.EventListener;
import org.eclipse.skalli.api.java.EventService;
import org.eclipse.skalli.api.java.events.EventCustomizingUpdate;
import org.eclipse.skalli.api.java.feeds.FeedManager;
import org.eclipse.skalli.api.java.tasks.RunnableSchedule;
import org.eclipse.skalli.api.java.tasks.SchedulerService;
import org.eclipse.skalli.common.configuration.ConfigurationService;
import org.eclipse.skalli.feed.internal.config.FeedUpdaterConfig;
import org.eclipse.skalli.feed.internal.config.FeedUpdaterResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FeedUpdaterRunnableServiceImpl implements FeedUpdaterRunnableService,
        EventListener<EventCustomizingUpdate> {

    private static final Logger LOG = LoggerFactory.getLogger(FeedUpdaterRunnableServiceImpl.class);

    private SchedulerService schedulerService;
    private ConfigurationService configService;
    private FeedManager feedManager;

    private UUID scheduleId;

    protected void bindSchedulerService(SchedulerService schedulerService) {
        LOG.info(MessageFormat.format("bindSchedulerService({0})", schedulerService)); //$NON-NLS-1$LOG.info(MessageFormat.format("bindSchedulerService({0})", schedulerService)); //$NON-NLS-1$
        this.schedulerService = schedulerService;
        synchronizeAllTasks();
    }

    protected void unbindSchedulerService(SchedulerService schedulerService) {
        LOG.info(MessageFormat.format("unbindSchedulerService({0})", schedulerService)); //$NON-NLS-1$
        scheduleId = null;
        this.schedulerService = null;
    }

    protected void bindConfigurationService(ConfigurationService configService) {
        LOG.info(MessageFormat.format("bindConfigurationService({0})", configService)); //$NON-NLS-1$
        this.configService = configService;
        synchronizeAllTasks();
    }

    protected void unbindConfigurationService(ConfigurationService configService) {
        LOG.info(MessageFormat.format("unbindConfigurationService({0})", configService)); //$NON-NLS-1$
        this.configService = null;
        synchronizeAllTasks();
    }

    protected void bindEventService(EventService eventService) {
        LOG.info(MessageFormat.format("bindEventService({0})", eventService)); //$NON-NLS-1$
        eventService.registerListener(EventCustomizingUpdate.class, this);
    }

    protected void unbindEventService(EventService eventService) {
        LOG.info(MessageFormat.format("unbindEventService({0})", eventService)); //$NON-NLS-1$
    }

    protected void bindFeedManager(FeedManager feedManager) {
        LOG.info(MessageFormat.format("bindFeedManager({0})", feedManager)); //$NON-NLS-1$
        this.feedManager = feedManager;
        synchronizeAllTasks();
    }

    protected void unbindFeedManager(FeedManager feedManager) {
        LOG.info(MessageFormat.format("unbindFeedManager({0})", feedManager)); //$NON-NLS-1$
        this.feedManager = null;
    }

    synchronized void startAllTasks() {
        if (schedulerService != null) {
            if (scheduleId != null) {
                stopAllTasks();
            }
            if (configService != null) {
                final FeedUpdaterConfig resolverConfig = configService.readCustomization(
                        FeedUpdaterResource.MAPPINGS_KEY, FeedUpdaterConfig.class);
                if (resolverConfig != null) {

                    RunnableSchedule runnableSchedule = new RunnableSchedule(resolverConfig.getSchedule()) {
                        @Override
                        public Runnable getRunnable() {
                            return new Runnable() {

                                @Override
                                public void run() {
                                    if (feedManager != null) {
                                        feedManager.updateAllFeeds();
                                    }

                                }

                            };

                        }
                    };
                    scheduleId = schedulerService.registerSchedule(runnableSchedule);
                }
            }
        }
    }

    synchronized void stopAllTasks() {
        if (schedulerService != null && scheduleId != null) {
            schedulerService.unregisterSchedule(scheduleId);
            scheduleId = null;
        }
    }

    void synchronizeAllTasks() {
        stopAllTasks();
        startAllTasks();
    }

    @Override
    public void onEvent(EventCustomizingUpdate event) {
        if (FeedUpdaterResource.MAPPINGS_KEY.equals(event.getCustomizationName())) {
            synchronizeAllTasks();
        }
    }
}
