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
package org.eclipse.skalli.services.feed;

import java.util.UUID;

/**
 * Services that manages and updates feeds for projects.
 */
public interface FeedManager {

    /**
     * Updates the feeds for a given project.
     * @param project  the unique identifier of the project, for which the feeds should be updated.
     */
    public void updateFeeds(UUID projectId);

    /**
     * Updates the feeds of all currently existing projects.
     */
    public void updateAllFeeds();
}
