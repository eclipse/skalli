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
package org.eclipse.skalli.services.configuration.rest;

import java.util.Map;

import org.eclipse.skalli.services.configuration.ConfigurationService;

/**
 * Representation of application customization parameters.
 */
public abstract class CustomizingResource<T> extends ConfigResourceBase<T> {

    /**
     * Returns the storage category that will be used to store the customizing entity
     * in the storage service.
     */
    protected abstract String getKey();

    @Override
    protected void storeConfig(ConfigurationService configService, T configObject, Map<String,Object> requestAttributes) {
        configService.writeCustomization(getKey(), configObject);
    }

    @Override
    protected T readConfig(ConfigurationService configService, Map<String,Object> requestAttributes) {
        return configService.readCustomization(getKey(), getConfigClass());
    }
}
