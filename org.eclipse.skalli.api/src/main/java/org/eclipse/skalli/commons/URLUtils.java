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
package org.eclipse.skalli.commons;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class URLUtils {

    // no instances, please!
    private URLUtils() {
    }

    /**
     * Utility method to convert an enumeration of URLs to a corresponding list.
     *
     * @param u  the enumeration to evaluate, or <code>null</code>.
     * @return a list if URLs, or an empty list.
     */
    public static List<URL> asURLs(Enumeration<URL> u) {
        List<URL> ret = new LinkedList<URL>();
        if (u != null) {
            while (u.hasMoreElements()) {
                URL url = u.nextElement();
                ret.add(url);
            }
        }
        return ret;
    }


    /**
     * Converts a given string into a corresponding URL.
     * <p>
     * Encodes path and/or query parts of the given string according to
     * {@link URI#URI(String, String, String, int, String, String, String)}.
     * For example, blanks in the path are converted to <tt>%20</tt>.
     *
     * @param s  the string to convert to an URL.
     * @return  an URL, or <code>null</code> if the string is <code>null</code>, empty or whitespace.
     *
     * @throws MalformedURLException  if the given string is not a valid URL and cannot be
     * "sanitized" to yield a valid URL even after proper encoding of its parts.
     */
    public static URL stringToURL(String s) throws MalformedURLException {
        if (StringUtils.isBlank(s)) {
            return null;
        }
        URI uri = null;
        try {
            uri = new URI(s);
        } catch (URISyntaxException e) {
            URL url = new URL(s);
            try {
                uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(),
                                url.getPath(), url.getQuery(), url.getRef());
            } catch (URISyntaxException e1) {
                MalformedURLException e2 = new MalformedURLException(e1.getMessage());
                e2.initCause(e1);
                throw e2;
            }
        }
        return new URL(uri.toASCIIString());
    }
}
