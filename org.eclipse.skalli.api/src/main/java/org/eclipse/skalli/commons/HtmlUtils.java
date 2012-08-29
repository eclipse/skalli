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

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

/**
 * Utilities for validation and cleaning of HTML fragments.
 */
public class HtmlUtils {

    private HtmlUtils() {
    }

    /**
     * List of "safe" HTML tags.
     */
    @SuppressWarnings("nls")
    public static final String[] ALLOWED_TAGS = { "i", "b", "u", "a", "p", "br", "hr", "tt", "code", "pre", "em",
        "strong", "u", "s", "strike", "big", "small", "sup", "sub", "span", "blockquote", "dl", "dt", "dd", "h1",
        "h2", "h3", "h4", "h5", "h6", "cite", "q", "ul", "ol", "li" };

    /**
     * Returns a {@link Whitelist whitelist} of HTML tags and attributes that can safely be used
     * when rendering HTML/JSP pages. Use the returned whitelist with {@link JSoup}.
     */
    @SuppressWarnings("nls")
    public static Whitelist getWhiteList() {
        Whitelist whitelist = new Whitelist();
        whitelist.addTags(ALLOWED_TAGS).addAttributes(":all", "style")
                .addAttributes("a", "href", "target", "name", "title", "rel")
                .addAttributes("ul", "type")
                .addAttributes("ol", "start", "type")
                .addAttributes("li", "value")
                .addAttributes("blockquote", "cite")
                .addAttributes("q", "cite")
                .addProtocols("a", "href", "http", "https", "mailto")
                .addProtocols("blockquote", "cite", "http", "https")
                .addProtocols("cite", "cite", "http", "https")
                .addProtocols("q", "cite", "http", "https");
        return whitelist;
    }

    /**
     * Returns <code>true</code> if the  given HTML fragment contains only trusted tags and attributes
     * according to the {@link #getWhiteList() whitelist} of allowed tags and attributes.
     * If the HTML fragment passes this check, it can safely be rendered on HTML/JSP pages.
     *
     * @param html  the HTML fragment to check.
     */
    public static boolean isValid(String html) {
        return Jsoup.isValid(html, getWhiteList());
    }

    /**
     * Filters untrusted tags and attributes from the given HTML fragment by using
     * the {@link #getWhiteList() whitelist} of allowed tags and attributes.
     *
     * @param html  the HTML fragment to clean.
     * @return the cleaned input string.
     */
    public static String clean(String html) {
        return Jsoup.clean(html, getWhiteList());
    }

    /**
     * Filters untrusted tags and attributes from the given HTML fragment by using
     * the {@link #getWhiteList() whitelist} of allowed tags and attributes.
     *
     * @param html  the HTML fragment to clean.
     * @param baseUri  base URL to resolve relative URLs against.
     * @return the cleaned input string.
     */
    public static String clean(String html, String baseUri) {
        return Jsoup.clean(html, baseUri, getWhiteList());
    }
}