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
package org.eclipse.skalli.services.validation;

import java.util.UUID;

import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Severity;

/**
 * Service for the synchronous and asynchronous validation of entities.
 */
public interface ValidationService {

    /**
     * Adds an entity to the queue of entities scheduled for validation.
     *
     * @param validation  describes the details of the queued entity.
     */
    public <T extends EntityBase> void queue(Validation<T> validation);

    /**
     * Adds all entities provided by the given entity service to the queue
     * of entities scheduled for validation.
     *
     * @param entityClass  the class of entities to validate.
     * @param minSeverity the minimal severity of issues to report.
     * @param userId  unique identifier of the user that queues the entities.
     */
    public <T extends EntityBase> void queueAll(Class<T> entityClass, Severity minSeverity, String userId);

    /**
     * Checks whether the given entity is in the queue of entities scheduled for validation.
     *
     * @param entity  the entity to check.
     *
     * @return <code>true</code>, if the entity is queued for validation.
     */
    public <T extends EntityBase> boolean isQueued(T entity);

    /**
     * Validates the given entity and persists the result.
     * <p>
     * Checks whether the entity has validation issues equal to or more serious
     * than the given severity. The result set is sorted according to {@link Issue#compareTo(Issue)}.
     *
     * @param entityClass  the class of the entities to validate.
     * @param entityId  unique identifier of the entity to validate.
     * @param minSeverity  the minimal severity of issues to report.
     * @param userId  unique identifier of the user initiating the validation.
     */
    public <T extends EntityBase> void validate(Class<T> entityClass, UUID entityId, Severity minSeverity, String userId);

    /**
     * Validates all entities matching the given class and persists the result.
     * <p>
     * Checks whether the entity has validation issues equal to or more serious
     * than the given severity. The result set is sorted according to {@link Issue#compareTo(Issue)}.
     *
     * @param entityClass  the class of the entities to validate.
     * @param minSeverity the minimal severity of issues to report.
     * @param userId  unique identifier of the user initiating the validation.
     */
    public <T extends EntityBase> void validateAll(Class<T> entityClass, Severity minSeverity, String userId);
}
