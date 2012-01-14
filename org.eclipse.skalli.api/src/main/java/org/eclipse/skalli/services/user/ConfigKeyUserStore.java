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
package org.eclipse.skalli.services.user;

import org.eclipse.skalli.model.User;
import org.eclipse.skalli.services.configuration.ConfigKey;

/**
 * Configuration for the {@link UserService user store} to use for resolving of {@link User users}.
 */
public enum ConfigKeyUserStore implements ConfigKey {

    /**
     * Configuration parameter determining the user service to use.
     * Implementations of {@link UserService} should declare the service property
     * <code>userstore.type</code> that corresponds to this configuration parameter.
     * The default value for this parameter is <tt>"local"</tt>.
     */
    TYPE("userstore.type", "local", false), //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Configuration parameter determining whether a local user store should be
     * used if the configured user store is not available.
     * The default value for this parameter is <tt>"true"</tt>.
     */
    USE_LOCAL_FALLBACK("userstore.use-local-fallback", "true", false); //$NON-NLS-1$ //$NON-NLS-2$

    private final String key;
    private final String defaultValue;
    private final boolean isEncrypted;

    private ConfigKeyUserStore(String key, String defaultValue, boolean isEncrypted) {
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
