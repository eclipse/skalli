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
package org.eclipse.skalli.core.rest.resources;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.skalli.core.rest.JSONRestWriter;
import org.eclipse.skalli.core.rest.XMLRestWriter;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.services.rest.RestWriter;
import org.eclipse.skalli.testutil.RestWriterTestBase;
import org.eclipse.skalli.testutil.TestUUIDs;
import org.junit.Test;

@SuppressWarnings("nls")
public class SubprojectsConverterTest extends RestWriterTestBase {

    private static final String NAMESPACE_ATTRIBUTES = MessageFormat.format(ATTRIBUTES_PATTERN,
            SubprojectsConverter.NAMESPACE, "subprojects", SubprojectsConverter.API_VERSION);

    private static final String COMMON_PART_XML = MessageFormat.format(
            "<subprojects {0}>"
            + "<link rel=\"self\" href=\"http://example.org/api/projects/{1}/subprojects\"/>"
            + "<link rel=\"project\" href=\"http://example.org/api/projects/{1}\"/>",
            NAMESPACE_ATTRIBUTES, TestUUIDs.TEST_UUIDS[0]);

    private static final String COMMON_PART_JSON = MessageFormat.format(
            "'{'"
            + "\"apiVersion\":\"{0}\",\"links\":"
            + "['{'\"rel\":\"self\",\"href\":\"http://example.org/api/projects/{1}/subprojects\"},"
            + "'{'\"rel\":\"project\",\"href\":\"http://example.org/api/projects/{1}\"}]",
            SubprojectsConverter.API_VERSION, TestUUIDs.TEST_UUIDS[0]);

    @Test
    public void testMarshalEmptySubprojectsXML() throws Exception {
        Subprojects subprojects = new Subprojects(TestUUIDs.TEST_UUIDS[0], Collections.<Project>emptyList());
        XMLRestWriter restWriter = new XMLRestWriter(writer, "http://example.org");
        marshalSubprojects(subprojects, restWriter);
        assertEqualsXML(COMMON_PART_XML + "</subprojects>");
    }

    @Test
    public void testMarshalEmptySubprojectsJSON() throws Exception {
        Subprojects subprojects = new Subprojects(TestUUIDs.TEST_UUIDS[0], Collections.<Project>emptyList());
        JSONRestWriter restWriter = new JSONRestWriter(writer, "http://example.org");
        marshalSubprojects(subprojects, restWriter);
        assertEqualsJSON(COMMON_PART_JSON + ",\"subprojects\":[]}");
    }

    @Test
    public void testMarshalSubprojectsXML() throws Exception {
        Subprojects subprojects = newSubprojects();
        XMLRestWriter restWriter = new XMLRestWriter(writer, "http://example.org");
        marshalSubprojects(subprojects, restWriter);
        assertEqualsXML(COMMON_PART_XML
                + CommonProjectConverterTest.MINIMAL_PROJECT_XML(TestUUIDs.TEST_UUIDS[3], "id1", "name1", true)
                + CommonProjectConverterTest.MINIMAL_PROJECT_XML(TestUUIDs.TEST_UUIDS[4], "id2", "name2", true)
                + CommonProjectConverterTest.MINIMAL_PROJECT_XML(TestUUIDs.TEST_UUIDS[5], "id2", "name2", true)
                +"</subprojects>");
    }

    @Test
    public void testMarshalSubprojectsJSON() throws Exception {
        Subprojects subprojects = newSubprojects();
        JSONRestWriter restWriter = new JSONRestWriter(writer, "http://example.org");
        marshalSubprojects(subprojects, restWriter);
        assertEqualsJSON(COMMON_PART_JSON
                + ",\"subprojects\":["
                + CommonProjectConverterTest.MINIMAL_PROJECT_JSON(TestUUIDs.TEST_UUIDS[3], "id1", "name1")
                + "," + CommonProjectConverterTest.MINIMAL_PROJECT_JSON(TestUUIDs.TEST_UUIDS[4], "id2", "name2")
                + "," + CommonProjectConverterTest.MINIMAL_PROJECT_JSON(TestUUIDs.TEST_UUIDS[5], "id2", "name2")
                +"]}");
    }

    private Subprojects newSubprojects() {
        List<Project> projects = new ArrayList<Project>();
        projects.add(CommonProjectConverterTest.newMinimalProject(TestUUIDs.TEST_UUIDS[3], "id1", "name1"));
        projects.add(CommonProjectConverterTest.newMinimalProject(TestUUIDs.TEST_UUIDS[4], "id2", "name2"));
        projects.add(CommonProjectConverterTest.newMinimalProject(TestUUIDs.TEST_UUIDS[5], "id2", "name2"));
        Subprojects subprojects = new Subprojects(TestUUIDs.TEST_UUIDS[0], projects);
        return subprojects;
    }

    private void marshalSubprojects(Subprojects subprojects, RestWriter restWriter) throws Exception {
        SubprojectsConverter converter = new SubprojectsConverter();
        converter.marshal(subprojects, restWriter);
        restWriter.flush();
    }
}
