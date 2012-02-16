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
import org.eclipse.skalli.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LDAPClient {

    private final static Logger LOG = LoggerFactory.getLogger(LDAPClient.class);

    private final String factory;
    private final String hostname;
    private final String password;
    private final String username;
    private final String usersGroup;

    public LDAPClient(final String factory, final String hostname, final String username,
            final String password, final String usersGroup) {
        this.factory = factory;
        this.hostname = hostname;
        this.username = username;
        this.password = password;
        this.usersGroup = usersGroup;
    }

    private LdapContext getConnection() throws AuthException {
        try {
            if (StringUtils.isBlank(password) || StringUtils.isBlank(username)
                    || StringUtils.isBlank(hostname) || StringUtils.isBlank(factory)) {
                throw new AuthException("LDAP not configured");
            }
            Hashtable<String, String> env = new Hashtable<String, String>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, factory);
            env.put(Context.PROVIDER_URL, hostname); // SET YOUR SERVER AND STARTING
            // CONTEXT HERE
            env.put(Context.SECURITY_PRINCIPAL, username); // SET USER
            env.put(Context.SECURITY_CREDENTIALS, password); // SET PASSWORD HERE
            env.put(Context.SECURITY_AUTHENTICATION, "simple");
            InitialLdapContext ctx = new InitialLdapContext(env, null);
            return ctx;
        } catch (AuthenticationException e) {
            throw new AuthException(e);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    public User searchUserById(String userId) {
        LdapContext ldap = null;
        try {
            try {
                ldap = getConnection();
            } catch (AuthException e1) {
                LOG.debug("Could not authenticate to LDAP");
                return User.createUserWithoutDetails(userId);
            }
            try {
                return searchUserById(ldap, userId);
            } catch (NamingException e) {
                throw new RuntimeException(e);
            }
        } finally {
            if (ldap != null) {
                try {
                    ldap.close();
                } catch (NamingException e) {
                    // Makes no sense while closing...
                    throw new RuntimeException();
                }
            }
        }
    }

    public Set<User> searchUsersByIds(Set<String> userIds) {
        LdapContext ldap = null;
        Set<User> ret = new HashSet<User>();
        try {
            try {
                ldap = getConnection();
            } catch (AuthException e1) {
                LOG.debug("Could not authenticate to LDAP");
                return ret;
            }
            try {
                for (String userId : userIds) {
                    ret.add(searchUserById(ldap, userId));
                }
                return ret;
            } catch (NamingException e) {
                throw new RuntimeException(e);
            }
        } finally {
            if (ldap != null) {
                try {
                    ldap.close();
                } catch (NamingException e) {
                    // Makes no sense while closing...
                    throw new RuntimeException();
                }
            }
        }
    }

    public List<User> searchUserByName(String name) {
        LdapContext ldap = null;
        try {
            try {
                ldap = getConnection();
            } catch (AuthException e1) {
                return Collections.emptyList();
            }
            try {
                return searchUserByName(ldap, name);
            } catch (NamingException e) {
                throw new RuntimeException(e);
            }
        } finally {
            if (ldap != null) {
                try {
                    ldap.close();
                } catch (NamingException e) {
                    // Makes no sense while closing...
                    throw new RuntimeException();
                }
            }
        }
    }

    private User searchUserById(LdapContext ldap, String userId) throws NamingException {
        SearchControls sc = new SearchControls();
        sc.setReturningAttributes(LDAPAttributeNames.getAll());
        NamingEnumeration<SearchResult> results = ldap.search(usersGroup,
                "(&(objectClass=user)(sAMAccountName=" + userId + "))", sc);
        while (results != null && results.hasMore()) {
            SearchResult entry = results.next();
            User user = processEntry(entry);
            if (user != null) {
                LOG.debug("Success reading from LDAP: " + user.getUserId() + ", " + user.getDisplayName()
                        + " <" + user.getEmail() + ">");
                return user;
            }
        }
        return User.createUserWithoutDetails(userId);
    }

    private List<User> searchUserByName(LdapContext ldap, String name) throws NamingException {
        List<User> ret = new ArrayList<User>(0);
        try {
            boolean somethingAdded = false;
            SearchControls sc = new SearchControls();
            sc.setReturningAttributes(LDAPAttributeNames.getAll());

            String[] parts = StringUtils.split(NormalizeUtil.normalize(name), " ,");
            if (parts.length == 1) {
                somethingAdded = search(parts[0], ret, ldap, sc);
            }
            else if (parts.length > 1) {
                // givenname surname ('Michael Ochmann'), or surname givenname('Ochmann, Michael')
                NamingEnumeration<SearchResult> results = ldap.search(
                        usersGroup,
                        "(&(objectClass=user)(givenName=" + parts[0] + "*)(sn=" + parts[1] + "*))", sc);
                somethingAdded |= addLDAPSearchResult(ret, results);
                results = ldap.search(
                        usersGroup,
                        "(&(objectClass=user)(sn=" + parts[0] + "*)(givenName=" + parts[1] + "*))", sc);
                somethingAdded |= addLDAPSearchResult(ret, results);
                // givenname initial surname, e.g. 'Michael R. Ochmann'
                if (parts.length > 2) {
                    results = ldap.search(
                            usersGroup,
                            "(&(objectClass=user)(givenName=" + parts[0] + "*)(sn=" + parts[2] + "*))", sc);
                    somethingAdded |= addLDAPSearchResult(ret, results);
                    results = ldap.search(
                            usersGroup,
                            "(&(objectClass=user)(sn=" + parts[0] + "*)(givenName=" + parts[2] + "*))", sc);
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
            LOG.warn("LDAP query size limit exceeded while searching for '" + name + "'", e);
        }
        return ret;
    }

    private boolean search(String s, List<User> ret, LdapContext ldap, SearchControls sc) throws NamingException {
        // try a match with surname*
        NamingEnumeration<SearchResult> results = ldap.search(
                usersGroup,
                "(&(objectClass=user)(|(sn=" + s + "*)(givenName=" + s + "*)))", sc);
        boolean somethingAdded = addLDAPSearchResult(ret, results);
        if (!somethingAdded) {
            // try a match with the account name and mail address
            results = ldap.search(
                    usersGroup,
                    "(&(objectClass=user)(sAMAccountName=" + s + "*))", sc);
            somethingAdded |= addLDAPSearchResult(ret, results);
            if (!somethingAdded) {
                // try to match surname~= or givenname~=
                results = ldap.search(usersGroup,
                        "(&(objectClass=user)(|(sn~=" + s + ")(givenName~=" + s + ")))", sc);
                somethingAdded |= addLDAPSearchResult(ret, results);
                if (!somethingAdded) {
                    results = ldap.search(usersGroup,
                            "(&(objectClass=user)(mail=" + s + "*))", sc);
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
                LOG.debug("Success reading from LDAP: " + user.getUserId() + ", " + user.getDisplayName()
                        + " <" + user.getEmail() + ">");
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

}
