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
package org.eclipse.skalli.api.rest.internal;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.skalli.api.rest.internal.admin.ProjectBackupResource;
import org.eclipse.skalli.api.rest.internal.admin.StatisticsResource;
import org.eclipse.skalli.api.rest.internal.admin.StatusResource;
import org.eclipse.skalli.api.rest.internal.resources.IssuesResource;
import org.eclipse.skalli.api.rest.internal.resources.ProjectResource;
import org.eclipse.skalli.api.rest.internal.resources.ProjectsResource;
import org.eclipse.skalli.api.rest.internal.resources.SubprojectsResource;
import org.eclipse.skalli.api.rest.internal.resources.UserResource;
import org.eclipse.skalli.api.rest.monitor.Monitorable;
import org.eclipse.skalli.services.configuration.rest.ConfigSection;
import org.eclipse.skalli.services.extension.rest.RestExtension;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestApplication extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(RestApplication.class);

    private final static Set<ConfigSection> configSections = new HashSet<ConfigSection>();
    private final static Set<Monitorable> serviceMonitors = new HashSet<Monitorable>();
    private final static Set<RestExtension> extensions = new HashSet<RestExtension>();

    @Override
    public synchronized Restlet createInboundRoot() {
        Router router = new Router(getContext());

        for (ConfigSection configSection : configSections) {
            router.attach("/config/" + configSection.getName(), configSection.getServerResource()); //$NON-NLS-1$
        }

        router.attach("/admin/status", StatusResource.class); //$NON-NLS-1$
        router.attach("/admin/statistics", StatisticsResource.class); //$NON-NLS-1$
        router.attach("/admin/backup", ProjectBackupResource.class); //$NON-NLS-1$
        attachServiceMonitors("/admin/services/", router); //$NON-NLS-1$

        router.attach("/projects", ProjectsResource.class); //$NON-NLS-1$
        router.attach("/projects/{id}", ProjectResource.class); //$NON-NLS-1$
        router.attach("/projects/{id}/issues", IssuesResource.class); //$NON-NLS-1$
        router.attach("/projects/{id}/subprojects", SubprojectsResource.class); //$NON-NLS-1$

        router.attach("/user/{id}", UserResource.class); //$NON-NLS-1$
        attachCustomResources(router);

        return router;
    }

    protected void bindConfigSection(ConfigSection configSection) {
        configSections.add(configSection);
    }

    protected void unbindConfigSection(ConfigSection configSection) {
        configSections.remove(configSection);
    }

    protected void bindMonitorable(Monitorable monitorable) {
        // TODO  find out if resources can be attached dynamically to the router:
        // monitors come and go together with their service, but currently
        // everything is attached to the router in createInboundRoot()
        //
        serviceMonitors.add(monitorable);
    }

    protected void unbindMonitorable(Monitorable monitorable) {
        // TODO  find out if resources can be detached dynamically from the router:
        // monitors come and go together with their service, but currently
        // everything is attached to the router in createInboundRoot()
        serviceMonitors.remove(monitorable);
    }

    protected void bindRestExtension(RestExtension restExtension) {
        // TODO  find out if resources can be attached dynamically to the router:
        // monitors come and go together with their service, but currently
        // everything is attached to the router in createInboundRoot()
        //
        extensions.add(restExtension);
    }

    protected void unbindRestExtension(RestExtension restExtension) {
        // TODO  find out if resources can be detached dynamically from the router:
        // monitors come and go together with their service, but currently
        // everything is attached to the router in createInboundRoot()
        extensions.remove(restExtension);
    }

    private void attachServiceMonitors(String basePath, Router router) {
        for (Monitorable serviceMonitor : serviceMonitors) {
            String servicePath = basePath + serviceMonitor.getServiceComponentName() + "/"; //$NON-NLS-1$
            for (String resourceName : serviceMonitor.getResourceNames()) {
                String path = servicePath + "monitors/" + resourceName; //$NON-NLS-1$
                Class<? extends ServerResource> monitorResource = serviceMonitor.getServerResource(resourceName);
                if (monitorResource != null) {
                    router.attach(path, monitorResource);
                    LOG.info("Attached service monitor to path " + path);
                } else {
                    LOG.warn("No monitor resource provided for path " + path);
                }
            }
        }
    }

    private void attachCustomResources(Router router) {
        for (RestExtension ext : extensions) {
            String resourcePath = ext.getResourcePath();
            if (!resourcePath.startsWith("/")) {
                resourcePath = "/" + resourcePath;
            }
            Class<? extends ServerResource> resource = ext.getServerResource();
            if (resource != null) {
                router.attach(resourcePath, resource);
                LOG.info("Attached REST extension " + resource.getName() + " to path " + resourcePath);
            } else {
                LOG.warn("No REST resource provided for path " + resourcePath);
            }
        }
    }
}
