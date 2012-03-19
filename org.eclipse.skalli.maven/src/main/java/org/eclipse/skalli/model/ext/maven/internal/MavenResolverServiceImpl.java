/*******************************************************************************
 * Copyright (c) 2010, 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.model.ext.maven.internal;

import java.text.MessageFormat;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.ext.maven.MavenResolverService;
import org.eclipse.skalli.model.ext.maven.internal.config.MavenResolverConfig;
import org.eclipse.skalli.model.ext.maven.internal.config.MavenResolverResource;
import org.eclipse.skalli.nexus.NexusClient;
import org.eclipse.skalli.services.configuration.ConfigurationService;
import org.eclipse.skalli.services.event.EventCustomizingUpdate;
import org.eclipse.skalli.services.event.EventListener;
import org.eclipse.skalli.services.event.EventService;
import org.eclipse.skalli.services.scheduler.RunnableSchedule;
import org.eclipse.skalli.services.scheduler.SchedulerService;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MavenResolverServiceImpl implements MavenResolverService, EventListener<EventCustomizingUpdate> {

    private static final Logger LOG = LoggerFactory.getLogger(MavenResolverServiceImpl.class);

    private SchedulerService schedulerService;
    private ConfigurationService configService;
    private NexusClient nexusClient;

    private UUID scheduleId;

    protected void activate(ComponentContext context) {
        LOG.info(MessageFormat.format("[MavenResolverService] {0} : activated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void deactivate(ComponentContext context) {
        LOG.info(MessageFormat.format("[MavenResolverService] {0} : deactivated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

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

    protected void bindNexusClient(NexusClient nexusClient) {
        LOG.info(MessageFormat.format("bindNexusClient({0})", nexusClient)); //$NON-NLS-1$
        this.nexusClient = nexusClient;
    }

    protected void unbindNexusClient(NexusClient nexusClient) {
        LOG.info(MessageFormat.format("unbindNexusClient({0})", nexusClient)); //$NON-NLS-1$
        this.nexusClient = null;
    }

    @Override
    public void queue(Project project, String userId) {
        new MavenResolverRunnable(nexusClient, userId, project).run();
    }

    synchronized void startAllTasks() {
        if (schedulerService != null) {
            if (scheduleId != null) {
                stopAllTasks();
            }
            if (configService != null) {
                final MavenResolverConfig resolverConfig = configService.readCustomization(
                        MavenResolverResource.MAPPINGS_KEY, MavenResolverConfig.class);
                if (resolverConfig != null) {

                    RunnableSchedule runnableSchedule = new RunnableSchedule(resolverConfig.getSchedule()) {
                        @Override
                        public Runnable getRunnable() {
                            String userId = resolverConfig.getUserId();
                            if (StringUtils.isBlank(userId)) {
                                userId = MavenResolverService.class.getName();
                            }
                            return new MavenResolverRunnable(nexusClient, userId);
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
        if (MavenResolverResource.MAPPINGS_KEY.equals(event.getCustomizationName())) {
            synchronizeAllTasks();
        }
    }
}
