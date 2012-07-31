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
package org.eclipse.skalli.core.internal.users;

import org.eclipse.skalli.services.configuration.ConfigKey;

public enum ConfigKeyLDAP implements ConfigKey {

    PASSWORD("ldap.password", "", true),
    USERNAME("ldap.username", "", false),
    HOSTNAME("ldap.hostname", "", false),
    FACTORY("ldap.ctxFactory", "", false),
    USERS_GROUP("ldap.usersGroup", "", false),
    CACHE_SIZE("ldap.cacheSize", "", false);

    private final String key;
    private final String defaultValue;
    private final boolean isEncrypted;

    private ConfigKeyLDAP(String key, String defaultValue, boolean isEncrypted) {
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
