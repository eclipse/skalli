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
package org.eclipse.skalli.services.configuration;

/**
 * Service providing configuration settings.
 */
public interface ConfigurationService {

    public String readString(ConfigKey key);

    public void writeString(ConfigTransaction tx, ConfigKey key, String value);

    public Integer readInteger(ConfigKey key);

    public void writeInteger(ConfigTransaction tx, ConfigKey key, Integer value);

    public Boolean readBoolean(ConfigKey key);

    public void writeBoolean(ConfigTransaction tx, ConfigKey key, Boolean value);

    public ConfigTransaction startTransaction();

    public void commit(ConfigTransaction tx);

    public void rollback(ConfigTransaction tx);

    public <T> T readCustomization(String customizationKey, Class<T> customizationClass);

    /**
     * Stores a customization object with the given key.
     *
     * <p>
     * In contrast to configurations that are typically technical oriented parameters (passwords, systems),
     * customizations are more related to controlling the behavior of the system.
     * </p>
     * <p>
     * Please note that customizations will not be encrypted,
     * so storing passwords and other secrets should be done as configurations.
     * </p>
     *
     * @param <T>
     * @param customizationKey
     * @param customization
     */
    public <T> void writeCustomization(String customizationKey, T customization);
}
