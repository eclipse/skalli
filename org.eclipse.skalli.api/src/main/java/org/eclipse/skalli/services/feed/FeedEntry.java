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

import java.util.Date;
import java.util.UUID;

/**
 * Interface representing an entry of a feed providing timeline information for a project.
 * This interface is used by providers of timeline information, such as {@link FeedUpdater
 * feed updaters), while {@link Entry}, from which this interface is derived, represents
 * the consumer's view on a project timeline.
 */
public interface FeedEntry extends Entry {

    /**
     * Defines the unique identifier of the feed entry.
     * Note that a {@link FeedManager} implementation may override or amend an identifier
     * provided by a {@link FeedUpdater feed updater} to ensure its uniqueness.
     */
    public void setId(String id);

    /**
     * Defines the unique identifier of the project to which this feed entry belongs.
     * @param projectId  the identifier of a project, or <code>null</code>.
     */
    public void setProjectId(UUID projectId);

    /**
     * Defines the source of the feed entry.
     */
    public void setSource(String source);

    /**
     * Defines the title of the feed entry.
     */
    public void setTitle(String title);

   /**
    * Defines the date and time of publication of the feed entry.
    */
   public void setPublished(Date published);

}
