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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.UUID;

import org.eclipse.skalli.model.Derived;
import org.eclipse.skalli.model.ExtensibleEntityBase;
import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.Member;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.extension.ExtensionService;
import org.eclipse.skalli.services.extension.ExtensionServices;
import org.eclipse.skalli.services.extension.rest.RestConverter;
import org.eclipse.skalli.services.extension.rest.RestConverterBase;
import org.eclipse.skalli.services.extension.rest.RestUtils;
import org.eclipse.skalli.services.project.ProjectService;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public abstract class CommonProjectConverter extends RestConverterBase<Project> {

    private final List<String> extensions;
    private boolean allExtensions;
    private boolean omitNSAttributes;

    public CommonProjectConverter(String host, boolean omitNSAttributes) {
        this(host, new String[] {}, omitNSAttributes);
        this.allExtensions = true;
    }

    public CommonProjectConverter(String host, String[] extensions, boolean omitNSAttributes) {
        super(Project.class, "project", host); //$NON-NLS-1$
        if (extensions != null) {
            this.extensions = Arrays.asList(extensions);
        } else {
            this.extensions = Collections.<String> emptyList();
        }
        this.allExtensions = false;
        this.omitNSAttributes = omitNSAttributes;
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        Project project = (Project) source;
        String host = getHost();
        if (!omitNSAttributes) {
            marshalNSAttributes(writer);
        }
        marshalCommonAttributes(writer, project);
        writeNode(writer, "uuid", project.getUuid().toString()); //$NON-NLS-1$
        writeNode(writer, "id", project.getProjectId()); //$NON-NLS-1$
        writeNode(writer, "template", project.getProjectTemplateId()); //$NON-NLS-1$
        writeNode(writer, "name", project.getName()); //$NON-NLS-1$
        writeNode(writer, "shortName", project.getShortName()); //$NON-NLS-1$
        writeProjectLink(writer, PROJECT_RELATION, project.getUuid());
        writeLink(writer, BROWSE_RELATION, host + RestUtils.URL_BROWSE + project.getProjectId());
        writeLink(writer, ISSUES_RELATION, host + RestUtils.URL_PROJECTS + project.getUuid().toString() + RestUtils.URL_ISSUES);
        writeNode(writer, "phase", project.getPhase()); //$NON-NLS-1$
        writeNode(writer, "description", project.getDescription()); //$NON-NLS-1$
        UUID parent = project.getParentProject();
        if (parent != null) {
            writeProjectLink(writer, PARENT_RELATION, parent);
        }
        ProjectService projectService = Services.getRequiredService(ProjectService.class);
        List<Project> subprojects = projectService.getSubProjects(project.getUuid());
        if (subprojects.size() > 0) {
            writer.startNode("subprojects"); //$NON-NLS-1$
            for (Project subproject : subprojects) {
                writeProjectLink(writer, SUBPROJECT_RELATION, subproject.getUuid());
            }
            writer.endNode();
        }

        marshalMembers(project, projectService, writer);
        marshalExtensions(project, writer, context);
    }

    private void marshalMembers(Project project, ProjectService projectService, HierarchicalStreamWriter writer) {
        if (allExtensions || extensions.contains("members")) { //$NON-NLS-1$
            writer.startNode("members"); //$NON-NLS-1$
            for (Member member : projectService.getMembers(project)) {
                writer.startNode("member"); //$NON-NLS-1$
                writeNode(writer, "userId", member.getUserID()); //$NON-NLS-1$
                writeUserLink(writer, USER_RELATION, member.getUserID());
                for (Entry<String, SortedSet<Member>> entry : projectService.getMembersByRole(project).entrySet()) {
                    if (entry.getValue().contains(member)) {
                        writeNode(writer, "role", entry.getKey()); //$NON-NLS-1$
                    }
                }
                writer.endNode();
            }
            writer.endNode();
        }
    }

    private void marshalExtensions(ExtensibleEntityBase extensibleEntity, HierarchicalStreamWriter writer,
            MarshallingContext context) {
        writer.startNode("extensions"); //$NON-NLS-1$
        for (ExtensionService<?> extensionService : ExtensionServices.getAll()) {
            if (allExtensions || extensions.contains(extensionService.getShortName())) {
                marshalExtension(extensibleEntity, extensionService, writer, context);
            }
        }
        writer.endNode();
    }

    protected void marshalExtension(ExtensibleEntityBase extensibleEntity, ExtensionService<?> extensionService,
            HierarchicalStreamWriter writer, MarshallingContext context) {
        Class<? extends ExtensionEntityBase> extensionClass = extensionService.getExtensionClass();
        if (extensionClass.equals(Project.class)) {
            return;
        }
        ExtensionEntityBase extension = extensibleEntity.getExtension(extensionClass);
        RestConverter converter = extensionService.getRestConverter(getHost());
        if (extension != null && converter != null) {
            writer.startNode(extensionService.getShortName());
            marshalNSAttributes(writer, converter);
            marshalCommonAttributes(writer, extension, converter);
            writer.addAttribute("inherited", Boolean.toString(extensibleEntity.isInherited(extensionClass))); //$NON-NLS-1$
            writer.addAttribute("derived", Boolean.toString(extensionClass.isAnnotationPresent(Derived.class))); //$NON-NLS-1$
            context.convertAnother(extension, converter);
            writer.endNode();
        }
    }
}
