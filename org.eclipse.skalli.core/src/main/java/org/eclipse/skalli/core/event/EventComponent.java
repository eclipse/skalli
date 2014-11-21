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
package org.eclipse.skalli.core.event;

import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.skalli.services.event.Event;
import org.eclipse.skalli.services.event.EventListener;
import org.eclipse.skalli.services.event.EventService;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of the eventing service.
 */
@SuppressWarnings("rawtypes")
public class EventComponent implements EventService {

    private static final Logger LOG = LoggerFactory.getLogger(EventComponent.class);

    private final ConcurrentHashMap<Class<?>, Map<EventListener,Boolean>> byEventClass =
            new ConcurrentHashMap<Class<?>, Map<EventListener,Boolean>>();

    protected void activate(ComponentContext context) {
        LOG.info(MessageFormat.format("[EventService] {0} : activated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void deactivate(ComponentContext context) {
        LOG.info(MessageFormat.format("[EventService] {0} : deactivated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    @Override
    public <T extends Event> void registerListener(Class<T> eventClass, EventListener<T> listener) {
        Map<EventListener,Boolean> map = new ConcurrentHashMap<EventListener,Boolean>(1);
        Map<EventListener,Boolean> listeners = byEventClass.putIfAbsent(eventClass, map);
        if (listeners == null) {
            listeners = map;
        }
        listeners.put(listener, Boolean.TRUE);
    }

    @Override
    public <T extends Event> void unregisterListener(Class<T> eventClass, EventListener<T> listener){
        Map<EventListener,Boolean> listeners = byEventClass.get(eventClass);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Event> void fireEvent(T event) {
        Map<EventListener,Boolean> listeners = byEventClass.get(event.getClass());
        if (listeners != null) {
            for (EventListener listener : listeners.keySet()) {
                listener.onEvent(event);
            }
        }
    }
}
