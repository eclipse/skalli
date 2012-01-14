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
package org.eclipse.skalli.view.internal.filter.ext;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.Member;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.model.ext.commons.PeopleExtension;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.project.ProjectService;
import org.eclipse.skalli.view.Consts;
import org.eclipse.skalli.view.internal.filter.FilterException;
import org.eclipse.skalli.view.internal.filter.FilterUtil;

public class P4Filter implements Filter {

    public static final String REQUEST_ATTRIBUTE_PROJECTURL = "projectUrl"; //$NON-NLS-1$
    public static final String REQUEST_ATTRIBUTE_PROPOSED_NAME = "proposedName"; //$NON-NLS-1$
    public static final String REQUEST_ATTRIBUTE_COMMITTERS = "committers"; //$NON-NLS-1$

    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // get some attributes from request provided by other filters
        String projectId = (String) request.getAttribute(Consts.ATTRIBUTE_PROJECTID);
        Project project = (Project) request.getAttribute(Consts.ATTRIBUTE_PROJECT);
        User user = (User) request.getAttribute(Consts.ATTRIBUTE_USER);

        FilterException fe = null;
        if (StringUtils.isBlank(projectId)) {
            fe = new FilterException("projectId is blank.");
        } else if (project == null) {
            fe = new FilterException("project is null.");
        } else if (user == null) {
            fe = new FilterException("user is null.");
        }
        if (fe != null) {
            FilterUtil.handleException(request, response, fe);
            // abort this filter, forward to error page is triggered
            return;
        }

        // Create: Project URL
        String projectUrl = getProjectUrl(httpRequest, projectId);
        request.setAttribute(REQUEST_ATTRIBUTE_PROJECTURL, projectUrl);

        // Create: Depot Proposal
        String depotProposal = getDepotProposal(project);
        request.setAttribute(REQUEST_ATTRIBUTE_PROPOSED_NAME, depotProposal);

        // Create: User(s)
        String comitters = getCommitters(project);
        request.setAttribute(REQUEST_ATTRIBUTE_COMMITTERS, comitters);

        // Filter
        chain.doFilter(request, response);
    }

    @SuppressWarnings("nls")
    private String getProjectUrl(HttpServletRequest httpRequest, String projectId) {
        return httpRequest.getRequestURL().toString().replaceFirst(httpRequest.getServletPath(), "")
                + Consts.URL_PROJECTS + "/" + projectId;
    }

    private String getDepotProposal(Project project) {
        StringBuffer sb = new StringBuffer();
        List<Project> parents = Services.getRequiredService(ProjectService.class).getParentChain(project.getUuid());
        for (Project parent : parents) {
            if (parent.getProjectId() == project.getProjectId()) {
                sb.insert(0, parent.getProjectId());
            } else {
                sb.insert(0, "/"); //$NON-NLS-1$
                sb.insert(0, parent.getOrConstructShortName());
            }
        }
        sb.insert(0, "//"); //$NON-NLS-1$
        return sb.toString();
    }

    private String getCommitters(Project project) {
        StringBuffer sb = new StringBuffer();
        PeopleExtension peopleExt = project.getExtension(PeopleExtension.class);
        if (peopleExt != null) {
            Set<String> committers = new TreeSet<String>();
            for (Member lead : peopleExt.getLeads()) {
                committers.add(lead.getUserID());
            }
            for (Member comitter : peopleExt.getMembers()) {
                committers.add(comitter.getUserID());
            }
            for (String comitter : committers) {
                if (sb.length() > 0) {
                    sb.append(";"); //$NON-NLS-1$
                }
                sb.append(comitter);
            }
        }
        return sb.toString();
    }

}
