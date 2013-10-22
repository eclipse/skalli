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

import java.util.List;

/**
 * Interface representing a distinct feed of timeline information.
 * Usually a feed updater will be connected to some remote data channel,
 * such as an RSS/Atom feed for a certain project provided by a GitWeb
 * server, or a mailing list.
 */
public interface FeedUpdater {

    /**
     * Retrieves the latest available timeline information. Note that
     * similar to RSS/Atom feeds a feed updater may return the
     * same entries over and over again, or for example only the ten
     * latest entries so that it is likely to miss entries if
     * <code>updateFeed</code> is not called frequently enough.
     * <p>
     * If available, a feed updater should initialize the attributes of feed entries.
     * Missing required attributes, such as the {@link #setSource(String) source identifier},
     * are added by the {@link FeedManager}.
     *
     * @param feedFactory  the factory to use for creating feed entries.
     * @return a list of feed entries delivered in the ordering provided by the remote feed, or
     * an empty list.
     */
    public List<FeedEntry> updateFeed(FeedFactory feedFactory);

    /**
     * Returns an identifier that indicates from which source this updater
     * retrieves its information. For example, a feed updater retrieving information
     * from a GitWeb server could return <code>"gitweb"</code>. Source identifiers
     * can be used to group or aggregate timeline information from "related" sources.
     * For example, a project may have multiple mailing lists each with its own
     * feed updater, but in the UI it one may want to show all mailing lists
     * in one merged timeline.
     */
    public String getSource();

    /**
     * Returns a human readable caption describing the source from which this updater
     * retrieves its information, e.g. "Git".
     */
    public String getCaption();

}
