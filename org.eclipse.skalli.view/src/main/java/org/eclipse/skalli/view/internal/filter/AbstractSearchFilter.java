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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.commons.Link;
import org.eclipse.skalli.ext.mapping.scm.ScmLocationMapper;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.model.ext.devinf.DevInfProjectExt;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.entity.EntityServices;
import org.eclipse.skalli.services.favorites.Favorites;
import org.eclipse.skalli.services.favorites.FavoritesService;
import org.eclipse.skalli.services.project.ProjectService;
import org.eclipse.skalli.services.search.SearchHit;
import org.eclipse.skalli.services.search.SearchResult;
import org.eclipse.skalli.services.search.SearchService;
import org.eclipse.skalli.services.template.ProjectTemplate;
import org.eclipse.skalli.services.template.ProjectTemplateService;
import org.eclipse.skalli.view.Consts;
import org.eclipse.skalli.view.LoginUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSearchFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractSearchFilter.class);

    public static final String ATTRIBUTE_RESULTSIZE = "resultSize"; //$NON-NLS-1$
    public static final String ATTRIBUTE_DURATION = "duration"; //$NON-NLS-1$
    public static final String ATTRIBUTE_VIEWSIZE = "viewSize"; //$NON-NLS-1$
    public static final String ATTRIBUTE_TITLE = "title"; //$NON-NLS-1$
    public static final String ATTRIBUTE_PROJECTS = "projects"; //$NON-NLS-1$
    public static final String ATTRIBUTE_NATURES = "natures"; //$NON-NLS-1$
    public static final String ATTRIBUTE_PARENTS = "parents"; //$NON-NLS-1$
    public static final String ATTRIBUTE_PARENTCHAINS = "parentChains"; //$NON-NLS-1$
    public static final String ATTRIBUTE_SUBPROJETS = "subprojects"; //$NON-NLS-1$
    public static final String ATTRIBUTE_SOURCELINKS = "sourceLinks"; //$NON-NLS-1$
    public static final String ATTRIBUTE_START = "start"; //$NON-NLS-1$
    public static final String ATTRIBUTE_CURRENTPAGE = "currentPage"; //$NON-NLS-1$
    public static final String ATTRIBUTE_PAGES = "pages"; //$NON-NLS-1$

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {

        // retrieve the logged-in user
        String userId = (String) request.getAttribute(Consts.ATTRIBUTE_USERID);
        User user = (User) request.getAttribute(Consts.ATTRIBUTE_USER);

        // calculate start param
        int start = toInt(request.getParameter(Consts.PARAM_START), 0, -1);
        // calculate count size param
        int count = toInt(request.getParameter(Consts.PARAM_COUNT), 10, 50);

        // retrieve search hits and based on that parent projects and subprojects
        SearchResult<Project> searchResult = getSearchHits(user, request, response, start, count);
        List<SearchHit<Project>> searchHits = searchResult.getResult();
        Map<String, String> natures = getProjectNatures(searchHits);
        Map<String, Project> parents = getParents(searchHits);
        Map<String, List<Project>> parentChains = getParentChains(searchHits);
        Map<String, List<SearchHit<Project>>> subprojects = getSubprojects(searchHits);
        Map<String, List<String>> sourceLinks = getSourceLinks(userId, searchHits);

        // retrieve the favorites of the user
        Favorites favorites = getFavorites(user);

        // calculate params for pager
        int resultSize = searchResult.getResultCount();
        int pages = (int) Math.ceil((double) resultSize / (double) count);
        int currentPage = (int) Math.floor((double) start / (double) count) + 1;
        long duration = searchResult.getDuration();

        // set the request attributes
        request.setAttribute(ATTRIBUTE_TITLE, getTitle(user));
        request.setAttribute(ATTRIBUTE_PROJECTS, searchHits);
        request.setAttribute(ATTRIBUTE_NATURES, natures);
        request.setAttribute(ATTRIBUTE_PARENTS, parents);
        request.setAttribute(ATTRIBUTE_PARENTCHAINS, parentChains);
        request.setAttribute(ATTRIBUTE_SUBPROJETS, subprojects);
        request.setAttribute(ATTRIBUTE_SOURCELINKS, sourceLinks);
        request.setAttribute(Consts.ATTRIBUTE_FAVORITES, favorites.asMap());
        request.setAttribute(ATTRIBUTE_DURATION, duration);
        request.setAttribute(ATTRIBUTE_START, start);
        request.setAttribute(ATTRIBUTE_VIEWSIZE, count);
        request.setAttribute(ATTRIBUTE_RESULTSIZE, resultSize);
        request.setAttribute(ATTRIBUTE_CURRENTPAGE, currentPage);
        request.setAttribute(ATTRIBUTE_PAGES, pages);
        request.setAttribute(Consts.ATTRIBUTE_USER, user);

        if (((HttpServletRequest) request).getPathInfo() == null) {
            request.getRequestDispatcher(Consts.JSP_SEARCHRESULT).forward(request, response);
            return;
        }

        // proceed along the chain
        chain.doFilter(request, response);
    }

    /**
     * Returns the next set of search hits with size <code>viewSize</code> beginning
     * with the entry whose index is <code>start</code>.
     *
     * @param user
     *            the logged-in user, or <code>null</code> for an anonymous user.
     * @param request
     *            the servlet request.
     * @param response
     *            the  servlet response (can be used for error handling).
     * @param start
     *            the index of the first search hit to return.
     * @param count
     *            the number of search hits to return.
     */
    protected abstract SearchResult<Project> getSearchHits(User user, ServletRequest request, ServletResponse response,
            int start, int count) throws IOException, ServletException;

    /**
     * Returns <code>true</code> if for components the nearest project in
     * the hierarchy should be shown.
     *
     * @param user
     *            the logged-in user, or <code>null</code> for an anonymous user.
     * @param request
     *            the servlet request.
     * @param response
     *            the  servlet response (can be used for error handling).
     */
    protected abstract boolean showNearestProjects(User user, ServletRequest request, ServletResponse response);

    /**
     * Returns the title of the search result window.
     * This method always returns "Search Result". Derived filters should overwrite this method.
     *
     * @param user
     *          the logged-in user, or <code>null</code> for an anonymous user.
     */
    protected String getTitle(User user) {
        return "Search Result";
    }

    protected ProjectService getProjectService() {
        return ((ProjectService)EntityServices.getByEntityClass(Project.class));
    }

    protected ProjectTemplateService getProjectTemplateService() {
        return Services.getRequiredService(ProjectTemplateService.class);
    }

    protected SearchService getSearchService() {
        return Services.getRequiredService(SearchService.class);
    }

    protected FavoritesService getFavoritesService() {
        return Services.getService(FavoritesService.class);
    }

    protected User getUser(HttpServletRequest request) {
        LoginUtils util = new LoginUtils(request);
        return util.getLoggedInUser();
    }

    /**
     * Returns the favorites of the given user. For anonymous users
     * the method returns {@link Favorites#Favorites()}.
     *
     * @param user
     *          the logged-in user, or <code>null</code> for an anonymous user.
     */
    protected Favorites getFavorites(User user) {
        if (user == null) {
            return new Favorites();
        }
        FavoritesService favoritesService = getFavoritesService();
        if (favoritesService == null) {
            return new Favorites(user.getUserId());
        }
        return favoritesService.getFavorites(user.getUserId());
    }

    /**
     * Returns a map of the natures of the given project search hits.
     * Key: UUID.toString() of a project in searchHits; Value: ProjectNature.toString() of that project.
     */
    protected Map<String, String> getProjectNatures(List<SearchHit<Project>> searchHits) {
        ProjectTemplateService templateService = getProjectTemplateService();
        Map<String, String> natures = new HashMap<String, String>();
        for (SearchHit<Project> searchHit : searchHits) {
            Project project = searchHit.getEntity();
            if (project != null) {
                String uuid = project.getUuid().toString();
                ProjectTemplate template = templateService.getProjectTemplateById(project.getProjectTemplateId());
                if (template != null) {
                    natures.put(uuid, template.getProjectNature().toString());
                }
            }
        }
        return natures;
    }

    protected Map<String, List<String>> getSourceLinks(String userId, List<SearchHit<Project>> searchHits) {
        Map<String, List<String>> links = new HashMap<String, List<String>>();
        for (SearchHit<Project> searchHit : searchHits) {
            Project project = searchHit.getEntity();
            if (project != null) {
                String uuid = project.getUuid().toString();
                DevInfProjectExt devInf = project.getExtension(DevInfProjectExt.class);
                if (devInf != null && CollectionUtils.isNotBlank(devInf.getScmLocations())) {
                    ScmLocationMapper mapper = new ScmLocationMapper(ScmLocationMapper.ALL_PROVIDERS,
                            ScmLocationMapper.PURPOSE_BROWSE);
                    for (String scmLocation : devInf.getScmLocations()) {
                        List<String> scmUrls = new ArrayList<String>();
                        List<Link> mappedLinks = mapper.getMappedLinks(scmLocation, userId, project);
                        for (Link link: mappedLinks) {
                            scmUrls.add(link.getUrl());
                        }
                        if (scmUrls.isEmpty()) {
                            String scmUrl = searchHit.getSingleValues().get("scmUrl"); //$NON-NLS-1$
                            if (scmUrl != null) {
                                scmUrls.add(scmUrl);
                            }
                        }
                        links.put(uuid, scmUrls);
                    }
                }
            }
        }
        return links;
    }

    /**
     * Returns a map with the direct parent projects of the given search hits.
     * Key: UUID.toString() of a project in searchHits; Value: SearchHit of that project.
     */
    protected Map<String, Project> getParents(List<SearchHit<Project>> searchHits) {
        ProjectService projectService = getProjectService();
        Map<String, Project> parents = new HashMap<String, Project>();
        for (SearchHit<Project> searchHit : searchHits) {
            Project project = searchHit.getEntity();
            String uuid = project.getUuid().toString();
            Project parent = projectService.getByUUID(project.getParentEntityId());
            if (parent != null) {
                parents.put(uuid, parent);
            }
        }
        return parents;
    }

    protected Map<String, List<Project>> getParentChains(List<SearchHit<Project>> searchHits) {
        ProjectService projectService = getProjectService();
        Map<String, List<Project>> ret = new HashMap<String, List<Project>>();
        for (SearchHit<Project> searchHit : searchHits) {
            Project project = searchHit.getEntity();
            List<Project> parentChain = projectService.getParentChain(project.getUuid());
            if (parentChain.size() > 0) {
                parentChain.remove(0);
            }
            Collections.reverse(parentChain);
            ret.put(project.getUuid().toString(), parentChain);
        }
        return ret;
    }

    /**
     * Returns a map with the subprojects of the given search hits.
     * Key: UUID.toString() of a project in searchHits; Value: list of SearchHits of subprojects of that project.
     */
    protected Map<String, List<SearchHit<Project>>> getSubprojects(List<SearchHit<Project>> searchHits) {
        SearchService searchService = getSearchService();
        Map<String, List<SearchHit<Project>>> subrojects = new HashMap<String, List<SearchHit<Project>>>();
        for (SearchHit<Project> searchHit : searchHits) {
            Project project = searchHit.getEntity();
            UUID uuid = project.getUuid();
            SortedSet<Project> subprojects = project.getSubProjects();
            if (subprojects.size() > 0) {
                subrojects.put(uuid.toString(), searchService.asSearchHits(subprojects));
            }
        }
        return subrojects;
    }

    /**
     * Tries to convert the request parameter into an <code>int</code>.
     *
     * If unsuccessful or above the limit the default value will be returned.
     */
    private int toInt(String requestParameter, int defaultValue, int limit) {
        if (StringUtils.isBlank(requestParameter)) {
            return defaultValue;
        }
        try {
            int value = Integer.parseInt(requestParameter);
            return (limit > -1 && value > limit) ? defaultValue : value;
        } catch (NumberFormatException e) {
            LOG.info(MessageFormat.format("''{0}'' is not a valid number, using default: {1}", requestParameter,
                    defaultValue));
            return defaultValue;
        }
    }

    @Override
    public void init(FilterConfig paramFilterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}
