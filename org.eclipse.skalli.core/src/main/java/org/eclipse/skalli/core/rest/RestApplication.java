/*******************************************************************************
 * Copyright (c) 2010-2014 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.core.rest;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.core.rest.admin.ProjectBackupResource;
import org.eclipse.skalli.core.rest.admin.StatisticsBackupResource;
import org.eclipse.skalli.core.rest.admin.StatisticsResource;
import org.eclipse.skalli.core.rest.admin.StatusResource;
import org.eclipse.skalli.core.rest.monitor.Monitorable;
import org.eclipse.skalli.core.rest.resources.InheritableExtensionResource;
import org.eclipse.skalli.core.rest.resources.InheritableExtensionsResource;
import org.eclipse.skalli.core.rest.resources.IssuesResource;
import org.eclipse.skalli.core.rest.resources.ProjectResource;
import org.eclipse.skalli.core.rest.resources.ProjectsResource;
import org.eclipse.skalli.core.rest.resources.SubprojectsResource;
import org.eclipse.skalli.core.rest.resources.TimelineResource;
import org.eclipse.skalli.core.rest.resources.UserPermitsResource;
import org.eclipse.skalli.core.rest.resources.UserResource;
import org.eclipse.skalli.services.configuration.ConfigSection;
import org.eclipse.skalli.services.extension.rest.ErrorRepresentation;
import org.eclipse.skalli.services.extension.rest.RestExtension;
import org.eclipse.skalli.services.rest.RequestContext;
import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;
import org.restlet.service.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestApplication extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(RestApplication.class);

    private final static Set<ConfigSection<?>> configSections = new HashSet<ConfigSection<?>>();
    private final static Set<Monitorable> serviceMonitors = new HashSet<Monitorable>();
    private final static Set<RestExtension> extensions = new HashSet<RestExtension>();

    @Override
    public synchronized Restlet createInboundRoot() {
        Router router = new Router(getContext());

        attachConfigurationResources(router);

        router.attach("/admin/status", StatusResource.class); //$NON-NLS-1$
        router.attach("/admin/statistics", StatisticsResource.class); //$NON-NLS-1$
        router.attach("/admin/statistics/backup", StatisticsBackupResource.class); //$NON-NLS-1$
        router.attach("/admin/statistics/{section}", StatisticsResource.class); //$NON-NLS-1$
        router.attach("/admin/statistics/{section}/{filter}", StatisticsResource.class); //$NON-NLS-1$
        router.attach("/admin/backup", ProjectBackupResource.class); //$NON-NLS-1$
        attachServiceMonitors("/admin/services/", router); //$NON-NLS-1$

        router.attach("/projects", ProjectsResource.class); //$NON-NLS-1$
        router.attach("/projects/{id}", ProjectResource.class); //$NON-NLS-1$
        router.attach("/projects/{id}/issues", IssuesResource.class); //$NON-NLS-1$
        router.attach("/projects/{id}/subprojects", SubprojectsResource.class); //$NON-NLS-1$
        router.attach("/projects/{id}/timeline", TimelineResource.class); //$NON-NLS-1$
        router.attach("/projects/{id}/extensions", InheritableExtensionsResource.class); //$NON-NLS-1$
        router.attach("/projects/{id}/extensions/{shortName}", InheritableExtensionResource.class); //$NON-NLS-1$

        router.attach("/user/{userId}", UserResource.class); //$NON-NLS-1$
        router.attach("/users/{userId}", UserResource.class); //$NON-NLS-1$
        router.attach("/users/{userId}/permits", UserPermitsResource.class); //$NON-NLS-1$
        router.attach("/users/{userId}/permits/{projectId}", UserPermitsResource.class); //$NON-NLS-1$

        attachCustomResources(router);

        return router;
    }

    protected void bindConfigSection(ConfigSection<?> configSection) {
        configSections.add(configSection);
    }

    protected void unbindConfigSection(ConfigSection<?> configSection) {
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

    public RestApplication() {
        setStatusService(new StatusService() {
            @Override
            public Representation getRepresentation(Status status, Request request, Response response) {
                Throwable t = status.getThrowable();
                if (t != null) {
                    RequestContext context = new RequestContext(request);
                    String errorId = MessageFormat.format("rest:{0}:00", context.getPath()); //$NON-NLS-1$
                    String message = "An unexpected exception happend. Please report this error " +
                            "response to the server administrators.";
                    LOG.error(MessageFormat.format(MessageFormat.format("{0} ({1})", message, errorId), t)); //$NON-NLS-1$
                    return new ErrorRepresentation(context, status, errorId, message);
                }
                return super.getRepresentation(status, request, response);
            }
        });
    }

    private void attachConfigurationResources(Router router) {
        for (ConfigSection<?> configSection : configSections) {
            String[] resourcePaths = configSection.getResourcePaths();
            if (resourcePaths == null || resourcePaths.length == 0) {
                LOG.warn(MessageFormat.format(
                        "Configuration extension ''{0}'' does not register any resource paths",
                        configSection.getStorageKey()));
                return;
            }
            for (String resourcePath: resourcePaths) {
                resourcePath = StringUtils.trim(resourcePath);
                if (StringUtils.isBlank(resourcePath) || "/".equals(resourcePath)) { //$NON-NLS-1$
                    LOG.warn(MessageFormat.format(
                            "Configuration extension ''{0}'': resource path '/' is not allowed",
                            configSection.getStorageKey()));
                    continue;
                }
                if (!resourcePath.startsWith("/")) { //$NON-NLS-1$
                    resourcePath = "/" + resourcePath; //$NON-NLS-1$
                }
                Class<? extends ServerResource> resource = configSection.getServerResource(resourcePath);
                if (resource != null) {
                    router.attach("/config" + resourcePath, resource); //$NON-NLS-1$
                    LOG.info(MessageFormat.format(
                            "Attached REST resource ''{0}'' to path ''{1}'' for configuration extension ''{2}''",
                            resource.getName(), resourcePath, configSection.getStorageKey()));
                } else {
                    LOG.warn(MessageFormat.format(
                            "Configuration extension ''{0}'': No REST resource provided for path ''{1}''",
                            configSection.getStorageKey(), resourcePath));
                }
            }
        }
    }

    private void attachServiceMonitors(String basePath, Router router) {
        for (Monitorable serviceMonitor : serviceMonitors) {
            String servicePath = basePath + serviceMonitor.getServiceComponentName() + "/"; //$NON-NLS-1$
            for (String resourceName : serviceMonitor.getResourceNames()) {
                String path = servicePath + "monitors/" + resourceName; //$NON-NLS-1$
                Class<? extends ServerResource> monitorResource = serviceMonitor.getServerResource(resourceName);
                if (monitorResource != null) {
                    router.attach(path, monitorResource);
                    LOG.info(MessageFormat.format("Attached service monitor to path {0}", path));
                } else {
                    LOG.warn(MessageFormat.format("No monitor resource provided for path {0}", path));
                }
            }
        }
    }

    private void attachCustomResources(Router router) {
        for (RestExtension ext : extensions) {
            String[] resourcePaths = ext.getResourcePaths();
            if (resourcePaths == null  || resourcePaths.length == 0) {
                LOG.warn(MessageFormat.format(
                        "REST extension ''{0}'' does not register any resource paths",
                        ext.getClass().getName()));
                continue;
            }
            for (String resourcePath: resourcePaths) {
                resourcePath = StringUtils.trim(resourcePath);
                if (StringUtils.isBlank(resourcePath) || "/".equals(resourcePath)) { //$NON-NLS-1$
                    LOG.warn(MessageFormat.format(
                            "REST extension ''{0}'': resource path '/' is not allowed",
                            ext.getClass().getName()));
                    continue;
                }
                if (!resourcePath.startsWith("/")) { //$NON-NLS-1$
                    resourcePath = "/" + resourcePath; //$NON-NLS-1$
                }
                Class<? extends ServerResource> resource = ext.getServerResource(resourcePath);
                if (resource != null) {
                    router.attach(resourcePath, resource);
                    LOG.info(MessageFormat.format(
                            "Attached REST resource ''{0}'' to path ''{1}''",
                            resource.getName(), resourcePath));
                } else {
                    LOG.warn(MessageFormat.format(
                            "REST extension ''{0}'': No REST resource provided for path ''{1}''",
                            ext.getClass().getName(), resourcePath));
                }
            }
        }
    }
}
