package org.eclipse.skalli.services.destination;

import java.net.URL;

import org.apache.commons.httpclient.HttpClient;

public interface DestinationService {

    public static final String HTTPS = "https"; //$NON-NLS-1$
    public static final String HTTP = "http"; //$NON-NLS-1$

    public HttpClient getClient(URL url);
    public boolean isSupportedProtocol(URL url);
}
