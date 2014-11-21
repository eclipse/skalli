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

import java.text.MessageFormat;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.safety.Whitelist;

/**
 * Utilities for validation and cleaning of HTML fragments.
 */
public class HtmlUtils {

    // no instances, please!
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
        whitelist.addTags(ALLOWED_TAGS)
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
     * Returns <code>true</code> if the  given string contains any HTML tags.
     *
     * @param s  the string to check.
     */
    public static boolean containsTags(String s) {
        if (StringUtils.isBlank(s)) {
            return false;
        }
        return !Jsoup.isValid(s, Whitelist.none());
    }

    /**
     * Filters untrusted tags and attributes from the given HTML fragment by using
     * the {@link #getWhiteList() default whitelist} of allowed tags and attributes.
     * Escapes the XML entities <tt>&amp;quot</tt>, <tt>&amp;amp</tt>, <tt>&amp;apos</tt>,
     * <tt>&amp;lt</tt>, and <tt>&amp;gt</tt> in the output.
     *
     * @param html  the HTML fragment to clean.
     *
     * @return the cleaned input string.
     */
    public static String clean(String html) {
        return clean(html, null, null, null);
    }

    /**
     * Filters untrusted tags and attributes from the given HTML fragment by using
     * a whitelist of allowed tags and attributes.
     * Escapes the XML entities <tt>&amp;quot</tt>, <tt>&amp;amp</tt>, <tt>&amp;apos</tt>,
     * <tt>&amp;lt</tt>, and <tt>&amp;gt</tt> in the output.
     *
     * @param html  the HTML fragment to clean.
     * @param whitelist  whitelist of allowed tags and attributes, or <code>null</code>
     * if the {@link #getWhiteList() default whitelist} should be used.
     *
     * @return the cleaned input string.
     */
    public static String clean(String html, Whitelist whitelist) {
        return clean(html, whitelist, null, null);
    }

    /**
     * Filters untrusted tags and attributes from the given HTML fragment by using
     * a whitelist of allowed tags and attributes.
     *
     * @param html  the HTML fragment to clean.
     * @param whitelist  whitelist of allowed tags and attributes, or <code>null</code>
     * if the {@link #getWhiteList() default whitelist} should be used.
     * @param baseUri  base URL to resolve relative URLs against, or <code>null</code>.
     * @param escapeMode  determines how XML/HTML entities are to be escaped,
     * or <code>null</code>. The default escape mode is {@link EscapeMode.xhtml},
     * i.e. only the XML entities <tt>&amp;quot</tt>, <tt>&amp;amp</tt>, <tt>&amp;apos</tt>,
     * <tt>&amp;lt</tt>, and <tt>&amp;gt</tt> are recognized.
     *
     * @return the cleaned input string.
     */
    public static String clean(String html, Whitelist whitelist, String baseUri, EscapeMode escapeMode) {
        if (StringUtils.isBlank(html)) {
            return html;
        }
        if (whitelist == null) {
            whitelist = getWhiteList();
        }
        String cleaned = Jsoup.clean(html, baseUri != null? baseUri : "", whitelist); //$NON-NLS-1$
        Document cleanedDocument = Jsoup.parse(cleaned);
        cleanedDocument.outputSettings().escapeMode(escapeMode != null? escapeMode : EscapeMode.xhtml);
        return cleanedDocument.body().html();
    }

   /**
     * Wrapper for {@link MessageFormat#format(String, Object...)}, which
     * HTML-escapes the given arguments before inserting them into the pattern.
     *
     * @param pattern  the pattern with placeholders for the arguments.
     * @param arguments  the arguments to insert.
     * @return the formatted result.
     *
     * @see StringEscapeUtils#escapeHtml(String)
     */
    public static String formatEscaped(String pattern, Object... arguments) {
        if (arguments == null || arguments.length == 0) {
            return pattern;
        }
        Object[] escapedArguments = new String[arguments.length];
        for (int i = 0; i < arguments.length; ++i) {
            escapedArguments[i] = arguments[i] != null?
                    StringEscapeUtils.escapeHtml(arguments[i].toString()) : null;
        }
        return MessageFormat.format(pattern, escapedArguments);
    }
}
