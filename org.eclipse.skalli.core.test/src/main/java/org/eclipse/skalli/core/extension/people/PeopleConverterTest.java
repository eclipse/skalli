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
package org.eclipse.skalli.core.extension.people;

import org.eclipse.skalli.model.Member;
import org.eclipse.skalli.model.ext.commons.PeopleExtension;
import org.eclipse.skalli.services.rest.RestWriter;
import org.eclipse.skalli.testutil.RestWriterTestBase;
import org.junit.Test;

@SuppressWarnings("nls")
public class PeopleConverterTest extends RestWriterTestBase {

    @Test
    public void testMarshalBlankExtensionXML() throws Exception {
        PeopleExtension people = new PeopleExtension();
        RestWriter restWriter = getRestWriterXML();
        marshalPeopleExtension(people, restWriter);
        assertEqualsXML("<people><leads/><members/></people>");
    }

    @Test
    public void testMarshalXML() throws Exception {
        PeopleExtension people = newPeopleExtension();
        RestWriter restWriter = getRestWriterXML();
        marshalPeopleExtension(people, restWriter);
        assertEqualsXML("<people>"
                + "<leads>"
                + "<lead><userId>a</userId><link rel=\"user\" href=\"http://example.org/api/users/a\"/></lead>"
                + "<lead><userId>b</userId><link rel=\"user\" href=\"http://example.org/api/users/b\"/></lead>"
                + "</leads>"
                + "<members>"
                + "<member><userId>c</userId><link rel=\"user\" href=\"http://example.org/api/users/c\"/></member>"
                + "</members></people>");
    }

    @Test
    public void testMarshalBlankExtensionJSON() throws Exception {
        PeopleExtension people = new PeopleExtension();
        RestWriter restWriter = getRestWriterJSON();
        marshalPeopleExtension(people, restWriter);
        assertEqualsJSON("{\"leads\":[],\"members\":[]}");
    }

    @Test
    public void testMarshalJSON() throws Exception {
        PeopleExtension people = newPeopleExtension();
        RestWriter restWriter = getRestWriterJSON();
        marshalPeopleExtension(people, restWriter);
        assertEqualsJSON("{\"leads\":["
                + "{\"userId\":\"a\",\"link\":{\"rel\":\"user\",\"href\":\"http://example.org/api/users/a\"}},"
                + "{\"userId\":\"b\",\"link\":{\"rel\":\"user\",\"href\":\"http://example.org/api/users/b\"}}],"
                + "\"members\":["
                + "{\"userId\":\"c\",\"link\":{\"rel\":\"user\",\"href\":\"http://example.org/api/users/c\"}}]}");
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
}
