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
package org.eclipse.skalli.services.tagging;

import java.util.SortedMap;
import java.util.SortedSet;

import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.model.ExtensibleEntityBase;
import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.Taggable;

/**
 * Service for managing tags and taggable entities.
 * <p>
 * Note, all methods of this service expect that either the given entity class implements the
 * interface {@link Taggable}, or that it is an {@link ExtensibleEntityBase extensible entity}
 * and at least one of its associated {@link ExtensionEntityBase extensions} implements
 * <code>Taggable</code>.
 */
public interface TaggingService {

    /**
     * Returns all known tags assigned to entities of the given class sorted alphanumerically
     * by tag name and mapped to their respective number <sof occurence.
     *
     * @param entityClass the class of entities to examine.
     * @return all known tags assigned to entities of the given class, or an empty map.
     */
    public <T extends EntityBase> SortedMap<String, Integer> getTags(Class<T> entityClass);

    /**
     * Returns all known tags assigned to entities of the given class sorted by
     * decreasing number of occurence. The most popular tag is returned first in the result.
     *
     * @param entityClass the class of entities to examine.
     * @return all known tags assigned to entities of the given class, or an empty set.
     */
    public <T extends EntityBase> SortedSet<TagCount> getMostPopular(Class<T> entityClass);


    /**
     * Returns the <code>count</code> most popular tags assigned to entities of the
     * given class sorted by decreasing number of occurence. The most popular tag is
     * returned first in the result.
     *
     * @param entityClass the class of entities to examine.
     * @param count  the number of entries to return. If <code>count</code> is negative,
     * the result is the same as for {@link #getMostPopular()}. If <code>count</code> is
     * zero, an empty list is returned.
     *
     * @return the most popular tags, or an empty set.
     */
    public <T extends EntityBase> SortedSet<TagCount> getMostPopular(Class<T> entityClass, int count);
}
