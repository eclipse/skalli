/*******************************************************************************
 * Copyright (c) 2010-2017 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.services.user;

import org.eclipse.skalli.model.User;
import org.eclipse.skalli.services.event.Event;
import org.eclipse.skalli.services.event.EventService;

/**
 * Event that notifies about changes to the user base cache. This event is fired by
 * the {@link EventService} when a user is added or updated in the current UserService
 * instance.
 * <br>
 * Any service that for example caches or shows {@link org.eclipse.skalli.model.User users}
 * should register to this event
 * (see {@link org.eclipse.skalli.services.event.EventService#registerListener(Class,
 *  org.eclipse.skalli.services.event.EventListener)}).
 */
public class EventUserUpdate extends Event {

    private final User user;

    /**
     * Creates a user update event.
     *
     * @param user  the user that has been updated
     */
    public EventUserUpdate(User user) {
        this.user = user;
    }

    /**
     * Returns the user that has been updated.
     */
    public User getUser() {
        return user;
    }
}
