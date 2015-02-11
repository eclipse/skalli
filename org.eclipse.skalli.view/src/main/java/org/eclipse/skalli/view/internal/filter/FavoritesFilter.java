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
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.entity.EntityServices;
import org.eclipse.skalli.services.project.ProjectService;
import org.eclipse.skalli.services.search.SearchHit;
import org.eclipse.skalli.services.search.SearchResult;
import org.eclipse.skalli.services.search.SearchService;

public class FavoritesFilter extends AbstractSearchFilter {

    @Override
    protected boolean showNearestProjects(User user, ServletRequest request, ServletResponse response) {
        return false;
    }

    @Override
    protected SearchResult<Project> getSearchHits(User user, ServletRequest request, ServletResponse response,
            int start, int count)
            throws IOException, ServletException {
        int resultCount = 0;
        List<Project> projects = new ArrayList<Project>();
        if (user != null) {
            ProjectService projectService = ((ProjectService)EntityServices.getByEntityClass(Project.class));
            List<UUID> uuids = getFavorites(user).getProjects();
            resultCount = uuids.size();
            if (start < uuids.size()) {
                int end = Math.min(start + count, uuids.size());
                for (int i = start; i < end; ++i) {
                    Project project = projectService.getByUUID(uuids.get(i));
                    if (project != null) {
                        projects.add(project);
                    }
                }
            }
        }
        SearchService searchService = Services.getRequiredService(SearchService.class);
        List<SearchHit<Project>> searchHits = searchService.asSearchHits(projects);
        SearchResult<Project> searchResult = new SearchResult<Project>();
        searchResult.setResult(searchHits);
        searchResult.setResultCount(resultCount);
        searchResult.setPagingInfo(start, count);
        searchResult.setDuration(0);
        return searchResult;
    }

    @Override
    protected String getTitle(User user) {
        return user != null ? MessageFormat.format("Favorites for {0}", user.getDisplayName()) : "Favorites";
    }

}
