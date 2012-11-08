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

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.MessageFormat;
import java.util.Comparator;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LDAPTrustAllSocketFactory extends SocketFactory implements Comparator<Object> {

    private static final Logger LOG = LoggerFactory.getLogger(LDAPTrustAllSocketFactory.class);

    @SuppressWarnings("nls")
    private static final String[] SSL_PROTOCOLS = new String[] { "TLS", "SSLv3", "SSLv2", "SSL" };

    private static final LDAPTrustAllSocketFactory INSTANCE = getInstance();

    private SocketFactory contextFactory;

    private LDAPTrustAllSocketFactory(SocketFactory contextFactory) {
        this.contextFactory = contextFactory;
    }

    public static SocketFactory getDefault() {
        return INSTANCE;
    }

    @Override
    public Socket createSocket() throws IOException {
      return INSTANCE.createSocket();
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        Socket socket = contextFactory.createSocket(host, port);
        socket.setTcpNoDelay(true);
        return socket;
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        Socket socket = contextFactory.createSocket(host, port);
        socket.setTcpNoDelay(true);
        return socket;
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
            throws IOException, UnknownHostException {
      Socket socket = contextFactory.createSocket(host, port, localHost, localPort);
      socket.setTcpNoDelay(true);
      return socket;

    }

    @Override
    public Socket createSocket(InetAddress address, int port,
        InetAddress localAddress, int localPort) throws IOException {
      Socket socket = contextFactory.createSocket(address, port, localAddress, localPort);
      socket.setTcpNoDelay(true);
      return socket;

    }

    private static LDAPTrustAllSocketFactory getInstance() {
        SSLContext sslContext = getSSLContext();
        try {
            sslContext.init(null, new TrustManager[]{ new LDAPTrustAllX509Manager() }, new SecureRandom());
        } catch (KeyManagementException e) {
            // should not happen since we do not use any keystore
            throw new IllegalStateException("Failed to initialize SSL context", e);
        }
        return new LDAPTrustAllSocketFactory(sslContext.getSocketFactory());
    }

    private static SSLContext getSSLContext() {
        SSLContext sslContext = null;
        for (int i = 0; i < SSL_PROTOCOLS.length && sslContext == null; ++i) {
            try {
                sslContext = SSLContext.getInstance(SSL_PROTOCOLS[i]);
            } catch (NoSuchAlgorithmException e) {
                LOG.debug(MessageFormat.format("SSL protocol {0} is not supported by the platform", SSL_PROTOCOLS[i]));
            }
        }
        if (sslContext == null) {
            throw new IllegalStateException("Platform does not support a suitable SSL protocol");
        }
        return sslContext;
    }

    /**
     * This is a workaround for the absolutely stupid SSL connection pooling
     * handling in com.sun.jndi.ldap.ClientId. According to the LDAP documentation,
     * one is supped to implement a Comparator&lt;SocketFactory&gt;, but then
     * just the class names of the socket factories to compare are passed for
     * comparision. Can't imagine what one is supposed to compare here, so we
     * just compare the strings.
     */
    @Override
    public int compare(Object o1, Object o2) {
        return o1.toString().compareTo(o2.toString());
    }
}
