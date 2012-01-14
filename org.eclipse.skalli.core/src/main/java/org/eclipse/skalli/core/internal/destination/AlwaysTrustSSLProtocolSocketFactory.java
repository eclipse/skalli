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
package org.eclipse.skalli.core.internal.destination;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;

class AlwaysTrustSSLProtocolSocketFactory implements ProtocolSocketFactory {

    private static final String SSL_PROTOCOL = "SSL";

    private SSLContext getSSLContext() throws IOException {
        try {
            SSLContext context = SSLContext.getInstance(SSL_PROTOCOL);
            context.init(null, new TrustManager[] { new AlwaysTrustX509Manager() }, null);
            return context;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Socket createSocket(final String hostname, final int port) throws IOException, UnknownHostException {
        return getSSLContext().getSocketFactory().createSocket(hostname, port);
    }

    @Override
    public Socket createSocket(final String hostname, final int port, final InetAddress localAddress,
            final int localPort) throws IOException, UnknownHostException {
        return getSSLContext().getSocketFactory().createSocket(hostname, port, localAddress, localPort);
    }

    @Override
    public Socket createSocket(final String hostname, final int port, final InetAddress localAddress,
            final int localPort, final HttpConnectionParams params) throws IOException, UnknownHostException,
            ConnectTimeoutException {
        if (params.getConnectionTimeout() != 0) {
            Socket socket = getSSLContext().getSocketFactory().createSocket();
            socket.bind(new InetSocketAddress(localAddress, localPort));
            socket.connect(new InetSocketAddress(hostname, port), params.getConnectionTimeout());
            return socket;
        } else {
            return getSSLContext().getSocketFactory().createSocket(hostname, port, localAddress, localPort);
        }
    }

}
