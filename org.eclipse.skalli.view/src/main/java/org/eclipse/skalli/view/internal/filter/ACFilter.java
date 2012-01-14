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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.services.group.GroupUtils;
import org.eclipse.skalli.services.project.ProjectUtils;
import org.eclipse.skalli.view.Consts;

public class ACFilter implements Filter {

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {

        // check userId and project instance from depending servlet filters
        String userId = (String) request.getAttribute(Consts.ATTRIBUTE_USERID);
        Project project = (Project) request.getAttribute(Consts.ATTRIBUTE_PROJECT);

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        String servletPath = httpRequest.getServletPath();
        String pathInfo = httpRequest.getPathInfo();

        if (servletPath.startsWith(Consts.URL_PROJECTS)) {
            // handling of 'Consts.URL_PROJECTS' url mapping
            String actionValue = request.getParameter(Consts.PARAM_ACTION);
            if (project != null && Consts.PARAM_VALUE_EDIT.equals(actionValue)) {
                // handles 'Consts.URL_PROJECTS/{projectId}?Consts.PARAM_ACTION=Consts.PARAM_VALUE_EDIT'
                if (!(GroupUtils.isAdministrator(userId) || ProjectUtils.isProjectAdmin(userId, project))) {
                    AccessControlException e = new AccessControlException(MessageFormat.format(
                            "User {0} is not authorized to call this page for project {1}.", userId,
                            project.getProjectId()));
                    FilterUtil.handleACException(httpRequest, response, e);
                }
            } else if (project == null && pathInfo != null && pathInfo.length() > 0) {
                // handles 'Consts.URL_PROJECTS/{projectIdThatDoesNotExist}' => project creation dialog
                if (StringUtils.isBlank(userId)) {
                    AccessControlException e = new AccessControlException(
                            "Anonymous user is not authorized to create new projects.");
                    FilterUtil.handleACException(httpRequest, response, e);
                }
            }
            // else {
            // handles all other urls like
            // 'Consts.URL_PROJECTS/{projectId}?Consts.PARAM_QUERY={query}
            // 'Consts.URL_PROJECTS/{projectId}?Consts.PARAM_TAG={tag}
            // 'Consts.URL_PROJECTS/{projectId}?Consts.PARAM_USER={user}
            // ...
            // }
        } else {
            // handling of all other url mappings
            if (StringUtils.isBlank(userId)) {
                AccessControlException e = new AccessControlException(
                        "Anonymous users are not authorized to call this page.");
                FilterUtil.handleACException(request, response, e);
            }
            if (!StringUtils.isBlank(pathInfo)) {
                if (project == null) {
                    FilterException e = new FilterException(MessageFormat.format(
                            "project instance is null, servletPath is {0}.",
                            servletPath));
                    FilterUtil.handleException(request, response, e);
                } else if (!GroupUtils.isAdministrator(userId) && !ProjectUtils.isProjectAdmin(userId, project)) {
                    AccessControlException e = new AccessControlException("User is not authorized to call this page.");
                    FilterUtil.handleACException(request, response, e);
                }
            } // else {
              // project creation dialog, do nothing
              // }

        }

        // proceed along the chain
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }

}
