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

    /**
     * Removes trailing and leading slashes and whitespace from a given
     * URL path string.
     *
     * @param s the string to transform, may be <code>null</code>.
     *
     * @return  the string without leading and trailing slashes and whitespace.
     */
    public static String removeSlashStartEnd(String s) {
        String ret = StringUtils.trim(s);
        if (!StringUtils.isEmpty(ret)) {
            while (ret.startsWith("/")){ //$NON-NLS-1$
                ret = ret.substring(1);
            }
            while (ret.endsWith("/")) { //$NON-NLS-1$
                ret = ret.substring(0, ret.length() - 1);
            }
        }
        return ret;
    }

    /**
     * Constructs an URL from a web locator (something like
     * <tt>"http://example.org:8080"</tt>) and given path segments.
     * <p>
     * This method handles leading and trailing slashes and whitespace
     * in all arguments.
     *
     * @param webLocator  a web locator, or <code>null</code>. If no web locator
     * is specified, the returned string is just a path (without leading slash).
     * @param pathSegments the path segments to concatenate. If no path segements
     * are specified, the <code>webLocator</code> is returned.
     *
     * @return an URL, which may be an absolute URL with a web locator,
     * or just a (relative) path. The result may be the empty string, but
     * never <code>null</code>.
     */
    public static String concat(String webLocator, Object... pathSegments) {
        StringBuilder ret = new StringBuilder();
        if (webLocator != null) {
            ret.append(removeSlashStartEnd(webLocator));
        }
        if (pathSegments != null) {
            for (Object next: pathSegments) {
                String pathSegment = next != null ? removeSlashStartEnd(next.toString()) : null;
                if (StringUtils.isNotBlank(pathSegment)) {
                    if (ret.length() > 0) {
                        ret.append('/');
                    }
                    ret.append(pathSegment);
                }
            }
        }
        return ret.toString();
    }

}
