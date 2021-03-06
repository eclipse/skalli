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

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.services.ServiceFilter;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.favorites.Favorites;
import org.eclipse.skalli.services.favorites.FavoritesService;
import org.eclipse.skalli.services.issues.Issues;
import org.eclipse.skalli.services.issues.IssuesService;
import org.eclipse.skalli.services.template.ProjectTemplate;
import org.eclipse.skalli.services.template.ProjectTemplateService;
import org.eclipse.skalli.services.validation.ValidationService;
import org.eclipse.skalli.view.Consts;
import org.eclipse.skalli.view.ext.InfoBox;
import org.eclipse.skalli.view.ext.ProjectContextLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter reads the following attributes:
 * <ul>
 * <li>"{@value Consts#ATTRIBUTE_USERID}" - the identifier of the logged in user; if not defined: anonymous user.</li>
 * <li>"{@value Consts#ATTRIBUTE_PROJECT}" - the project, or <code>null</code> in case the project is to be created.</li>
 * <li>"{@value Consts#ATTRIBUTE_PROJECTADMIN}" - <code>true</code>, if the logged in user is project administrator,
 * <code>false</code> otherwise.</li>
 * </ul>
 * This filter sets the following attributes:
 * <ul>
 * <li>"{@value Consts#ATTRIBUTE_WINDOWNAME}" - the name of the Vaadin window, either the project's id, or the
 * project's uuid for deleted projects. If the project is to be created, the path info from the request is set
 * as window name.</li>
 * <li>"{@value Consts#ATTRIBUTE_APP_URI}" - the <tt>appUri</tt> parameter passed to Vaadin,
 * i.e. <tt>/vprojects/&lt;windowName&gt;</tt>. In edit mode, the suffix <tt>/edit</tt> is appended.</li>
 * <li>"{@value Consts#ATTRIBUTE_EDITMODE}" - <code>true</code>, if the project is to be edited.</li>
 * </ul>
 * <p>
 * This filter sets the following additional attributes if the request attribute "{@value Consts#ATTRIBUTE_PROJECT}"
 * is defined:
 * <ul>
 * <li>"{@value Consts#ATTRIBUTE_PROJECTTEMPLATE}" - the {@link ProjectTemplate} assigned to the project.</li>
 * <li>"{@value Consts#ATTRIBUTE_NATURE}" - the nature of the project.</li>
 * </ul>
 * This filter sets the following additional attributes if the request attributes "{@value Consts#ATTRIBUTE_PROJECT}"
 * and "{@value Consts#ATTRIBUTE_USERID}" are defined:
 * <ul>
 * <li>"{@value Consts#ATTRIBUTE_FAVORITES}" - the favorites of the logged in user.</li>
 * <li>"{@value Consts#ATTRIBUTE_SHOW_ISSUES}" - <code>true</code>, if the logged in user is allowed to see the issues
 * of the project, <code>false</code> otherwise. A user must be administrator of the project or administrator of
 * any project in the project's parent hierarchy to see issues.</li>
 * <li>"{@value Consts#ATTRIBUTE_ISSUES}" - the {@link Issues issues} of the project.</li>
 * <li>"{@value Consts#ATTRIBUTE_MAX_SEVERITY}" - the maximum severity found in the project issues.</li>
 * <li>"{@value Consts#ATTRIBUTE_PROJECTCONTEXTLINKS}" - the context links to be display in the navigation bar.</li>
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

        String userId = (String) request.getAttribute(Consts.ATTRIBUTE_USERID);
        Project project = (Project) request.getAttribute(Consts.ATTRIBUTE_PROJECT);
        boolean isProjectAdmin = BooleanUtils.toBoolean((Boolean)request.getAttribute(Consts.ATTRIBUTE_PROJECTADMIN));
        boolean isProjectAdminInParentChain =
                BooleanUtils.toBoolean((Boolean)request.getAttribute(Consts.ATTRIBUTE_PARENTPROJECTADMIN));
        String action = request.getParameter(Consts.PARAM_ACTION);

        String windowName = StringUtils.strip(httpRequest.getPathInfo(), "/"); //$NON-NLS-1$
        if (project != null) {
            if (!project.isDeleted()) {
                windowName = project.getProjectId();
            } else {
                windowName = project.getUuid().toString();
            }

            ProjectTemplateService templateService = Services.getRequiredService(ProjectTemplateService.class);
            ProjectTemplate projectTemplate = templateService.getProjectTemplateById(project.getProjectTemplateId());
            request.setAttribute(Consts.ATTRIBUTE_PROJECTTEMPLATE, projectTemplate);
            request.setAttribute(Consts.ATTRIBUTE_NATURE, projectTemplate.getProjectNature().toString());

            if (userId != null) {
                FavoritesService favoritesService = Services.getService(FavoritesService.class);
                Favorites favorites = null;
                if (favoritesService == null) {
                    favorites = new Favorites(userId);
                } else {
                    favorites = favoritesService.getFavorites(userId);
                }
                request.setAttribute(Consts.ATTRIBUTE_FAVORITES, favorites.asMap());

                boolean showIssues = isProjectAdmin || isProjectAdminInParentChain;
                request.setAttribute(Consts.ATTRIBUTE_SHOW_ISSUES, isProjectAdmin);

                if (showIssues) {
                    IssuesService issuesService = Services.getService(IssuesService.class);
                    if (issuesService != null) {
                        if (Consts.PARAM_VALUE_VALIDATE.equals(action)) {
                            ValidationService validationService = Services.getService(ValidationService.class);
                            if (validationService != null) {
                                validationService.validate(Project.class, project.getUuid(), Severity.INFO, userId);
                            }
                        }
                        Issues issues = issuesService.getByUUID(project.getUuid());
                        if (issues != null && issues.hasIssues()) {
                            request.setAttribute(Consts.ATTRIBUTE_ISSUES, issues);
                            request.setAttribute(Consts.ATTRIBUTE_MAX_SEVERITY, issues.getIssues().first().getSeverity()
                                    .name());
                        }
                    }
                }

                request.setAttribute(Consts.ATTRIBUTE_PROJECTCONTEXTLINKS,
                        getOrderedVisibleProjectContextLinks(project, userId));
            }

            String pathInfo = httpRequest.getPathInfo();
            if (pathInfo != null) {
                int infoBoxIndex = pathInfo.indexOf(Consts.URL_INFOBOXES);
                if (infoBoxIndex > 0) {
                    if (StringUtils.isNotBlank(action)) {
                        String infoBoxShortName = pathInfo.substring(infoBoxIndex + Consts.URL_INFOBOXES.length());
                        if (infoBoxShortName.startsWith(FilterUtil.PATH_SEPARATOR)) {
                            infoBoxShortName = infoBoxShortName.substring(1);
                        }
                        filterInfobox(project, infoBoxShortName, action, userId);
                    }
                }
            }
        }
        request.setAttribute(Consts.ATTRIBUTE_WINDOWNAME, windowName);
        request.setAttribute(Consts.ATTRIBUTE_EDITMODE, false);

        String appUri = Consts.URL_VAADIN_PROJECTS + windowName;
        if (StringUtils.isNotBlank(action)) {
            if (action.equals(Consts.PARAM_VALUE_EDIT)) {
                request.setAttribute(Consts.ATTRIBUTE_EDITMODE, true);
                appUri = appUri + "/edit"; //$NON-NLS-1$
            }
        }
        request.setAttribute(Consts.ATTRIBUTE_APP_URI, appUri);

        // proceed along the chain
        chain.doFilter(request, response);
    }

    private void filterInfobox(final Project project, String infoBoxShortName, String action, final String userId) {
        final boolean performAllInfoBoxes = StringUtils.isBlank(infoBoxShortName) ? true : false;

        Set<InfoBox> infoboxes = Services.getServices(InfoBox.class,
                new ServiceFilter<InfoBox>() {
                    @Override
                    public boolean accept(InfoBox infoBox) {
                        return infoBox.isVisible(project, userId);
                    }
                });

        for (InfoBox infoBox : infoboxes) {
            if (performAllInfoBoxes || infoBoxShortName.equalsIgnoreCase(infoBox.getShortName())) {
                infoBox.perform(action, project, userId);
            }
        }
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
                LOG.warn(MessageFormat.format(
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
