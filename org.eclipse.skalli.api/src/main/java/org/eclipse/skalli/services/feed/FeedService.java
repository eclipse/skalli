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
package org.eclipse.skalli.services.feed;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Interface for a service that provides a timeline for a certain project, such as commits in a
 * source code repository, mails received on a mailing list, changes to a project Wiki, or changes
 * done to the project in Skalli itself.
 */
public interface FeedService {

    /**
     * Constant indicating that all timeline entries matching certain filter criteria
     * should be returned instead of only a limited number.
     */
    final int SELECT_ALL = -1;

    /**
     * Returns up to <code>maxResult</code> timeline entries for the given project.
     *
     * @param projectId  the unique identifier of the project.
     * @param maxResults the maximal number of entries to be returned
     * or {@link FeedService#SELECT_ALL} if all available entries should be returned.
     *
     * @return a list of timeline entries sorted by descending
     * {@link Entry#getPublished() publishing date}, or an empty list.
     *
     * @throws IOException if an i/o error occured when retrieving timeline entries.
     */
    public List<Entry> findEntries(UUID projectId, int maxResults) throws IOException;

    /**
     * Returns up to <code>maxResult</code> timeline entries for a project coming
     * from a given set of {@link #findSources(UUID) sources}.
     *
     * @param projectId the unique identifier of the project.
     * @param sources a collection of sources from which timeline entries should
     * be provided, or <code>null</code> if entries from all available sources should
     * be returned. If the sources collection is empty, an empty entry list will be
     * returned.
     * @param maxResults the maximal number of entries to be returned
     * or {@link FeedService#SELECT_ALL} if all available entries should be returned.
     *
     * @return a list of timeline entries ordered by descending
     * {@link Entry#getPublished() publishing date}, or an empty list.
     *
     * @throws IOException if an i/o error occured when retrieving timeline entries.
     */
    public List<Entry> findEntries(UUID projectId, Collection<String> sources, int maxResults) throws IOException;

    /**
     * Returns a list of sources of timeline entries this service can provide for a given project.
     *
     * Sources for timeline entries could for example be an Atom/RSS feed of a Wiki, a mailing list or Skalli
     * itself. Each source has an unique identifier such as "gitweb" or "skalli".
     *
     * Note that this list is not static since additional {@link FeedProvider feed providers} can
     * be added or removed at any time from a Skalli instance.
     *
     * @param projectId the unique identifier of the project.
     *
     * @return a list of source identifiers sorted alphanumerically, or an empty list.
     *
     * @throws IOException if an i/o error occured when listing sources.
     */
    public List<String> findSources(UUID projectId) throws IOException;
}
