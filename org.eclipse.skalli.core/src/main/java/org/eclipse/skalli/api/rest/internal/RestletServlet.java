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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.Statistics;
import org.eclipse.skalli.commons.UUIDUtils;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.permit.PermitService;
import org.eclipse.skalli.services.project.ProjectService;
import org.eclipse.skalli.services.user.UserUtils;
import org.restlet.ext.servlet.ServerServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestletServlet extends ServerServlet {
    private static final long serialVersionUID = -7953560055729006206L;
    private static final Logger LOG = LoggerFactory.getLogger(RestletServlet.class);

    @Override
    protected Class<?> loadClass(String className) throws ClassNotFoundException {
        Class<?> ret = null;

        // Try restlet classloader first
        if (ret == null) {
            try {
                ret = super.loadClass(className);
            } catch (ClassNotFoundException e) {
                // Ignore, because that's the whole point here...
                LOG.debug(MessageFormat.format("Class {0} not found in current bundle", className)); //$NON-NLS-1$
            }
        }

        // Next, try the current context classloader
        if (ret == null) {
            try {
                ret = Thread.currentThread().getContextClassLoader().loadClass(className);
            } catch (ClassNotFoundException e) {
                // Ignore, because that's the whole point here...
                LOG.debug(MessageFormat.format("Class {0} not found by context class loader", className)); //$NON-NLS-1$
            }
        }

        if (ret == null) {
            throw new ClassNotFoundException("Class not found: " + className); //$NON-NLS-1$
        }

        return ret;
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        // determine the project from the URL
        Project project = null;
        ProjectService projectService = Services.getRequiredService(ProjectService.class);

        // first check if project can be deduced from pathInfo
        if (StringUtils.isNotBlank(pathInfo)) {
            if (pathInfo.startsWith("/")) { //$NON-NLS-1$
                pathInfo = pathInfo.replaceFirst("/", StringUtils.EMPTY); //$NON-NLS-1$
            }
            if (pathInfo.contains("/")) { //$NON-NLS-1$
                pathInfo = pathInfo.substring(0, pathInfo.indexOf("/")); //$NON-NLS-1$
            }
            project = projectService.getProjectByProjectId(pathInfo);

            // project not found by name, search by UUID
            if (project == null && UUIDUtils.isUUID(pathInfo)) {
                UUID uuid = UUIDUtils.asUUID(pathInfo);
                project = projectService.getByUUID(uuid);
                // project not found by UUID, search for deleted project by UUID
                if (project == null) {
                    project = projectService.getDeletedProject(uuid);
                }
            }
        }

        // determine the user and login
        PermitService permitService = Services.getRequiredService(PermitService.class);
        String userId = permitService.login(request, project);
        User user = null;
        boolean isAnonymousUser = StringUtils.isBlank(userId);
        if (!isAnonymousUser) {
            user = UserUtils.getUser(userId);
        }

        Statistics statistics = Statistics.getDefault();
        if (user != null) {
            statistics.trackUser(userId, user.getDepartment(), user.getLocation());
        } else if (StringUtils.isNotBlank(userId)) {
            statistics.trackUser(userId, null, null);
        }
        String referer = request.getHeader("Referer"); //$NON-NLS-1$
        if (StringUtils.isBlank(referer)) {
            referer = request.getParameter("referer"); //$NON-NLS-1$
        }
        if (StringUtils.isNotBlank(referer)) {
            statistics.trackReferer(userId, referer);
        }
        statistics.trackUsage(userId, MessageFormat.format("{0} {1}", //$NON-NLS-1$
                request.getMethod(), request.getRequestURI()), referer);
        if (project != null) {
            statistics.trackUsage(userId, MessageFormat.format("{0} /api/projects/{0}", //$NON-NLS-1$
                    request.getMethod(), project.getProjectId()), referer);
        }
        String browser = request.getHeader("User-Agent"); //$NON-NLS-1$
        if (StringUtils.isNotBlank(browser)) {
            statistics.trackBrowser(userId, browser);
        }

        super.service(request, response);
    }

}
