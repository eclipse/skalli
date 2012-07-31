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

public class LDAPResource extends ConfigResource<ConfigKeyLDAP, LDAPConfig> {

    @Override
    protected Class<LDAPConfig> getConfigClass() {
        return LDAPConfig.class;
    }

    @Override
    protected ConfigKeyLDAP[] getAllKeys() {
        return ConfigKeyLDAP.values();
    }

    @Override
    protected Map<ConfigKey, String> configToMap(LDAPConfig configObject) {
        Map<ConfigKey, String> ret = new HashMap<ConfigKey, String>();
        ret.put(ConfigKeyLDAP.FACTORY, configObject.getCtxFactory());
        ret.put(ConfigKeyLDAP.HOSTNAME, configObject.getHostname());
        ret.put(ConfigKeyLDAP.USERNAME, configObject.getUsername());
        ret.put(ConfigKeyLDAP.PASSWORD, configObject.getPassword());
        ret.put(ConfigKeyLDAP.USERS_GROUP, configObject.getUsersGroup());
        ret.put(ConfigKeyLDAP.CACHE_SIZE, configObject.getCacheSize());
        return ret;
    }

    @Override
    protected LDAPConfig mapToConfig(Map<ConfigKeyLDAP, String> values) {
        LDAPConfig ret = new LDAPConfig();
        ret.setCtxFactory(values.get(ConfigKeyLDAP.FACTORY));
        ret.setHostname(values.get(ConfigKeyLDAP.HOSTNAME));
        ret.setUsername(values.get(ConfigKeyLDAP.USERNAME));
        ret.setPassword(values.get(ConfigKeyLDAP.PASSWORD));
        ret.setUsersGroup(values.get(ConfigKeyLDAP.USERS_GROUP));
        ret.setCacheSize(values.get(ConfigKeyLDAP.CACHE_SIZE));
        return ret;
    }

}
