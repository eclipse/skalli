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
package org.eclipse.skalli.model.ext.linkgroups.internal;

import java.io.IOException;
import java.util.Collection;

import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.commons.Link;
import org.eclipse.skalli.model.ext.linkgroups.LinkGroup;
import org.eclipse.skalli.model.ext.linkgroups.LinkGroupsProjectExt;
import org.eclipse.skalli.services.extension.rest.RestConverterBase;
import org.restlet.data.MediaType;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

class LinkGroupsConverter extends RestConverterBase<LinkGroupsProjectExt> {

    public static final String API_VERSION = "1.0"; //$NON-NLS-1$
    public static final String NAMESPACE = "http://www.eclipse.org/skalli/2010/API/Extension-LinkGroups"; //$NON-NLS-1$

    public LinkGroupsConverter() {
        super(LinkGroupsProjectExt.class);
    }

    @SuppressWarnings("nls")
    @Override
    protected void marshal(LinkGroupsProjectExt extension) throws IOException {
        Collection<LinkGroup> linkGroups = extension.getLinkGroups();
        if (CollectionUtils.isNotBlank(linkGroups)) {
            writer.array("linkGroup");
            for (LinkGroup linkGroup : linkGroups) {
                writer.object();
                writer.attribute("caption", linkGroup.getCaption());
                if (writer.isMediaType(MediaType.APPLICATION_JSON)) {
                    writer.key("links");
                }
                writer.array("link");
                for (Link link : linkGroup.getItems()) {
                    if (link != null) {
                        writer.object();
                        writer.attribute("ref", link.getUrl());
                        writer.value(link.getLabel());
                        writer.end();
                    }
                }
                writer.end();
                writer.end();
            }
            writer.end();
        }
    }

    @Deprecated
    public LinkGroupsConverter(String host) {
        super(LinkGroupsProjectExt.class, "linkGroups", host); //$NON-NLS-1$
    }

    @Deprecated
    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        LinkGroupsProjectExt ext = (LinkGroupsProjectExt) source;

        Collection<LinkGroup> linkGroups = ext.getLinkGroups();
        if (linkGroups != null && !linkGroups.isEmpty()) {
            writer.startNode("linkGroups"); //$NON-NLS-1$
            for (LinkGroup linkGroup : linkGroups) {
                writer.startNode("linkGroup"); //$NON-NLS-1$
                writer.addAttribute("caption", linkGroup.getCaption()); //$NON-NLS-1$
                for (Link link : linkGroup.getItems()) {
                    if (link != null) {
                        writer.startNode("link"); //$NON-NLS-1$
                        writer.addAttribute("ref", link.getUrl()); //$NON-NLS-1$
                        writer.setValue(link.getLabel());
                        writer.endNode();
                    }
                }
                writer.endNode();
            }
            writer.endNode();
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        return iterateNodes(null, reader, context);
    }

    private LinkGroupsProjectExt iterateNodes(LinkGroupsProjectExt ext, HierarchicalStreamReader reader,
            UnmarshallingContext context) {
        if (ext == null) {
            ext = new LinkGroupsProjectExt();
        }

        while (reader.hasMoreChildren()) {
            reader.moveDown();

            String field = reader.getNodeName();

            if ("linkGroups".equals(field)) { //$NON-NLS-1$
                iterateNodes(ext, reader, context);
            } else if ("linkGroup".equals(field)) { //$NON-NLS-1$
                String caption = reader.getAttribute("caption"); //$NON-NLS-1$
                LinkGroup linkGroup = new LinkGroup();
                linkGroup.setCaption(caption);
                iterateLinkNodes(linkGroup, reader);

                ext.getLinkGroups().add(linkGroup);
            }

            reader.moveUp();
        }
        return ext;
    }

    private void iterateLinkNodes(LinkGroup linkGroup, HierarchicalStreamReader reader) {
        while (reader.hasMoreChildren()) {
            reader.moveDown();

            String field = reader.getNodeName();
            String value = reader.getValue();
            if ("link".equals(field)) { //$NON-NLS-1$
                String ref = reader.getAttribute("ref"); //$NON-NLS-1$
                linkGroup.add(new Link(ref, value));
            }

            reader.moveUp();
        }
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
        return "extension-linkgroups.xsd"; //$NON-NLS-1$
    }
}
