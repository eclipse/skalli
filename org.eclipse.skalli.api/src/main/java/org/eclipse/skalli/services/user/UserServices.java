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

import org.apache.commons.lang.BooleanUtils;
import org.eclipse.skalli.services.Services;
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
        UserService ret = null;
        String type = "(undefined)"; //$NON-NLS-1$
        if (configService != null) {
            type = configService.readString(ConfigKeyUserStore.TYPE);
            ret = getUserServiceByType(type);
        }
        // TODO replace by configService.readBoolean(...)
        if (ret == null
                && (configService == null || BooleanUtils.toBoolean(configService
                        .readString(ConfigKeyUserStore.USE_LOCAL_FALLBACK)))) {
            LOG.info("User service '" + type + "' not found, trying fallback 'local'"); //$NON-NLS-1$ //$NON-NLS-2$
            ret = getUserServiceByType("local"); //$NON-NLS-1$
        }
        if (ret == null) {
            throw new IllegalStateException("No user service registered"); //$NON-NLS-1$
        }
        return ret;
    }

    UserService getUserServiceByType(String type) {
        String filter = "(&(" + Constants.OBJECTCLASS + "=" + UserService.class.getName() + ")(userService.type=" + type + "))"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        UserService userService = Services.getService(UserService.class, filter);
        return userService;
    }

}
