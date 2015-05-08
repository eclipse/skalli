/*******************************************************************************
 * Copyright (c) 2010-2015 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.core.gerrit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.core.gerrit.ssh.GerritClientSSH;
import org.eclipse.skalli.services.configuration.ConfigurationService;
import org.eclipse.skalli.services.gerrit.GerritClient;
import org.eclipse.skalli.services.gerrit.GerritServerConfig;
import org.eclipse.skalli.services.gerrit.GerritServersConfig;
import org.eclipse.skalli.services.gerrit.GerritService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("nls")
public class GerritComponent implements GerritService {

    private final static Logger LOG = LoggerFactory.getLogger(GerritComponent.class);

    private ConfigurationService configService;

    protected void bindConfigurationService(ConfigurationService configService) {
        this.configService = configService;
    }

    protected void unbindConfigurationService(ConfigurationService configService) {
        this.configService = null;
    }

    @Override
    public GerritClient getClient(String id, String onBehalfOf) {
        if (StringUtils.isBlank(id)) {
            LOG.warn("Failed to create a Gerrit client: No server id specified");
            return null;
        }
        if (StringUtils.isBlank(onBehalfOf)) {
            LOG.warn("Failed to create a Gerrit client: No acting user available");
            return null;
        }
        if (configService == null) {
            LOG.warn("Failed to create a Gerrit client:  No configuration service available");
            return null;
        }

        GerritServersConfig gerritServers = configService.readConfiguration(GerritServersConfig.class);
        if (gerritServers == null) {
            LOG.warn("Failed to create a Gerrit client:  No Gerrit configuration available");
            return null;
        }
        GerritServerConfig gerritConfig = gerritServers.getServer(id);
        if (gerritConfig == null) {
            LOG.warn("Failed to create a Gerrit client:  No Gerrit configuration for id=" + id);
            return null;
        }
        if (StringUtils.isBlank(gerritConfig.getHost())) {
            LOG.warn("Failed to create a Gerrit client:  No host configuration available");
            return null;
        }
        if (StringUtils.isBlank(gerritConfig.getPort()) || !StringUtils.isNumeric(gerritConfig.getPort())) {
            LOG.warn("Failed to create a Gerrit client:  No port configuration available");
            return null;
        }
        if (StringUtils.isBlank(gerritConfig.getUser())
                || StringUtils.isBlank(gerritConfig.getPrivateKey())
                || StringUtils.isBlank(gerritConfig.getPassphrase())) {
            LOG.warn("Failed to create a Gerrit client:  No credentials configuration available");
            return null;
        }
        if ("ssh".equalsIgnoreCase(gerritConfig.getProtocol())) {
            return new GerritClientSSH(gerritConfig, onBehalfOf);
        }
        LOG.error("Failed to create a Gerrit client: Unsupported protocol");
        return null;
    }
}
