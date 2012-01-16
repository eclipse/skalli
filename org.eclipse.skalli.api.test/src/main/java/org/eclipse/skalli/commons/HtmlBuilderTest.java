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

import java.net.URL;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("nls")
public class HtmlBuilderTest {

    private static final String DUMMY_COMMENT = "<!-- -->";
    private static final String TEST_ICON = "icon";
    private static final String TEST_ALT_TEXT = "alt";
    private static final String TEST_STYLE = "style";
    private static final String TEST_STYLE_CLASS = "styleClass";
    private static final String TEST_URL = "http://example.org";
    private static final String TEST_LABEL = "label";
    private static final String TEST_SEC_LABEL = "alternative-label";
    private static final String TEST_TARGET = "target";
    private static final String TEST_MAILTO = "homer@example.org";
    private static final String TEST_MAILTO_URL_ENCODED = "homer%40example.org";
    private static final int TEST_LEVEL = 2;

    private static final String[] TEST_URLS = new String[]{"url1", "url2", "url3"};
    private static final List<String> TEST_URLS_SET = Arrays.asList(TEST_URLS);

    private static final Link[] TEST_LINKS = new Link[]{
        new Link("url1","label1"), new Link("url2","label2"), new Link("url3","label3")};
    private static final List<Link> TEST_LINKS_SET = Arrays.asList(TEST_LINKS);

    private static final String PATTERN_ICON = "<img src=\"{0}\"/>";
    private static final String PATTERN_ICON_ALL_PARAMS = "<img src=\"{0}\" alt=\"{1}\" class=\"{2}\" style=\"{3}\"/>";
    private static final String PATTERN_ICON_NO_STYLE = "<img src=\"{0}\" alt=\"{1}\" class=\"{2}\"/>";
    private static final String PATTERN_ICON_NO_STYLECLASS = "<img src=\"{0}\" alt=\"{1}\" style=\"{2}\"/>";
    private static final String PATTERN_ICON_NO_ALT = "<img src=\"{0}\" class=\"{1}\" style=\"{2}\"/>";

    private static final String PATTERN_LINK_ALL_PARAMS = "<a href=\"{0}\" target=\"{1}\" class=\"{2}\" style=\"{3}\">{4}</a>";
    private static final String PATTERN_LINK_URL_AND_LABEL_ONLY = "<a href=\"{0}\" target=\"{1}\">{2}</a>";
    private static final String PATTERN_LINK_URL_LABEL_WITH_STYLECLASS = "<a href=\"{0}\" target=\"{1}\" class=\"{2}\">{3}</a>";
    private static final String PATTERN_LINK_NO_STYLE = "<a href=\"{0}\" target=\"{1}\" class=\"{2}\">{3}</a>";
    private static final String PATTERN_LINK_NO_STYLE_CLASS = "<a href=\"{0}\" target=\"{1}\" style=\"{2}\">{3}</a>";

    private static final String PATTERN_ICONIZED_LINK = "<img src=\"{0}\"/><a href=\"{1}\" target=\"{2}\">{3}</a>";

    private static final String PATTERN_LINKS = "<a href=\"{0}\" target=\"{3}\">{5}</a>"
            + "<a href=\"{1}\" target=\"{3}\" class=\"{4}\">{6}</a>"
            + "<a href=\"{2}\" target=\"{3}\" class=\"{4}\">{7}</a>";
    private static final String PATTERN_ICONIZED_LINKS = "<img src=\"{8}\"/>"
            + "<a href=\"{0}\" target=\"{3}\">{5}</a>"
            + "<a href=\"{1}\" target=\"{3}\" class=\"{4}\">{6}</a>"
            + "<a href=\"{2}\" target=\"{3}\" class=\"{4}\">{7}</a>";

    private static final String PATTERN_MAILTO = "<a href=\"mailto:{0}\">{1}</a>";
    private static final String PATTERN_MAILTO_WITH_ICON = "<img src=\"{0}\"/><a href=\"mailto:{1}\">{2}</a>";

