/*******************************************************************************
 * Copyright (c) 2010 - 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.api.java.feeds;

import java.util.List;

import org.eclipse.skalli.model.core.Project;

/**
 * Interface for a service that provides feeds for timeline information ("news")
 * associated with a certain project. For example, an implementation could provide
 * issues created in a bugtracking system, while another monitors mailing lists
 * of the project.
 * <p>
 * Note: The {@link FeedManager feed manager service} detects feed provider implementations
 * by searching the OSGi service registry.
 */
public interface FeedProvider {

    /**
     * Returns a list of feed updaters that can be used to retrieve
     * news about the given project.
     *
     * Each feed Update should give a different kind of timeline-information.
     * Examples might be a GitWeb or mailingList feed update.
     *
     * @param project  the project for which to provide feed updaters.
     * @return  a list of feed updaters, or an empty list.
     */
    public List<FeedUpdater> getFeedUpdaters(Project project);

}
