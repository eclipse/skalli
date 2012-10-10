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
package org.eclipse.skalli.core.internal.search;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.skalli.commons.ThreadPool;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.Taggable;
import org.eclipse.skalli.services.extension.ExtensionService;
import org.eclipse.skalli.services.extension.ExtensionServices;
import org.eclipse.skalli.services.extension.Indexer;
import org.eclipse.skalli.services.project.ProjectService;
import org.eclipse.skalli.services.search.PagingInfo;
import org.eclipse.skalli.services.search.QueryParseException;
import org.eclipse.skalli.services.search.SearchHit;
import org.eclipse.skalli.services.search.SearchResult;
import org.eclipse.skalli.services.search.SearchService;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchServiceImpl implements SearchService {

    private static final Logger LOG = LoggerFactory.getLogger(SearchServiceImpl.class);
    private LuceneIndex<Project> luceneIndex;

    protected void activate(ComponentContext context) {
        LOG.info(MessageFormat.format("[SearchService] {0} : activated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void deactivate(ComponentContext context) {
        LOG.info(MessageFormat.format("[SearchService] {0} : deactivated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void bindProjectService(ProjectService projectService) {
        LOG.info(MessageFormat.format("bindProjectService({0})", projectService)); //$NON-NLS-1$
        luceneIndex = new LuceneIndex<Project>(projectService);

        // perform re-indexing asynchronously: do not block service binding!
        ThreadPool.submit(new Runnable() {
            @Override
            public void run() {
                reindexAll();
            }
        });
    }

    protected void unbindProjectService(ProjectService projectService) {
        LOG.info(MessageFormat.format("unbindProjectService({0})", projectService)); //$NON-NLS-1$
        luceneIndex = null;
    }

    @Override
    public void reindex(Collection<Project> projects) {
        if (projects == null || projects.isEmpty()) {
            return;
        }
        try {
            long time = System.currentTimeMillis();
            LOG.info(MessageFormat.format("Re-indexing {0} projects...", projects.size()));
            luceneIndex.reindex(projects);
            LOG.info(MessageFormat.format("Re-indexing {0} projects finished in {1} milliseconds",
                    projects.size(), Long.toString(System.currentTimeMillis() - time)));
        } catch (RuntimeException e) {
            LOG.warn("Failed to re-index projects", e);
        }
    }

    @Override
    public void reindexAll() {
        try {
            long time = System.currentTimeMillis();
            LOG.info("Re-indexing all projects...");
            luceneIndex.reindexAll();
            LOG.info(MessageFormat.format("Re-indexing all projects finished in {0} milliseconds",
                    Long.toString(System.currentTimeMillis() - time)));
        } catch (RuntimeException e) {
            LOG.warn("Failed to re-index all projects", e);
        }
    }

    @Override
    public void update(final Project project) {
        update(Collections.singleton(project));
    }

    @Override
    public void update(Collection<Project> projects) {
        luceneIndex.update(projects);
    }

    @Override
    public SearchResult<Project> findProjectsByQuery(String queryString, PagingInfo pagingInfo)
            throws QueryParseException {
        Set<String> fieldSet = new HashSet<String>();
        for (ExtensionService<?> ext : ExtensionServices.getAll()) {
            Indexer<?> indexer = ext.getIndexer();
            if (indexer != null) {
                Set<String> fields = indexer.getDefaultSearchFields();
                if (fields != null) {
                    fieldSet.addAll(fields);
                }
            }
        }
        return luceneIndex.search(fieldSet.toArray(new String[fieldSet.size()]), queryString, pagingInfo);
    }

    @Override
    public SearchResult<Project> findProjectsByUser(String userId, PagingInfo pagingInfo)
            throws QueryParseException {
        String[] fields = new String[] { "allMembers" }; //$NON-NLS-1$
        return luceneIndex.search(fields, userId, pagingInfo);
    }

    @Override
    public SearchResult<Project> findProjectsByTag(String tag, PagingInfo pagingInfo)
            throws QueryParseException {
        String[] fields = new String[] { Taggable.PROPERTY_TAGS };
        return luceneIndex.search(fields, tag, pagingInfo);
    }

    @Override
    public SearchResult<Project> getRelatedProjects(Project project, int count) {
        String[] fields = new String[] { Project.PROPERTY_NAME, Project.PROPERTY_DESCRIPTION, Taggable.PROPERTY_TAGS };
        return luceneIndex.moreLikeThis(project, fields, count);
    }

    @Override
    public List<SearchHit<Project>> asSearchHits(List<Project> projects) {
        return luceneIndex.entitiesToHit(projects);
    }

}
