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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.core.internal.cache.Cache;
import org.eclipse.skalli.core.internal.cache.GroundhogCache;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.configuration.ConfigurationService;
import org.eclipse.skalli.services.event.EventConfigUpdate;
import org.eclipse.skalli.services.event.EventListener;
import org.eclipse.skalli.services.event.EventService;
import org.eclipse.skalli.services.user.UserService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link UserService} accessing an LDAP server.
 * It relies on the {@link ConfigurationService} for LDAP authentication
 * and localtion information.<br>
 * This service implementation is the default and is enabled during
 * portal startup automatically.
 */
public class LDAPUserServiceImpl implements UserService, EventListener<EventConfigUpdate> {

    private static final Logger LOG = LoggerFactory.getLogger(LDAPUserServiceImpl.class);
    private final Cache<String, User> cache = new GroundhogCache<String, User>(2000);
    private EventService eventService;

    protected void activate(ComponentContext context) {
        eventService.registerListener(EventConfigUpdate.class, this);
        LOG.info("LDAP User Service activated");
    }

    protected void deactivate(ComponentContext context) {
        LOG.info("LDAP User Service deactivated");
    }

    protected void bindEventService(EventService eventService) {
        this.eventService = eventService;
    }

    protected void unbindEventService(EventService eventService) {
        this.eventService = null;
    }

    private LDAPClient getLDAPClient() {
        String password = null;
        String username = null;
        String hostname = null;
        String factory = null;
        String usersGroup = null;
        ConfigurationService configService = Services.getService(ConfigurationService.class);
        if (configService != null) {
            password = configService.readString(ConfigKeyLDAP.PASSWORD);
            username = configService.readString(ConfigKeyLDAP.USERNAME);
            hostname = configService.readString(ConfigKeyLDAP.HOSTNAME);
            factory = configService.readString(ConfigKeyLDAP.FACTORY);
            usersGroup = configService.readString(ConfigKeyLDAP.USERS_GROUP);
        } else {
            LOG.warn("Failed to read LDAP configuration - no instance of "
                    + ConfigurationService.class.getName() + "available. Either provide a suitable "
                    + "configuration service or switch to another user service, for example Local User Service.");
        }
        LDAPClient ldapClient = new LDAPClient(factory, hostname, username, password, usersGroup);
        return ldapClient;
    }

    @Override
    public synchronized List<User> findUser(String searchText) {
        // get from server
        LDAPClient ldap = getLDAPClient();
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
            user = ldap.searchUserById(userId);
            if (user != null) {
                cache.put(StringUtils.lowerCase(user.getUserId()), user);
            }
        }
        return user;
    }

    @Override
    public synchronized List<User> getUsers() {
        return new LinkedList<User>(cache.getAll());
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
        cache.clear();
    }

}
