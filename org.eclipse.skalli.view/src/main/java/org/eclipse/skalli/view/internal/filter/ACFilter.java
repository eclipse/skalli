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

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.view.Consts;

/**
 * Checks project related permits, e.g. that only project members or users with explicit PUT permit
 * are allowed to edit a project.
 * <p>
 * This filter reads the following attributes:
 * <ul>
 * <li>"{@value Consts#ATTRIBUTE_USERID}" - the identifier of the logged in user; if not defined: anonymous user.</li>
 * <li>"{@value Consts#ATTRIBUTE_PROJECT}" - the project, or <code>null</code> in case the project is to be created.</li>
 * </ul>
 * Furthermore, it reads the "{@value Consts#PARAM_ACTION}" query parameter.
 * <p>
 * This filter sets the following attributes:
 * <ul>
 * <li>"{@value Consts#ATTRIBUTE_PROJECTADMIN}" - <code>true</code>, if the logged in user is project administrator,
 * <code>false</code> otherwise.</li>
 * </ul>
 */
public class ACFilter implements Filter {

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // retrieve userId and project instance from previous filters in chain
        String userId = (String) request.getAttribute(Consts.ATTRIBUTE_USERID);
        Project project = (Project) request.getAttribute(Consts.ATTRIBUTE_PROJECT);
        boolean isAnonymousUser = BooleanUtils.toBoolean((Boolean)request.getAttribute(Consts.ATTRIBUTE_ANONYMOUS_USER));
        boolean isProjectAdmin = BooleanUtils.toBoolean((Boolean)request.getAttribute(Consts.ATTRIBUTE_PROJECTADMIN));

        String servletPath = httpRequest.getServletPath();
        String pathInfo = httpRequest.getPathInfo();

        if (servletPath.startsWith(Consts.URL_PROJECTS)) {
            // handle URL starting with /projects
            String actionValue = request.getParameter(Consts.PARAM_ACTION);
            if (project != null && Consts.PARAM_VALUE_EDIT.equals(actionValue)) {
                // handle /projects/{projectId}?action=edit
                if (!isProjectAdmin) {
                    AccessControlException e = new AccessControlException(MessageFormat.format(
                            "User {0} is not authorized to edit project {1}", userId,
                            project.getProjectId()));
                    FilterUtil.handleACException(httpRequest, response, e);
                }
            } else if (project == null && StringUtils.isNotBlank(pathInfo)) {
                // handle /projects/{projectId} with unknown projectId => project creation dialog
                if (isAnonymousUser) {
                    AccessControlException e = new AccessControlException(
                            "Anonymous users are not authorized to create new projects");
                    FilterUtil.handleACException(httpRequest, response, e);
                }
            }
        } else {
            // handle all other URLs not starting with /projects
            if (isAnonymousUser) {
                AccessControlException e = new AccessControlException(
                        "Anonymous users are not authorized to request this page");
                FilterUtil.handleACException(request, response, e);
            }
            if (StringUtils.isNotBlank(pathInfo)) {
                if (project == null) {
                    FilterException e = new FilterException(MessageFormat.format(
                            "No project instance available although servlet path is {0}.",
                            servletPath));
                    FilterUtil.handleException(request, response, e);
                } else if (!isProjectAdmin) {
                    AccessControlException e = new AccessControlException("User is not authorized to request this page");
                    FilterUtil.handleACException(request, response, e);
                }
            }
        }

        // proceed along the chain
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }

}
