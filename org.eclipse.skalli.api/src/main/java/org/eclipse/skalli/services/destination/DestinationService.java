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
package org.eclipse.skalli.services.destination;

import java.net.URL;

import org.apache.http.client.HttpClient;

/**
 * Service providing preconfigured {@link HttpClient HTTP clients} for various destinations.
 */
public interface DestinationService {

    /**
     * Returns a preconfigured HTTP client (including credentials and proxy settings)
     * for the given URL.
     *
     * @param url  the URL for which to return a client.
     * @return  an HTTP client for the given url.
     *
     * @throws IllegalArgumentException  if the protocol of the URL is not supported.
     */
    public HttpClient getClient(URL url);

    /**
     * Checks of the protocol of the given URL is supported. Supported protocols are
     * {@link #HTTP http://} and {@link #HTTPS https://}.
     *
     * @param url  the url to check.
     * @return <code>true</code>, if the protocol specifier of the url is
     * either <tt>http://</tt> or <tt>https://</tt>, respectively.
     */
    public boolean isSupportedProtocol(URL url);
}