    private static final String PATTERN_HEADER = "<h{0} class=\"{1}\" style=\"{2}\">{3}</h{0}>\n";
    private static final String PATTERN_HEADER_NO_STYLES = "<h{0}>{1}</h{0}>\n";

    private HtmlBuilder html;

    @Before
    public void setup() throws Exception {
        html = new HtmlBuilder();
    }

    @Test
    public void testAppendLineBreak() throws Exception {
        assertEquals("<br/>\n", html.appendLineBreak());
    }

    @Test
    public void testAppendIcon() throws Exception {
        assertEquals(MessageFormat.format(PATTERN_ICON, TEST_ICON), html.appendIcon(TEST_ICON));
        assertEquals("", html.appendIcon(null));
        assertEquals("", html.appendIcon(""));
    }

    @Test
    public void testAppendIconWithAllParams() throws Exception {
        assertEquals(MessageFormat.format(PATTERN_ICON_ALL_PARAMS,
                    TEST_ICON, TEST_ALT_TEXT, TEST_STYLE_CLASS, TEST_STYLE),
                html.appendIcon(TEST_ICON, TEST_ALT_TEXT, TEST_STYLE_CLASS, TEST_STYLE));

        assertEquals(MessageFormat.format(PATTERN_ICON, TEST_ICON), html.appendIcon(TEST_ICON, null, null, null));

        assertEquals("", html.appendIcon(null, TEST_ALT_TEXT, TEST_STYLE_CLASS, TEST_STYLE));
        assertEquals("", html.appendIcon("", TEST_ALT_TEXT, TEST_STYLE_CLASS, TEST_STYLE));

        assertEquals(MessageFormat.format(PATTERN_ICON_NO_ALT,
                    TEST_ICON, TEST_STYLE_CLASS, TEST_STYLE),
                html.appendIcon(TEST_ICON, null, TEST_STYLE_CLASS, TEST_STYLE));
        assertEquals(MessageFormat.format(PATTERN_ICON_NO_ALT,
                TEST_ICON, TEST_STYLE_CLASS, TEST_STYLE),
            html.appendIcon(TEST_ICON, "", TEST_STYLE_CLASS, TEST_STYLE));

        assertEquals(MessageFormat.format(PATTERN_ICON_NO_STYLECLASS,
                TEST_ICON, TEST_ALT_TEXT, TEST_STYLE),
            html.appendIcon(TEST_ICON, TEST_ALT_TEXT, null, TEST_STYLE));
        assertEquals(MessageFormat.format(PATTERN_ICON_NO_STYLECLASS,
                TEST_ICON, TEST_ALT_TEXT, TEST_STYLE),
            html.appendIcon(TEST_ICON, TEST_ALT_TEXT, "", TEST_STYLE));

        assertEquals(MessageFormat.format(PATTERN_ICON_NO_STYLE,
                TEST_ICON, TEST_ALT_TEXT, TEST_STYLE_CLASS),
            html.appendIcon(TEST_ICON, TEST_ALT_TEXT, TEST_STYLE_CLASS, null));
        assertEquals(MessageFormat.format(PATTERN_ICON_NO_STYLE,
                TEST_ICON, TEST_ALT_TEXT, TEST_STYLE_CLASS),
            html.appendIcon(TEST_ICON, TEST_ALT_TEXT, TEST_STYLE_CLASS, ""));
    }

