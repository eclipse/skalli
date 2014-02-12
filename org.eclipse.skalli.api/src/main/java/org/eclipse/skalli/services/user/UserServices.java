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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.services.BundleProperties;
import org.eclipse.skalli.services.configuration.Configurations;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class that provides methods for retrieving the currently active
 * {@link UserService user service} and some frequently used utility
 * functions for looking up users.
 */
public class UserServices {

    private static final Logger LOG = LoggerFactory.getLogger(UserServices.class);

    private static final String LOCAL_USERSTORE = "local"; //$NON-NLS-1$

    private static final String CONFIG_KEY_USERSTORE = "userStore"; //$NON-NLS-1$
    private static final String USERSTORE_TYPE_PROPERTY = CONFIG_KEY_USERSTORE + ".type"; //$NON-NLS-1$
    private static final String USERSTORE_USE_LOCAL_FALLBACK_PROPERTY =
            CONFIG_KEY_USERSTORE + ".useLocalFallback"; //$NON-NLS-1$

    private static Map<String, UserService> byType =
            new ConcurrentHashMap<String, UserService>();

    private static volatile UserService activeUserService;

    protected void activate(ComponentContext context) {
        activeUserService = null;
        LOG.info(MessageFormat.format("[UserServices] {0} : activated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void deactivate(ComponentContext context) {
        activeUserService = null;
        LOG.info(MessageFormat.format("[UserServices] {0} : deactivated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void bindUserService(UserService userService) {
        String type = userService.getType();
        if (StringUtils.isNotBlank(type)) {
            byType.put(type, userService);
            activeUserService = null;
            LOG.info(MessageFormat.format("[UserServices(type={0})][registered {1}]", type, userService));
        }
    }

    protected void unbindUserService(UserService userService) {
        String type = userService.getType();
        if (StringUtils.isNotBlank(type)) {
            byType.remove(type);
            activeUserService = null;
            LOG.info(MessageFormat.format("[UserServices(type={0})][unregistered {1}]", type, userService));
        }
    }

    /**
     * Returns the currently active {@link UserService}.
     * <p>
     * Checks whether a dedicated user service has been {@link ConfigKeyUserStore configured},
     * otherwise falls backs to {@LocalUserStore}, if the configuration permits that.
     *
     * @return the configured and active user service, or <code>null</code> if
     * no user service is registered and fallback to the local user store is forbidden.
     */
    public static UserService getUserService() {
        UserService userService = activeUserService;
        if (userService == null) {
            synchronized (UserServices.class) {
                userService = activeUserService;
                if (userService == null) {
                    activeUserService = userService = getActiveUserService();
                }
            }
        }
        return userService;
    }

    private static UserService getActiveUserService() {
        UserService userService = null;
        // retrieve params from configuration properties;
        // use "local" userstore as fallback in case no explicit property/configuration is available
        String type = BundleProperties.getProperty(USERSTORE_TYPE_PROPERTY, LOCAL_USERSTORE);
        boolean useLocalFallback = BooleanUtils.toBoolean(
                BundleProperties.getProperty(USERSTORE_USE_LOCAL_FALLBACK_PROPERTY));

        // if there is a configuration via REST API available, override the properties
        UserStoreConfig userStoreConfig = Configurations.getConfiguration(UserStoreConfig.class);
        if (userStoreConfig != null) {
            type = userStoreConfig.getType();
            useLocalFallback = userStoreConfig.isUseLocalFallback();
        }

        // first: lookup the preferred user store
        if (StringUtils.isNotBlank(type)) {
            userService = byType.get(type);
        }

        // second: if the preferred user store is not available, but fallback
        // to the local store is allowed, use the local store
        if (userService == null && useLocalFallback) {
            LOG.info(MessageFormat.format(
                    "Preferred user service ''{0}'' not found, falling back to local user store", type));
            userService = byType.get(LOCAL_USERSTORE);
        }
        return userService;
    }

    /**
     * Returns the {@link User} matching a given unique identifier.
     * <p>
     * This is a convenience method equivalent to obtaining the active user service
     * with {@link #getUserService()} followed by {@link UserService#getUserById(String)}.
     *
     * @param userId  the unique identifier of the user.
     *
     * @return a user, which may be the {@link User#isUnknown() unknown} user,
     * if the given unique identifier does not match any user or no user store is active.
     * returns <code>null</code>, if the given <code>userId</code> is <code>null</code>
     * or a blank string.
     */
    public static User getUser(String userId) {
        User user = null;
        if (StringUtils.isNotBlank(userId)) {
            UserService userService =  getUserService();
            user = userService != null? userService.getUserById(userId) : new User(userId);
        }
        return user;
    }
}
