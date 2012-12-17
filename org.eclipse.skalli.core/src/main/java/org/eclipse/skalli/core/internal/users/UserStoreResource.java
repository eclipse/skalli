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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.skalli.services.configuration.ConfigKey;
import org.eclipse.skalli.services.configuration.rest.ConfigResource;
import org.eclipse.skalli.services.user.ConfigKeyUserStore;

public class UserStoreResource extends ConfigResource<ConfigKeyUserStore, UserStoreConfig> {

    @Override
    protected Class<UserStoreConfig> getConfigClass() {
        return UserStoreConfig.class;
    }

    @Override
    protected Map<ConfigKey, String> configToMap(UserStoreConfig configObject) {
        Map<ConfigKey, String> ret = new HashMap<ConfigKey, String>();
        ret.put(ConfigKeyUserStore.TYPE, configObject.getType());
        ret.put(ConfigKeyUserStore.USE_LOCAL_FALLBACK, configObject.getUseLocalFallback());
        return ret;
    }

    @Override
    protected ConfigKeyUserStore[] getAllKeys() {
        return ConfigKeyUserStore.values();
    }

    @Override
    protected UserStoreConfig mapToConfig(Map<ConfigKeyUserStore, String> values) {
        UserStoreConfig ret = new UserStoreConfig();
        ret.setType(values.get(ConfigKeyUserStore.TYPE));
        ret.setUseLocalFallback(values.get(ConfigKeyUserStore.USE_LOCAL_FALLBACK));
        return ret;
    }
}