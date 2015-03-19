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
package org.eclipse.skalli.model.ext.scrum.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.skalli.model.Member;
import org.eclipse.skalli.model.ext.scrum.ScrumProjectExt;
import org.eclipse.skalli.services.rest.RestReader;
import org.eclipse.skalli.services.rest.RestWriter;
import org.eclipse.skalli.testutil.AssertUtils;
import org.eclipse.skalli.testutil.RestWriterTestBase;
import org.junit.Test;

@SuppressWarnings("nls")
public class ScrumConverterTest extends RestWriterTestBase {

    private static final String INITIAL_SCRUM_EXTENSION_XML =
            "<scrum><scrumMasters/><productOwners/></scrum>";
    private static final String SCRUM_EXTENSION_XML =
            "<scrum>"
            + "<backlogUrl>http://example.org:8080/backlog</backlogUrl>"
            + "<scrumMasters>"
            + "<scrumMaster><userId>a</userId><link rel=\"user\" href=\"http://example.org:8080/api/users/a\"/></scrumMaster>"
            + "</scrumMasters>"
            + "<productOwners>"
            + "<productOwner><userId>b</userId><link rel=\"user\" href=\"http://example.org:8080/api/users/b\"/></productOwner>"
            + "<productOwner><userId>c</userId><link rel=\"user\" href=\"http://example.org:8080/api/users/c\"/></productOwner>"
            + "</productOwners></scrum>";
    private static final String INITIAL_SCRUM_EXTENSION_JSON = "{\"scrumMasters\":[],\"productOwners\":[]}";
    private static final String SCRUM_EXTENSION_JSON =
            "{\"backlogUrl\":\"http://example.org:8080/backlog\","
            + "\"scrumMasters\":["
            + "{\"userId\":\"a\",\"link\":{\"rel\":\"user\",\"href\":\"http://example.org:8080/api/users/a\"}}],"
            + "\"productOwners\":["
            + "{\"userId\":\"b\",\"link\":{\"rel\":\"user\",\"href\":\"http://example.org:8080/api/users/b\"}},"
            + "{\"userId\":\"c\",\"link\":{\"rel\":\"user\",\"href\":\"http://example.org:8080/api/users/c\"}}]}";
    private static final String SCRUM_EXTENSION_UNKNOWN_ATTR_JSON =
            "{\"ignore\":true,\"backlogUrl\":\"http://example.org:8080/backlog\","
            + "\"unknown\":\"yes\","
            + "\"productOwners\":["
            + "{\"userId\":\"b\",\"link\":{\"rel\":\"user\",\"href\":\"http://example.org:8080/api/users/b\"}},"
            + "{\"userId\":\"c\",\"ignore\":true,\"link\":{\"rel\":\"user\",\"href\":\"http://example.org:8080/api/users/c\"}}],"
            + "\"scrumMasters\":["
            + "{\"unknown\":\"yes\",\"userId\":\"a\",\"link\":{\"ignore\":true,\"rel\":\"user\",\"href\":\"http://example.org:8080/api/users/a\"}}]}";

    @Test
    public void testMarshalBlankExtensionXML() throws Exception {
        ScrumProjectExt scrum = new ScrumProjectExt();
        RestWriter restWriter = getRestWriterXML();
        marshalScrumExtension(scrum, restWriter);
        assertEqualsXML(INITIAL_SCRUM_EXTENSION_XML);
    }

    @Test
    public void testMarshalExtensionXML() throws Exception {
        ScrumProjectExt scrum = newScrumExtension();
        RestWriter restWriter = getRestWriterXML();
        marshalScrumExtension(scrum, restWriter);
        assertEqualsXML(SCRUM_EXTENSION_XML);
    }

    @Test
    public void testMarshalBlankExtensionJSON() throws Exception {
        ScrumProjectExt scrum = new ScrumProjectExt();
        RestWriter restWriter = getRestWriterJSON();
        marshalScrumExtension(scrum, restWriter);
        assertEqualsJSON(INITIAL_SCRUM_EXTENSION_JSON);
    }

    @Test
    public void testMarshalExtensionJSON() throws Exception {
        ScrumProjectExt scrum = newScrumExtension();
        RestWriter restWriter = getRestWriterJSON();
        marshalScrumExtension(scrum, restWriter);
        assertEqualsJSON(SCRUM_EXTENSION_JSON);
    }

    @Test
    public void testUnmarshallInitialJSON() throws Exception {
        RestReader restReader = getRestReaderJSON(INITIAL_SCRUM_EXTENSION_JSON);
        ScrumProjectExt scrum = unmarshalPeopleExtension(restReader);
        assertEquals("", scrum.getBacklogUrl());
        assertTrue(scrum.getScrumMasters().isEmpty());
        assertTrue(scrum.getProductOwners().isEmpty());
    }

    @Test
    public void testUnmarshallJSON() throws Exception {
        RestReader restReader = getRestReaderJSON(SCRUM_EXTENSION_JSON);
        ScrumProjectExt scrum = unmarshalPeopleExtension(restReader);
        assertEquals("http://example.org:8080/backlog", scrum.getBacklogUrl());
        AssertUtils.assertEquals("getScrumMasters", scrum.getScrumMasters(), new Member("a"));
        AssertUtils.assertEquals("getProductOwners", scrum.getProductOwners(), new Member("b"), new Member("c"));
    }

    @Test
    public void testUnmarshallIgnoreUnknownAttributesJSON() throws Exception {
        RestReader restReader = getRestReaderJSON(SCRUM_EXTENSION_UNKNOWN_ATTR_JSON);
        ScrumProjectExt scrum = unmarshalPeopleExtension(restReader);
        assertEquals("http://example.org:8080/backlog", scrum.getBacklogUrl());
        AssertUtils.assertEquals("getScrumMasters", scrum.getScrumMasters(), new Member("a"));
        AssertUtils.assertEquals("getProductOwners", scrum.getProductOwners(), new Member("b"), new Member("c"));
    }

    private ScrumProjectExt newScrumExtension() {
        ScrumProjectExt scrum = new ScrumProjectExt();
        scrum.setBacklogUrl("http://example.org:8080/backlog");
        scrum.addScrumMaster(new Member("a"));
        scrum.addProductOwner(new Member("b"));
        scrum.addProductOwner(new Member("c"));
        return scrum;
    }

    private void marshalScrumExtension(ScrumProjectExt scrum, RestWriter restWriter) throws Exception {
        ScrumConverter converter = new ScrumConverter();
        restWriter.object("scrum");
        converter.marshal(scrum, restWriter);
        restWriter.end();
        restWriter.flush();
    }

    private ScrumProjectExt unmarshalPeopleExtension(RestReader restReader) throws Exception {
        ScrumConverter converter = new ScrumConverter();
        restReader.object();
        ScrumProjectExt scrum = converter.unmarshal(restReader);
        restReader.end();
        return scrum;
    }
}
