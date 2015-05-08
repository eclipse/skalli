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
package org.eclipse.skalli.commons;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpUtils {

    private static final Logger LOG = LoggerFactory.getLogger(HttpUtils.class);

    // no instances, please!
    private HttpUtils() {
    }

    public static final String CONTENT_TYPE_XML = "text/xml"; //$NON-NLS-1$
    public static final String CONTENT_TYPE_JSON = "application/json"; //$NON-NLS-1$

    /**
     * Wraps the given string as UTF-8 encoded entity with <tt>Content-Type: text/xml"</tt>
     *
     * @param s  the string to wrap.
     * @return  a string entity.
     */
    public static StringEntity asXMLEntity(String s) {
        try {
            return new StringEntity(s, CONTENT_TYPE_XML, "UTF-8"); //$NON-NLS-1$
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e); // cannot happen
        }
    }

    /**
     * Wraps the given string as UTF-8 encoded entity with <tt>Content-Type: application/json"</tt>
     *
     * @param s  the string to wrap.
     * @return  a string entity.
     */
    public static StringEntity asJsonEntity(String s) {
        try {
            return new StringEntity(s, CONTENT_TYPE_JSON, "UTF-8"); //$NON-NLS-1$
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e); // cannot happen
        }
    }

    public static void consumeQuietly(HttpResponse response) {
        if (response != null) {
            try {
                EntityUtils.consume(response.getEntity());
            } catch (IOException e) {
                LOG.warn("I/O Exception when trying to consume an HTTP response", e); //$NON-NLS-1$
            }
        }
    }
}
