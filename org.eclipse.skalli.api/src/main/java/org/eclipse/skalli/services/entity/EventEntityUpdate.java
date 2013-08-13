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
package org.eclipse.skalli.services.entity;

import java.util.UUID;

import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.services.event.Event;

/**
 * Event that notifies about a change of an entity. This event is fired by an {@link EntityService}
 * when an entity is {@link EntityService#persist(org.eclipse.skalli.model.EntityBase, String) persisted}.
 * <br>
 * Any service that handles entities and wants to be notified about changes of these entities
 * should register to this event (see {@link org.eclipse.skalli.services.event.EventService#registerListener(Class,
 *  org.eclipse.skalli.services.event.EventListener)}).
 */
public class EventEntityUpdate extends Event {

    private final Class<?> entityClass;
    private final EntityBase entity;
    private final String userId;

    /**
     * Creates an entity update event.
     *
     * @param entityClass  class of the entity.
     * @param entity  the updated value of the entity.
     * @param userId  the unique identifier of the user that triggered the update.
     */
    public EventEntityUpdate(Class<?> entityClass, EntityBase entity, String userId) {
        this.entityClass = entityClass;
        this.entity = entity;
        this.userId = userId;
    }

    /**
     * Returns the class of the entity.
     */
    public Class<?> getEntityClass() {
        return entityClass;
    }

    /**
     * Returns the unique identifier of the entity.
     */
    public UUID getEntityId() {
        return entity.getUuid();
    }

    /**
     * Returns the updated entity value.
     */
    public EntityBase getEntity() {
        return entity;
    }

    /**
     * Returns the unique identifier of the user that triggered the update.
     */
    public String getUserId() {
        return userId;
    }
}
