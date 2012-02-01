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

import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.services.Services;

public class SearchUtils {

    public static SearchResult<Project> searchProjects(String query, String tag, String user, PagingInfo pagingInfo)
            throws QueryParseException {

        SearchService searchService = Services.getService(SearchService.class);
        SearchResult<Project> result = null;

        if (query != null) {
            result = searchService.findProjectsByQuery(query, pagingInfo);
        } else if (tag != null) {
            result = searchService.findProjectsByTag(tag, pagingInfo);
        } else if (user != null) {
            result = searchService.findProjectsByUser(user, pagingInfo);
        } else {
            result = searchService.findProjectsByQuery("*", pagingInfo); //$NON-NLS-1$
        }

        return result;
    }

}
