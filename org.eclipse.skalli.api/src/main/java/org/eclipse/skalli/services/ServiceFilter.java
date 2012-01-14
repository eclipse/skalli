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
package org.eclipse.skalli.services;

/**
 * Callback interface for filtering of OSGi service instances.
 *
 * @param <T>
 *          type parameter representing an OSGi service interface.
 *
 * @see Services#getServices(Class, ServiceFilter)
 */
public interface ServiceFilter<T> {

    /**
     * Tests if the specified service instance should be included in a list of
     * service instances.
     *
     * @param instance
     *          an instance of an OSGi service interface.
     *
     * @return <code>true</code> if and only if the service instance should be
     *         included in a list of service instances; <code>false</code>
     *         otherwise.
     */
    public boolean accept(T instance);
}
