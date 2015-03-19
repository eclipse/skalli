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
package org.eclipse.skalli.model.ext.linkgroups.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.skalli.commons.Link;
import org.eclipse.skalli.model.ext.linkgroups.LinkGroup;
import org.eclipse.skalli.model.ext.linkgroups.LinkGroupsProjectExt;
import org.eclipse.skalli.services.rest.RestReader;
import org.eclipse.skalli.services.rest.RestWriter;
import org.eclipse.skalli.testutil.AssertUtils;
import org.eclipse.skalli.testutil.RestWriterTestBase;
import org.junit.Test;

@SuppressWarnings("nls")
public class LinkGroupsConverterTest extends RestWriterTestBase {

    private static final String INITIAL_LINKGRP_EXTENSION_XML = "<linkGroups/>";
    private static final String LINKGRP_EXTENSION_XML = "<linkGroups><linkGroups>"
            + "<linkGroup caption=\"group1\">"
            + "<link ref=\"urlA\">a</link>"
            + "<link ref=\"urlB\">b</link>"
            + "</linkGroup>"
            + "<linkGroup caption=\"group2\">"
            + "<link ref=\"urlX\">x</link>"
            + "</linkGroup>"
            + "</linkGroups></linkGroups>";
    private static final String INITIAL_LINKGRP_EXTENSION_JSON = "{}";
    private static final String LINKGRP_EXTENSION_JSON = "{\"items\":["
            + "{\"caption\":\"group1\","
            + "\"links\":["
            + "{\"ref\":\"urlA\",\"value\":\"a\"},"
            + "{\"ref\":\"urlB\",\"value\":\"b\"}"
            + "]},"
            + "{\"caption\":\"group2\","
            + "\"links\":["
            + "{\"ref\":\"urlX\",\"value\":\"x\"}"
            + "]}]}";
    private static final String LINKGRP_EXTENSION_UNKNOWN_ATTR_JSON = "{\"items\":["
            + "{\"ignore\":true},"
            + "{\"caption\":\"group1\",\"ignore\":true,"
            + "\"links\":["
            + "{\"ref\":\"urlB\",\"value\":\"b\"},"
            + "{\"ref\":\"urlA\",\"value\":\"a\"},{\"whatever\":4711}"
            + "]},"
            + "{\"unknown\":\"yes\"},"
            + "{\"caption\":\"group2\","
            + "\"links\":["
            + "{\"ref\":\"urlX\",\"value\":\"x\"}"
            + "]}]}";

    @Test
    public void testMarshalBlankExtensionXML() throws Exception {
        LinkGroupsProjectExt links = new LinkGroupsProjectExt();
        RestWriter restWriter = getRestWriterXML();
        marshalLinkGroupsExtension(links, restWriter);
        assertEqualsXML(INITIAL_LINKGRP_EXTENSION_XML);
    }

    @Test
    public void testMarshalExtensionXML() throws Exception {
        LinkGroupsProjectExt links = newLinkGroups();
        RestWriter restWriter = getRestWriterXML();
        marshalLinkGroupsExtension(links, restWriter);
        assertEqualsXML(LINKGRP_EXTENSION_XML);
    }

    @Test
    public void testMarshalBlankExtensionJSON() throws Exception {
        LinkGroupsProjectExt links = new LinkGroupsProjectExt();
        RestWriter restWriter = getRestWriterJSON();
        marshalLinkGroupsExtension(links, restWriter);
        assertEqualsJSON(INITIAL_LINKGRP_EXTENSION_JSON);
    }

    @Test
    public void testMarshalExtensionJSON() throws Exception {
        LinkGroupsProjectExt links = newLinkGroups();
        RestWriter restWriter = getRestWriterJSON();
        marshalLinkGroupsExtension(links, restWriter);
        assertEqualsJSON(LINKGRP_EXTENSION_JSON);
    }

    @Test
    public void testUnmarshallInitialJSON() throws Exception {
        RestReader restReader = getRestReaderJSON(INITIAL_LINKGRP_EXTENSION_JSON);
        LinkGroupsProjectExt linkGrp = unmarshalLinkGroupsExtension(restReader);
        assertTrue(linkGrp.getLinkGroups().isEmpty());
    }

    @Test
    public void testUnmarshallJSON() throws Exception {
        RestReader restReader = getRestReaderJSON(LINKGRP_EXTENSION_JSON);
        LinkGroupsProjectExt linkGrp = unmarshalLinkGroupsExtension(restReader);
        assertEquals(2, linkGrp.getLinkGroups().size());
        Iterator<LinkGroup> it = linkGrp.getLinkGroups().iterator();
        assertLinkGroup(it.next(), "group1", new Link("urlA","a"), new Link("urlB","b"));
        assertLinkGroup(it.next(), "group2", new Link("urlX","x"));
    }

    @Test
    public void testUnmarshallIgnoreUnknownAttributesJSON() throws Exception {
        RestReader restReader = getRestReaderJSON(LINKGRP_EXTENSION_UNKNOWN_ATTR_JSON);
        LinkGroupsProjectExt linkGrp = unmarshalLinkGroupsExtension(restReader);
        assertEquals(2, linkGrp.getLinkGroups().size());
        Iterator<LinkGroup> it = linkGrp.getLinkGroups().iterator();
        assertLinkGroup(it.next(), "group1", new Link("urlB","b"),  new Link("urlA","a"));
        assertLinkGroup(it.next(), "group2", new Link("urlX","x"));
    }

    private void assertLinkGroup(LinkGroup grp, String caption, Link...links) {
        assertEquals(caption, grp.getCaption());
        AssertUtils.assertEquals("getItems()", grp.getItems(), links);
    }

    private LinkGroupsProjectExt newLinkGroups() {
        LinkGroupsProjectExt links = new LinkGroupsProjectExt();
        links.addLinkGroup(new LinkGroup("group1",
                Arrays.asList(
                        new Link("urlA", "a"),
                        new Link("urlB", "b"))));
        links.addLinkGroup(new LinkGroup("group2",
                Arrays.asList(
                        new Link("urlX", "x"))));
        return links;
    }

    private void marshalLinkGroupsExtension(LinkGroupsProjectExt links, RestWriter restWriter) throws Exception {
        LinkGroupsConverter converter = new LinkGroupsConverter();
        restWriter.object("linkGroups");
        converter.marshal(links, restWriter);
        restWriter.end();
        restWriter.flush();
    }

    private LinkGroupsProjectExt unmarshalLinkGroupsExtension(RestReader restReader) throws Exception {
        LinkGroupsConverter converter = new LinkGroupsConverter();
        restReader.object();
        LinkGroupsProjectExt linkGrp = converter.unmarshal(restReader);
        restReader.end();
        return linkGrp;
    }
}
