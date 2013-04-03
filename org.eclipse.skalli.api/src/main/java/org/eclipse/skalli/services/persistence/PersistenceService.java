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
package org.eclipse.skalli.services.persistence;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.model.Project;

/**
 * Interface for a service that handles the persistence of {@link EntityBase entities},
 * e.g. {@link Project projects}.
 */
public interface PersistenceService {

    /**
     * Persists the given model entity.
     *
     * @param entityClass  the class the entity belongs to.
     * @param entity  the model entity to persist.
     * @param userId  unique identifier of the user performing the modification
     *                (relevant for the audit trail).
     */
    public <T extends EntityBase> void persist(Class<T> entityClass, EntityBase entity, String userId);

    /**
     * Loads the entity with the given UUID and its parent hierarchy, if available,
     * directly from the underyling storage service without caching it.
     *
     * @param <T> a type derived from <code>EntityBase</code>.
     * @param entityClass  the class the entity belongs to.
     * @param uuid  the unique identifier of the entity.
     *
     * @return  the entity, or <code>null</code> if there is no persisted entity
     * with the given unique identifier.
     */
    public <T extends EntityBase> T loadEntity(Class<T> entityClass, UUID uuid);

    /**
     * Returns the entity with the given unique identifier.
     *
     * @param <T> a type derived from <code>EntityBase</code>.
     * @param entityClass  the class the entity belongs to.
     * @param uuid  the unique identifier of the entity.
     *
     * @return  the entity instance for the given unique identifier, or <code>null</code>
     * if no such entity exists.
     */
    public <T extends EntityBase> T getEntity(Class<T> entityClass, UUID uuid);

    /**
     * Returns all existing entities of a given entity class.
     *
     * @param <T> a type derived from <code>EntityBase</code>.
     * @param entityClass  the class of the entities to retrieve.
     *
     * @returns a list of entities, or an empty list.
     */
    public <T extends EntityBase> List<T> getEntities(Class<T> entityClass);

    /**
     * Returns the overall number of entities of a given entity class.
     *
     * @param <T> a type derived from <code>EntityBase</code>.
     * @param entityClass  the class of the entities to count.
     *
     * @return the number of entities of the given class, or zero
     * if there are no entities of the given entity class.
     */
    public <T extends EntityBase> int size(Class<T> entityClass);

    /**
     * Returns the unique identifiers of all existing entities of a given entity class.
     *
     * @param <T> a type derived from <code>EntityBase</code>.
     * @param entityClass  the class of the entities, for which all known
     * unique identifiers are to be returned.
     *
     * @return a set of unique identifiers, or an empty set.
     */
    public <T extends EntityBase> Set<UUID> keySet(Class<T> entityClass);

    /**
     * Returns the first entity of a given entity type matching the given filter.
     *
     * @param <T> a type derived from <code>EntityBase</code>.
     * @param entityClass  the class of the entity.
     * @param filter an entity filter that should match exactly one entity.
     *
     * @return the first entity that matched the filter, or <code>null</code>.
     */
    public <T extends EntityBase> T getEntity(Class<T> entityClass, EntityFilter<T> filter);

    /**
     * Returns the {@link EntityBase#isDeleted() deleted} entity with the given unique identifier.
     *
     * @param <T> a type derived from <code>EntityBase</code>.
     * @param entityClass  the class the entity belongs to.
     * @param uuid  the unique identifier of the entity.
     *
     * @return  the entity instance for the given unique identifier, or <code>null</code>
     * if no such entity exists or the entity is not marked as deleted.
     */
    public <T extends EntityBase> T getDeletedEntity(Class<T> entityClass, UUID uuid);

    /**
     * Returns all {@link EntityBase#isDeleted() deleted} entities of a given entity class.
     *
     * @param <T> a type derived from <code>EntityBase</code>.
     * @param entityClass  the class of the entities to retrieve.
     *
     * @returns a list of entities, or an empty list.
     */
    public <T extends EntityBase> List<T> getDeletedEntities(Class<T> entityClass);

    /**
     * Returns the unique identifiers of all {@link EntityBase#isDeleted() deleted} entities
     * of a given entity class.
     *
     * @param <T> a type derived from <code>EntityBase</code>.
     * @param entityClass  the class of the entities, for which all known
     * unique identifiers are to be returned.
     *
     * @return a set of unique identifiers, or an empty set.
     */
    public <T extends EntityBase> Set<UUID> deletedSet(Class<T> entityClass);

    /**
     * Enforces a refresh of all entities of the given class from the underlying storage service.
     *
     * @param <T> a type derived from <code>EntityBase</code>.
     * @param entityClass  the class of entities to refresh.
     */
    public <T extends EntityBase> void refresh(Class<T> entityClass);

    /**
     * Enforces a refresh of all entities (of any type) from the underlying storage service.
     */
    public void refreshAll();
}
