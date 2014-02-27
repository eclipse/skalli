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
import java.security.AccessControlException;
import java.text.MessageFormat;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.Statistics;
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
import org.eclipse.skalli.services.user.UserServices;
import org.eclipse.skalli.view.Consts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter determines the user and requested project from the servlet request
 * and performs a login of the user with the {@link PermitService permit service}.
 * Furthermore, the filter is the basis for {@link Statistics statistics} tracking.
 *
 * This  filter sets the following boolean attributes:
 * <ul>
 * <li>{@link Consts#ATTRIBUTE_ANONYMOUS_USER} - <code>true</code>, if the user is not the anonymous
 * user, <code>false</code> otherwise.</li>
 * <li>{@link Consts#ATTRIBUTE_PROJECTADMIN} - <code>true</code>, if the user is not anonymous, the
 * request specified a project and the user is an administrator of this project, i.e. has the permit
 * <tt>PUT /projects/&lt;projectId&gt; ALLOW</tt>, <code>false</code> otherwise.</li>
 * <li>{@link Consts#ATTRIBUTE_PARENTPROJECTADMIN} - <code>true</code>, if the user is administrator
 * of one of the projects in the parent chain of the requested project, <code>false</code> otherwise.</li>
 * </ul>
 * <p>
 * This filter sets the following attributes if user is not anonymous:
 * <ul>
 * <li>{@link Consts#ATTRIBUTE_USERID} - the unique identifier of the logged in user.</li>
 * <li>{@link Consts#ATTRIBUTE_USER} - the {@link User} instance of the logged in user; undefined if the user
 * service in charge does not know the logged in user.</li>
 * </ul>
 * <p>
 * This filter sets the following attributes if the request specifies a project:
 * <ul>
 * <li>{@link Consts#ATTRIBUTE_PROJECT} - the {@link Project} instance.</li>
 * <li>{@link Consts#ATTRIBUTE_PROJECTID} - the {@link Project#getName() symbolic identifier} of the project.</li>
 * <li>{@link Consts#ATTRIBUTE_PROJECTUUID} - the {@link Project#getUuid() unique identifier} of the project.</li>
 * </ul>
 * Otherwise the filter retrieves the {@link HttpServletRequest#getPathInfo() path info} from the request and
 * sets the attribute {@link Consts#ATTRIBUTE_WINDOWNAME}.
 * <p>
 * For convenience the filter sets the following attributes that are derived from the request URL:
 * <ul>
 * <li>{@link Consts#ATTRIBUTE_WEBLOCATOR} - <tt>schema://host:port</tt></li>
 * <li>{@link Consts#ATTRIBUTE_BASE_URL} - <tt>schema://host:port/contextPath</tt></li>
 * <li>{@link Consts#ATTRIBUTE_SERVLET_URL} - <tt>schema://host:port/contextPath/servletPath</tt></li>
 * </ul>
 */
public class LoginFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(LoginFilter.class);

    private boolean rejectAnonymousUsers;

    @Override
    public void init(FilterConfig config) throws ServletException {
        this.rejectAnonymousUsers = BooleanUtils.toBoolean(
                config.getInitParameter("rejectAnonymousUsers")); //$NON-NLS-1$
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {

        long timeBeginnProcessing = System.currentTimeMillis();

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        String pathInfo = httpRequest.getPathInfo();
        String requestURL = httpRequest.getRequestURL().toString();

        // servletUrl = schema://host:port/contextPath/servletPath
        String servletURL = StringUtils.removeEnd(requestURL, pathInfo);
        request.setAttribute(Consts.ATTRIBUTE_SERVLET_URL, servletURL);

        // baseUrl = schema://host:port/contextPath
        String baseURL = StringUtils.removeEnd(servletURL, httpRequest.getServletPath());
        request.setAttribute(Consts.ATTRIBUTE_BASE_URL, baseURL);

        // webLocator = schema://host:port
        String webLocator = StringUtils.removeEnd(requestURL, httpRequest.getRequestURI());
        request.setAttribute(Consts.ATTRIBUTE_WEBLOCATOR, webLocator);

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

        // login and ensure that the user is allowed to access
        PermitService permitService = Services.getRequiredService(PermitService.class);
        String userId = permitService.login(httpRequest, project);
        User user = null;
        boolean isAnonymousUser = StringUtils.isBlank(userId);
        if (isAnonymousUser && rejectAnonymousUsers) {
            FilterUtil.handleACException(httpRequest, response,
                    new AccessControlException("Forbidden for anonymous users"));
        }
        if (!isAnonymousUser) {
            request.setAttribute(Consts.ATTRIBUTE_USERID, userId);
            String userDisplayName = userId;
            user = UserServices.getUser(userId);
            if (user != null) {
                userDisplayName = user.getDisplayName();
                request.setAttribute(Consts.ATTRIBUTE_USER, user);
            }
            request.setAttribute(Consts.ATTRIBUTE_USER_DISPLAY_NAME, userDisplayName);
        }

        boolean isProjectAdmin = !isAnonymousUser && project != null &&
                (GroupUtils.isAdministrator(userId) || Permits.isAllowed(Permit.ACTION_PUT, project));
        boolean isProjectAdminInParentChain = !isAnonymousUser && project != null &&
                ProjectUtils.isProjectAdminInParentChain(userId, project);

        request.setAttribute(Consts.ATTRIBUTE_ANONYMOUS_USER, isAnonymousUser);
        request.setAttribute(Consts.ATTRIBUTE_PROJECTADMIN, isProjectAdmin);
        request.setAttribute(Consts.ATTRIBUTE_PARENTPROJECTADMIN, isProjectAdminInParentChain);

        // track the access
        Statistics statistics = Statistics.getDefault();
        if (user != null) {
            statistics.trackUser(userId, user.getDepartment(), user.getLocation());
        } else if (StringUtils.isNotBlank(userId)) {
            statistics.trackUser(userId, null, null);
        }

        String referer = httpRequest.getHeader("Referer"); //$NON-NLS-1$
        if (StringUtils.isBlank(referer)) {
            referer = request.getParameter("referer"); //$NON-NLS-1$
        }

        if (StringUtils.isNotBlank(referer)) {
            statistics.trackReferer(userId, referer);
        }

        String requestLine = MessageFormat.format("{0} {1}", //$NON-NLS-1$
                httpRequest.getMethod(), httpRequest.getRequestURI());
        if (project != null) {
            requestLine = MessageFormat.format("{0} /projects/{1}", //$NON-NLS-1$
                    httpRequest.getMethod(), project.getProjectId());
        }
        statistics.trackUsage(userId, requestLine, referer);

        String browser = httpRequest.getHeader("User-Agent"); //$NON-NLS-1$
        if (StringUtils.isNotBlank(browser)) {
            statistics.trackBrowser(userId, browser);
        }

        // proceed along the chain
        chain.doFilter(request, response);

        // track the overall response time
        long responseTime = System.currentTimeMillis() - timeBeginnProcessing;
        statistics.trackResponseTime(userId, requestLine, responseTime);
        LOG.info(MessageFormat.format("{0}: responseTime={1} milliseconds)", requestLine, Long.toString(responseTime)));
    }
}