    @Test
    public void testAppendLink() throws Exception {
        final URL url = new URL(TEST_URL);

        assertEquals(MessageFormat.format(PATTERN_LINK_ALL_PARAMS,
                    TEST_URL, TEST_TARGET, TEST_STYLE_CLASS, TEST_STYLE, TEST_LABEL),
                html.appendLink(TEST_LABEL, TEST_URL, TEST_TARGET, TEST_STYLE_CLASS, TEST_STYLE));
        assertEquals(MessageFormat.format(PATTERN_LINK_ALL_PARAMS,
                TEST_URL, TEST_TARGET, TEST_STYLE_CLASS, TEST_STYLE, TEST_LABEL),
            html.appendLink(TEST_LABEL, url, TEST_TARGET, TEST_STYLE_CLASS, TEST_STYLE));

        assertEquals(TEST_LABEL, html.appendLink(TEST_LABEL, (String)null, TEST_TARGET, TEST_STYLE_CLASS, TEST_STYLE));
        assertEquals(TEST_LABEL, html.appendLink(TEST_LABEL, "", TEST_TARGET, TEST_STYLE_CLASS, TEST_STYLE));

        assertEquals(MessageFormat.format(PATTERN_LINK_ALL_PARAMS,
                    TEST_URL, TEST_TARGET, TEST_STYLE_CLASS, TEST_STYLE, TEST_URL),
                html.appendLink(null, TEST_URL, TEST_TARGET, TEST_STYLE_CLASS, TEST_STYLE));
        assertEquals(MessageFormat.format(PATTERN_LINK_ALL_PARAMS,
                TEST_URL, TEST_TARGET, TEST_STYLE_CLASS, TEST_STYLE, TEST_URL),
            html.appendLink("", TEST_URL, TEST_TARGET, TEST_STYLE_CLASS, TEST_STYLE));

        assertEquals(DUMMY_COMMENT, html.appendLink(null, (String)null, TEST_TARGET, TEST_STYLE_CLASS, TEST_STYLE));
        assertEquals(DUMMY_COMMENT, html.appendLink("", "", TEST_TARGET, TEST_STYLE_CLASS, TEST_STYLE));

        assertEquals(MessageFormat.format(PATTERN_LINK_ALL_PARAMS,
                TEST_URL, HtmlBuilder.DEFAULT_TARGET, TEST_STYLE_CLASS, TEST_STYLE, TEST_LABEL),
            html.appendLink(TEST_LABEL, TEST_URL, null, TEST_STYLE_CLASS, TEST_STYLE));
        assertEquals(MessageFormat.format(PATTERN_LINK_ALL_PARAMS,
                TEST_URL, HtmlBuilder.DEFAULT_TARGET, TEST_STYLE_CLASS, TEST_STYLE, TEST_LABEL),
            html.appendLink(TEST_LABEL, TEST_URL, "", TEST_STYLE_CLASS, TEST_STYLE));

        assertEquals(MessageFormat.format(PATTERN_LINK_NO_STYLE_CLASS,
                TEST_URL, TEST_TARGET, TEST_STYLE, TEST_LABEL),
            html.appendLink(TEST_LABEL, TEST_URL, TEST_TARGET, null, TEST_STYLE));
        assertEquals(MessageFormat.format(PATTERN_LINK_NO_STYLE_CLASS,
                TEST_URL, TEST_TARGET, TEST_STYLE, TEST_LABEL),
            html.appendLink(TEST_LABEL, TEST_URL, TEST_TARGET, "", TEST_STYLE));

        assertEquals(MessageFormat.format(PATTERN_LINK_NO_STYLE,
                TEST_URL, TEST_TARGET, TEST_STYLE_CLASS, TEST_LABEL),
            html.appendLink(TEST_LABEL, TEST_URL, TEST_TARGET, TEST_STYLE_CLASS, null));
        assertEquals(MessageFormat.format(PATTERN_LINK_NO_STYLE,
                TEST_URL, TEST_TARGET, TEST_STYLE_CLASS, TEST_LABEL),
            html.appendLink(TEST_LABEL, TEST_URL, TEST_TARGET, TEST_STYLE_CLASS, ""));

        // overloaded helpers
        assertEquals(MessageFormat.format(PATTERN_LINK_URL_AND_LABEL_ONLY, TEST_URL, HtmlBuilder.DEFAULT_TARGET, TEST_LABEL),
                html.appendLink(TEST_LABEL, TEST_URL));
        assertEquals(MessageFormat.format(PATTERN_LINK_URL_AND_LABEL_ONLY, TEST_URL, HtmlBuilder.DEFAULT_TARGET, TEST_LABEL),
                html.appendLink(TEST_LABEL, url));

        assertEquals(MessageFormat.format(PATTERN_LINK_URL_LABEL_WITH_STYLECLASS, TEST_URL,
                    HtmlBuilder.DEFAULT_TARGET, TEST_STYLE_CLASS, TEST_LABEL),
                html.appendLink(TEST_LABEL, TEST_URL, TEST_STYLE_CLASS));
        assertEquals(MessageFormat.format(PATTERN_LINK_URL_LABEL_WITH_STYLECLASS, TEST_URL,
                HtmlBuilder.DEFAULT_TARGET, TEST_STYLE_CLASS, TEST_LABEL),
            html.appendLink(TEST_LABEL, url, TEST_STYLE_CLASS));
    }

