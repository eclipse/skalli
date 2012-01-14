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

import java.util.Collection;
import java.util.List;

import org.eclipse.skalli.model.Project;

/**
 * Service that provides access to the search engine.
 *
 * <p>
 * This interface exposes methods specific to the {@link Project} entity.
 * </p>
 */
public interface SearchService {

    /**
     * Updates the search index entry for the given {@link Project}.
     *
     * <p>
     * The {@link Project} does not necessarily need to be known to the index already.
     * </p>
     * <p>
     * A {@link Project} marked as deleted is removed from the index,
     * hence will not be included in search results anymore.
     * </p>
     *
     * @param project
     */
    public void update(Project project);

    /**
     * Updates the search index entries for the given {@link Project}s.
     *
     * <p>
     * The {@link Project}s do not necessarily need to be known to the index already.
     * </p>
     * <p>
     * {@link Project}s marked as deleted are removed from the index,
     * hence will not be included in search results anymore.
     * </p>
     *
     * @param project
     */
    public void update(Collection<Project> projects);

    /**
     * Searches for {@link Project}s using the given queryString.
     *
     * <p>
     * All major fields (e.g. Project Name, Description, Members, Tags,...) will be used to
     * determine the search result and the ranking of each {@link Project}.
     * </p>
     *
     * @param queryString
     * @param pagingInfo (if required, null allowed)
     * @return the {@link SearchResult}. It be empty but never returns null.
     */
    public SearchResult<Project> findProjectsByQuery(String queryString, PagingInfo pagingInfo)
            throws QueryParseException;

    /**
     * Searches for {@link Project}s that the given user is member of.
     *
     * @param queryString
     * @param pagingInfo (if required, null allowed)
     * @return the {@link SearchResult}. It be empty but never returns null.
     */
    public SearchResult<Project> findProjectsByUser(String queryString, PagingInfo pagingInfo)
            throws QueryParseException;

    /**
     * Searches for {@link Project}s that have the given tag.
     *
     * @param queryString
     * @param pagingInfo (if required, null allowed)
     * @return the {@link SearchResult}. It be empty but never returns null.
     */
    public SearchResult<Project> findProjectsByTag(String queryString, PagingInfo pagingInfo)
            throws QueryParseException;

    /**
     * Rebuilds the index with the given {@link Project}s.
     *
     * <p>
     * All existing index entries will be dropped.
     * </p>
     *
     * @param projects
     */
    public void reindex(Collection<Project> projects);

    /**
     * Uses a fuzzy search to find {@link Project}s that have similarities to the given {@link Project} ("More like this").
     *
     * <p>
     * The content of fields like Project Name, Description and Tags is used to infer the relation of {@link Project}s.
     * </p>
     *
     * @param project
     * @param count
     * @return the {@link SearchResult}. It be empty but never returns null.
     */
    public SearchResult<Project> getRelatedProjects(Project project, int count);

    /**
     * Converts a list of projects into a list of search hits.
     * @param projects  the projects to convert.
     * @return the list of search hits, or an empty list.
     */
    public List<SearchHit<Project>> asSearchHits(List<Project> projects);
}
