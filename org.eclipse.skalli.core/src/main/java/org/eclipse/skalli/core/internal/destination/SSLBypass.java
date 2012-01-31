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

import java.security.SecureRandom;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deactivates SSL certificate trust checks.
 *
 */
@SuppressWarnings("nls")
public class SSLBypass {
    private static Logger logger = LoggerFactory.getLogger(SSLBypass.class);
    private static SSLContext oldDefault;

    private static class DefaultTrustManager implements X509TrustManager {

        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
                throws java.security.cert.CertificateException {
            return;
        }

        @Override
        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
                throws java.security.cert.CertificateException {
            return;
        }
    }

    public static void activate() {
        try {
            oldDefault = SSLContext.getDefault();

            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(new KeyManager[0], new TrustManager[] { new DefaultTrustManager() }, new SecureRandom());
            SSLContext.setDefault(ctx);

            logger.info("SSL TrustAll has been activated.");
        } catch (Exception e) {
            logger.error("SSL TrustAll cannot be activated.", e);
        }
    }

    public static void deactivate() {
        SSLContext.setDefault(oldDefault);
        logger.info("SSL TrustAll has been deactivated.");
    }
}
