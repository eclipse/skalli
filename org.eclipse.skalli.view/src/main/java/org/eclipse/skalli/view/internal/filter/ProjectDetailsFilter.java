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
import java.net.URI;
import java.text.MessageFormat;
import java.util.Comparator;
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
import org.eclipse.skalli.api.java.FavoritesService;
import org.eclipse.skalli.api.java.IssuesService;
import org.eclipse.skalli.api.java.ProjectTemplateService;
import org.eclipse.skalli.api.java.ValidationService;
import org.eclipse.skalli.api.java.authentication.UserUtil;
import org.eclipse.skalli.common.Consts;
import org.eclipse.skalli.common.ServiceFilter;
import org.eclipse.skalli.common.Services;
import org.eclipse.skalli.model.core.Favorites;
import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.model.core.ProjectTemplate;
import org.eclipse.skalli.model.ext.Issues;
import org.eclipse.skalli.model.ext.Severity;
import org.eclipse.skalli.view.ext.ProjectContextLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter sets the following attributes if the request attribute "{@value Consts#ATTRIBUTE_PROJECT}" is defined:
 * <ul>
 * <li>"{@value Consts#ATTRIBUTE_PROJECTTEMPLATE} - the {@link ProjectTemplate} assigned to the project.</li>
 * </ul>
 * This filter sets the following additional attributes if the request attributes "{@value Consts#ATTRIBUTE_PROJECT}"
 * and "{@value Consts#ATTRIBUTE_USERID}" are defined:
 * <ul>
 * <li>"{@value Consts#ATTRIBUTE_FAVORITES} - the favorites of the logged in user.</li>
 * <li>"{@value Consts#ATTRIBUTE_PROJECTADMIN} - <code>true</code>, if the logged in user is project administrator,
 * <code>false</code> otherwise.</li>
 * <li>"{@value Consts#ATTRIBUTE_SHOW_ISSUES} - code>true</code>, if the logged in user is allows to see the issues
 * of the project, <code>false</code> otherwise. A user must be administrator of the project or administrator of
 * any project ion the project's parent hierarchy to see issues.</li>
 * </li>
 * <li>"{@value Consts#ATTRIBUTE_ISSUES} - the {@link Issues} of the project.</li>
 * <li>"{@value Consts#ATTRIBUTE_MAX_SEVERITY} - the maximum severity found in the project issues.</li>
 * <li>"{@value Consts#ATTRIBUTE_PROJECTCONTEXTLINKS} - ????</li>
 * </ul>
 */
public class ProjectDetailsFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectDetailsFilter.class);

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        final Project project = (Project) request.getAttribute(Consts.ATTRIBUTE_PROJECT);
        final String userId = (String) request.getAttribute(Consts.ATTRIBUTE_USERID);

        if (project != null) {
            ProjectTemplateService templateService = Services.getRequiredService(ProjectTemplateService.class);
            ProjectTemplate projectTemplate = templateService.getProjectTemplateById(project.getProjectTemplateId());
            request.setAttribute(Consts.ATTRIBUTE_PROJECTTEMPLATE, projectTemplate);

            if (userId != null) {
                FavoritesService favoritesService = Services.getService(FavoritesService.class);
                Favorites favorites = null;
                if (favoritesService == null) {
                    favorites = new Favorites(userId);
                } else {
                    favorites = favoritesService.getFavorites(userId);
                }
                request.setAttribute(Consts.ATTRIBUTE_FAVORITES, favorites.asMap());

                boolean isProjectAdmin = UserUtil.isAdministrator(userId) || UserUtil.isProjectAdmin(userId, project);
                request.setAttribute(Consts.ATTRIBUTE_PROJECTADMIN, isProjectAdmin);

                boolean showIssues = isProjectAdmin || UserUtil.isProjectAdminInParentChain(userId, project);
                request.setAttribute(Consts.ATTRIBUTE_SHOW_ISSUES, isProjectAdmin);

                IssuesService issuesService = Services.getService(IssuesService.class);
                if (issuesService != null && showIssues) {
                    String action = request.getParameter(Consts.PARAM_ACTION);
                    if (action != null && action.equals(Consts.PARAM_VALUE_VALIDATE)) {
                        ValidationService validationService = Services.getService(ValidationService.class);
                        if (validationService != null) {
                            validationService.validate(Project.class, project.getUuid(), Severity.INFO, userId);
                        }
                    }
                    Issues issues = issuesService.getByUUID(project.getUuid());
                    if (issues != null && issues.hasIssues()) {
                        request.setAttribute(Consts.ATTRIBUTE_ISSUES, issues);
                        request.setAttribute(Consts.ATTRIBUTE_MAX_SEVERITY, issues.getIssues().first().getSeverity().name());
                    }
                }

                request.setAttribute(Consts.ATTRIBUTE_PROJECTCONTEXTLINKS,
                        getOrderedVisibleProjectContextLinks(project, userId));
            }
        } else {
            request.setAttribute(Consts.ATTRIBUTE_WINDOWNAME, httpRequest.getPathInfo());
            // do nothing else as we have to support creation of projects and search urls, too
        }

        // proceed along the chain
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }

    /**
     * returns a sorted set of links in context of the current project.
     */
    private Set<Link> getOrderedVisibleProjectContextLinks(Project project, final String userId) {
        final Project p = project;
        Set<ProjectContextLink> set = Services.getServices(ProjectContextLink.class,
                new ServiceFilter<ProjectContextLink>() {
                    @Override
                    public boolean accept(ProjectContextLink contextLink) {
                        return contextLink.isVisible(p, userId);
                    }
                });

        Set<Link> result = new TreeSet<Link>(new Comparator<Link>() {
            @Override
            public int compare(Link l1, Link l2) {
                if (l1.getPositionWeight() != l2.getPositionWeight()) {
                    return new Float(l1.getPositionWeight()).compareTo(l2.getPositionWeight());
                } else {
                    // in case the position weight is equal, compare by link caption
                    // to prevent that one of both links is sorted out of the result set
                    return (l1.getCaption().compareTo(l2.getCaption()));
                }
            }
        });

        for (ProjectContextLink contextLink : set) {
            if (StringUtils.isBlank(contextLink.getCaption(project))) {
                LOG.warn(MessageFormat
                        .format(
                                "instance of {0} returned null or blank when calling method getCaption(project) with projectId={1}",
                                contextLink.getClass(), project.getProjectId()));
            } else if (contextLink.getUri(project) == null) {
                LOG.warn(MessageFormat.format(
                        "instance of {0} returned null when calling method getUri(project) with projectId={1}",
                        contextLink.getClass(), project.getProjectId()));
            } else {
                Link link = new Link();
                // set the class name as id, this can be used for UI testing
                link.setId(contextLink.getClass().getName());
                link.setCaption(contextLink.getCaption(project));
                link.setUri(contextLink.getUri(project));
                link.setPositionWeight(contextLink.getPositionWeight());
                result.add(link);
            }
        }
        return result;
    }

    public class Link {
        private String caption;
        private URI uri;
        private float positionWeight;
        private String id;

        public String getCaption() {
            return caption;
        }

        public void setCaption(String caption) {
            this.caption = caption;
        }

        public URI getUri() {
            return uri;
        }

        public void setUri(URI uri) {
            this.uri = uri;
        }

        public float getPositionWeight() {
            return positionWeight;
        }

        public void setPositionWeight(float positionWeight) {
            this.positionWeight = positionWeight;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }

}
