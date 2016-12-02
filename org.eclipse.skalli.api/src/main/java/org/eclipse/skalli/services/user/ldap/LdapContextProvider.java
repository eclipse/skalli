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
package org.eclipse.skalli.services.user.ldap;

import javax.naming.AuthenticationException;
import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

import org.eclipse.skalli.services.user.UserService;

/**
 * Extension point for LDAP-based {@link UserService} implementations.
 */
public interface LdapContextProvider {

    /**
     * Returns a unique identifier for this LDAP context provider.
     */
    public String getId();

    /**
     * Returns a pre-initialized LDAP context instance. Note, a provider
     * must return a new context instance every time this method is called,
     * since {@LdapContext} usually is not thread-safe.
     *
     * @throws AuthenticationException if the provider could not authenticate with the LDAP backend.
     * @throws NamingException if the creation of the LDAP context failed, for example due to
     * insufficent configuration configuration or because a connection to the LDAP backend could
     * not be established.
     */
    public LdapContext getLdapContext(String destination) throws NamingException, AuthenticationException;

}
