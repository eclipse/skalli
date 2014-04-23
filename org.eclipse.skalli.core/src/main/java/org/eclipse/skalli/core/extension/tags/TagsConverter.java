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
package org.eclipse.skalli.core.extension.tags;

import java.io.IOException;

import org.eclipse.skalli.model.ext.commons.TagsExtension;
import org.eclipse.skalli.services.extension.rest.RestConverterBase;
import org.restlet.data.MediaType;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class TagsConverter extends RestConverterBase<TagsExtension> {

    public static final String API_VERSION = "1.0"; //$NON-NLS-1$
    public static final String NAMESPACE = "http://www.eclipse.org/skalli/2010/API/Extension-Tags"; //$NON-NLS-1$

    public TagsConverter() {
        super(TagsExtension.class);
    }

    @SuppressWarnings("nls")
    @Override
    protected void marshal(TagsExtension extension) throws IOException {
        if (writer.isMediaType(MediaType.APPLICATION_JSON)) {
            writer.key("items");
        }
        writer.array("tag");
        for (String tag : extension.getTags()) {
            writer.value(tag);
        }
        writer.end();
    }

    @Deprecated
    public TagsConverter(String host) {
        super(TagsExtension.class, "tags", host); //$NON-NLS-1$
    }

    @Deprecated
    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        TagsExtension ext = (TagsExtension) source;
        for (String tag: ext.getTags()) {
            writeNode(writer, "tag", tag); //$NON-NLS-1$
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        return iterateNodes(null, reader, context);
    }

    private TagsExtension iterateNodes(TagsExtension ext, HierarchicalStreamReader reader,
            UnmarshallingContext context) {
        if (ext == null) {
            ext = new TagsExtension();
        }

        while (reader.hasMoreChildren()) {
            reader.moveDown();

            String field = reader.getNodeName();
            String value = reader.getValue();

            if ("tags".equals(field)) { //$NON-NLS-1$
                ext.addTag(value);
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
        return "extension-tags.xsd"; //$NON-NLS-1$
    }

}
