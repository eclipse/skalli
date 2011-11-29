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
package org.eclipse.skalli.view.internal.filter;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.api.java.ProjectService;
import org.eclipse.skalli.common.Consts;
import org.eclipse.skalli.common.Services;
import org.eclipse.skalli.common.util.UUIDUtils;
import org.eclipse.skalli.model.core.Project;

/**
 * This filter sets the following attributes if the request specifies a project:
 * <ul>
 * <li>"{@value Consts#ATTRIBUTE_PROJECT} - the {@link Project} instance.</li>
 * <li>"{@value Consts#ATTRIBUTE_PROJECTID} - the {@link Project#getName() symbolic identifier} of the project.</li>
 * <li>"{@value Consts#ATTRIBUTE_PROJECTUUID} - the {@link Project#getUuid() unique identifier} of the project.</li>
 * </ul>
 */
public class ProjectFilter implements Filter {

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String pathInfo = httpRequest.getPathInfo();
        String paramProjectId = request.getParameter(Consts.PARAM_ID);

        ProjectService projectService = Services.getRequiredService(ProjectService.class);

        Project project = null;

        // first check if project is provided in pathInfo
        if (StringUtils.isNotBlank(pathInfo)) {
            if (pathInfo.startsWith(FilterUtil.PATH_SEPARATOR)) {
                pathInfo = pathInfo.replaceFirst(FilterUtil.PATH_SEPARATOR, StringUtils.EMPTY);
            }
            if (pathInfo.contains(FilterUtil.PATH_SEPARATOR)) {
                pathInfo = pathInfo.substring(0, pathInfo.indexOf(FilterUtil.PATH_SEPARATOR));
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

            if (project == null) {
                request.setAttribute(Consts.ATTRIBUTE_WINDOWNAME, httpRequest.getPathInfo());
            }
        }

        // project not found by pathInfo, check if project is provided via URL parameter
        if (project == null && StringUtils.isNotBlank(paramProjectId)) {
            project = projectService.getProjectByProjectId(paramProjectId);
            if (project == null) {
                // currently we don't support a scenario where projects are passed via UUID
                FilterUtil.handleException(request, response,
                        new FilterException(String.format("Project id '%s' set in url parameter '%s' not valid.",
                                paramProjectId, Consts.PARAM_ID)));
            }
        }

        if (project != null) {
            request.setAttribute(Consts.ATTRIBUTE_PROJECT, project);
            request.setAttribute(Consts.ATTRIBUTE_PROJECTID, project.getProjectId());
            request.setAttribute(Consts.ATTRIBUTE_PROJECTUUID, project.getUuid().toString());
        } else {
          // do nothing if project is null as this filter runs during
          // creation of projects and displaying of search results, too
        }

        // proceed along the chain
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }
}
