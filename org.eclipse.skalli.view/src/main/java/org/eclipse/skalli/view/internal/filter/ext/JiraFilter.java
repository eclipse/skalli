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
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.model.ValidationException;
import org.eclipse.skalli.model.ext.devinf.DevInfProjectExt;
import org.eclipse.skalli.model.ext.scrum.ScrumProjectExt;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.project.ProjectService;
import org.eclipse.skalli.view.Consts;
import org.eclipse.skalli.view.internal.filter.FilterException;
import org.eclipse.skalli.view.internal.filter.FilterUtil;

public class JiraFilter implements Filter {

    public static final String PARAMETER_CANCEL = "cancel"; //$NON-NLS-1$
    public static final String PARAMETER_SAVE = "save"; //$NON-NLS-1$
    public static final String PARAMETER_JIRA = "jira"; //$NON-NLS-1$
    public static final String PARAMETER_ADDSCRUMBACKLOG = "addScrumBacklog"; //$NON-NLS-1$
    public static final String PARAMETER_ADDBUGTRACKER = "addBugTracker"; //$NON-NLS-1$
    public static final String REQUEST_ATTRIBUTE_ADDSCRUMBACKLOG = PARAMETER_ADDSCRUMBACKLOG;
    public static final String REQUEST_ATTRIBUTE_ADDBUGTRACKER = PARAMETER_ADDBUGTRACKER;
    public static final String REQUEST_ATTRIBUTE_JIRAPROJECTKEY = "jiraProjectKey"; //$NON-NLS-1$
    public static final String REQUEST_ATTRIBUTE_PROJECTURL = "projectUrl"; //$NON-NLS-1$
    public static final String REQUEST_ATTRIBUTE_INVALIDURL = "invalidUrl"; //$NON-NLS-1$
    public static final String REQUEST_ATTRIBUTE_BUGTRACKER = "bugTracker"; //$NON-NLS-1$
    public static final String REQUEST_ATTRIBUTE_SCRUMBACKLOG = "scrumBacklog"; //$NON-NLS-1$
    public static final String REQUEST_ATTRIBUTE_JIRAURL = "remoteJiraUrl"; //$NON-NLS-1$
    public static final String REQUEST_ATTRIBUTE_EXCEPTION = "exception"; //$NON-NLS-1$
    public static final String REQUEST_ATTRIBUTE_JIRABASEURL = "jiraBaseUrl"; //$NON-NLS-1$
    public static final String REQUEST_ATTRIBUTE_VALIDATIONEXCEPTION = "validationException"; //$NON-NLS-1$
    public static final String REQUEST_ATTRIBUTE_SCRUMEXTINHERITED = "scrumExtInherited"; //$NON-NLS-1$
    public static final String REQUEST_ATTRIBUTE_DEVINFEXTINHERITED = "devInfExtInherited"; //$NON-NLS-1$

    public static final String REQUEST_ATTRIBUTE_NAME = "name"; //$NON-NLS-1$
    public static final String REQUEST_ATTRIBUTE_KEY = "key"; //$NON-NLS-1$
    public static final String REQUEST_ATTRIBUTE_BACK_URL = "backUrl"; //$NON-NLS-1$
    public static final String REQUEST_ATTRIBUTE_PROJECT_TYPE = "projectType"; //$NON-NLS-1$
    public static final String REQUEST_ATTRIBUTE_ABBREVIATED_DESCIPTION = "abbrevDescr"; //$NON-NLS-1$
    public static final String DEFAULT_PROJECT_TYPE = "All-in-one"; //$NON-NLS-1$


    // make jira configurable?
    public static final String JIRA_SERVER = "jtrack"; //$NON-NLS-1$
    public static final String JIRA_PROTOCOL = "https"; //$NON-NLS-1$
    public static final String JIRA_URL_CREATE = JIRA_PROTOCOL + "://" + JIRA_SERVER //$NON-NLS-1$
            + "/secure/CreateProject!default.jspa"; //$NON-NLS-1$
    public static final String JIRA_URL_PROJECT = JIRA_PROTOCOL + "://" + JIRA_SERVER + "/browse/"; //$NON-NLS-1$ //$NON-NLS-2$

