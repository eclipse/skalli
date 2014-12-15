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
package org.eclipse.skalli.model.ext.misc.internal;

import java.io.IOException;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.UUIDList;
import org.eclipse.skalli.commons.UUIDUtils;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.ext.misc.RelatedProjectsExt;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.extension.rest.RestConverterBase;
import org.eclipse.skalli.services.extension.rest.RestUtils;
import org.eclipse.skalli.services.project.ProjectService;
import org.eclipse.skalli.services.search.SearchHit;
import org.eclipse.skalli.services.search.SearchResult;
import org.eclipse.skalli.services.search.SearchService;
import org.restlet.data.MediaType;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class RelatedProjectsConverter extends RestConverterBase<RelatedProjectsExt> {

    public static final String API_VERSION = "1.0"; //$NON-NLS-1$
    public static final String NAMESPACE = "http://www.eclipse.org/skalli/2010/API/Extension-RelatedProjects"; //$NON-NLS-1$

    public RelatedProjectsConverter() {
        super(RelatedProjectsExt.class);
    }

    @SuppressWarnings("nls")
    @Override
    protected void marshal(RelatedProjectsExt extension) throws IOException {
        UUIDList relatedProjects = new UUIDList();
        boolean calculated = extension.getCalculated();
        writer.pair("calculated", calculated);
        if (calculated) {
            SearchService searchService = Services.getService(SearchService.class);
            if (searchService != null) {
                SearchResult<Project> hits = searchService.getRelatedProjects((Project)extension.getExtensibleEntity(), 3);
                for (SearchHit<Project> hit : hits.getResult()) {
                    relatedProjects.add(hit.getEntity().getUuid());
                }
            }
        } else  {
            relatedProjects = extension.getRelatedProjects();
        }
        marshalLinksSection(relatedProjects, Services.getRequiredService(ProjectService.class));
    }

    void marshalLinksSection(UUIDList relatedProjects, ProjectService projectService) throws IOException {
        writer.links();
        if (writer.isMediaType(MediaType.TEXT_XML)) {
            for (UUID uuid: relatedProjects) {
                Project project = projectService.getByUUID(uuid);
                if (project != null) {
                    writer.link(PROJECT_RELATION, RestUtils.URL_PROJECTS, uuid);
                }
            }
        } else {
            for (UUID uuid: relatedProjects) {
                Project project = projectService.getByUUID(uuid);
                if (project != null) {
                    writer.object();
                    writer.attribute("rel", PROJECT_RELATION);
                    writer.attribute("href", writer.hrefOf(RestUtils.URL_PROJECTS, uuid));
                    writer.attribute("uuid", uuid);
                    writer.attribute("id", project.getProjectId());
                    writer.attribute("name", project.getName());
                    writer.end();
                }
            }
        }
        writer.end();
    }

    @Deprecated
    public RelatedProjectsConverter(String host) {
        super(RelatedProjectsExt.class, "relatedProjects", host); //$NON-NLS-1$
    }

    @Deprecated
    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        RelatedProjectsExt ext = (RelatedProjectsExt)source;
        UUIDList relatedProjects = new UUIDList();
        if (ext.getCalculated()) {
            writeNode(writer, "calculated", true); //$NON-NLS-1$
            SearchService searchService = Services.getService(SearchService.class);
            if (searchService != null) {
                SearchResult<Project> hits = searchService.getRelatedProjects((Project)ext.getExtensibleEntity(), 3);
                for (SearchHit<Project> hit : hits.getResult()) {
                    relatedProjects.add(hit.getEntity().getUuid());
                }
            }
        } else  {
            relatedProjects = ext.getRelatedProjects();
        }
        ProjectService projectService = Services.getRequiredService(ProjectService.class);
        for (UUID uuid: relatedProjects) {
            Project project = projectService.getByUUID(uuid);
            if (project != null) {
                writeProjectLink(writer, PROJECT_RELATION, uuid);
            }
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        return iterateNodes(null, reader, context);
    }

    private RelatedProjectsExt iterateNodes(RelatedProjectsExt ext, HierarchicalStreamReader reader,
            UnmarshallingContext context) {
        if (ext == null) {
            ext = new RelatedProjectsExt();
        }

        while (reader.hasMoreChildren()) {
            reader.moveDown();

            String field = reader.getNodeName();

            if ("relatedProjects".equals(field)) { //$NON-NLS-1$
                iterateNodes(ext, reader, context);
            } else {
                if ("link".equals(field)) { //$NON-NLS-1$
                    String href = reader.getAttribute("href"); //$NON-NLS-1$
                    if (StringUtils.isNotBlank(href)) {
                        int n = href.lastIndexOf('/');
                        String s = n>0? href.substring(n+1) : href;
                        if (UUIDUtils.isUUID(s)) {
                            ext.getRelatedProjects().add(UUID.fromString(s));
                        }
                    }
                }
            }
            reader.moveUp();
        }
        return ext;
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
        return "extension-relatedProjects.xsd";
    }
}