    @Test
    public void testAppendIconizedLink() throws Exception {
        assertEquals(MessageFormat.format(PATTERN_ICONIZED_LINK,
                    TEST_ICON, TEST_URL, HtmlBuilder.DEFAULT_TARGET, TEST_LABEL),
                html.appendIconizedLink(TEST_ICON, TEST_LABEL, TEST_URL));
    }

    @Test
    public void testAppendLinks() throws Exception {
        assertEquals(MessageFormat.format(PATTERN_LINKS,
                TEST_URLS[0], TEST_URLS[1], TEST_URLS[2], HtmlBuilder.DEFAULT_TARGET,
                HtmlBuilder.STYLE_LEFT_MARGIN, TEST_LABEL, TEST_SEC_LABEL, TEST_SEC_LABEL),
            html.appendLinks(TEST_LABEL, TEST_SEC_LABEL, TEST_URLS_SET));

        assertEquals("", html.appendLinks(TEST_LABEL, TEST_SEC_LABEL, null));

        assertEquals(MessageFormat.format(PATTERN_LINKS,
                TEST_URLS[0], TEST_URLS[1], TEST_URLS[2], HtmlBuilder.DEFAULT_TARGET,
                HtmlBuilder.STYLE_LEFT_MARGIN, TEST_LABEL, TEST_LABEL, TEST_LABEL),
            html.appendLinks(TEST_LABEL, null, TEST_URLS_SET));

        assertEquals(MessageFormat.format(PATTERN_LINKS,
                TEST_URLS[0], TEST_URLS[1], TEST_URLS[2], HtmlBuilder.DEFAULT_TARGET,
                HtmlBuilder.STYLE_LEFT_MARGIN, TEST_SEC_LABEL, TEST_SEC_LABEL, TEST_SEC_LABEL),
            html.appendLinks(null, TEST_SEC_LABEL, TEST_URLS_SET));

        assertEquals(MessageFormat.format(PATTERN_LINKS,
                TEST_URLS[0], TEST_URLS[1], TEST_URLS[2], HtmlBuilder.DEFAULT_TARGET,
                HtmlBuilder.STYLE_LEFT_MARGIN, TEST_URLS[0], TEST_URLS[1], TEST_URLS[2]),
            html.appendLinks(null, null, TEST_URLS_SET));

        assertEquals(MessageFormat.format(PATTERN_LINKS,
                TEST_LINKS[0].getUrl(), TEST_LINKS[1].getUrl(), TEST_LINKS[2].getUrl(),
                HtmlBuilder.DEFAULT_TARGET, HtmlBuilder.STYLE_LEFT_MARGIN,
                TEST_LINKS[0].getLabel(), TEST_LINKS[1].getLabel(), TEST_LINKS[2].getLabel()),
            html.appendLinks(TEST_LINKS_SET));
    }

