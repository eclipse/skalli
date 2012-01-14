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

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.search.PagingInfo;
import org.eclipse.skalli.services.search.SearchResult;
import org.eclipse.skalli.services.search.SearchService;
import org.eclipse.skalli.view.Consts;

public class SearchFilter extends AbstractSearchFilter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, javax.servlet.FilterChain chain)
            throws IOException, ServletException {

        String view = request.getParameter(Consts.PARAM_VIEW);

        if (StringUtils.equals(view, Consts.PARAM_VALUE_VIEW_HIERARCHY)) {
            FilterUtil.forward(request, response, Consts.URL_HIERARCHY);
            return;
        }

        super.doFilter(request, response, chain);
    }

    @Override
    protected boolean showNearestProjects(User user, ServletRequest request, ServletResponse response) {
        String userquery = request.getParameter(Consts.PARAM_USER);
        return userquery == null || user == null || !userquery.equals(user.getUserId());
    }

    @Override
    protected SearchResult<Project> getSearchHits(User user, ServletRequest request, ServletResponse response,
            int start,
            int viewSize) throws IOException, ServletException {

        String query = request.getParameter(Consts.PARAM_QUERY);
        String tagquery = request.getParameter(Consts.PARAM_TAG);
        String userquery = request.getParameter(Consts.PARAM_USER);

        SearchService searchService = Services.getService(SearchService.class);
        SearchResult<Project> result = null;

        try {
            if (query != null) {
                result = searchService.findProjectsByQuery(query, new PagingInfo(start, viewSize));
            } else if (tagquery != null) {
                result = searchService.findProjectsByTag(tagquery, new PagingInfo(start, viewSize));
            } else if (userquery != null) {
                result = searchService.findProjectsByUser(userquery, new PagingInfo(start, viewSize));
            } else {
                result = searchService.findProjectsByQuery("*", new PagingInfo(start, viewSize)); //$NON-NLS-1$
            }
        } catch (Exception e) {
            FilterUtil.handleException(request, response, e);
        }

        request.setAttribute(Consts.ATTRIBUTE_QUERY, query);
        request.setAttribute(Consts.ATTRIBUTE_USERQUERY, userquery);
        request.setAttribute(Consts.ATTRIBUTE_TAGQUERY, tagquery);

        return result;
    }
}
