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

/**
 * Service to persist entries for a project timeline.
 */
public interface FeedPersistenceService extends FeedFactory {

    /**
     * Merges the given collection of feed entries with the previously
     * stored timeline information. Entries are compared by their
     * {@link Entry#getId() unique identifiers}, duplicates are rejected.
     *
     * @param entries  a collection of feed entries to merge.
     *
     * @throws IOException  if an i/o error occured when merging the entries.
     */
    public void merge(Collection<FeedEntry> entries) throws IOException;

}