    @Test
    public void testAppendIconizedLinks() throws Exception {
        assertEquals(MessageFormat.format(PATTERN_ICONIZED_LINKS,
                TEST_URLS[0], TEST_URLS[1], TEST_URLS[2], HtmlBuilder.DEFAULT_TARGET,
                HtmlBuilder.STYLE_LEFT_MARGIN, TEST_LABEL, TEST_SEC_LABEL, TEST_SEC_LABEL,
                TEST_ICON),
            html.appendIconizedLinks(TEST_ICON, TEST_LABEL, TEST_SEC_LABEL, TEST_URLS_SET));
    }

    private void assertEquals(String s, HtmlBuilder html) throws Exception {
        Assert.assertEquals(s, html.toString());
        html.clear();
    }

    @Test
    public void testAppendMailToLink() throws Exception {
        assertEquals(MessageFormat.format(PATTERN_MAILTO,
                TEST_MAILTO_URL_ENCODED, TEST_LABEL),
                html.appendMailToLink(null, TEST_MAILTO, TEST_LABEL));
        assertEquals(MessageFormat.format(PATTERN_MAILTO,
                TEST_MAILTO_URL_ENCODED, TEST_MAILTO),
                html.appendMailToLink(null, TEST_MAILTO, null));
        assertEquals(MessageFormat.format(PATTERN_MAILTO,
                TEST_MAILTO_URL_ENCODED, TEST_MAILTO),
                html.appendMailToLink(null, TEST_MAILTO, ""));
        assertEquals(MessageFormat.format(PATTERN_MAILTO,
                TEST_MAILTO_URL_ENCODED, "&lt;"),
                html.appendMailToLink(null, TEST_MAILTO, "<"));

        assertEquals(MessageFormat.format(PATTERN_MAILTO,
                TEST_MAILTO_URL_ENCODED, TEST_MAILTO),
                html.appendMailToLink(null, TEST_MAILTO, null));
        assertEquals("", html.appendMailToLink(null, null, null));
        assertEquals("", html.appendMailToLink(null, "", null));

        assertEquals(MessageFormat.format(PATTERN_MAILTO_WITH_ICON,
                TEST_ICON, TEST_MAILTO_URL_ENCODED, TEST_LABEL),
                html.appendMailToLink(TEST_ICON, TEST_MAILTO, TEST_LABEL));
    }


    @Test
    public void testAppendHeader() throws Exception {
        assertEquals(MessageFormat.format(PATTERN_HEADER,
                TEST_LEVEL, TEST_STYLE_CLASS, TEST_STYLE, TEST_LABEL),
            html.appendHeader(TEST_LABEL, TEST_LEVEL, TEST_STYLE_CLASS, TEST_STYLE));

        assertEquals(MessageFormat.format(PATTERN_HEADER,
                1, TEST_STYLE_CLASS, TEST_STYLE, TEST_LABEL),
            html.appendHeader(TEST_LABEL, 0, TEST_STYLE_CLASS, TEST_STYLE));

        assertEquals(MessageFormat.format(PATTERN_HEADER,
                6, TEST_STYLE_CLASS, TEST_STYLE, TEST_LABEL),
            html.appendHeader(TEST_LABEL, Integer.MAX_VALUE, TEST_STYLE_CLASS, TEST_STYLE));

        assertEquals(MessageFormat.format(PATTERN_HEADER,
                TEST_LEVEL, TEST_STYLE_CLASS, TEST_STYLE, DUMMY_COMMENT),
            html.appendHeader(null, TEST_LEVEL, TEST_STYLE_CLASS, TEST_STYLE));

        assertEquals(MessageFormat.format(PATTERN_HEADER_NO_STYLES,
                TEST_LEVEL, TEST_LABEL),
            html.appendHeader(TEST_LABEL, TEST_LEVEL));
    }
}
