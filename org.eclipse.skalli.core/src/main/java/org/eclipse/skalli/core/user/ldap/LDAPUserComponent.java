/*******************************************************************************
 * Copyright (c) 2010-2016 SAP AG and others.
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.SizeLimitExceededException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.commons.VisibleForTesting;
import org.eclipse.skalli.core.cache.Cache;
import org.eclipse.skalli.core.cache.GroundhogCache;
import org.eclipse.skalli.core.user.NormalizeUtil;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.services.configuration.ConfigurationService;
import org.eclipse.skalli.services.configuration.Configurations;
import org.eclipse.skalli.services.configuration.EventConfigUpdate;
import org.eclipse.skalli.services.event.EventListener;
import org.eclipse.skalli.services.event.EventService;
import org.eclipse.skalli.services.user.UserService;
import org.eclipse.skalli.services.user.ldap.LdapContextProvider;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link UserService} accessing an LDAP server.
 */
public class LDAPUserComponent implements UserService, EventListener<EventConfigUpdate> {

    private static final Logger LOG = LoggerFactory.getLogger(LDAPUserComponent.class);

    private static final String DEFAULT_POOL_PROTOCOLS = "plain ssl"; //$NON-NLS-1$
    private static final int DEFAULT_POOL_MAXSIZE = 5;
    private static final long DEFAULT_POOL_TIMEOUT = 10000L;
    private static final int DEFAULT_CACHE_SIZE = 100;

    private static final String CONNECT_POOL_PROTOCOLS = "com.sun.jndi.ldap.connect.pool.protocol"; //$NON-NLS-1$
    private static final String CONNECT_POOL_DEBUG = "com.sun.jndi.ldap.connect.pool.debug"; //$NON-NLS-1$
    private static final String CONNECT_POOL_MAXSIZE = "com.sun.jndi.ldap.connect.pool.maxsize"; //$NON-NLS-1$
    private static final String CONNECT_POOL_TIMEOUT = "com.sun.jndi.ldap.connect.pool.timeout"; //$NON-NLS-1$

    private ConcurrentHashMap<String,LdapContextProvider> ctxProviders =
            new ConcurrentHashMap<String, LdapContextProvider>();

    private LdapContextProvider ctxProvider;
    private String destination;
    private String baseDN;
    private String searchScope;

    private Cache<String, User> cache;

