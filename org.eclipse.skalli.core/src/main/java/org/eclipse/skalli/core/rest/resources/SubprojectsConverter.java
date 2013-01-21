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

import java.util.Set;

import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.services.extension.rest.RestConverterBase;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class SubprojectsConverter extends RestConverterBase<Subprojects> {

    public static final String API_VERSION = "1.0"; //$NON-NLS-1$
    public static final String NAMESPACE = "http://www.eclipse.org/skalli/2010/API"; //$NON-NLS-1$

    private String[] extensions;

    public SubprojectsConverter(String host, String[] extensions) {
        super(Subprojects.class, "subprojects", host); //$NON-NLS-1$
        this.extensions = extensions;
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
        return "subprojects.xsd";
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {

        Subprojects subprojectsSource = (Subprojects) source;

        marshalNSAttributes(writer);
        marshalApiVersion(writer);

        Set<Project> subprojects = subprojectsSource.getSubprojects();
        if (subprojects != null && subprojects.size() > 0) {
            for (Project subproject : subprojects) {
                writer.startNode("project"); //$NON-NLS-1$
                new ProjectConverter(getHost(), extensions, true).marshal(subproject, writer, context);
                writer.endNode();
            }
        }

    }

    @Override
    public Object unmarshal(HierarchicalStreamReader arg0, UnmarshallingContext arg1) {
        // don't support that yet
        return null;
    }

}
