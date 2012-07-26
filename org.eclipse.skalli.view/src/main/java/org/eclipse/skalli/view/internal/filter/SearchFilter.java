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
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.Statistics;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.services.search.QueryParseException;
import org.eclipse.skalli.services.search.SearchResult;
import org.eclipse.skalli.services.search.SearchUtils;
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
            int start, int count) throws IOException, ServletException {

        SearchQuery searchQuery = null;
        SearchResult<Project> result = null;
        try {
            searchQuery = new SearchQuery(request);
            searchQuery.setPagingInfo(start, count);
            result = SearchUtils.searchProjects(searchQuery);
        } catch (Exception e) {
            FilterUtil.handleException(request, response, e);
        }
        Statistics.getDefault().trackSearch(user.getUserId(), result.getQueryString(),
                result.getResultCount(), result.getDuration());

        request.setAttribute(Consts.ATTRIBUTE_QUERY, searchQuery.getQuery());
        request.setAttribute(Consts.ATTRIBUTE_USERQUERY, searchQuery.getUser());
        request.setAttribute(Consts.ATTRIBUTE_TAGQUERY, searchQuery.getTag());

        return result;
    }

    private static class SearchQuery extends org.eclipse.skalli.services.search.SearchQuery {
        public SearchQuery(ServletRequest request) throws QueryParseException {
            super(asMap(request));
        }

        private static Map<String, String> asMap(ServletRequest request) {
            HashMap<String, String> params = new HashMap<String, String>();
            for (String key: PARAMS) {
                params.put(key, request.getParameter(key));
            }
            return params;
        }
    }
}
