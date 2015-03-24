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
package org.eclipse.skalli.core.rest.resources;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;

import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.model.Derived;
import org.eclipse.skalli.model.ExtensibleEntityBase;
import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.Member;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.services.entity.EntityServices;
import org.eclipse.skalli.services.extension.ExtensionService;
import org.eclipse.skalli.services.extension.ExtensionServices;
import org.eclipse.skalli.services.extension.rest.RestConverter;
import org.eclipse.skalli.services.extension.rest.RestConverterBase;
import org.eclipse.skalli.services.extension.rest.RestUtils;
import org.eclipse.skalli.services.project.ProjectService;
import org.eclipse.skalli.services.rest.RestWriter;
import org.eclipse.skalli.services.user.UserService;
import org.eclipse.skalli.services.user.UserServices;
import org.restlet.data.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class CommonProjectConverter extends RestConverterBase<Project> {

    private static final Logger LOG = LoggerFactory.getLogger(CommonProjectConverter.class);

    public static final String API_VERSION = "1.4"; //$NON-NLS-1$
    public static final String NAMESPACE = "http://www.eclipse.org/skalli/2010/API"; //$NON-NLS-1$

    public static final String[] ALL_EXTENSIONS = new String[] { "*" }; //$NON-NLS-1$

    private Set<String> extensions = Collections.emptySet();;

    /**
     * Create a <code>CommonProjectConverter</code> for rendering of
     * projects without any extensions.
     */
    public CommonProjectConverter() {
        super(Project.class);
    }

    /**
     * Create a <code>ProjectConverter</code> for rendering of
     * projects with selected extensions.
     *
     * @param extensions the extensions to render, or <code>null</code>
     * if no extensions should be rendered. If the array contains the
     * entry <tt>"*"</tt> or {@link CommonProjectConverter#ALL_EXTENSIONS}
     * is passed as argument, all extensions will be rendered.
     */
    public CommonProjectConverter(String[] extensions) {
        super(Project.class);
        if (extensions != null) {
            this.extensions = CollectionUtils.asSet(extensions);
        }
    }

    // for testing purposes
    void setRestWriter(RestWriter writer) {
        this.writer = writer;
    }

    @SuppressWarnings("nls")
    @Override
    protected void marshal(Project project) throws IOException {
        ProjectService projectService = ((ProjectService)EntityServices.getByEntityClass(Project.class));
        UUID uuid = project.getUuid();
        writer.pair("uuid", uuid);
        writer.pair("id", project.getProjectId());
        writer.pair("nature", projectService.getProjectNature(uuid).toString());
        writer.pair("template", project.getProjectTemplateId());
        writer.pair("name", project.getName());
        writer.pair("shortName", project.getShortName());
        writer.pair("phase", project.getPhase());
        if (project.getRegistered() > 0) {
            writer.timestamp("registered", project.getRegistered());
        }
        writer.pair("descriptionFormat", project.getDescriptionFormat());
        writer.pair("description", project.getDescription());
        writer.links();
            writer.link(PROJECT_RELATION, RestUtils.URL_PROJECTS, uuid);
            writer.link(PROJECT_PERMALINK, RestUtils.URL_BROWSE, uuid);
            writer.link(BROWSE_RELATION, RestUtils.URL_BROWSE, project.getProjectId());
            writer.link(ISSUES_RELATION, RestUtils.URL_PROJECTS, uuid, RestUtils.URL_ISSUES);
            writer.link(SUBPROJECTS_RELATION, RestUtils.URL_PROJECTS, uuid, RestUtils.URL_SUBPROJECTS);
            Project parent = project.getParentProject();
            if (parent != null) {
                marshalProjectLink(parent, PARENT_RELATION);
            }
        writer.end();
        marshalSubprojects(project);
        marshalMembers(uuid, projectService.getMembers(uuid), projectService.getMembersByRole(uuid));
        marshalExtensions(project, ExtensionServices.getAll());
    }

    @SuppressWarnings("nls")
    void marshalSubprojects(Project project) throws IOException {
        writer.key("subprojects").array();
        SortedSet<Project> subprojectList = project.getSubProjects();
        if (subprojectList.size() > 0) {
            for (Project subproject : subprojectList) {
                marshalProjectLink(subproject, SUBPROJECT_RELATION);
            }
        }
        writer.end();
    }

    @SuppressWarnings("nls")
    void marshalProjectLink(Project project, String relation) throws IOException {
        if (writer.isMediaType(MediaType.TEXT_XML)) {
            writer.link(relation, RestUtils.URL_PROJECTS, project.getUuid());
        } else {
            writer.object();
            writer.attribute("rel", relation);
            writer.attribute("href", writer.hrefOf(RestUtils.URL_PROJECTS, project.getUuid()));
            writer.attribute("uuid", project.getUuid());
            writer.attribute("id", project.getProjectId());
            writer.attribute("name", project.getName());
            writer.end();
        }
    }

    @SuppressWarnings("nls")
    void marshalMembers(UUID uuid, SortedSet<Member> members, Map<String, SortedSet<Member>> membersByRole)
            throws IOException {
        writer.array("members", "member");
        if (extensions.contains("*") || extensions.contains("members")) {
            UserService userService = UserServices.getUserService();
            for (Member member : members) {
                String userId = member.getUserID();
                writer.object();
                    writer.pair("userId", userId);
                    writer.link(USER_RELATION, RestUtils.URL_USERS, userId);
                    if (userService != null) {
                        User user = userService.getUserById(userId);
                        if (!user.isUnknown()) {
                            writer.pair("name", user.getDisplayName());
                            writer.pair("firstName", user.getFirstname());
                            writer.pair("lastName", user.getLastname());
                            writer.pair("email", user.getEmail());
                        }
                    }

                    if (writer.isMediaType(MediaType.TEXT_XML)) {
                        writer.array("role");
                    } else {
                        writer.key("roles").array();
                    }
                    for (Entry<String, SortedSet<Member>> entry : membersByRole.entrySet()) {
                        if (entry.getValue().contains(member)) {
                            writer.value(entry.getKey());
                        }
                    }
                    writer.end();
                writer.end();
            }
        }
        writer.end();
    }

    @SuppressWarnings("nls")
    void marshalExtensions(Project project, Collection<ExtensionService<?>> extensionServices)
            throws IOException {
        writer.object("extensions");
        for (ExtensionService<?> extensionService : extensionServices) {
            if (extensions.contains("*") || extensions.contains(extensionService.getShortName())) {
                marshalExtension(project, extensionService);
            }
        }
        writer.end();
    }

    @SuppressWarnings("nls")
    void marshalExtension(Project project, ExtensionService<?> extensionService) throws IOException {
        Class<? extends ExtensionEntityBase> extensionClass = extensionService.getExtensionClass();
        if (extensionClass.equals(Project.class)) {
            return;
        }
        ExtensionEntityBase extension = project.getExtension(extensionClass);
        RestConverter<?> converter = extensionService.getRestConverter();
        if (extension != null && converter != null && converter.getConversionClass().isAssignableFrom(extensionClass)) {
            writer.object(extensionService.getShortName());
                namespaces(converter);
                commonAttributes(extension, converter);
                writer.attribute("inherited", project.isInherited(extensionClass));
                writer.attribute("derived", extensionClass.isAnnotationPresent(Derived.class));
                try {
                    converter.marshal(extension, writer);
                } catch (Exception e) {
                    // don't let a buggy extension converter disturb the rendering of other converters!
                    LOG.error(MessageFormat.format("Failed to render extension ''{0}'' of project ''{1}''",
                            extensionService.getShortName(), project.getProjectId()), e);
                }
            writer.end();
        }
    }

    @Deprecated
    public CommonProjectConverter(String host) {
        super(Project.class, "project", host); //$NON-NLS-1$
    }

    @Deprecated
    public CommonProjectConverter(String host, String[] extensions) {
        super(Project.class, "project", host); //$NON-NLS-1$
        if (extensions != null) {
            this.extensions = CollectionUtils.asSet(extensions);
        }
    }

    @Deprecated
    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        Project project = (Project) source;
        UUID uuid = project.getUuid();
        String host = getHost();
        ProjectService projectService = ((ProjectService)EntityServices.getByEntityClass(Project.class));
        writeNode(writer, "uuid", uuid.toString()); //$NON-NLS-1$
        writeNode(writer, "id", project.getProjectId()); //$NON-NLS-1$
        writeNode(writer, "nature", projectService.getProjectNature(uuid)); //$NON-NLS-1$
        writeNode(writer, "template", project.getProjectTemplateId()); //$NON-NLS-1$
        writeNode(writer, "name", project.getName()); //$NON-NLS-1$
        writeNode(writer, "shortName", project.getShortName()); //$NON-NLS-1$
        writeProjectLink(writer, PROJECT_RELATION, uuid);
        writeLink(writer, BROWSE_RELATION, host + RestUtils.URL_BROWSE + project.getProjectId());
        writeLink(writer, ISSUES_RELATION, host + RestUtils.URL_PROJECTS + uuid.toString() + RestUtils.URL_ISSUES);
        writeNode(writer, "phase", project.getPhase()); //$NON-NLS-1$
        if (project.getRegistered() > 0) {
            writeDateTime(writer, "registered", project.getRegistered()); //$NON-NLS-1$
        }
        writeNode(writer, "description", project.getDescription()); //$NON-NLS-1$
        UUID parent = project.getParentEntityId();
        if (parent != null) {
            writeProjectLink(writer, PARENT_RELATION, parent);
        }

        SortedSet<Project> subprojectList = project.getSubProjects();
        if (subprojectList.size() > 0) {
            writer.startNode("subprojects"); //$NON-NLS-1$
            for (Project subproject : subprojectList) {
                writeProjectLink(writer, SUBPROJECT_RELATION, subproject.getUuid());
            }
            writer.endNode();
        }

        marshalMembers(uuid, projectService, writer);
        marshalExtensions(project, writer, context);
    }

    @Deprecated
    private void marshalMembers(UUID uuid, ProjectService projectService, HierarchicalStreamWriter writer) {
        if (extensions.contains("*") || extensions.contains("members")) { //$NON-NLS-1$ //$NON-NLS-2$
            writer.startNode("members"); //$NON-NLS-1$
            UserService userService = UserServices.getUserService();
            for (Member member : projectService.getMembers(uuid)) {
                writer.startNode("member"); //$NON-NLS-1$
                String userId = member.getUserID();
                writeNode(writer, "userId", userId); //$NON-NLS-1$
                writeUserLink(writer, USER_RELATION, userId);
                if (userService != null) {
                    User user = userService.getUserById(userId);
                    if (!user.isUnknown()) {
                        writeNode(writer, "name", user.getDisplayName()); //$NON-NLS-1$
                        writeNode(writer, "firstName", user.getFirstname()); //$NON-NLS-1$
                        writeNode(writer, "lastName", user.getLastname()); //$NON-NLS-1$
                        writeNode(writer, "email", user.getEmail()); //$NON-NLS-1$
                    }
                }
                for (Entry<String, SortedSet<Member>> entry : projectService.getMembersByRole(uuid).entrySet()) {
                    if (entry.getValue().contains(member)) {
                        writeNode(writer, "role", entry.getKey()); //$NON-NLS-1$
                    }
                }
                writer.endNode();
            }
            writer.endNode();
        }
    }

    @Deprecated
    private void marshalExtensions(ExtensibleEntityBase extensibleEntity, HierarchicalStreamWriter writer,
            MarshallingContext context) {
        writer.startNode("extensions"); //$NON-NLS-1$
        for (ExtensionService<?> extensionService : ExtensionServices.getAll()) {
            if (extensions.contains("*") || extensions.contains(extensionService.getShortName())) { //$NON-NLS-1$
                marshalExtension(extensibleEntity, extensionService, writer, context);
            }
        }
        writer.endNode();
    }

    @Deprecated
    protected void marshalExtension(ExtensibleEntityBase extensibleEntity, ExtensionService<?> extensionService,
            HierarchicalStreamWriter writer, MarshallingContext context) {
        Class<? extends ExtensionEntityBase> extensionClass = extensionService.getExtensionClass();
        if (extensionClass.equals(Project.class)) {
            return;
        }
        ExtensionEntityBase extension = extensibleEntity.getExtension(extensionClass);
        RestConverter<?> converter = extensionService.getRestConverter(getHost());
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

    @Deprecated
    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    return null;
    }

    @Override
    public String getApiVersion() {
        return API_VERSION;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public String getXsdFileName() {
        return "project.xsd"; //$NON-NLS-1$
    }
}
