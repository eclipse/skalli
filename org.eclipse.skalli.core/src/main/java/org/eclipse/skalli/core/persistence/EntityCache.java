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
package org.eclipse.skalli.core.persistence;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.model.EntityFilter;

/**
 * Most trivial implementation of an in-memory cache for all kind of entities.
 * <p>
 * For any given entity class, there is a separate cache that can store
 * instances of the entity class alongside with instances of derived classes.
 * Note however, that the accessor methods of this class should be used
 * with the base class as parameter only, otherwise these accessors likely
 * will fail with {@link ClassCastException class cast exceptions}.
 */
class EntityCache {

    private final Map<Class<? extends EntityBase>, Map<UUID, EntityBase>> cache =
            new HashMap<Class<? extends EntityBase>, Map<UUID, EntityBase>>(0);

    /**
     * Checks if the given entity class has been registered with this cache before.
     *
     * @param entityClass  the class of the entity to check.
     */
    synchronized <T extends EntityBase> boolean isRegistered(Class<T> entityClass) {
        return cache.get(mapEntityType(entityClass)) != null;
    }

    /**
     * Registers the given entity class as entity base class with this cache.
     *
     * @param entityClass  the class of the entity to register.
     *
     * @throws IllegalArgumentException if the entity class (or any base class of the
     * entity class) has already been registered before.
     */
    synchronized <T extends EntityBase> void registerEntityClass(Class<T> entityClass) {
        Map<UUID, EntityBase> entityMap = cache.get(mapEntityType(entityClass));
        if (entityMap != null) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Entity type \"{0}\" already registered", entityClass.getName()));
        }
        entityMap = new HashMap<UUID, EntityBase>(0);
        cache.put(entityClass, entityMap);
    }

    /**
     * Maps the given entity class to a entity base class that has been registered before.
     *
     * @param entityClass  the class of the entity to map.
     *
     * @return  a base class of the given entity class, or the entity class itself.
     */
    synchronized <T extends EntityBase> Class<? extends EntityBase> mapEntityType(Class<T> entityClass) {
        for (Class<? extends EntityBase> knownEntityClass: cache.keySet()) {
            if (knownEntityClass.isAssignableFrom(entityClass)) {
                return knownEntityClass;
            }
        }
        return entityClass;
    }

    /**
     * Returns the number of entities for the given entity class.
     *
     * @param entityClass  the class of the entity.
     */
    synchronized <T extends EntityBase> int size(Class<T> entityClass) {
        Map<UUID, EntityBase> entityMap = cache.get(mapEntityType(entityClass));
        return entityMap != null ? entityMap.size() : 0;
    }

    /**
     * Adds the given entity to the cache.
     *
     * @param entity  the entity to add.
     *
     * @throws IllegalStateException  if the entity class has not
     * yet been {@link #registerEntityClass(Class) registered}.
     */
    synchronized void putEntity(EntityBase entity) {
        if (entity == null) {
            return;
        }
        Map<UUID, EntityBase> entityMap = cache.get(mapEntityType(entity.getClass()));
        if (entityMap == null) {
            throw new IllegalStateException(MessageFormat.format(
                    "Entity type \"{0}\" has not been registered", entity.getClass().getName()));
        }
        entityMap.put(entity.getUuid(), entity);
    }

    /**
     * Removes the given entity from the cache.
     * If the entity does not exist, this method does nothing.
     *
     * @param entity  the entity to remove.
     */
    synchronized void removeEntity(EntityBase entity) {
        if (entity == null) {
            return;
        }
        Map<UUID, EntityBase> entityMap = cache.get(mapEntityType(entity.getClass()));
        if (entityMap != null) {
            entityMap.remove(entity.getUuid());
        }
    }

    /**
     * Returns the entity with the given unique identifier.
     *
     * @param <T> a type derived from <code>EntityBase</code>.
     * @param entityClass  the class of the entity.
     * @param uuid  the unique identifier of the entity.
     *
     * @return the entity with the given unique identifier, or
     * <code>null</code> if no matching entity exists.
     *
     * @throws ClassCastException  if the resulting cache entry cannot be
     * {@link Class#cast(Object) cast} to the requested <code>entityClass</code>.
     */
    synchronized <T extends EntityBase> T getEntity(Class<T> entityClass, UUID uuid) {
        Map<UUID, EntityBase> entityMap = cache.get(mapEntityType(entityClass));
        return entityMap != null ? entityClass.cast(entityMap.get(uuid)) : null;
    }

    /**
     * Returns the first entity matching the given filter.
     *
     * @param <T> a type derived from <code>EntityBase</code>.
     * @param entityClass  the class of the entity.
     * @param filter an entity filter that should match exactly one entity.
     *
     * @return the first entity that matched the filter, or <code>null</code>.
     *
     * @throws ClassCastException  if a cache entry cannot be
     * {@link Class#cast(Object) cast} to the requested <code>entityClass</code>.
     */
    synchronized <T extends EntityBase> T getEntity(Class<T> entityClass, EntityFilter<T> filter) {
        Map<UUID, EntityBase> entityMap = cache.get(mapEntityType(entityClass));
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
     *
     * @param <T> a type derived from <code>EntityBase</code>.
     * @param entityClass  the class of the entity.
     * @return all entities of the given type, or an empty list.
     *
     * @throws ClassCastException  if a cache entry cannot be
     * {@link Class#cast(Object) cast} to the requested <code>entityClass</code>.
     */
    synchronized <T extends EntityBase> List<T> getEntities(Class<T> entityClass) {
        Map<UUID, EntityBase> entityMap = cache.get(mapEntityType(entityClass));
        if (entityMap == null || entityMap.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<T> result = new ArrayList<T>();
        for (EntityBase value : entityMap.values()) {
            result.add(entityClass.cast(value));
        }
        return result;
    }

    /**
     * Returns all entities matching the given filter.
     *
     * @param <T> a type derived from <code>EntityBase</code>.
     * @param entityClass  the class of the entity.
     * @param filter an entity filter, or <code>null</code>. If no filter
     * is specified, all entities are returned.
     *
     * @return all entities of the given type matching the filter,
     * or an empty list.
     *
     * @throws ClassCastException  if a cache entry cannot be
     * {@link Class#cast(Object) cast} to the requested <code>entityClass</code>.
     */
    synchronized <T extends EntityBase> List<T> getEntities(Class<T> entityClass, EntityFilter<T> filter) {
        ArrayList<T> result = new ArrayList<T>();
        Map<UUID, EntityBase> entityMap = cache.get(mapEntityType(entityClass));
        if (entityMap != null && entityMap.size() > 0) {
            for (EntityBase value : entityMap.values()) {
                T entity = entityClass.cast(value);
                if (filter == null || filter.accept(entityClass, entity)) {
                    result.add(entity);
                }
            }
        }
        return result;
    }

    /**
     * Returns the entities with the given unique identifiers.
     *
     * @param <T> a type derived from <code>EntityBase</code>.
     * @param entityClass  the class of the entity.
     * @param uuids a collection of unique identifiers.
     *
     * @return all entities referenced by the collection of unique identifiers.
     *
     * @throws ClassCastException  if a cache entry cannot be
     * {@link Class#cast(Object) cast} to the requested <code>entityClass</code>.
     */
    synchronized <T extends EntityBase> List<T> getEntities(Class<T> entityClass, Collection<UUID> uuids) {
        ArrayList<T> result = new ArrayList<T>();
        if (uuids != null && uuids.size() > 0) {
            Map<UUID, EntityBase> entityMap = cache.get(mapEntityType(entityClass));
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

    /**
     * Returns the set of unique identifiers of the entities of a given entity class.
     *
     * @param entityClass  the class of the entity.
     *
     * @return a set of unique identifiers, or an empty set.
     */
    synchronized <T extends EntityBase> Set<UUID> keySet(Class<T> entityClass) {
        Map<UUID, EntityBase> entityMap = cache.get(mapEntityType(entityClass));
        if (entityMap == null || entityMap.isEmpty()) {
            return Collections.emptySet();
        }
        return entityMap.keySet();
    }

    /**
     * Returns the entity classes managed by this cache.
     *
     * @return a set of entity classes, or an empty set.
     */
    synchronized Set<Class<? extends EntityBase>> getEntityTypes() {
        return cache.keySet();
    }

    /**
     * Clears the entity cache.
     */
    synchronized void clearAll() {
        for (Map<UUID, EntityBase> entityMap: cache.values()) {
            entityMap.clear();
        }
    }

    /**
     * Clears the entity cache for the given class of entities.
     * @param entityClass  the class of the entities.
     */
    synchronized <T extends EntityBase> void clearAll(Class<T> entityClass) {
        Map<UUID, EntityBase> entityMap = cache.get(mapEntityType(entityClass));
        if (entityMap != null) {
            entityMap.clear();
        }
    }
}
