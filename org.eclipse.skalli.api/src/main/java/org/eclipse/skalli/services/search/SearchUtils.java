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
package org.eclipse.skalli.services.search;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.services.Services;

public class SearchUtils {

    public static SearchResult<Project> searchProjects(SearchQuery searchQuery)
            throws QueryParseException {

        SearchService searchService = Services.getService(SearchService.class);
        SearchResult<Project> result = null;

        PagingInfo pagingInfo = searchQuery.getPagingInfo();
        if (StringUtils.isNotBlank(searchQuery.getQuery())) {
            result = searchService.findProjectsByQuery(searchQuery.getQuery(), pagingInfo);
        } else if (StringUtils.isNotBlank(searchQuery.getTag())) {
            result = searchService.findProjectsByTag(searchQuery.getTag(), pagingInfo);
        } else if (StringUtils.isNotBlank(searchQuery.getUser())) {
            result = searchService.findProjectsByUser(searchQuery.getUser(), pagingInfo);
        } else {
            result = searchService.findProjectsByQuery("*", pagingInfo); //$NON-NLS-1$
        }

        return result;
    }

}
