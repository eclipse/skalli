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

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.services.extension.rest.RestConverterBase;
import org.eclipse.skalli.services.permit.Permit;
import org.eclipse.skalli.services.permit.PermitSet;
import org.restlet.data.MediaType;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class PermitSetConverter extends RestConverterBase<PermitSet> {

    private static final String API_VERSION = "1.0"; //$NON-NLS-1$
    private static final String NAMESPACE = "http://www.eclipse.org/skalli/2010/API"; //$NON-NLS-1$

    private String owner;

    @SuppressWarnings("nls")
    public PermitSetConverter(String owner) {
        super(PermitSet.class);
        this.owner = StringUtils.isNotBlank(owner)? owner : "anonymous";
    }

    @SuppressWarnings("nls")
    @Override
    protected void marshal(PermitSet permits) throws IOException {
        writer.object("permits");
        namespaces();
        apiVersion();
        writer.pair("owner", owner);
        if (writer.isMediaType(MediaType.APPLICATION_JSON)) {
            writer.key("permits");
        }
        writer.array("permit");
        for (Permit permit: permits) {
            String path = StringUtils.replace(permit.getPath(), "/projects/?/", "/projects/**/");
            writer.object();
                writer.pair("action", permit.getAction());
                writer.pair("path", path);
                writer.pair("level", permit.getLevel());
            writer.end();
        }
        writer.end();
        writer.end();
    }

    @Deprecated
    @SuppressWarnings("nls")
    public PermitSetConverter(String owner, String host) {
        super(PermitSet.class, "permits", host);
        this.owner = StringUtils.isNotBlank(owner)? owner : "anonymous";
    }

    @Deprecated
    @SuppressWarnings("nls")
    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        PermitSet permits = (PermitSet)source;
        marshalNSAttributes(writer);
        marshalApiVersion(writer);
        writeNode(writer, "owner", owner);
        for (Permit permit: permits) {
            writer.startNode("permit");
            writeNode(writer, "action", permit.getAction());
            String path = StringUtils.replace(permit.getPath(), "/projects/?/", "/projects/**/");
            writeNode(writer, "path", path);
            writeNode(writer, "level", Integer.toString(permit.getLevel()));
            writer.endNode();
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        // don't support that yet
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
        return "permits.xsd"; //$NON-NLS-1$
    }
}
