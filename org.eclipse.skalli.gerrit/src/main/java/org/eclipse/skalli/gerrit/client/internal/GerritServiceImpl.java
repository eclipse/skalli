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
import org.eclipse.skalli.gerrit.client.config.ConfigKeyGerrit;
import org.eclipse.skalli.services.configuration.ConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("nls")
public class GerritServiceImpl implements GerritService {

  private final static Logger LOG = LoggerFactory.getLogger(GerritServiceImpl.class);

  private ConfigurationService configService;

  public GerritServiceImpl() {

  }

  GerritServiceImpl(ConfigurationService configService) {
    this.configService = configService;
  }

  public void bindConfigurationService(ConfigurationService configService) {
    this.configService = configService;
  }

  public void unbindConfigurationService(ConfigurationService configService) {
    this.configService = null;
  }

  @Override
  public GerritClient getClient(String userId) {
    if (StringUtils.isBlank(userId)) {
      LOG.warn("No user ID passed. Cannot return GerritClient, but null.");
      return null;
    }
    if (configService == null) {
      LOG.warn("No ConfigurationService found. Cannot return GerritClient, but null.");
      return null;
    }

    String cfgHost = configService.readString(ConfigKeyGerrit.HOST);
    String cfgPort = configService.readString(ConfigKeyGerrit.PORT);
    String cfgUser = configService.readString(ConfigKeyGerrit.USER);
    String cfgPrivateKey = configService.readString(ConfigKeyGerrit.PRIVATEKEY);
    String cfgPassphrase = configService.readString(ConfigKeyGerrit.PASSPHRASE);

    // check all so that the log gives as much information as possible for reconfiguration
    boolean validHost = isValidString(cfgHost, "host");
    boolean validPort = isValidNumber(cfgPort, "port");
    boolean validUser = isValidString(cfgUser, "user");
    boolean validPrivateKey = isValidString(cfgPrivateKey, "privateKey");
    boolean validPassphrase = isValidString(cfgPassphrase, "passphrase");

    if (!validHost || !validPort || !validUser || !validPrivateKey || !validPassphrase) {
      LOG.warn("Invalid configuration. Cannot return GerritClient, but null.");
      return null;
    }

    LOG.info("Configuration loaded successfully. Trying to initialize GerritClient.");
    return new GerritClientImpl(cfgHost, Integer.valueOf(cfgPort), cfgUser, cfgPrivateKey, cfgPassphrase, userId);
  }

  private boolean isValidString(String value, String property) {
    boolean isValid = !StringUtils.isBlank(value);

    if (!isValid && !StringUtils.isBlank(property)) {
      LOG.warn(String.format("Property '%s' is not set.", property));
    }
    return isValid;
  }

  private boolean isValidNumber(String value, String property) {
    boolean isValid = !StringUtils.isBlank(value) && StringUtils.isNumeric(value);

    if (!isValid && !StringUtils.isBlank(property)) {
      LOG.warn(String.format("Property '%s' is not set or not numeric.", property));
    }
    return isValid;
  }

}
