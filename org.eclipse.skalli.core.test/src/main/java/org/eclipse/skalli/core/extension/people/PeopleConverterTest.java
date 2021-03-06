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
package org.eclipse.skalli.core.extension.people;

import static org.junit.Assert.assertTrue;

import org.eclipse.skalli.model.Member;
import org.eclipse.skalli.model.ext.commons.PeopleExtension;
import org.eclipse.skalli.services.rest.RestReader;
import org.eclipse.skalli.services.rest.RestWriter;
import org.eclipse.skalli.testutil.AssertUtils;
import org.eclipse.skalli.testutil.RestWriterTestBase;
import org.junit.Test;

@SuppressWarnings("nls")
public class PeopleConverterTest extends RestWriterTestBase {

    private static final String INITIAL_PEOPLE_EXTENSION_XML = "<people><leads/><members/></people>";
    private static final String PEOPLE_EXTENSION_XML = "<people>"
            + "<leads>"
            + "<lead><userId>a</userId><link rel=\"user\" href=\"http://example.org:8080/api/users/a\"/></lead>"
            + "<lead><userId>b</userId><link rel=\"user\" href=\"http://example.org:8080/api/users/b\"/></lead>"
            + "</leads>"
            + "<members>"
            + "<member><userId>c</userId><link rel=\"user\" href=\"http://example.org:8080/api/users/c\"/></member>"
            + "</members></people>";
    private static final String INITIAL_PEOPLE_EXTENSION_JSON = "{\"leads\":[],\"members\":[]}";
    private static final String PEOPLE_EXTENSION_JSON = "{\"leads\":["
            + "{\"userId\":\"a\",\"link\":{\"rel\":\"user\",\"href\":\"http://example.org:8080/api/users/a\"}},"
            + "{\"userId\":\"b\",\"link\":{\"rel\":\"user\",\"href\":\"http://example.org:8080/api/users/b\"}}],"
            + "\"members\":["
            + "{\"userId\":\"c\",\"link\":{\"rel\":\"user\",\"href\":\"http://example.org:8080/api/users/c\"}}]}";
    private static final String PEOPLE_EXTENSION_UNKNOWN_ATTR_JSON = "{"
            + "\"ignore\":true,"
            + "\"members\":[{\"userId\":\"c\",\"link\":{\"rel\":\"user\",\"href\":\"http://example.org:8080/api/users/c\"}}],"
            + "\"unknown\":\"yes\","
            + "\"leads\":[{\"userId\":\"a\","
            + "\"ignore\":true,"
            + "\"link\":{\"rel\":\"user\",\"href\":\"http://example.org:8080/api/users/b\",\"userId\":\"b\"}}]}";

    @Test
    public void testMarshalBlankExtensionXML() throws Exception {
        PeopleExtension people = new PeopleExtension();
        RestWriter restWriter = getRestWriterXML();
        marshalPeopleExtension(people, restWriter);
        assertEqualsXML(INITIAL_PEOPLE_EXTENSION_XML);
    }

    @Test
    public void testMarshalXML() throws Exception {
        PeopleExtension people = newPeopleExtension();
        RestWriter restWriter = getRestWriterXML();
        marshalPeopleExtension(people, restWriter);
        assertEqualsXML(PEOPLE_EXTENSION_XML);
    }

    @Test
    public void testMarshalBlankExtensionJSON() throws Exception {
        PeopleExtension people = new PeopleExtension();
        RestWriter restWriter = getRestWriterJSON();
        marshalPeopleExtension(people, restWriter);
        assertEqualsJSON(INITIAL_PEOPLE_EXTENSION_JSON);
    }

    @Test
    public void testMarshalJSON() throws Exception {
        PeopleExtension people = newPeopleExtension();
        RestWriter restWriter = getRestWriterJSON();
        marshalPeopleExtension(people, restWriter);
        assertEqualsJSON(PEOPLE_EXTENSION_JSON);
    }

    @Test
    public void testUnmarshallInitialJSON() throws Exception {
        RestReader restReader = getRestReaderJSON(INITIAL_PEOPLE_EXTENSION_JSON);
        PeopleExtension people = unmarshalPeopleExtension(restReader);
        assertTrue(people.getLeads().isEmpty());
        assertTrue(people.getMembers().isEmpty());
    }

    @Test
    public void testUnmarshallJSON() throws Exception {
        RestReader restReader = getRestReaderJSON(PEOPLE_EXTENSION_JSON);
        PeopleExtension people = unmarshalPeopleExtension(restReader);
        AssertUtils.assertEquals("getLeads", people.getLeads(), new Member("a"), new Member("b"));
        AssertUtils.assertEquals("getMembers", people.getMembers(), new Member("c"));
    }

    @Test
    public void testUnmarshallIgnoreUnknownAttributesJSON() throws Exception {
        RestReader restReader = getRestReaderJSON(PEOPLE_EXTENSION_UNKNOWN_ATTR_JSON);
        PeopleExtension people = unmarshalPeopleExtension(restReader);
        AssertUtils.assertEquals("getLeads", people.getLeads(), new Member("a"));
        AssertUtils.assertEquals("getMembers", people.getMembers(), new Member("c"));
    }

    private PeopleExtension newPeopleExtension() {
        PeopleExtension people = new PeopleExtension();
        people.addLead(new Member("a"));
        people.addLead(new Member("b"));
        people.addMember(new Member("c"));
        return people;
    }

    private void marshalPeopleExtension(PeopleExtension people, RestWriter restWriter) throws Exception {
        PeopleConverter converter = new PeopleConverter();
        restWriter.object("people");
        converter.marshal(people, restWriter);
        restWriter.end();
        restWriter.flush();
    }

    private PeopleExtension unmarshalPeopleExtension(RestReader restReader) throws Exception {
        PeopleConverter converter = new PeopleConverter();
        restReader.object();
        PeopleExtension people = converter.unmarshal(restReader);
        restReader.end();
        return people;
    }
}
