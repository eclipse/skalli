package org.eclipse.skalli.core.internal.destination;

import java.io.IOException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AllowAllHostnamesVerifier implements X509HostnameVerifier {

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