    protected void activate(ComponentContext context) {
        // define properties for the LDAP connection pool globally, but let the individual context providers
        // decide whether to use the connection pool or not; note, for some weird reason the pool properties
        // are system properties and cannot be set per context
        System.setProperty(CONNECT_POOL_PROTOCOLS, DEFAULT_POOL_PROTOCOLS);
        System.setProperty(CONNECT_POOL_MAXSIZE, Integer.toString(DEFAULT_POOL_MAXSIZE));
        System.setProperty(CONNECT_POOL_TIMEOUT, Long.toString(DEFAULT_POOL_TIMEOUT));
        if (LOG.isDebugEnabled()) {
            System.setProperty(CONNECT_POOL_DEBUG, "fine"); //$NON-NLS-1$
        }

        LOG.info(MessageFormat.format("[UserService][LDAP] {0} : activated", //$NON-NLS-1$
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
        initialize();
    }

    protected void deactivate(ComponentContext context) {
        LOG.info(MessageFormat.format("[UserService][LDAP] {0} : deactivated", //$NON-NLS-1$
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void bindEventService(EventService eventService) {
        eventService.registerListener(EventConfigUpdate.class, this);
    }

    protected void unbindEventService(EventService eventService) {
        eventService.unregisterListener(EventConfigUpdate.class, this);
    }

    protected void bindConfigurationService(ConfigurationService configurationService) {
        initialize();
    }

    protected void unbindConfigurationService(ConfigurationService configurationService) {
    }

    protected void bindLdapContextProvider(LdapContextProvider ctxProvider) {
        ctxProviders.put(ctxProvider.getId(), ctxProvider);
        initialize();
    }

    protected void unbindLdapContextProvider(LdapContextProvider ctxProvider) {
        ctxProviders.remove(ctxProvider.getId());
    }

    @VisibleForTesting
    protected synchronized void initialize() {
        int cacheSize = DEFAULT_CACHE_SIZE;
        LDAPConfig ldapConfig = Configurations.getConfiguration(LDAPConfig.class);
        if (ldapConfig != null) {
            String providerId = StringUtils.isNotBlank(ldapConfig.getProviderId())
                    ? ldapConfig.getProviderId()
                    : "default"; //$NON-NLS-1$
            ctxProvider = ctxProviders.get(providerId);
            destination = ldapConfig.getDestination();
            baseDN = ldapConfig.getBaseDN();
            searchScope = ldapConfig.getSearchScope();
            cacheSize = NumberUtils.toInt(ldapConfig.getCacheSize(), DEFAULT_CACHE_SIZE);
        }
        cache = new GroundhogCache<String, User>(cacheSize, cache);
    }

    @Override
    public String getType() {
        return "ldap"; //$NON-NLS-1$
    }

    @Override
    public synchronized List<User> findUser(String searchText) {
        if (StringUtils.isBlank(searchText)) {
            return Collections.emptyList();
        }
        if (ctxProvider == null) {
            return Collections.emptyList();
        }
        List<User> users = searchUserByName(searchText);
        for (User user : users) {
            if (user != null) {
                cache.put(StringUtils.lowerCase(user.getUserId()), user);
            }
        }
        return users;
    }

    @Override
    public synchronized User getUserById(String userId) {
        if (StringUtils.isBlank(userId)) {
            return null;
        }
        String lowerCaseUserId = userId.toLowerCase(Locale.ENGLISH);
        User user = cache.get(lowerCaseUserId);
        if (user == null) {
            if (ctxProvider == null) {
                return null;
            }
            user = searchUserById(userId);
            if (user != null) {
                cache.put(lowerCaseUserId, user);
            }
        }
        return user;
    }

    @Override
    public synchronized List<User> getUsers() {
        return new LinkedList<User>(cache.values());
    }

    @Override
    public synchronized Set<User> getUsersById(Set<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptySet();
        }
        Set<User> users = new HashSet<User>(userIds.size());
        Set<String> notFoundInCache = new HashSet<String>();
        for (String userId : userIds) {
            if (StringUtils.isBlank(userId)) {
                continue;
            }
            String lowerCaseUserId = userId.toLowerCase(Locale.ENGLISH);
            User user = cache.get(lowerCaseUserId);
            if (user != null) {
                users.add(user);
            } else {
                notFoundInCache.add(lowerCaseUserId);
            }
        }
        if (notFoundInCache.size() > 0) {
            if (ctxProvider == null) {
                return users;
            }
            Set<User> ldapUsers = searchUsersByIds(notFoundInCache);
            for (User user : ldapUsers) {
                if (user != null) {
                    String userId = user.getUserId();
                    if (StringUtils.isNotBlank(userId)) {
                        cache.put(userId.toLowerCase(Locale.ENGLISH), user);
                        users.add(user);
                    }
                }
            }
        }
        return users;
    }

    @Override
    public synchronized void onEvent(EventConfigUpdate event) {
        if (LDAPConfig.class.equals(event.getConfigClass())) {
            initialize();
        }
    }

    private User searchUserById(String userId) {
        LdapContext ctx = null;
        try {
            ctx = ctxProvider.getLdapContext(destination);
            return searchUserById(ctx, userId);
        }  catch (Exception e) {
            LOG.debug(MessageFormat.format("Failed to retrieve user ''{0}''", userId), e);
            return new User(userId);
        } finally {
            closeQuietly(ctx);
        }
    }

    private Set<User> searchUsersByIds(Set<String> userIds) {
        LdapContext ctx = null;
        try {
            if (ctxProvider == null) {
                return Collections.emptySet();
            }
            ctx = ctxProvider.getLdapContext(destination);
            Set<User> ret = new HashSet<User>();
            for (String userId : userIds) {
                ret.add(searchUserById(ctx, userId));
            }
            return ret;
        } catch (Exception e) {
            LOG.debug(MessageFormat.format("Failed to retrieve users {0}",
                    CollectionUtils.toString(userIds, ',')), e);
            return Collections.emptySet();
        } finally {
            closeQuietly(ctx);
        }
    }

    private List<User> searchUserByName(String name) {
        LdapContext ctx = null;
        try {
            if (ctxProvider == null) {
                return Collections.emptyList();
            }
            ctx = ctxProvider.getLdapContext(destination);
            return searchUserByName(ctx, name);
        } catch (Exception e) {
            LOG.debug(MessageFormat.format("Failed to search user ''{0}''", name), e);
            return Collections.emptyList();
        } finally {
            closeQuietly(ctx);
        }
    }

    private User searchUserById(LdapContext ctx, String userId) throws NamingException {
        SearchControls sc = getSearchControls();
        NamingEnumeration<SearchResult> results = null;
        try {
            results = ctx.search(baseDN,
                MessageFormat.format("(&(objectClass=user)(sAMAccountName={0}))", userId), sc); //$NON-NLS-1$
            while (results != null && results.hasMore()) {
                SearchResult entry = results.next();
                User user = processEntry(entry);
                if (user != null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(MessageFormat.format("Success reading from LDAP: {0}, {1} <{2}>", //$NON-NLS-1$
                                user.getUserId(), user.getDisplayName(), user.getEmail()));
                    }
                    return user;
                }
            }
        } finally {
            closeQuietly(results);
        }
        return new User(userId);
    }

    private List<User> searchUserByName(LdapContext ctx, String name) throws NamingException {
        List<User> ret = new ArrayList<User>(0);
        try {
            boolean somethingAdded = false;
            SearchControls sc = getSearchControls();
            String[] parts = StringUtils.split(NormalizeUtil.normalize(name), " ,"); //$NON-NLS-1$
            if (parts.length == 1) {
                somethingAdded = search(parts[0], ret, ctx, sc);
            }
            else if (parts.length > 1) {
                // givenname surname ('Michael Ochmann'), or surname givenname('Ochmann, Michael')
                NamingEnumeration<SearchResult> results = null;
                try {
                    results = ctx.search(baseDN,
                        MessageFormat.format("(&(objectClass=user)(givenName={0}*)(sn={1}*))", //$NON-NLS-1$
                                parts[0], parts[1]), sc);
                    somethingAdded |= addLDAPSearchResult(ret, results);
                } finally {
                    closeQuietly(results);
                }
                try {
                    results = ctx.search(baseDN,
                            MessageFormat.format("(&(objectClass=user)(sn={0}*)(givenName={1}*))", //$NON-NLS-1$
                                    parts[0], parts[1]), sc);
                    somethingAdded |= addLDAPSearchResult(ret, results);
                } finally {
                    closeQuietly(results);
                }
                // givenname initial surname, e.g. 'Michael R. Ochmann'
                if (parts.length > 2) {
                    try {
                        results = ctx.search(baseDN,
                                MessageFormat.format("(&(objectClass=user)(givenName={0}*)(sn={1}*))", //$NON-NLS-1$
                                        parts[0], parts[2]), sc);
                        somethingAdded |= addLDAPSearchResult(ret, results);
                    } finally {
                        closeQuietly(results);
                    }
                    try {
                        results = ctx.search(baseDN,
                                MessageFormat.format("(&(objectClass=user)(sn={0}*)(givenName={1}*))", //$NON-NLS-1$
                                        parts[0], parts[2]), sc);
                        somethingAdded |= addLDAPSearchResult(ret, results);
                    } finally {
                        closeQuietly(results);
                    }
                }
                if (!somethingAdded) {
                    // try to match each part individually
                    for (int i = 0; i < parts.length; ++i) {
                        somethingAdded = search(parts[i], ret, ctx, sc);
                    }
                }
            }
        } catch (SizeLimitExceededException e) {
            // 1000 is good enough at the moment for this use case...
            LOG.warn(MessageFormat.format("LDAP query size limit exceeded while searching for ''{0}''", name), e);
        }
        return ret;
    }

    private boolean search(String s, List<User> ret, LdapContext ctx, SearchControls sc) throws NamingException {
        // try a match with surname*
        boolean somethingAdded = false;
        NamingEnumeration<SearchResult> results = null;
        try {
            results = ctx.search(baseDN,
                    MessageFormat.format("(&(objectClass=user)(|(sn={0}*)(givenName={1}*)))", s, s), sc); //$NON-NLS-1$
            somethingAdded = addLDAPSearchResult(ret, results);
        } finally {
            closeQuietly(results);
        }
        if (!somethingAdded) {
            try {
                // try a match with the account name and mail address
                results = ctx.search(baseDN,
                        MessageFormat.format("(&(objectClass=user)(sAMAccountName={0}*))", s), sc); //$NON-NLS-1$
                somethingAdded |= addLDAPSearchResult(ret, results);
            } finally {
                closeQuietly(results);
            }
            if (!somethingAdded) {
                try {
                    // try to match surname~= or givenname~=
                    results = ctx.search(baseDN,
                            MessageFormat.format("(&(objectClass=user)(|(sn~={0})(givenName~={1})))", s, s), sc); //$NON-NLS-1$
                    somethingAdded |= addLDAPSearchResult(ret, results);
                } finally {
                    closeQuietly(results);
                }
                if (!somethingAdded) {
                    try {
                        results = ctx.search(baseDN,
                                MessageFormat.format("(&(objectClass=user)(mail={0}*))", s), sc); //$NON-NLS-1$
                        somethingAdded |= addLDAPSearchResult(ret, results);
                    } finally {
                        closeQuietly(results);
                    }
                }
            }
        }
        return somethingAdded;
    }

    // Iterate over a batch of search results sent by the server
    private boolean addLDAPSearchResult(List<User> users, NamingEnumeration<SearchResult> results)
            throws NamingException {
        boolean somethingAdded = false;
        while (results != null && results.hasMore()) {
            // Display an entry
            SearchResult entry = results.next();
            User user = processEntry(entry);
            if (user != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format("Success reading from LDAP: {0}, {1} <{2}>",
                            user.getUserId(), user.getDisplayName(), user.getEmail()));
                }
                users.add(user);
                somethingAdded = true;
            }
        }
        return somethingAdded;
    }

    private String getStringValue(Attributes attributes, LDAPAttributeNames attributeName)
            throws NamingException {
        String ret = null;
        Attribute attribute = attributes.get(attributeName.getLdapKey());
        if (attribute != null) {
            for (int i = 0; i < attribute.size(); i++) {
                ret = (String) attribute.get(i);
            }
        }
        return ret;
    }

    private User processEntry(SearchResult entry) throws NamingException {
        User user = new User();
        Attributes attrs = entry.getAttributes();
        Attribute attrBits = attrs.get(LDAPAttributeNames.BITS.getLdapKey());
        if (attrBits != null) {
            long lng = Long.parseLong(attrBits.get(0).toString());
            long secondBit = lng & 2; // get bit 2
            if (secondBit != 0) {
                // User not enabled
                return null;
            }
        }
        user.setUserId(StringUtils.lowerCase(getStringValue(attrs, LDAPAttributeNames.USERID)));
        user.setFirstname(getStringValue(attrs, LDAPAttributeNames.FIRSTNAME));
        user.setLastname(getStringValue(attrs, LDAPAttributeNames.LASTNAME));
        user.setEmail(getStringValue(attrs, LDAPAttributeNames.EMAIL));
        user.setTelephone(getStringValue(attrs, LDAPAttributeNames.TELEPHONE));
        user.setMobile(getStringValue(attrs, LDAPAttributeNames.MOBILE));
        user.setRoom(getStringValue(attrs, LDAPAttributeNames.ROOM));
        user.setLocation(getStringValue(attrs, LDAPAttributeNames.LOCATION));
        user.setDepartment(getStringValue(attrs, LDAPAttributeNames.DEPARTMENT));
        user.setCompany(getStringValue(attrs, LDAPAttributeNames.COMPANY));
        user.setSip(getStringValue(attrs, LDAPAttributeNames.SIP));
        return user;
    }

    @SuppressWarnings("nls")
    private SearchControls getSearchControls() {
        SearchControls sc = new SearchControls();
        if ("base".equalsIgnoreCase(searchScope)) {
            sc.setSearchScope(SearchControls.OBJECT_SCOPE);
        } else if ("onelevel".equalsIgnoreCase(searchScope)) {
            sc.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        } else if ("subtree".equalsIgnoreCase(searchScope)) {
            sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
        }
        sc.setReturningAttributes(LDAPAttributeNames.getAll());
        return sc;
    }

    private void closeQuietly(LdapContext ctx) {
        if (ctx != null) {
            try {
                ctx .close();
            } catch (NamingException e) {
                LOG.error("Failed to close LDAP connection", e);
            }
        }
    }

    private void closeQuietly(NamingEnumeration<?> result) {
        if (result != null) {
            try {
                result.close();
            } catch (NamingException e) {
                LOG.error("Failed to close LDAP result set", e);
            }
        }
    }
}
