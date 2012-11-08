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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.SizeLimitExceededException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.model.User;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.jndi.JNDIConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LDAPClient {

    private final static Logger LOG = LoggerFactory.getLogger(LDAPClient.class);

    private static final String LDAPS_SCHEME = "ldaps:"; //$NON-NLS-1$
    private static final String SIMPLE_AUTHENTICATION = "simple"; //$NON-NLS-1$
    private static final String JNDI_SOCKET_FACTORY = "java.naming.ldap.factory.socket"; //$NON-NLS-1$
    private static final String DEFAULT_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory"; //$NON-NLS-1$
    private static final String USE_CONNECTION_POOLING = "com.sun.jndi.ldap.connect.pool"; //$NON-NLS-1$
    private static final String CONNECT_POOL_PROTOCOLS = "com.sun.jndi.ldap.connect.pool.protocol"; //$NON-NLS-1$

    static {
        // heaven knows why this is a system property while all other params can be set per context...
        System.setProperty(CONNECT_POOL_PROTOCOLS, "plain ssl"); //$NON-NLS-1$
    }

    private LDAPConfig config;

    public LDAPClient(LDAPConfig config) {
        this.config = config;
    }

    private LdapContext getLdapContext() throws NamingException, AuthenticationException {
        if (config == null) {
            throw new NamingException("LDAP not configured");
        }
        if (StringUtils.isBlank(config.getProviderUrl())) {
            throw new NamingException("No LDAP server available");
        }
        if (StringUtils.isBlank(config.getUsername()) || StringUtils.isBlank(config.getPassword())) {
            throw new AuthenticationException("No LDAP credentials available");
        }
        String ctxFactory = config.getCtxFactory();
        if (StringUtils.isBlank(ctxFactory)) {
            ctxFactory = DEFAULT_CONTEXT_FACTORY;
        }
        String authentication = config.getAuthentication();
        if (StringUtils.isBlank(authentication)) {
            authentication = SIMPLE_AUTHENTICATION;
        }

        Hashtable<String, Object> env = new Hashtable<String, Object>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, ctxFactory);
        env.put(Context.PROVIDER_URL, config.getProviderUrl());
        env.put(Context.SECURITY_PRINCIPAL, config.getUsername());
        env.put(Context.SECURITY_CREDENTIALS, config.getPassword());
        env.put(Context.SECURITY_AUTHENTICATION, authentication);
        if (StringUtils.isNotBlank(config.getReferral())) {
            env.put(Context.REFERRAL, config.getReferral());
        }
        if (config.getProviderUrl().startsWith(LDAPS_SCHEME)) {
            env.put(Context.SECURITY_PROTOCOL, "ssl"); //$NON-NLS-1$
            if (config.isSslNoVerify()) {
                env.put(JNDI_SOCKET_FACTORY, LDAPTrustAllSocketFactory.class.getName());
            }
        }
        // Gemini-specific properties
        env.put(JNDIConstants.BUNDLE_CONTEXT,
                FrameworkUtil.getBundle(LDAPClient.class).getBundleContext());

        // com.sun.jndi.ldap.LdapCtxFactory specific properties
        env.put(USE_CONNECTION_POOLING, "true"); //$NON-NLS-1$

        // extremly ugly classloading workaround:
        // com.sun.jndi.ldap.LdapCtxFactory uses Class.forName() to load the socket factory, shame on them!
        InitialLdapContext ctx = null;
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(LDAPTrustAllSocketFactory.class.getClassLoader());
            ctx = new InitialLdapContext(env, null);
        } finally {
            if (classloader != null) {
                Thread.currentThread().setContextClassLoader(classloader);
            }
        }
        return ctx;
    }

    private void closeQuietly(LdapContext ldap) {
        if (ldap != null) {
            try {
                ldap.close();
            } catch (NamingException e) {
                LOG.error("Failed to close LDAP connection", e);
            }
        }
    }

    public User searchUserById(String userId) {
        LdapContext ldap = null;
        try {
            ldap = getLdapContext();
            return searchUserById(ldap, userId);
        }  catch (Exception e) {
            LOG.debug(MessageFormat.format("Failed to retrieve user ''{0}''", userId), e);
            return new User(userId);
        } finally {
            closeQuietly(ldap);
        }
    }

    public Set<User> searchUsersByIds(Set<String> userIds) {
        LdapContext ldap = null;
        try {
            Set<User> ret = new HashSet<User>();
            ldap = getLdapContext();
            for (String userId : userIds) {
                ret.add(searchUserById(ldap, userId));
            }
            return ret;
        } catch (Exception e) {
            LOG.debug(MessageFormat.format("Failed to retrieve users {0}",
                    CollectionUtils.toString(userIds, ',')), e);
            return Collections.emptySet();
        } finally {
            closeQuietly(ldap);
        }
    }

    public List<User> searchUserByName(String name) {
        LdapContext ldap = null;
        try {
            ldap = getLdapContext();
            return searchUserByName(ldap, name);
        } catch (Exception e) {
            LOG.debug(MessageFormat.format("Failed to search user ''{0}''", name), e);
            return Collections.emptyList();
        } finally {
            closeQuietly(ldap);
        }
    }

    private User searchUserById(LdapContext ldap, String userId) throws NamingException {
        SearchControls sc = getSearchControls();
        NamingEnumeration<SearchResult> results = ldap.search(config.getBaseDN(),
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
        return new User(userId);
    }

    private List<User> searchUserByName(LdapContext ldap, String name) throws NamingException {
        List<User> ret = new ArrayList<User>(0);
        try {
            boolean somethingAdded = false;
            SearchControls sc = new SearchControls();
            String[] parts = StringUtils.split(NormalizeUtil.normalize(name), " ,"); //$NON-NLS-1$
            if (parts.length == 1) {
                somethingAdded = search(parts[0], ret, ldap, sc);
            }
            else if (parts.length > 1) {
                // givenname surname ('Michael Ochmann'), or surname givenname('Ochmann, Michael')
                NamingEnumeration<SearchResult> results = ldap.search(
                        config.getBaseDN(),
                        MessageFormat.format("(&(objectClass=user)(givenName={0}*)(sn={1}*))", //$NON-NLS-1$
                                parts[0], parts[1]), sc);
                somethingAdded |= addLDAPSearchResult(ret, results);
                results = ldap.search(
                        config.getBaseDN(),
                        MessageFormat.format("(&(objectClass=user)(sn={0}*)(givenName={1}*))", //$NON-NLS-1$
                                parts[0], parts[1]), sc);
                somethingAdded |= addLDAPSearchResult(ret, results);
                // givenname initial surname, e.g. 'Michael R. Ochmann'
                if (parts.length > 2) {
                    results = ldap.search(
                            config.getBaseDN(),
                            MessageFormat.format("(&(objectClass=user)(givenName={0}*)(sn={1}*))", //$NON-NLS-1$
                                    parts[0], parts[2]), sc);
                    somethingAdded |= addLDAPSearchResult(ret, results);
                    results = ldap.search(
                            config.getBaseDN(),
                            MessageFormat.format("(&(objectClass=user)(sn={0}*)(givenName={1}*))", //$NON-NLS-1$
                                    parts[0], parts[2]), sc);
                    somethingAdded |= addLDAPSearchResult(ret, results);
                }
                if (!somethingAdded) {
                    // try to match each part individually
                    for (int i = 0; i < parts.length; ++i) {
                        somethingAdded = search(parts[i], ret, ldap, sc);
                    }
                }
            }
        } catch (SizeLimitExceededException e) {
            // 1000 is good enough at the moment for this use case...
            LOG.warn(MessageFormat.format("LDAP query size limit exceeded while searching for ''{0}''", name), e);
        }
        return ret;
    }

    private boolean search(String s, List<User> ret, LdapContext ldap, SearchControls sc) throws NamingException {
        // try a match with surname*
        NamingEnumeration<SearchResult> results = ldap.search(
                config.getBaseDN(),
                MessageFormat.format("(&(objectClass=user)(|(sn={0}*)(givenName={1}*)))", s, s), sc); //$NON-NLS-1$
        boolean somethingAdded = addLDAPSearchResult(ret, results);
        if (!somethingAdded) {
            // try a match with the account name and mail address
            results = ldap.search(
                    config.getBaseDN(),
                    MessageFormat.format("(&(objectClass=user)(sAMAccountName={0}*))", s), sc); //$NON-NLS-1$
            somethingAdded |= addLDAPSearchResult(ret, results);
            if (!somethingAdded) {
                // try to match surname~= or givenname~=
                results = ldap.search(config.getBaseDN(),
                        MessageFormat.format("(&(objectClass=user)(|(sn~={0})(givenName~={1})))", s, s), sc); //$NON-NLS-1$
                somethingAdded |= addLDAPSearchResult(ret, results);
                if (!somethingAdded) {
                    results = ldap.search(config.getBaseDN(),
                            MessageFormat.format("(&(objectClass=user)(mail={0}*))", s), sc); //$NON-NLS-1$
                    somethingAdded |= addLDAPSearchResult(ret, results);
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
        if ("base".equalsIgnoreCase(config.getSearchScope())) {
            sc.setSearchScope(SearchControls.OBJECT_SCOPE);
        } else if ("onelevel".equalsIgnoreCase(config.getSearchScope())) {
            sc.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        } else if ("subtree".equalsIgnoreCase(config.getSearchScope())) {
            sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
        }
        sc.setReturningAttributes(LDAPAttributeNames.getAll());
        return sc;
    }

}
