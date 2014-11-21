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
package org.eclipse.skalli.model.ext.devinf.internal;

import java.io.IOException;

import org.eclipse.skalli.model.ext.devinf.DevInfProjectExt;
import org.eclipse.skalli.services.extension.rest.RestConverterBase;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

class DevInfConverter extends RestConverterBase<DevInfProjectExt> {

    public static final String API_VERSION = "1.0"; //$NON-NLS-1$
    public static final String NAMESPACE = "http://www.eclipse.org/skalli/2010/API/Extension-DevInf"; //$NON-NLS-1$

    public DevInfConverter() {
        super(DevInfProjectExt.class);
    }

    @SuppressWarnings("nls")
    @Override
    protected void marshal(DevInfProjectExt extension) throws IOException {
        writer
        .pair("bugtrackerUrl", extension.getBugtrackerUrl())
        .pair("ciUrl", extension.getCiUrl())
        .pair("metricsUrl", extension.getMetricsUrl())
        .pair("scmUrl", extension.getScmUrl())
        .collection("scmLocations", "scmLocation", extension.getScmLocations())
        .collection("javadocs", "javadoc", extension.getJavadocs());
    }

    @Deprecated
    public DevInfConverter(String host) {
        super(DevInfProjectExt.class, "devInf", host); //$NON-NLS-1$
    }

    @Deprecated
    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        DevInfProjectExt ext = (DevInfProjectExt) source;
        writeNode(writer, "bugtrackerUrl", ext.getBugtrackerUrl()); //$NON-NLS-1$
        writeNode(writer, "ciUrl", ext.getCiUrl()); //$NON-NLS-1$
        writeNode(writer, "metricsUrl", ext.getMetricsUrl()); //$NON-NLS-1$
        writeNode(writer, "scmUrl", ext.getScmUrl()); //$NON-NLS-1$
        writeNode(writer, "scmLocations", "scmLocation", ext.getScmLocations()); //$NON-NLS-1$ //$NON-NLS-2$
        writeNode(writer, "javadocs", "javadoc", ext.getJavadocs()); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        return iterateNodes(null, reader, context);
    }

    private DevInfProjectExt iterateNodes(DevInfProjectExt ext, HierarchicalStreamReader reader,
            UnmarshallingContext context) {
        if (ext == null) {
            ext = new DevInfProjectExt();
        }

        while (reader.hasMoreChildren()) {
            reader.moveDown();

            String field = reader.getNodeName();
            String value = reader.getValue();

            if ("bugtrackerUrl".equals(field)) { //$NON-NLS-1$
                ext.setBugtrackerUrl(value);
            } else if ("ciUrl".equals(field)) { //$NON-NLS-1$
                ext.setCiUrl(value);
            } else if ("metricsUrl".equals(field)) { //$NON-NLS-1$
                ext.setMetricsUrl(value);
            } else if ("scmUrl".equals(field)) { //$NON-NLS-1$
                ext.setScmUrl(value);
            } else if ("scmLocations".equals(field)) { //$NON-NLS-1$
                iterateNodes(ext, reader, context);
            } else if ("scmLocation".equals(field)) { //$NON-NLS-1$
                ext.getScmLocations().add(value);
            } else if ("javadocs".equals(field)) { //$NON-NLS-1$
                iterateNodes(ext, reader, context);
            } else if ("javadoc".equals(field)) { //$NON-NLS-1$
                ext.getJavadocs().add(value);
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
        return "extension-devinf.xsd"; //$NON-NLS-1$
    }
}
