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
package org.eclipse.skalli.services.validation;

import java.util.UUID;

import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.model.Severity;

/**
 * Helper class describing entities scheduled for validation.
 * A <code>Validation</code> must specify the unique identifier and
 * class of an entity, the identifier of the user that initiated the validation,
 * and may optionally specify the minimal {@link Severity} to apply to the validation.
 * If no severity is specified, {@link Severity#INFO} is assumed.
 */
public class Validation<T extends EntityBase> {

    /**
     * The default priority for validation jobs.
     */
    public static final int DEFAULT_PRIORITY = 5;
    public static final int HIGHEST_PRIORITY = 0;
    public static final int LOWEST_PRIORITY = 9;

    /**
     * Validations with priority <code>IMMEDIATE</code> will be executed
     * as single shot task.
     */
    public static final int IMMEDIATE = -1;

    private Class<T> entityClass;
    private UUID entityId;
    private Severity minSeverity;
    private String userId;
    private int priority = DEFAULT_PRIORITY;

    /**
     * Creates a <code>Validation</code> instance with default priority.
     *
     * @param entityClass  the class of the entities to validate.
     * @param entityId  the unique identifier of the entity to validate.
     * @param minSeverity  the minimal severity of issues to report.
     * @param userId  unique identifier of the user initiating the validation.
     */
    public Validation(Class<T> entityClass, UUID entityId, Severity minSeverity, String userId) {
        this(entityClass, entityId, minSeverity, userId, DEFAULT_PRIORITY);
    }

    /**
     * Creates a <code>Validation</code> instance.
     *
     * @param entityClass  the class of the entities to validate.
     * @param entityId  the unique identifier of the entity to validate.
     * @param minSeverity  the minimal severity of issues to report.
     * @param userId  unique identifier of the user initiating the validation.
     * @param priority  the priority with which this validation should be executed.
     * Must be a number between 0 (highest priority) and 9 (lowest priority), or
     * -1 for validations to be executed immediately.
     * 5 is the default priority.
     */
    public Validation(Class<T> entityClass, UUID entityId, Severity minSeverity, String userId, int priority) {
        if (entityClass == null) {
            throw new IllegalArgumentException("argument 'entityClass' must not be null");
        }
        if (entityId == null) {
            throw new IllegalArgumentException("argument 'entityId' must not be null");
        }
        if (priority < IMMEDIATE || priority > LOWEST_PRIORITY) {
            throw new IllegalArgumentException("argument 'priority' must be a number in the range -1..9");
        }
        this.entityClass = entityClass;
        this.entityId = entityId;
        this.priority = priority;
        setUserId(userId);
        setMinSeverity(minSeverity);
    }

    public Class<T> getEntityClass() {
        return entityClass;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public Severity getMinSeverity() {
        return minSeverity;
    }

    public void setMinSeverity(Severity minSeverity) {
        if (minSeverity == null) {
            minSeverity = Severity.INFO;
        }
        this.minSeverity = minSeverity;
    }

    public String getUserId() {
        return userId;
    }

    public int getPriority() {
        return priority;
    }

    public void setUserId(String userId) {
        if (userId == null) {
            throw new IllegalArgumentException("argument 'userId' must not be null");
        }
        this.userId = userId;
    }

    @Override
    public int hashCode() {
        int result = 31 + entityClass.getName().hashCode();
        result = 31 * result + entityId.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof Validation) {
            Validation<?> v = (Validation<?>) obj;
            return entityClass.equals(v.entityClass) && entityId.equals(v.entityId);
        }
        return false;
    }

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Validation [entityClass=");
        stringBuilder.append(entityClass);
        stringBuilder.append(", entityId=");
        stringBuilder.append(entityId);
        stringBuilder.append(", minSeverity=");
        stringBuilder.append(minSeverity);
        stringBuilder.append(", userId=");
        stringBuilder.append(userId);
        stringBuilder.append(", priority=");
        stringBuilder.append(priorityAsString());
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    @SuppressWarnings("nls")
    public String priorityAsString() {
        switch (priority) {
        case IMMEDIATE:
            return "IMMEDIATE";
        case DEFAULT_PRIORITY:
            return "DEFAULT";
        case HIGHEST_PRIORITY:
            return "HIGHEST";
        case LOWEST_PRIORITY:
            return "LOWEST";
        default:
            return Integer.toString(priority);
        }
    }

}
