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
import java.util.Hashtable;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.eclipse.skalli.core.destination.DestinationConfig;
import org.eclipse.skalli.core.destination.DestinationsConfig;
import org.eclipse.skalli.services.configuration.Configurations;
import org.eclipse.skalli.services.user.ldap.LdapContextProvider;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.jndi.JNDIConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultLdapContextProvider implements LdapContextProvider {

    private final static Logger LOG = LoggerFactory.getLogger(DefaultLdapContextProvider.class);

    private static final String LDAPS = "ldaps://"; //$NON-NLS-1$
    private static final String SIMPLE_AUTHENTICATION = "simple"; //$NON-NLS-1$
    private static final String NO_SSL_VERIFY = "NO_SSL_VERIFY"; //$NON-NLS-1$
    private static final String JNDI_SOCKET_FACTORY = "java.naming.ldap.factory.socket"; //$NON-NLS-1$
    private static final String DEFAULT_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory"; //$NON-NLS-1$
    private static final String READ_TIMEOUT = "com.sun.jndi.ldap.read.timeout";//$NON-NLS-1$
    private static final String USE_CONNECTION_POOLING = "com.sun.jndi.ldap.connect.pool"; //$NON-NLS-1$

    private static final long DEFAULT_READ_TIMEOUT = 30000L;

    protected void activate(ComponentContext context) {
        LOG.info(MessageFormat.format("[LdapContextProvider][default] {0} : activated", //$NON-NLS-1$
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void deactivate(ComponentContext context) {
        LOG.info(MessageFormat.format("[LdapContextProvider][default] {0} : deactivated", //$NON-NLS-1$
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    @Override
    public String getId() {
        return "default"; //$NON-NLS-1$
    }

    @Override
    public LdapContext getLdapContext(String destination) throws NamingException, AuthenticationException {
        DestinationsConfig destinationsConfig = Configurations.getConfiguration(DestinationsConfig.class);
        if (destinationsConfig == null) {
            throw new NamingException("No LDAP destination available");
        }
        DestinationConfig destinationConfig = destinationsConfig.getDestination(destination);
        if (destinationConfig == null) {
            throw new NamingException("No LDAP destination available");
        }
        String providerUrl = destinationConfig.getUrlPattern();
        if (StringUtils.isBlank(providerUrl)) {
            throw new NamingException("No LDAP server configured");
        }
        if (StringUtils.isBlank(destinationConfig.getUser()) || StringUtils.isBlank(destinationConfig.getPassword())) {
            throw new AuthenticationException("No LDAP credentials available");
        }
        String ctxFactory = destinationConfig.getProperty(Context.INITIAL_CONTEXT_FACTORY);
        if (StringUtils.isBlank(ctxFactory)) {
            ctxFactory = DEFAULT_CONTEXT_FACTORY;
        }
        String authentication = destinationConfig.getAuthentication();
        if (StringUtils.isBlank(authentication)) {
            authentication = SIMPLE_AUTHENTICATION;
        }

        Hashtable<String, Object> env = new Hashtable<String, Object>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, ctxFactory);
        env.put(Context.PROVIDER_URL, providerUrl);
        env.put(Context.SECURITY_PRINCIPAL, destinationConfig.getUser());
        env.put(Context.SECURITY_CREDENTIALS, destinationConfig.getPassword());
        env.put(Context.SECURITY_AUTHENTICATION, authentication);
        String referral = destinationConfig.getProperty(Context.REFERRAL);
        if (StringUtils.isNotBlank(referral)) {
            env.put(Context.REFERRAL, referral);
        }
        if (providerUrl.startsWith(LDAPS)) {
            env.put(Context.SECURITY_PROTOCOL, "ssl"); //$NON-NLS-1$
            if (BooleanUtils.toBoolean(destinationConfig.getProperty(NO_SSL_VERIFY))) {
                env.put(JNDI_SOCKET_FACTORY, LDAPTrustAllSocketFactory.class.getName());
            }
        }
        // Gemini-specific properties
        env.put(JNDIConstants.BUNDLE_CONTEXT,
                FrameworkUtil.getBundle(DefaultLdapContextProvider.class).getBundleContext());

        // com.sun.jndi.ldap.LdapCtxFactory specific properties
        long readTimeout = NumberUtils.toLong(destinationConfig.getProperty(READ_TIMEOUT), DEFAULT_READ_TIMEOUT);
        env.put(READ_TIMEOUT, Long.toString(readTimeout));
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
}
