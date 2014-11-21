/*******************************************************************************
 * Copyright (c) 2010-2014 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.gerrit.client.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("servers")
public class GerritServersConfig {

    @XStreamImplicit(itemFieldName = "server")
    private ArrayList<GerritServerConfig> servers;

    // do not remove: required by xstream
    public GerritServersConfig() {
    }

    /**
     * Returns the (unmodifiable) list of Gerrit configurations,
     * or an empty list.
     *
     * @return the server configurations, or an empty list.
     */
    public synchronized List<GerritServerConfig> getServers() {
        if (servers == null) {
            servers = new ArrayList<GerritServerConfig>();
        }
        return Collections.unmodifiableList(servers);
    }

    /**
     * Adds another server to the  list of Gerrit configurations.
     *
     * @param server  the server config to add.
     */
    public synchronized void addServer(GerritServerConfig server) {
        if (servers == null) {
            servers = new ArrayList<GerritServerConfig>();
        }
        servers.add(server);
    }

    /**
     * Returns the Gerrit server configuration for a given unique identifier.
     *
     * @param id  the identifier to search for.
     * @return a server configuration, or <code>null</code> if no matching
     * configuration could be found.
     */
    public GerritServerConfig getServer(String id) {
        List<GerritServerConfig> servers = getServers();
        for (GerritServerConfig server: servers) {
            if (server.getId().equals(id)) {
                return server;
            }
        }
        return null;
    }

    /**
     * Returns the (first) Gerrit server configuration with a
     * set {@link GerritServerConfig#isPreferred() preferred flag},
     * or the first available server if no server is marked as
     * preferred.
     *
     * @return the preferred server configuration, or the first
     * configuration in the list, or <code>null</code> if no
     * configurations are available at all.
     */
    public GerritServerConfig getPreferredServer() {
        List<GerritServerConfig> servers = getServers();
        for (GerritServerConfig server: servers) {
            if (server.isPreferred()) {
                return server;
            }
        }
        return servers.size() > 0? servers.get(0) : null;
    }

    /**
     * Checks if there are any Gerrit server configurations available.
     * @return <code>true</code> if there is at least one configuration available.
     */
    public boolean isEmpty() {
        return getServers().isEmpty();
    }
}
