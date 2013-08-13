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
package org.eclipse.skalli.testutil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.services.persistence.EntityFilter;
import org.eclipse.skalli.services.persistence.PersistenceService;

/**
 * A simple implementation of <code>PersistenceService</code> based on {@link HashMap}.
 * Note that this implementation stores only entities of a certain type specified
 * in the constructor. All methods will throw {@link IllegalArgumentException}
 * if an entity of the wrong type is supplied.
 */
public class HashMapPersistenceService implements PersistenceService {

    private final Class<? extends EntityBase> expectedEntityClass;

    private final Map<UUID, EntityBase> entities = new HashMap<UUID, EntityBase>();
    private final Map<UUID, EntityBase> deletedEntities = new HashMap<UUID, EntityBase>();

    public HashMapPersistenceService(Class<? extends EntityBase> expectedEntityClass) {
        this.expectedEntityClass = expectedEntityClass;
    }

    private <T extends EntityBase> void assertIsAssignable(Class<T> entityClass) {
        if (!expectedEntityClass.isAssignableFrom(entityClass)) {
            throw new IllegalArgumentException("Expected entity class: " + expectedEntityClass.getName());
        }
    }

    @Override
    public <T extends EntityBase> void persist(Class<T> entityClass, EntityBase entity, String userId) {
        assertIsAssignable(entityClass);
        if (entity.isDeleted()) {
            entities.remove(entity.getUuid());
            deletedEntities.put(entity.getUuid(), entity);
        } else {
            entities.put(entity.getUuid(), entity);
            deletedEntities.remove(entity.getUuid());
        }
    }

    @Override
    public <T extends EntityBase> T loadEntity(Class<T> entityClass, UUID uuid) {
        assertIsAssignable(entityClass);
        return entityClass.cast(entities.get(uuid));
    }

    @Override
    public <T extends EntityBase> T getEntity(Class<T> entityClass, UUID uuid) {
        assertIsAssignable(entityClass);
        return entityClass.cast(entities.get(uuid));
    }

    @Override
    public <T extends EntityBase> List<T> getEntities(Class<T> entityClass) {
        assertIsAssignable(entityClass);
        ArrayList<T> list = new ArrayList<T>(entities.size());
        for (EntityBase entity: entities.values()) {
            list.add(entityClass.cast(entity));
        }
        return list;
    }

    @Override
    public <T extends EntityBase> int size(Class<T> entityClass) {
        assertIsAssignable(entityClass);
        return entities.size();
    }

    @Override
    public <T extends EntityBase> Set<UUID> keySet(Class<T> entityClass) {
        assertIsAssignable(entityClass);
        return entities.keySet();
    }

    @Override
    public <T extends EntityBase> T getEntity(Class<T> entityClass, EntityFilter<T> filter) {
        assertIsAssignable(entityClass);
        for (EntityBase entity: entities.values()) {
            if (filter.accept(entityClass, entityClass.cast(entity))) {
                return entityClass.cast(entity);
            }
        }
        return null;
    }

    @Override
    public <T extends EntityBase> T getDeletedEntity(Class<T> entityClass, UUID uuid) {
        assertIsAssignable(entityClass);
        return entityClass.cast(deletedEntities.get(uuid));
    }

    @Override
    public <T extends EntityBase> List<T> getDeletedEntities(Class<T> entityClass) {
        assertIsAssignable(entityClass);
        ArrayList<T> list = new ArrayList<T>(deletedEntities.size());
        for (EntityBase entity: deletedEntities.values()) {
            list.add(entityClass.cast(entity));
        }
        return list;
    }

    @Override
    public <T extends EntityBase> Set<UUID> deletedSet(Class<T> entityClass) {
        assertIsAssignable(entityClass);
        return deletedEntities.keySet();
    }

    @Override
    public <T extends EntityBase> void refresh(Class<T> entityClass) {
        // nothing to do
    }

    @Override
    public void refreshAll() {
        // nothing to do
    }

}
