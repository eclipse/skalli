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

import org.apache.commons.httpclient.HttpClient;

public interface DestinationService {

    public static final String HTTPS = "https"; //$NON-NLS-1$
    public static final String HTTP = "http"; //$NON-NLS-1$

    public HttpClient getClient(URL url);
    public boolean isSupportedProtocol(URL url);
}
