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
package org.eclipse.skalli.core.user.ldap;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.eclipse.skalli.core.cache.Cache;
import org.eclipse.skalli.core.cache.GroundhogCache;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.services.configuration.ConfigurationService;
import org.eclipse.skalli.services.event.EventConfigUpdate;
import org.eclipse.skalli.services.event.EventListener;
import org.eclipse.skalli.services.event.EventService;
import org.eclipse.skalli.services.user.UserService;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link UserService} accessing an LDAP server.
 * It relies on the {@link ConfigurationService} for LDAP authentication
 * and location information.
 */
public class LDAPUserComponent implements UserService, EventListener<EventConfigUpdate> {

    private static final Logger LOG = LoggerFactory.getLogger(LDAPUserComponent.class);

    private static final int DEFAULT_CACHE_SIZE = 100;

    private Cache<String, User> cache;

    private EventService eventService;
    private ConfigurationService configurationService;

    protected void activate(ComponentContext context) {
        LOG.info(MessageFormat.format("[UserService][LDAP] {0} : activated", //$NON-NLS-1$
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
        eventService.registerListener(EventConfigUpdate.class, this);
        initializeCache();
    }

    protected void deactivate(ComponentContext context) {
        LOG.info(MessageFormat.format("[UserService][LDAP] {0} : deactivated", //$NON-NLS-1$
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void bindEventService(EventService eventService) {
        this.eventService = eventService;
    }

    protected void unbindEventService(EventService eventService) {
        this.eventService = null;
    }

    protected void bindConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
        initializeCache();
    }

    protected void unbindConfigurationService(ConfigurationService configurationService) {
        this.configurationService = null;
    }

    private synchronized void initializeCache() {
        int cacheSize = DEFAULT_CACHE_SIZE;
        if (configurationService != null) {
            LDAPConfig config = configurationService.readCustomization(LDAPResource.KEY, LDAPConfig.class);
            if (config != null) {
                cacheSize = NumberUtils.toInt(config.getCacheSize(), DEFAULT_CACHE_SIZE);
            }
        }
        cache = new GroundhogCache<String, User>(cacheSize, cache);
    }

    private LDAPClient getLDAPClient() {
        if (configurationService != null) {
            LDAPConfig config = configurationService.readCustomization(LDAPResource.KEY, LDAPConfig.class);
            if (config != null) {
                return new LDAPClient(config);
            }
        }
        return null;
    }

    @Override
    public synchronized List<User> findUser(String searchText) {
        LDAPClient ldap = getLDAPClient();
        if (ldap == null) {
            return Collections.emptyList();
        }
        List<User> users = ldap.searchUserByName(searchText);
        for (User user : users) {
            if (user != null) {
                cache.put(StringUtils.lowerCase(user.getUserId()), user);
            }
        }
        return users;
    }

    @Override
    public synchronized User getUserById(String userId) {
        // look in cache
        String lowerUserId = StringUtils.lowerCase(userId);
        User user = cache.get(StringUtils.lowerCase(lowerUserId));
        if (user == null) {
            // get from server
            LDAPClient ldap = getLDAPClient();
            if (ldap == null) {
                return null;
            }
            user = ldap.searchUserById(userId);
            if (user != null) {
                cache.put(StringUtils.lowerCase(user.getUserId()), user);
            }
        }
        return user;
    }

    @Override
    public synchronized List<User> getUsers() {
        return new LinkedList<User>(cache.values());
    }

    @Override
    public Set<User> getUsersById(Set<String> userIds) {
        Set<User> users = new HashSet<User>(userIds.size());
        Set<String> userIdsToSearch = new HashSet<String>(0);

        // look in cache
        for (String userId : userIds) {
            User user = cache.get(StringUtils.lowerCase(userId));
            if (user != null) {
                users.add(user);
            } else {
                userIdsToSearch.add(userId);
            }
        }
        // search unknown in ldap
        if (userIdsToSearch.size() > 0) {
            LDAPClient ldap = getLDAPClient();
            if (ldap == null) {
                return users;
            }
            Set<User> ldapUsers = ldap.searchUsersByIds(userIdsToSearch);
            for (User user : ldapUsers) {
                if (user != null) {
                    cache.put(StringUtils.lowerCase(user.getUserId()), user);
                    users.add(user);
                }
            }
        }
        return users;
    }

    @Override
    public synchronized void onEvent(EventConfigUpdate event) {
        initializeCache();
    }

}
