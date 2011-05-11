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
package org.eclipse.skalli.api.java;

import java.util.List;
import java.util.UUID;

import org.eclipse.skalli.model.ext.EntityBase;

/**
 * Services to persist model entities, e.g. projects.
 */
public interface PersistenceService {

    /**
     * Persists the given model entity.
     *
     * @param entity
     *          the model entity to persist.
     * @param userId
     *          unique identifier of the user performing the modification
     *          (relevant for the audit trail).
     */
    public void persist(EntityBase entity, String userId);

    public <T extends EntityBase> T loadEntity(Class<T> entityClass, UUID uuid);

    public <T extends EntityBase> T getEntity(Class<T> entityClass, UUID uuid);

    public <T extends EntityBase> List<T> getEntities(Class<T> entityClass);

    public <T extends EntityBase> T getEntity(Class<T> entityClass, EntityFilter<T> filter);

    public <T extends EntityBase> T getDeletedEntity(Class<T> entityClass, UUID uuid);

    public <T extends EntityBase> List<T> getDeletedEntities(Class<T> entityClass);

}
