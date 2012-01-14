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
package org.eclipse.skalli.services.tagging;

import java.util.Map;
import java.util.Set;

import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.model.ExtensibleEntityBase;
import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.Taggable;

/**
 * Service for managing of tags and taggable entities.
 *
 * Note, all methods of this service expect that either the given entity class implements the
 * interface {@link Taggable}, or that it is an {@link ExtensibleEntityBase} and at least one of
 * its assigned {@link ExtensionEntityBase extensions} implementsc<code>Taggable</code>.
 * If neither of these conditions is met, an empty result will be returned.
 */
public interface TaggingService {

    /**
     * Returns the overall set of tags assigned to entities of the given class.
     *
     * @param entityClass the class of entities to examine.
     * @return a set of tags, or an empty set.
     */
    public <T extends EntityBase> Set<String> getAllTags(Class<T> entityClass);

    /**
     * Returns all entities of a given class that are tagged with a certain tag.
     * The order of the result is not specified.
     *
     * @param entityClass the class of entities to examine.
     * @param tag the tag to search for.
     * @return  entities tagged with the given tag, or an empty list.
     */
    public <T extends EntityBase> Set<T> getTaggables(Class<T> entityClass, String tag);

    /**
     * Returns a map of entities of the given class sorted by their respective tags.
     *
     * @param entityClass  the class of entities to examine.
     * @return a map of entities with tags as keys, or an empty map.
     */
    public <T extends EntityBase> Map<String, Set<T>> getTaggables(Class<T> entityClass);
}
