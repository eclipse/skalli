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
package org.eclipse.skalli.core.internal.search;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.eclipse.skalli.api.java.EntityService;
import org.eclipse.skalli.model.ext.Issue;
import org.eclipse.skalli.model.ext.Severity;

public class TestEntityService implements EntityService<TestEntity> {

    private final List<TestEntity> entities;

    public TestEntityService(List<TestEntity> entities) {
        this.entities = entities;
    }

    @Override
    public TestEntity getByUUID(UUID uuid) {
        for (TestEntity entity : entities) {
            if (entity.getUuid().equals(uuid)) {
                return entity;
            }
        }
        return null;
    }

    @Override
    public List<TestEntity> getAll() {
        return entities;
    }

    @Override
    public void persist(TestEntity entity, String userId) {
    }

    @Override
    public TestEntity loadEntity(Class<TestEntity> entityClass, UUID uuid) {
        return null;
    }

    @Override
    public SortedSet<Issue> validate(TestEntity entity, Severity minSeverity) {
        return new TreeSet<Issue>();
    }

    @Override
    public SortedSet<Issue> validateAll(Severity minSeverity) {
        return new TreeSet<Issue>();
    }

    @Override
    public Class<TestEntity> getEntityClass() {
        return TestEntity.class;
    }

    @Override
    public int size() {
        return entities.size();
    }
}
