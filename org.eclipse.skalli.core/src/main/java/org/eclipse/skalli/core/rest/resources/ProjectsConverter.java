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

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.extension.rest.RestConverterBase;
import org.eclipse.skalli.services.project.ProjectService;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

class ProjectsConverter extends RestConverterBase<Projects> {

    private String[] extensions;
    private int start = 0;

    public ProjectsConverter(String host, String[] extensions) {
        super(Projects.class, "projects", host); //$NON-NLS-1$
        this.extensions = extensions;
    }

    public ProjectsConverter(String host, String[] extensions, int start) {
        this(host, extensions);
        this.start = start;
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        marshalNSAttributes(writer);
        marshalApiVersion(writer);

        List<Project> projects = ((Projects) source).getProjects();

        writer.addAttribute("start", Integer.toString(start)); //$NON-NLS-1$
        writer.addAttribute("count", Integer.toString(projects.size())); //$NON-NLS-1$

        ProjectService projectService = Services.getRequiredService(ProjectService.class);
        Map<UUID, List<Project>> subprojects = projectService.getSubProjects();

        for (Project project : projects) {
            writer.startNode("project"); //$NON-NLS-1$
            new ProjectConverter(getHost(), extensions, subprojects, true).marshal(project, writer, context);
            writer.endNode();
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader arg0, UnmarshallingContext arg1) {
        // don't support that yet
        return null;
    }

    @Override
    public String getApiVersion() {
        return ProjectConverter.API_VERSION;
    }

    @Override
    public String getNamespace() {
        return ProjectConverter.NAMESPACE;
    }

    @Override
    public String getXsdFileName() {
        return "projects.xsd"; //$NON-NLS-1$
    }
}
