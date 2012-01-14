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
package org.eclipse.skalli.core.internal.event;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.skalli.services.event.Event;
import org.eclipse.skalli.services.event.EventListener;
import org.eclipse.skalli.services.event.EventService;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Beware: This eventing implementation is just a POC!!!
 */
public class EventComponent implements EventService {
    private static final Logger LOG = LoggerFactory.getLogger(EventComponent.class);

    private final Map<String, Set> listeners = new HashMap<String, Set>();

    protected void activate(ComponentContext context) {
        LOG.info(MessageFormat.format("[EventService] {0} : activated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void deactivate(ComponentContext context) {
        LOG.info(MessageFormat.format("[EventService] {0} : deactivated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    @Override
    public <T extends Event> void registerListener(Class<T> event, EventListener<T> listener) {
        Set<EventListener<T>> set = listeners.get(event.getName());
        if (set == null) {
            set = new HashSet<EventListener<T>>(1);
            listeners.put(event.getName(), set);
        }
        set.add(listener);
    }

    @Override
    public <T extends Event> void unregisterEventListener(Class<T> event, EventListener<T> listener) {
        Set<EventListener<T>> set = listeners.get(event.getName());
        if (set != null) {
            set.remove(listener);
        }
    }

    @Override
    public <T extends Event> void fireEvent(T event) {
        Set<EventListener<T>> set = listeners.get(event.getClass().getName());
        if (set != null) {
            for (EventListener<T> listener : set) {
                listener.onEvent(event);
            }
        }
    }

}
