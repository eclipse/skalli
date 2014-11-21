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

/**
 * Interface of a service that provides configuration settings.
 */
public interface ConfigurationService {

    /**
     * Retrieves the configuration for the given configuration class
     * from the underlying storage.
     *
     * @param configurationClass  the class of the configuration to retrieve.
     *
     * @return the configuration instance, or <code>null</code> if no
     * such configuration exists.
     */
    public <T> T readConfiguration(Class<T> configurationClass);

    /**
     * Stores the given configuration.
     *
     * @param configuration  the configuration to store.
     */
    public <T> void writeConfiguration(T configuration);
}
