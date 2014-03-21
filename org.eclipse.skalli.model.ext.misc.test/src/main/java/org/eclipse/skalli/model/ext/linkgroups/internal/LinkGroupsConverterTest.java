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
package org.eclipse.skalli.model.ext.linkgroups.internal;

import java.util.Arrays;

import org.eclipse.skalli.commons.Link;
import org.eclipse.skalli.model.ext.linkgroups.LinkGroup;
import org.eclipse.skalli.model.ext.linkgroups.LinkGroupsProjectExt;
import org.eclipse.skalli.services.rest.RestWriter;
import org.eclipse.skalli.testutil.RestWriterTestBase;
import org.junit.Test;

@SuppressWarnings("nls")
public class LinkGroupsConverterTest extends RestWriterTestBase {

    @Test
    public void testMarshalBlankExtensionXML() throws Exception {
        LinkGroupsProjectExt links = new LinkGroupsProjectExt();
        RestWriter restWriter = getRestWriterXML();
        marshalLinkGroupsExtension(links, restWriter);
        assertEqualsXML("<linkGroups/>");
    }

    @Test
    public void testMarshalExtensionXML() throws Exception {
        LinkGroupsProjectExt links = newLinkGroups();
        RestWriter restWriter = getRestWriterXML();
        marshalLinkGroupsExtension(links, restWriter);
        assertEqualsXML("<linkGroups><linkGroups>"
                + "<linkGroup caption=\"group1\">"
                + "<link ref=\"urlA\">a</link>"
                + "<link ref=\"urlB\">b</link>"
                + "</linkGroup>"
                + "<linkGroup caption=\"group2\">"
                + "<link ref=\"urlX\">x</link>"
                + "</linkGroup>"
                + "</linkGroups></linkGroups>");
    }

    @Test
    public void testMarshalBlankExtensionJSON() throws Exception {
        LinkGroupsProjectExt links = new LinkGroupsProjectExt();
        RestWriter restWriter = getRestWriterJSON();
        marshalLinkGroupsExtension(links, restWriter);
        assertEqualsJSON("{}");
    }

    @Test
    public void testMarshalExtensionJSON() throws Exception {
        LinkGroupsProjectExt links = newLinkGroups();
        RestWriter restWriter = getRestWriterJSON();
        marshalLinkGroupsExtension(links, restWriter);
        assertEqualsJSON("{\"linkGroups\":["
                + "{\"caption\":\"group1\","
                + "\"links\":["
                + "{\"ref\":\"urlA\",\"value\":\"a\"},"
                + "{\"ref\":\"urlB\",\"value\":\"b\"}"
                + "]},"
                + "{\"caption\":\"group2\","
                + "\"links\":["
                + "{\"ref\":\"urlX\",\"value\":\"x\"}"
                + "]}]}");
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
}
