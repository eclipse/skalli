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
package org.eclipse.skalli.gerrit.client.config;

import org.eclipse.skalli.services.configuration.ConfigKey;

@SuppressWarnings("nls")
public enum ConfigKeyGerrit implements ConfigKey {

    HOST("scm.gerrit.host", "", false),
    PORT("scm.gerrit.port", "", false),
    USER("scm.gerrit.username", "", false),
    PRIVATEKEY("scm.gerrit.privatekey", "", true),
    PASSPHRASE("scm.gerrit.passphrase", "", true),
    CONTACT("scm.gerrit.contact", "", false),
    PARENT("scm.gerrit.parent", "", false);

    private final String key;
    private final String defaultValue;
    private final boolean isEncrypted;

    private ConfigKeyGerrit(String key, String defaultValue, boolean isEncrypted) {
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
