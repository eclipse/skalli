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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.Member;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.extension.ExtensionService;
import org.eclipse.skalli.services.extension.ExtensionServices;
import org.eclipse.skalli.services.extension.rest.RestConverter;
import org.eclipse.skalli.services.role.RoleProvider;

import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

public class ProjectConverter extends CommonProjectConverter {

    public ProjectConverter() {
        super(true);
    }

    public ProjectConverter(boolean omitNSAttributes) {
        super(omitNSAttributes);
    }

    public ProjectConverter(String[] extensions, boolean omitNSAttributes) {
        super(extensions, omitNSAttributes);
    }

    @SuppressWarnings("nls")
    @Override
    public void marshal(Project project) throws IOException {
        writer.object("project");
        super.marshal(project);
        writer.end();
    }

    @Deprecated
    public ProjectConverter(String host, boolean omitNSAttributes) {
        super(host, omitNSAttributes);
    }

    @Deprecated
    public ProjectConverter(String host, String[] extensions, boolean omitNSAttributes) {
        super(host, extensions, omitNSAttributes);
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        Project project = iterateProjectNodes(null, reader, context);
        return project;
    }

    private Project iterateProjectNodes(Project project, HierarchicalStreamReader reader,
            UnmarshallingContext context) {
        if (project == null) {
            project = new Project();
        }
        while (reader.hasMoreChildren()) {
            reader.moveDown();

            String field = reader.getNodeName();
            String value = reader.getValue();

            if ("members".equals(field) && reader.hasMoreChildren()) { //$NON-NLS-1$
                iterateProjectNodes(project, reader, context);
            } else if ("extensions".equals(field) && reader.hasMoreChildren()) { //$NON-NLS-1$
                Set<ExtensionEntityBase> extensions = iterateExtensions(reader, context);
                for (ExtensionEntityBase extension : extensions) {
                    project.addExtension(extension);
                }
            } else if ("id".equals(field)) { //$NON-NLS-1$
                project.setProjectId(value);
            } else if ("name".equals(field)) { //$NON-NLS-1$
                project.setName(value);
            } else if ("shortName".equals(field)) { //$NON-NLS-1$
                project.setShortName(value);
            } else if ("uuid".equals(field)) { //$NON-NLS-1$
                project.setUuid(UUID.fromString(value));
            } else if ("description".equals(field)) { //$NON-NLS-1$
                project.setDescription(value);
            } else if ("member".equals(field)) { //$NON-NLS-1$
                iterateMemberNodes(project, reader);
            }
            reader.moveUp();
        }
        return project;
    }

    private void iterateMemberNodes(Project project, HierarchicalStreamReader reader) {
        Member member = new Member(null);
        while (reader.hasMoreChildren()) {
            reader.moveDown();

            String field = reader.getNodeName();
            String value = reader.getValue();

            if ("userId".equals(field)) { //$NON-NLS-1$
                member.setUserID(value);
            } else if ("role".equals(field)) { //$NON-NLS-1$
                for (RoleProvider roleProvider : Services.getServices(RoleProvider.class)) {
                    roleProvider.addMember(project, member, value);
                }
            }
            reader.moveUp();
        }
    }

    private Set<ExtensionEntityBase> iterateExtensions(HierarchicalStreamReader reader,
            UnmarshallingContext context) {
        HashSet<ExtensionEntityBase> extensions = new HashSet<ExtensionEntityBase>();
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            String field = reader.getNodeName();
            for (ExtensionService<?> extensionService: ExtensionServices.getAll()) {
                if (extensionService.getExtensionClass().equals(Project.class)) {
                    continue;
                }
                if (extensionService.getShortName().equals(field)) {
                    RestConverter<?> converter = extensionService.getRestConverter(getHost());
                    ExtensionEntityBase extension = (ExtensionEntityBase) context.convertAnother(null,
                            extensionService.getExtensionClass(), converter);
                    extensions.add(extension);
                }
            }
            reader.moveUp();
        }

        return extensions;
    }
}
