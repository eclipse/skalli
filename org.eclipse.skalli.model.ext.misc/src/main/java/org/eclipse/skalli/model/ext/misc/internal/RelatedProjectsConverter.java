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

import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.api.java.SearchHit;
import org.eclipse.skalli.api.java.SearchResult;
import org.eclipse.skalli.api.java.SearchService;
import org.eclipse.skalli.common.Services;
import org.eclipse.skalli.common.util.UUIDList;
import org.eclipse.skalli.common.util.UUIDUtils;
import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.model.ext.AbstractConverter;
import org.eclipse.skalli.model.ext.misc.RelatedProjectsExt;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class RelatedProjectsConverter extends AbstractConverter<RelatedProjectsExt> {

    public static final String API_VERSION = "1.0"; //$NON-NLS-1$
    public static final String NAMESPACE = "http://www.eclipse.org/skalli/2010/API/Extension-RelatedProjects"; //$NON-NLS-1$

    public RelatedProjectsConverter(String host) {
        super(RelatedProjectsExt.class, "relatedProjects", host); //$NON-NLS-1$
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        RelatedProjectsExt ext = (RelatedProjectsExt)source;
        UUIDList relatedProjects = new UUIDList();
        if (ext.getCalculated()) {
            writeNode(writer, "calculated"); //$NON-NLS-1$
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
        for (UUID uuid: ext.getRelatedProjects()) {
            writeProjectLink(writer, PROJECT_RELATION, uuid);
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
