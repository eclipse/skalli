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
package org.eclipse.skalli.services.event;

public interface EventService {

    public <T extends Event> void registerListener(Class<T> eventClass, EventListener<T> listener);

    public <T extends Event> void unregisterListener(Class<T> eventClass, EventListener<T> listener);

    public <T extends Event> void fireEvent(T event);
}