    private static final String encoding = "UTF-8"; //$NON-NLS-1$
    private static final int MAX_LEN_DESCRIPTION = 100;

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {

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

        // check if cancel button was clicked, redirect to project details page
        if (request.getParameter(PARAMETER_CANCEL) != null) {
            ((HttpServletResponse) response).sendRedirect(Consts.URL_PROJECTS + "/" + projectId); //$NON-NLS-1$
            return;
        }

        // fetch settings from jira form
        boolean save = false;
        if (request.getParameter(PARAMETER_SAVE) != null) {
            save = true;
        }
        boolean addAsBugTracker = StringUtils.equals(request.getParameter(PARAMETER_ADDBUGTRACKER),
                Boolean.TRUE.toString());
        boolean addAsScrumBacklog = StringUtils.equals(request.getParameter(PARAMETER_ADDSCRUMBACKLOG),
                Boolean.TRUE.toString());
        String jira = request.getParameter(PARAMETER_JIRA);

        // process the form if save was clicked
        boolean invalidUrl = false;
        if (save) {
            if (jira != null) {
                String jiraUrl = JIRA_URL_PROJECT + URLEncoder.encode(jira, encoding);
                // check for valid jira url
                try {
                    new URL(jiraUrl);
                } catch (MalformedURLException e) {
                    invalidUrl = true; // used for rendering input validation info
                }
                if (StringUtils.isBlank(jira)) {
                    invalidUrl = true;
                }
                if (!invalidUrl) {
                    boolean modified = false;
                    // add as bug tracker if related checkbox was marked and dev inf extension is not inherited
                    if (addAsBugTracker && !project.isInherited(DevInfProjectExt.class)) {
                        DevInfProjectExt devInf = project.getExtension(DevInfProjectExt.class);
                        if (devInf == null) {
                            devInf = new DevInfProjectExt();
                            project.addExtension(devInf);
                        }
                        devInf.setBugtrackerUrl(jiraUrl);
                        modified = true;
                    }
                    // add as scrum backlog if related checkbox was marked and scrum extension is not inherited
                    if (addAsScrumBacklog && !project.isInherited(ScrumProjectExt.class)) {
                        ScrumProjectExt scrumExt = project.getExtension(ScrumProjectExt.class);
                        if (scrumExt == null) {
                            scrumExt = new ScrumProjectExt();
                            project.addExtension(scrumExt);
                        }
                        scrumExt.setBacklogUrl(jiraUrl);
                        modified = true;
                    }
                    try {
                        // only persist project when project was modified
                        if (modified) {
                            Services.getRequiredService(ProjectService.class).persist(project, user.getUserId());
                        }
                        // redirect to project page in any case
                        ((HttpServletResponse) response).sendRedirect(Consts.URL_PROJECTS + "/" + projectId); //$NON-NLS-1$
                    } catch (ValidationException e) {
                        request.setAttribute(REQUEST_ATTRIBUTE_VALIDATIONEXCEPTION, e);
                    }
                }
            } else {
                invalidUrl = true;
            }
        }

        // if we are here, the form was neither processed successfully nor canceled
        // put some attributes to servlet request for rendering

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String projectUrl = httpRequest.getRequestURL().toString().replaceFirst(httpRequest.getServletPath(), "") + Consts.URL_PROJECTS + "/" + projectId; //$NON-NLS-1$//$NON-NLS-2$
        request.setAttribute(REQUEST_ATTRIBUTE_PROJECTURL, projectUrl);
        request.setAttribute(REQUEST_ATTRIBUTE_INVALIDURL, invalidUrl);
        request.setAttribute(REQUEST_ATTRIBUTE_JIRABASEURL, JIRA_URL_PROJECT);
        if (jira != null) {
            request.setAttribute(REQUEST_ATTRIBUTE_JIRAPROJECTKEY, jira);
        }

        String addBugTracker = request.getParameter(PARAMETER_ADDBUGTRACKER);
        String addScrumBacklog = request.getParameter(PARAMETER_ADDSCRUMBACKLOG);
        if (project.isInherited(DevInfProjectExt.class)) {
            request.setAttribute(REQUEST_ATTRIBUTE_ADDBUGTRACKER, Boolean.FALSE);
            request.setAttribute(REQUEST_ATTRIBUTE_DEVINFEXTINHERITED, Boolean.TRUE);
        } else if (StringUtils.equals(addBugTracker, Boolean.FALSE.toString())) {
            request.setAttribute(REQUEST_ATTRIBUTE_ADDBUGTRACKER, Boolean.FALSE);
        } else {
            // set true as default
            request.setAttribute(REQUEST_ATTRIBUTE_ADDBUGTRACKER, Boolean.TRUE);
        }
        if (project.isInherited(ScrumProjectExt.class)) {
            request.setAttribute(REQUEST_ATTRIBUTE_ADDSCRUMBACKLOG, Boolean.FALSE);
            request.setAttribute(REQUEST_ATTRIBUTE_SCRUMEXTINHERITED, Boolean.TRUE);
        } else if (StringUtils.equals(addScrumBacklog, Boolean.FALSE.toString())) {
            request.setAttribute(REQUEST_ATTRIBUTE_ADDSCRUMBACKLOG, Boolean.FALSE);
        } else {
            // set true as default
            request.setAttribute(REQUEST_ATTRIBUTE_ADDSCRUMBACKLOG, Boolean.TRUE);
        }

        DevInfProjectExt devInf = project.getExtension(DevInfProjectExt.class);
        if (devInf != null && StringUtils.isNotBlank(devInf.getBugtrackerUrl())) {
            request.setAttribute(REQUEST_ATTRIBUTE_BUGTRACKER, devInf.getBugtrackerUrl());
        }

        ScrumProjectExt scrumExt = project.getExtension(ScrumProjectExt.class);
        if (scrumExt != null && StringUtils.isNotBlank(scrumExt.getBacklogUrl())) {
            request.setAttribute(REQUEST_ATTRIBUTE_SCRUMBACKLOG, scrumExt.getBacklogUrl());
        }

        // construct the Jira query parameters:
        String name = project.getName();
        String key = project.getOrConstructShortName().toUpperCase(Locale.ENGLISH);
        String descr = project.getDescription();
        if (StringUtils.isNotBlank(descr) && descr.length() > MAX_LEN_DESCRIPTION) {
            descr = WordUtils.abbreviate(descr, MAX_LEN_DESCRIPTION-10, MAX_LEN_DESCRIPTION, "..."); //$NON-NLS-1$
        }
        request.setAttribute(REQUEST_ATTRIBUTE_NAME, name);
        request.setAttribute(REQUEST_ATTRIBUTE_KEY, key);
        request.setAttribute(REQUEST_ATTRIBUTE_PROJECT_TYPE, DEFAULT_PROJECT_TYPE);
        request.setAttribute(REQUEST_ATTRIBUTE_BACK_URL, projectUrl);
        request.setAttribute(REQUEST_ATTRIBUTE_PROJECT_TYPE, DEFAULT_PROJECT_TYPE);
        request.setAttribute(REQUEST_ATTRIBUTE_ABBREVIATED_DESCIPTION, descr);
        StringBuilder jiraCreationUrl = new StringBuilder(JIRA_URL_CREATE);
        try {
            jiraCreationUrl.append("?name=").append(URLEncoder.encode(name, encoding)); //$NON-NLS-1$
            jiraCreationUrl.append("&amp;key=").append(URLEncoder.encode(key, encoding)); //$NON-NLS-1$
            jiraCreationUrl.append("&amp;url=").append(URLEncoder.encode(projectUrl, encoding)); //$NON-NLS-1$
            jiraCreationUrl.append("&amp;projectType=").append(DEFAULT_PROJECT_TYPE); //$NON-NLS-1$
            if (StringUtils.isNotBlank(descr)) {
                jiraCreationUrl.append("&amp;description=").append(URLEncoder.encode(descr, encoding)); //$NON-NLS-1$
            }
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
        request.setAttribute(REQUEST_ATTRIBUTE_JIRAURL, jiraCreationUrl.toString());

        // proceed along the chain
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }

}
