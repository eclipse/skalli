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

import java.text.MessageFormat;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.configuration.ConfigurationProperties;
import org.eclipse.skalli.services.configuration.ConfigurationService;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utiliy class that provides methods to retrieve the currently
 * active {@link UserService user service}.
 */
public class UserServices {

    private static final Logger LOG = LoggerFactory.getLogger(UserServices.class);

    private static final String LOCAL_USERSTORE = "local"; //$NON-NLS-1$

    private static final String CONFIG_KEY_USERSTORE = "userStore"; //$NON-NLS-1$
    private static final String USERSTORE_TYPE_PROPERTY = CONFIG_KEY_USERSTORE + ".type"; //$NON-NLS-1$
    private static final String USERSTORE_USE_LOCAL_FALLBACK_PROPERTY =
            CONFIG_KEY_USERSTORE + ".useLocalFallback"; //$NON-NLS-1$

    private static UserServices instance = null;

    // this class is a singleton!
    UserServices() {
    }

    /**
     * Returns the currently active {@link UserService}.
     * Checks whether a dedicated user service has been {link ConfigKeyUserStore configured},
     * otherwise falls backs to {@LocalUserStore} if the configuration permits that.
     *
     * @return the currently active user service.
     *
     * @throws IllegalStateException if no user service is available.
     */
    public static UserService getUserService() {
        if (instance == null) {
            instance = new UserServices();
        }
        return instance.getConfiguredUserService();
    }

    ConfigurationService getConfigService() {
        return Services.getService(ConfigurationService.class);
    }

    UserService getConfiguredUserService() {
        ConfigurationService configService = getConfigService();
        UserService userService = null;

        // retrieve params from configuration properties;
        // use "local" userstore as fallback in case no explicit property/configuration is available
        String type = ConfigurationProperties.getProperty(USERSTORE_TYPE_PROPERTY, LOCAL_USERSTORE);
        boolean isUseLocalFallback = BooleanUtils.toBoolean(
                ConfigurationProperties.getProperty(USERSTORE_USE_LOCAL_FALLBACK_PROPERTY));

        // if there is a configuration via REST API available, override the properties
        if (configService != null) {
            UserStoreConfig config = configService.readCustomization(CONFIG_KEY_USERSTORE, UserStoreConfig.class);
            if (config != null) {
                type = config.getType();
                isUseLocalFallback = config.isUseLocalFallback();
            }
        }

        // first: lookup the preferred user store
        if (StringUtils.isNotBlank(type)) {
            userService = getUserServiceByType(type);
        }

        // second: if the preferred user store is not available and fallback
        // to the local store is allowed, use the local store
        if (userService == null && isUseLocalFallback) {
            LOG.info(MessageFormat.format("User service ''{0}'' not found, trying local user store", type));
            userService = getUserServiceByType(LOCAL_USERSTORE);
        }
        return userService;
    }

    UserService getUserServiceByType(String type) {
        String filter = MessageFormat.format("(&({0}={1})(userService.type={2}))", //$NON-NLS-1$
                Constants.OBJECTCLASS, UserService.class.getName(), type);
        UserService userService = Services.getService(UserService.class, filter);
        return userService;
    }
}
