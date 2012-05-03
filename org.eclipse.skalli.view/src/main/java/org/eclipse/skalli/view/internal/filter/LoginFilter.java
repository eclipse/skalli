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
import org.eclipse.skalli.commons.UUIDUtils;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.group.GroupUtils;
import org.eclipse.skalli.services.permit.Permit;
import org.eclipse.skalli.services.permit.PermitService;
import org.eclipse.skalli.services.permit.Permits;
import org.eclipse.skalli.services.project.ProjectService;
import org.eclipse.skalli.services.project.ProjectUtils;
import org.eclipse.skalli.services.user.UserUtils;
import org.eclipse.skalli.view.Consts;

/**
 * This filter determines the user and requested project from the servlet request
 * and performs a login of the user with the {@link PermitService permit service}.
 *
 * This  filter sets the following boolean attributes:
 * <ul>
 * <li>"{@value Consts#ATTRIBUTE_ANONYMOUS_USER} - <code>true</code>, if the user is not the anonymous
 * user, <code>false</code> otherwise.</li>
 * <li>"{@value Consts#ATTRIBUTE_PROJECTADMIN} - <code>true</code>, if the user is not anonymous, the
 * request specified a project and the user is an administrator of this project, i.e. has the permit
 * <tt>PUT /projects/&lt;projectId&gt; ALLOW</tt>, <code>false</code> otherwise.</li>
 * <li>"{@value Consts#ATTRIBUTE_PARENTPROJECTADMIN} - <code>true</code>, if the user is administrator
 * of one of the projects in the parent chain of the requested project, <code>false</code> otherwise.</li>
 * </ul>
 * <p>
 * This filter sets the following attributes if user is not anonymous:
 * <ul>
 * <li>"{@value Consts#ATTRIBUTE_USERID}" - the unique identifier of the logged in user.</li>
 * <li>"{@value Consts#ATTRIBUTE_USER} "- the {@link User} instance of the logged in user; undefined if the user
 * service in charge does not know the logged in user.</li>
 * </ul>
 * <p>
 * This filter sets the following attributes if the request specifies a project:
 * <ul>
 * <li>"{@value Consts#ATTRIBUTE_PROJECT}" - the {@link Project} instance.</li>
 * <li>"{@value Consts#ATTRIBUTE_PROJECTID}" - the {@link Project#getName() symbolic identifier} of the project.</li>
 * <li>"{@value Consts#ATTRIBUTE_PROJECTUUID}" - the {@link Project#getUuid() unique identifier} of the project.</li>
 * </ul>
 * Otherwise the filter retrieves the {@link HttpServletRequest#getPathInfo() path info} from the request and
 * set the attribute "{@value Consts#ATTRIBUTE_WINDOWNAME}".
 */
public class LoginFilter implements Filter {

    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String pathInfo = httpRequest.getPathInfo();
        String paramProjectId = request.getParameter(Consts.PARAM_ID);

        // determine the project from the URL
        Project project = null;
        ProjectService projectService = Services.getRequiredService(ProjectService.class);

        // first check if project can be deduced from pathInfo
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
                        new FilterException(String.format("Invalid project identifier '%s' specified in query '%s'",
                                paramProjectId, Consts.PARAM_ID)));
            }
        }

        if (project != null) {
            request.setAttribute(Consts.ATTRIBUTE_PROJECT, project);
            request.setAttribute(Consts.ATTRIBUTE_PROJECTID, project.getProjectId());
            request.setAttribute(Consts.ATTRIBUTE_PROJECTUUID, project.getUuid().toString());
        } else {
          // do nothing if project is null since this filter runs during
          // creation of projects and displaying of search results, too
        }

        // determine the user and login
        PermitService permitService = Services.getRequiredService(PermitService.class);
        String userId = permitService.login(httpRequest, project);
        boolean isAnonymousUser = StringUtils.isBlank(userId);
        if (!isAnonymousUser) {
            request.setAttribute(Consts.ATTRIBUTE_USERID, userId);
            User user = UserUtils.getUser(userId);
            if (user != null) {
                request.setAttribute(Consts.ATTRIBUTE_USER, user);
            }
        }

        boolean isProjectAdmin = !isAnonymousUser && project != null &&
                (GroupUtils.isAdministrator(userId) || Permits.isAllowed(Permit.ACTION_PUT, project));
        boolean isProjectAdminInParentChain = !isAnonymousUser && project != null &&
                ProjectUtils.isProjectAdminInParentChain(userId, project);

        request.setAttribute(Consts.ATTRIBUTE_ANONYMOUS_USER, isAnonymousUser);
        request.setAttribute(Consts.ATTRIBUTE_PROJECTADMIN, isProjectAdmin);
        request.setAttribute(Consts.ATTRIBUTE_PARENTPROJECTADMIN, isProjectAdminInParentChain);

        // proceed along the chain
        chain.doFilter(request, response);
    }
}
