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
package org.eclipse.skalli.common.configuration.proxy;

import org.eclipse.skalli.common.configuration.ConfigKey;

@SuppressWarnings("nls")
public enum ConfigKeyProxy implements ConfigKey {

    HOST("proxy.host", "", false),
    PORT("proxy.port", "", false),
    HTTPS_HOST("https.proxy.host", "", false),
    HTTPS_PORT("https.proxy.port", "", false),
    NONPROXYHOSTS("proxy.nonProxyHosts", "", false);

    private final String key;
    private final String defaultValue;
    private final boolean isEncrypted;

    private ConfigKeyProxy(String key, String defaultValue, boolean isEncrypted) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.isEncrypted = isEncrypted;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public boolean isEncrypted() {
        return isEncrypted;
    }

}
