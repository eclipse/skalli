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
package org.eclipse.skalli.model.ext.misc.internal;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.eclipse.skalli.commons.UUIDList;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.ext.misc.RelatedProjectsExt;
import org.eclipse.skalli.services.project.ProjectService;
import org.eclipse.skalli.services.rest.RestWriter;
import org.eclipse.skalli.testutil.BundleManager;
import org.eclipse.skalli.testutil.RestWriterTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("nls")
public class RelatedProjectsConverterTest extends RestWriterTestBase {

    private List<Project> projects;
    private ProjectService projectService;

    @Override
    @Before
    public void setup() throws Exception {
        super.setup();
        projectService = BundleManager.getRequiredService(ProjectService.class);
        projects = projectService.getAll();
        Assert.assertTrue("projects.size() > 0", projects.size() > 0);
    }

    @Test
    public void testMarshalBlankExtensionXML() throws Exception {
        RelatedProjectsExt related = new RelatedProjectsExt();
        RestWriter restWriter = getRestWriterXML();
        marshalRelatedProjects(related, restWriter);
        assertEqualsXML("<relatedProjects><calculated>false</calculated></relatedProjects>");
    }

    @Test
    public void testMarshalExtensionXML() throws Exception {
        RelatedProjectsExt related = newRelatedProjects();
        RestWriter restWriter = getRestWriterXML();
        marshalRelatedProjects(related, restWriter);
        assertEqualsXML("<relatedProjects>"
                + "<calculated>false</calculated>"
                + "<link rel=\"project\" href=\"http://example.org/api/projects/ab721fce-25c7-4c9f-b5ad-9f1f59b23b6a\"/>"
                + "<link rel=\"project\" href=\"http://example.org/api/projects/5856b08a-0f87-4d91-b007-ac367ced247a\"/>"
                + "</relatedProjects>");
    }

    @Test
    public void testMarshalBlankExtensionJSON() throws Exception {
        RelatedProjectsExt related = new RelatedProjectsExt();
        RestWriter restWriter = getRestWriterJSON();
        marshalRelatedProjects(related, restWriter);
        assertEqualsJSON("{\"calculated\":false,\"links\":[]}");
    }

    @Test
    public void testMarshalExtensionJSON() throws Exception {
        RelatedProjectsExt related = newRelatedProjects();
        RestWriter restWriter = getRestWriterJSON();
        marshalRelatedProjects(related, restWriter);
        assertEqualsJSON("{\"calculated\":false,\"links\":["
                + "{\"rel\":\"project\",\"href\":\"http://example.org/api/projects/ab721fce-25c7-4c9f-b5ad-9f1f59b23b6a\","
                + "\"uuid\":\"ab721fce-25c7-4c9f-b5ad-9f1f59b23b6a\",\"id\":\"testproject\",\"name\":\"Test Project\"},"
                + "{\"rel\":\"project\",\"href\":\"http://example.org/api/projects/5856b08a-0f87-4d91-b007-ac367ced247a\","
                + "\"uuid\":\"5856b08a-0f87-4d91-b007-ac367ced247a\",\"id\":\"eclipse.skalli\",\"name\":\"Skalli\"}"
                + "]}");
    }

    private RelatedProjectsExt newRelatedProjects() {
        RelatedProjectsExt related = new RelatedProjectsExt();
        List<UUID> uuids = Arrays.asList(
                UUID.fromString("ab721fce-25c7-4c9f-b5ad-9f1f59b23b6a"),
                UUID.fromString("5856b08a-0f87-4d91-b007-ac367ced247a"));
        related.setRelatedProjects(new UUIDList(uuids));
        return related;
    }

    private void marshalRelatedProjects(RelatedProjectsExt related, RestWriter restWriter) throws Exception {
        RelatedProjectsConverter converter = new RelatedProjectsConverter();
        restWriter.object("relatedProjects");
        converter.marshal(related, restWriter);
        restWriter.end();
        restWriter.flush();
    }
}
