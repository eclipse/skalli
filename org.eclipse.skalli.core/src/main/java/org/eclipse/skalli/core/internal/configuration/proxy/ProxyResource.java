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
package org.eclipse.skalli.core.internal.configuration.proxy;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.skalli.services.configuration.ConfigKey;
import org.eclipse.skalli.services.configuration.rest.ConfigResource;
import org.eclipse.skalli.services.destination.ConfigKeyProxy;

public class ProxyResource extends ConfigResource<ConfigKeyProxy, ProxyConfig> {

    @Override
    protected Class<ProxyConfig> getConfigClass() {
        return ProxyConfig.class;
    }

    @Override
    protected ConfigKeyProxy[] getAllKeys() {
        return ConfigKeyProxy.values();
    }

    @Override
    protected Map<ConfigKey, String> configToMap(ProxyConfig configObject) {
        Map<ConfigKey, String> map = new HashMap<ConfigKey, String>();
        map.put(ConfigKeyProxy.HOST, configObject.getHost());
        map.put(ConfigKeyProxy.PORT, configObject.getPort());
        map.put(ConfigKeyProxy.NONPROXYHOSTS, configObject.getNonProxyHosts());

        return map;
    }

    @Override
    protected ProxyConfig mapToConfig(Map<ConfigKeyProxy, String> values) {
        ProxyConfig config = new ProxyConfig();
        config.setHost(values.get(ConfigKeyProxy.HOST));
        config.setPort(values.get(ConfigKeyProxy.PORT));
        config.setNonProxyHosts(values.get(ConfigKeyProxy.NONPROXYHOSTS));

        return config;
    }

}
