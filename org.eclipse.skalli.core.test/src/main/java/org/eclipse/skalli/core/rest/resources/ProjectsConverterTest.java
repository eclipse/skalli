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
import java.util.List;

import org.eclipse.skalli.core.rest.JSONRestWriter;
import org.eclipse.skalli.core.rest.XMLRestWriter;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.services.rest.RestWriter;
import org.eclipse.skalli.testutil.RestWriterTestBase;
import org.eclipse.skalli.testutil.TestUUIDs;
import org.junit.Test;

@SuppressWarnings("nls")
public class ProjectsConverterTest extends RestWriterTestBase {

    private static final String NAMESPACE_ATTRIBUTES = MessageFormat.format(ATTRIBUTES_PATTERN,
            ProjectConverter.NAMESPACE, "projects", ProjectConverter.API_VERSION);

    private static final String COMMON_PART_XML(int start, int count) {
        return MessageFormat.format(
            "<projects {0} start=\"{1}\" count=\"{2}\">",
            NAMESPACE_ATTRIBUTES, start, count);
    }

    private static final String COMMON_PART_JSON(int start, int count) {
        return MessageFormat.format(
            "'{'\"apiVersion\":\"{0}\",\"start\":{1},\"count\":{2}",
            ProjectConverter.API_VERSION, start, count);
    }

    @Test
    public void testMarshalEmptyProjectsXML() throws Exception {
        Projects projects = new Projects();
        XMLRestWriter restWriter = new XMLRestWriter(writer, "http://example.org");
        marshalProjects(projects, restWriter);
        assertEqualsXML(COMMON_PART_XML(0,0) + "</projects>");
    }

    @Test
    public void testMarshalEmptyProjectsJSON() throws Exception {
        Projects projects = new Projects();
        JSONRestWriter restWriter = new JSONRestWriter(writer, "http://example.org");
        marshalProjects(projects, restWriter);
        assertEqualsJSON(COMMON_PART_JSON(0,0) + ",\"projects\":[]}");
    }

    @Test
    public void testMarshalProjectsXML() throws Exception {
        Projects projects = newProjects();
        XMLRestWriter restWriter = new XMLRestWriter(writer, "http://example.org");
        marshalProjects(projects, restWriter);
        assertEqualsXML(COMMON_PART_XML(0,3)
                + CommonProjectConverterTest.MINIMAL_PROJECT_XML(TestUUIDs.TEST_UUIDS[3], "id1", "name1", true)
                + CommonProjectConverterTest.MINIMAL_PROJECT_XML(TestUUIDs.TEST_UUIDS[4], "id2", "name2", true)
                + CommonProjectConverterTest.MINIMAL_PROJECT_XML(TestUUIDs.TEST_UUIDS[5], "id2", "name2", true)
                + "</projects>");
    }

    @Test
    public void testMarshalProjectsJSON() throws Exception {
        Projects projects = newProjects();
        JSONRestWriter restWriter = new JSONRestWriter(writer, "http://example.org");
        marshalProjects(projects, restWriter);
        assertEqualsJSON(COMMON_PART_JSON(0,3)
                + ",\"projects\":["
                + CommonProjectConverterTest.MINIMAL_PROJECT_JSON(TestUUIDs.TEST_UUIDS[3], "id1", "name1")
                + "," + CommonProjectConverterTest.MINIMAL_PROJECT_JSON(TestUUIDs.TEST_UUIDS[4], "id2", "name2")
                + "," + CommonProjectConverterTest.MINIMAL_PROJECT_JSON(TestUUIDs.TEST_UUIDS[5], "id2", "name2")
                + "]}");
    }

    private Projects newProjects() {
        List<Project> list = new ArrayList<Project>();
        list.add(CommonProjectConverterTest.newMinimalProject(TestUUIDs.TEST_UUIDS[3], "id1", "name1"));
        list.add(CommonProjectConverterTest.newMinimalProject(TestUUIDs.TEST_UUIDS[4], "id2", "name2"));
        list.add(CommonProjectConverterTest.newMinimalProject(TestUUIDs.TEST_UUIDS[5], "id2", "name2"));
        return new Projects(list);
    }

    private void marshalProjects(Projects projects, RestWriter restWriter) throws Exception {
        ProjectsConverter converter = new ProjectsConverter();
        converter.marshal(projects, restWriter);
        restWriter.flush();
    }
}
