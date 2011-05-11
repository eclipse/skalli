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
package org.eclipse.skalli.api.rest.internal.resources;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.api.java.ProjectService;
import org.eclipse.skalli.common.util.XMLUtils;
import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.model.core.ProjectMember;
import org.eclipse.skalli.testutil.MarshallingContextMock;
import org.eclipse.skalli.testutil.ProjectServiceUtils;
import org.eclipse.skalli.testutil.RestUtils;
import org.eclipse.skalli.testutil.StringBufferHierarchicalStreamWriter;
import org.eclipse.skalli.testutil.XMLDiffUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

@SuppressWarnings("nls")
public class ProjectConverterTest {

    private static class ProjectConverterWrapper extends ProjectConverter {
        public ProjectConverterWrapper(String host, String[] extensions) {
            super(host, extensions, false);
        }

        @Override
        public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
            writer.startNode("project");
            super.marshal(source, writer, context);
            writer.endNode();
        }
    }

    private static class ProjectsConverterWrapper extends ProjectsConverter {
        public ProjectsConverterWrapper(String host, String[] extensions) {
            super(host, extensions);
        }

        @Override
        public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
            writer.startNode("projects");
            super.marshal(source, writer, context);
            writer.endNode();
        }
    }

    private List<Project> projects;
    private ProjectService projectService;

    @Before
    public void setup() throws Exception {
        projectService = ProjectServiceUtils.getProjectService();
        projects = projectService.getAll();
        Assert.assertTrue("projects.size() > 0", projects.size() > 0);
    }

    @Test
    public void testMarshal() throws Exception {
        for (Project project : projects) {
            //marshal the project using ProjectConverter
            Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.ENGLISH); //$NON-NLS-1$
            project.setLastModified(DatatypeConverter.printDateTime(now));
            StringBufferHierarchicalStreamWriter writer = new StringBufferHierarchicalStreamWriter();
            ProjectConverterWrapper converter = new ProjectConverterWrapper("https://localhost",
                    new String[] { "members" });
            MarshallingContext context = new MarshallingContextMock(writer);
            converter.marshal(project, writer, context);

            // render the expected output of the writer
            StringBuilder expected = new StringBuilder();
            expected.append(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                            +
                            "<project "
                            +
                            "xmlns=\"http://www.eclipse.org/skalli/2010/API\" "
                            +
                            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                            +
                            "xsi:schemaLocation=\"http://www.eclipse.org/skalli/2010/API https://localhost/schemas/project.xsd\" "
                            +
                            "apiVersion=\"" + ProjectConverter.API_VERSION + "\" " +
                            "lastModified=\"" + project.getLastModified() + "\" " +
                            "modifiedBy=\"" + project.getLastModifiedBy() + "\">");
            expected.append("  <uuid>").append(project.getUuid().toString()).append("</uuid>");
            expected.append("  <id>").append(enc(project.getProjectId())).append("</id>");
            expected.append("  <template>").append(enc(project.getProjectTemplateId())).append("</template>");
            expected.append("  <name>").append(enc(project.getName())).append("</name>");
            expected.append("  <link rel=\"project\" href=\"https://localhost/api/projects/")
                    .append(project.getUuid().toString()).append("\"/>");
            expected.append("  <link rel=\"browse\" href=\"https://localhost/projects/").append(project.getProjectId())
                    .append("\"/>");
            expected.append("  <link rel=\"issues\" href=\"https://localhost/api/projects/")
                    .append(project.getUuid().toString()).append("/issues\"/>");
            expected.append("  <phase>").append(enc(project.getPhase())).append("</phase>");
            if (StringUtils.isNotBlank(enc(project.getDescription()))) {
                expected.append("  <description>").append(enc(project.getDescription())).append("</description>");
            }
            if (project.getParentProject() != null) {
                expected.append("  <link rel=\"parent\" href=\"https://localhost/api/projects/")
                        .append(enc(project.getParentProject().toString())).append("\"/>");
            }
            List<Project> subprojects = projectService.getSubProjects(project.getUuid());
            if (subprojects.size() > 0) {
                expected.append("  <subprojects>");
                for (Project subproject : subprojects) {
                    expected.append("    <link rel=\"subproject\" href=\"https://localhost/api/projects/")
                            .append(enc(subproject.getUuid().toString())).append("\"/>");
                }
                expected.append("  </subprojects>");
            }

            Set<ProjectMember> allPeople = projectService.getAllPeople(project);
            if (allPeople.size() > 0) {
                expected.append("  <members>");
                for (ProjectMember member : allPeople) {
                    expected.append("    <member>");
                    expected.append("      <userId>").append(enc(member.getUserID())).append("</userId>");
                    expected.append("      <link rel=\"user\" href=\"https://localhost/api/user/")
                            .append(enc(member.getUserID())).append("\"/>");
                    for (Entry<String, Set<ProjectMember>> entry : projectService.getAllPeopleByRole(project)
                            .entrySet()) {
                        if (entry.getValue().contains(member)) {
                            expected.append("      <role>").append(enc(entry.getKey())).append("</role>");
                        }
                    }
                    expected.append("    </member>");
                }
                expected.append("  </members>");
            }
            expected.append("  <extensions/>");
            expected.append("</project>");

            // compare marshalled and expected output
            Document expectedDoc = XMLUtils.documentFromString(expected.toString());
            Document actualDoc = XMLUtils.documentFromString(writer.toString());
            XMLDiffUtil.assertEquals(expectedDoc, actualDoc, true);
        }
    }

    @Test
    public void testValidate() throws Exception {
        RestUtils.validate(projects, new ProjectConverterWrapper("https://localhost", new String[] { "members" }),
                "project.xsd");
        Projects plist = new Projects(projects);
        RestUtils.validate(plist, new ProjectsConverterWrapper("https://localhost", new String[] { "members" }),
                "projects.xsd");
    }

    private String enc(String s) {
        if (s == null) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            switch (s.charAt(i)) {
            case '&':
                buffer.append("&amp;"); //$NON-NLS-1$
                continue;
            case '<':
                buffer.append("&lt;"); //$NON-NLS-1$
                continue;
            case '>':
                buffer.append("&gt;"); //$NON-NLS-1$
                continue;
            case '\'':
                buffer.append("&apos;"); //$NON-NLS-1$
                continue;
            case '"':
                buffer.append("&quot;"); //$NON-NLS-1$
                continue;
            case '\r':
                buffer.append("&#xd;"); //$NON-NLS-1$
                continue;
            default:
                buffer.append(s.charAt(i));
            }
        }
        return buffer.toString();
    }
}