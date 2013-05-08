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
package org.eclipse.skalli.gerrit.client.internal;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.gerrit.client.GerritClient;
import org.eclipse.skalli.gerrit.client.GerritService;
import org.eclipse.skalli.gerrit.client.config.GerritConfig;
import org.eclipse.skalli.services.configuration.ConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("nls")
public class GerritServiceImpl implements GerritService {

    private final static Logger LOG = LoggerFactory.getLogger(GerritServiceImpl.class);

    private ConfigurationService configService;

    protected void bindConfigurationService(ConfigurationService configService) {
        this.configService = configService;
    }

    protected void unbindConfigurationService(ConfigurationService configService) {
        this.configService = null;
    }

    @Override
    public GerritClient getClient(String onBehalfOf) {
        if (StringUtils.isBlank(onBehalfOf)) {
            LOG.warn("Failed to create a Gerrit client: No acting user available");
            return null;
        }
        if (configService == null) {
            LOG.warn("Failed to create a Gerrit client:  No configuration service available");
            return null;
        }

        GerritConfig gerritConfig = configService.readConfiguration(GerritConfig.class);
        if (gerritConfig == null) {
            LOG.warn("Failed to create a Gerrit client:  No Gerrit configuration available");
            return null;
        }

        if (StringUtils.isBlank(gerritConfig.getHost())) {
            LOG.warn("Failed to create a Gerrit client:  No host configuration available");
            return null;
        }
        if (StringUtils.isBlank(gerritConfig.getPort())) {
            LOG.warn("No port configuration available: Using default port " + GerritClient.DEFAULT_PORT);
        } else if (!StringUtils.isNumeric(gerritConfig.getPort())) {
            LOG.warn("Failed to create a Gerrit client:  No host configuration available");
            return null;

        }
        if (StringUtils.isBlank(gerritConfig.getUser())
                || StringUtils.isBlank(gerritConfig.getPrivateKey())
                || StringUtils.isBlank(gerritConfig.getPassphrase())) {
            LOG.warn("Failed to create a Gerrit client:  No credentials configuration available");
            return null;
        }
        return new GerritClientImpl(gerritConfig, onBehalfOf);
    }
}
