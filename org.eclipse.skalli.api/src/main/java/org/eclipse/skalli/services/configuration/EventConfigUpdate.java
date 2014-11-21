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
package org.eclipse.skalli.services.configuration;

import org.eclipse.skalli.services.event.Event;

/**
 * Event that notifies about configuration changes. This event is fired by
 * the {@link ConfigurationService} when a configuration has been updated.
 * <br>
 * Any service that handles configurations should register to this event
 * (see {@link org.eclipse.skalli.services.event.EventService#registerListener(Class,
 *  org.eclipse.skalli.services.event.EventListener)}).
 */
public class EventConfigUpdate extends Event {

    private final Class<?> configClass;
    private final Object config;

    /**
     * Creates a configuration update event.
     *
     * @param configClass  the configuration that has been updated.
     */
    public EventConfigUpdate(Class<?> configClass, Object config) {
        this.configClass = configClass;
        this.config = config;
    }

    /**
     * Returns the configuration that has been updated.
     */
    public Class<?> getConfigClass() {
        return configClass;
    }

    /**
     * Returns the updated configuration value.
     */
    public Object getConfig() {
        return config;
    }

}
