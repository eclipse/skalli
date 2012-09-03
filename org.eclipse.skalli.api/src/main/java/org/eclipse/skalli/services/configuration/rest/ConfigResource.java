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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.skalli.services.configuration.ConfigKey;
import org.eclipse.skalli.services.configuration.ConfigTransaction;
import org.eclipse.skalli.services.configuration.ConfigurationService;

/**
 * Representation of configuration parameters that will be stored in a key-value store.
 *
 * @param <K> {@link ConfigKey} class that describes the configuration parameters
 * @param <O> Configuration object that holds the set of parameters
 */
public abstract class ConfigResource<K extends ConfigKey, O> extends ConfigResourceBase<O> {

    /**
     * The path prefix with which configuration resources are registered ({@value}).
     */
    public static final String CONFIG_PATH_PREFIX = "/config"; //$NON-NLS-1$

    /**
     * Converts all configuration parameters into a map.
     * @param configObject configuration object that should be converted into single values by their corresponding keys.
     * @return
     */
    protected abstract Map<ConfigKey, String> configToMap(O configObject);

    /**
     * Constructs a configuration object from a map.
     * @param values map containing the configuration parameters by their keys.
     * @return
     */
    protected abstract O mapToConfig(Map<K, String> values);

    /**
     * Defines all available {@link ConfigKey}s that will be used to store the values in the preference store.
     * @return
     */
    protected abstract K[] getAllKeys();

    @Override
    protected void storeConfig(ConfigurationService configService, O configObject, Map<String,Object> requestAttributes) {
        ConfigTransaction tx = configService.startTransaction();
        for (Entry<ConfigKey, String> entry : configToMap(configObject).entrySet()) {
            configService.writeString(tx, entry.getKey(), entry.getValue());
        }
        configService.commit(tx);
    }

    @Override
    protected O readConfig(ConfigurationService configService, Map<String,Object> requestAttributes) {
        Map<K, String> values = new HashMap<K, String>();
        for (K key : getAllKeys()) {
            if (key.isEncrypted()) {
                values.put(key, "*****"); //$NON-NLS-1$
            } else {
                values.put(key, configService.readString(key));
            }
        }
        O config = mapToConfig(values);
        return config;
    }

}
