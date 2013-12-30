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
package org.eclipse.skalli.model.ext.maven.internal;

import java.io.IOException;

import org.eclipse.skalli.model.ext.maven.MavenProjectExt;
import org.eclipse.skalli.services.extension.rest.RestConverterBase;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

class MavenConverter extends RestConverterBase<MavenProjectExt> {

    public static final String API_VERSION = "1.0"; //$NON-NLS-1$
    public static final String NAMESPACE = "http://www.eclipse.org/skalli/2010/API/Extension-Maven"; //$NON-NLS-1$

    public MavenConverter() {
        super(MavenProjectExt.class);
    }

    @SuppressWarnings("nls")
    @Override
    protected void marshal(MavenProjectExt extension) throws IOException {
        writer
        .pair("groupID", extension.getGroupID())
        .pair("siteUrl", extension.getSiteUrl())
        .pair("pomPath", extension.getReactorPOM());
    }

    @Deprecated
    public MavenConverter(String host) {
        super(MavenProjectExt.class, "maven", host); //$NON-NLS-1$
    }

    @Deprecated
    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        MavenProjectExt ext = (MavenProjectExt) source;
        writeNode(writer, "groupID", ext.getGroupID()); //$NON-NLS-1$
        writeNode(writer, "siteUrl", ext.getSiteUrl()); //$NON-NLS-1$
        writeNode(writer, "pomPath", ext.getReactorPOM()); //$NON-NLS-1$
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
        return "extension-maven.xsd"; //$NON-NLS-1$
    }
}
