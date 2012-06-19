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
package org.eclipse.skalli.core.internal.persistence.xstream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.services.persistence.EntityFilter;

/**
 * Most trivial implementation of an in-memory storage of our humble data model.
 */
class DataModelContainer {

    private final Map<Class<? extends EntityBase>, Map<UUID, EntityBase>> cache =
            new HashMap<Class<? extends EntityBase>, Map<UUID, EntityBase>>(0);

    private synchronized <T extends EntityBase> Map<UUID, EntityBase> getEntityMap(Class<T> entityClass) {
        Map<UUID, EntityBase> ret = new HashMap<UUID, EntityBase>();
        for (Entry<Class<? extends EntityBase>, Map<UUID, EntityBase>> entry : cache.entrySet()) {
            if (entityClass.isAssignableFrom(entry.getKey())) {
                ret.putAll(entry.getValue());
            }
        }
        return ret;
    }

    synchronized <T extends EntityBase> int size(Class<T> entityClass) {
        Map<UUID, EntityBase> entityMap = getEntityMap(entityClass);
        return entityMap != null ? entityMap.size() : 0;
    }

    /**
     * Adds the given entity to the cache.
     * @param entity  the entity to add.
     */
    synchronized void putEntity(EntityBase entity) {
        if (entity == null) {
            return;
        }
        Map<UUID, EntityBase> entityMap = cache.get(entity.getClass());
        if (entityMap == null) {
            entityMap = new HashMap<UUID, EntityBase>(0);
            cache.put(entity.getClass(), entityMap);
        }
        entityMap.put(entity.getUuid(), entity);
    }

    /**
     * Removes the given entity from the cache.
     * If the entity does not exist, this method does nothing.
     * @param entity  the entity to remove.
     */
    synchronized void removeEntity(EntityBase entity) {
        if (entity == null) {
            return;
        }
        Map<UUID, EntityBase> entityMap = cache.get(entity.getClass());
        if (entityMap != null) {
            entityMap.remove(entity.getUuid());
        }
    }

    /**
     * Returns the entity with the given unique identifier.
     * @param <T> a type derived from <code>EntityBase</code>.
     * @param entityClass  the class of the entity.
     * @param uuid  the unique identifier of the entity.
     * @return the entity with the given unique identifier, or
     * <code>null</code> if no matching entity exists.
     */
    synchronized <T extends EntityBase> T getEntity(Class<T> entityClass, UUID uuid) {
        Map<UUID, EntityBase> entityMap = getEntityMap(entityClass);
        return entityMap != null ? entityClass.cast(entityMap.get(uuid)) : null;
    }

    /**
     * Returns the first entity matching the given filter.
     * @param <T> a type derived from <code>EntityBase</code>.
     * @param entityClass  the class of the entity.
     * @param filter an entity filter that should match exactly one entity.
     * @return the first entity that matched the filter, or <code>null</code>.
     */
    synchronized <T extends EntityBase> T getEntity(Class<T> entityClass, EntityFilter<T> filter) {
        Map<UUID, EntityBase> entityMap = getEntityMap(entityClass);
        if (entityMap != null && entityMap.size() > 0) {
            for (EntityBase value : entityMap.values()) {
                T entity = entityClass.cast(value);
                if (filter.accept(entityClass, entity)) {
                    return entity;
                }
            }
        }
        return null;
    }

    /**
     * Returns all entities of the given type.
     * @param <T> a type derived from <code>EntityBase</code>.
     * @param entityClass  the class of the entity.
     * @return all entities of the given type, or an empty list.
     */
    synchronized <T extends EntityBase> List<T> getEntities(Class<T> entityClass) {
        Map<UUID, EntityBase> entityMap = getEntityMap(entityClass);
        ArrayList<T> result = new ArrayList<T>();
        for (EntityBase value : entityMap.values()) {
            result.add(entityClass.cast(value));
        }
        return result;
    }

    /**
     * Returns all entities matching the given filter.
     * @param <T> a type derived from <code>EntityBase</code>.
     * @param entityClass  the class of the entity.
     * @param filter an entity filter.
     * @return all entities of the given type matching the filter,
     * or an empty list.
     */
    synchronized <T extends EntityBase> List<T> getEntities(Class<T> entityClass, EntityFilter<T> filter) {
        ArrayList<T> result = new ArrayList<T>();
        Map<UUID, EntityBase> entityMap = getEntityMap(entityClass);
        if (entityMap != null && entityMap.size() > 0) {
            for (EntityBase value : entityMap.values()) {
                T entity = entityClass.cast(value);
                if (filter.accept(entityClass, entity)) {
                    result.add(entity);
                }
            }
        }
        return result;
    }

    /**
     * Returns the entities with the given unique identifiers.
     * @param <T> a type derived from <code>EntityBase</code>.
     * @param entityClass  the class of the entity.
     * @param uuids a collection of unique identifiers.
     * @return all entities referenced by the collection of unique identifiers.
     */
    synchronized <T extends EntityBase> List<T> getEntities(Class<T> entityClass, Collection<UUID> uuids) {
        ArrayList<T> result = new ArrayList<T>();
        if (uuids != null && uuids.size() > 0) {
            Map<UUID, EntityBase> entityMap = getEntityMap(entityClass);
            if (entityMap != null && entityMap.size() > 0) {
                for (UUID uuid : uuids) {
                    T targetEntity = entityClass.cast(entityMap.get(uuid));
                    if (targetEntity != null) {
                        result.add(targetEntity);
                    }
                }
            }
        }
        return result;
    }

    synchronized <T extends EntityBase> Set<UUID> keySet(Class<T> entityClass) {
        return getEntityMap(entityClass).keySet();
    }

    synchronized Set<Class<? extends EntityBase>> getEntityTypes() {
        return cache.keySet();
    }

    /**
     * Clears the entity cache.
     */
    synchronized void clearAll() {
        cache.clear();
    }

    /**
     * Clears the entity cache for the given class of entities.
     * @param entityClass
     */
    synchronized <T extends EntityBase> void clearAll(Class<T> entityClass) {
        List<T> entities = getEntities(entityClass);
        for (T entity: entities) {
            removeEntity(entity);
        }
    }
}
