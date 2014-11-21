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
package org.eclipse.skalli.services.search;

import java.util.Collection;
import java.util.List;

import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.services.extension.Indexer;

/**
 * Service that provides access to the search and indexing capabilities.
 * <p>
 * The default implementation of this service is based on
 * <a href="http://lucene.apache.org/core/">Lucene</a>.
 */
public interface SearchService {

    /**
     * Updates the search index entry for the given {@link Project project},
     * or adds a new index entry, if the project has not been indexed before.
     * <p>
     * A project marked as {@link Project#isDeleted() deleted} is removed from
     * the index, hence will not show up in search results anymore.
     *
     * @param project  the project to index.
     */
    public void update(Project project);

    /**
     * Updates the search index entries for the given collection of
     * {@link Project projects}, or add new index entries for projects
     * that have not previously been indexed.
     * <p>
     * Projects marked as {@link Project#isDeleted() deleted} are removed from
     * the index, hence will not show up in search results anymore.
     *
     * @param projects  the projects to index.
     */
    public void update(Collection<Project> projects);

    /**
     * Searches for {@link Project projects} based on a given query string.
     * <p>
     * All {@link Indexer#getDefaultSearchFields() default search fields} (e.g. project name,
     * description, members, tags,...) of the projects and their extensions will be used to
     * determine the search result and the ranking of the individual search hits.
     *
     * @param queryString  the query string to search for.
     * @param pagingInfo optional start and count parameters to filter the result, or <code>null</code>.
     *
     * @return the {@link SearchResult search result}, which is basically a list of
     * {@link SearchHit search hits}.
     */
    public SearchResult<Project> findProjectsByQuery(String queryString, PagingInfo pagingInfo)
            throws QueryParseException;

    /**
     * Searches for {@link Project projects} of which the given user is member of.
     *
     * @param userId  the unique identifier of the user to search for.
     * @param pagingInfo optional start and count parameters to filter the result, or <code>null</code>.
     *
     * @return the {@link SearchResult search result}, which is basically a list of
     * {@link SearchHit search hits}.
     */
    public SearchResult<Project> findProjectsByUser(String userId, PagingInfo pagingInfo)
            throws QueryParseException;

    /**
     * Searches for {@link Project projects} with a given tag
     *
     * @param tag  the tag so search for.
     * @param pagingInfo optional start and count parameters to filter the result, or <code>null</code>.
     *
     * @return the {@link SearchResult search result}, which is basically a list of
     * {@link SearchHit search hits}.
     */
    public SearchResult<Project> findProjectsByTag(String tag, PagingInfo pagingInfo)
            throws QueryParseException;

    /**
     * Rebuilds the index for the given collection of {@link Project projects} from scratch.
     * All previously existing index entries for these projects will be dropped.
     *
     * @param projects  the projects to re-index.
     */
    public void reindex(Collection<Project> projects);

    /**
     * Rebuilds the index from scratch.
     */
    public void reindexAll();

    /**
     * Uses a fuzzy search to find {@link Project projects} that have similarities to the
     * given projects ("More Like This" search).
     * <p>
     * All {@link Indexer#getDefaultSearchFields() default search fields} (e.g. project name,
     * description, members, tags,...) of the projects and their extensions will be used to
     * determine the search result and the ranking of the individual search hits.
     *
     * @param project  the project for which to find related projects.
     * @param count maximum number of results to return.
     *
     * @return the {@link SearchResult search result}, which is basically a list of
     * {@link SearchHit search hits}.
     */
    public SearchResult<Project> getRelatedProjects(Project project, int count);

    /**
     * Converts a given list of projects into a list of search hits.
     *
     * @param projects  the projects to convert.
     * @return the list of search hits, or an empty list.
     */
    public List<SearchHit<Project>> asSearchHits(Collection<Project> projects);
}
