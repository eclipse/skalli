/*******************************************************************************
 * Copyright (c) 2010-2014 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.core.destination;

import java.io.IOException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AllowAllHostnamesVerifier implements X509HostnameVerifier {

    private static final Logger LOG = LoggerFactory.getLogger(AllowAllHostnamesVerifier.class);

    @Override
    public boolean verify(String host, SSLSession session) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("accepting host " + host);
        }
        return true;
    }

    @Override
    public void verify(String host, SSLSocket ssl) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("accepting host " + host);
        }
    }

    @Override
    public void verify(String host, X509Certificate cert) throws SSLException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("accepting host " + host);
        }
    }

    @Override
    public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("accepting host " + host);
        }
    }
}
