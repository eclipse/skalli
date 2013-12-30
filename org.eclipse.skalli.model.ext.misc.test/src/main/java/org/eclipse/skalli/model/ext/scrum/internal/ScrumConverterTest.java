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
package org.eclipse.skalli.model.ext.scrum.internal;

import org.eclipse.skalli.model.Member;
import org.eclipse.skalli.model.ext.scrum.ScrumProjectExt;
import org.eclipse.skalli.services.rest.RestWriter;
import org.eclipse.skalli.testutil.RestWriterTestBase;
import org.junit.Test;

@SuppressWarnings("nls")
public class ScrumConverterTest extends RestWriterTestBase {

    @Test
    public void testMarshalBlankExtensionXML() throws Exception {
        ScrumProjectExt scrum = new ScrumProjectExt();
        RestWriter restWriter = getRestWriterXML();
        marshalScrumExtension(scrum, restWriter);
        assertEqualsXML("<scrum><scrumMasters/><productOwners/></scrum>");
    }

    @Test
    public void testMarshalExtensionXML() throws Exception {
        ScrumProjectExt scrum = newScrumExtension();
        RestWriter restWriter = getRestWriterXML();
        marshalScrumExtension(scrum, restWriter);
        assertEqualsXML("<scrum>"
                + "<scrumMasters>"
                + "<scrumMaster><userId>a</userId><link rel=\"user\" href=\"http://example.org/api/users/a\"/></scrumMaster>"
                + "</scrumMasters>"
                + "<productOwners>"
                + "<productOwner><userId>b</userId><link rel=\"user\" href=\"http://example.org/api/users/b\"/></productOwner>"
                + "<productOwner><userId>c</userId><link rel=\"user\" href=\"http://example.org/api/users/c\"/></productOwner>"
                + "</productOwners></scrum>");
    }

    @Test
    public void testMarshalBlankExtensionJSON() throws Exception {
        ScrumProjectExt scrum = new ScrumProjectExt();
        RestWriter restWriter = getRestWriterJSON();
        marshalScrumExtension(scrum, restWriter);
        assertEqualsJSON("{\"scrumMasters\":[],\"productOwners\":[]}");
    }

    @Test
    public void testMarshalExtensionJSON() throws Exception {
        ScrumProjectExt scrum = newScrumExtension();
        RestWriter restWriter = getRestWriterJSON();
        marshalScrumExtension(scrum, restWriter);
        assertEqualsJSON("{\"scrumMasters\":["
                + "{\"userId\":\"a\",\"link\":{\"rel\":\"user\",\"href\":\"http://example.org/api/users/a\"}}],"
                + "\"productOwners\":["
                + "{\"userId\":\"b\",\"link\":{\"rel\":\"user\",\"href\":\"http://example.org/api/users/b\"}},"
                + "{\"userId\":\"c\",\"link\":{\"rel\":\"user\",\"href\":\"http://example.org/api/users/c\"}}]}");
    }

    private ScrumProjectExt newScrumExtension() {
        ScrumProjectExt scrum = new ScrumProjectExt();
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
}
