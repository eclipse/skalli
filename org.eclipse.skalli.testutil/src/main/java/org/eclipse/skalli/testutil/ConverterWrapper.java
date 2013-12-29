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
package org.eclipse.skalli.testutil;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.Derived;
import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.services.extension.rest.RestConverter;
import org.eclipse.skalli.services.rest.RestWriter;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

@SuppressWarnings("rawtypes")
public class ConverterWrapper implements RestConverter {

    private static final String XSI_INSTANCE_NS = "http://www.w3.org/2001/XMLSchema-instance"; //$NON-NLS-1$
    private static final String URL_SCHEMAS = "/schemas/"; //$NON-NLS-1$

    private RestConverter<?> converter;
    private String nodeName;
    private boolean isInherited;
    private boolean omitInheritedAttribute;
    private String host;

    public ConverterWrapper(String host, RestConverter<?> converter, String nodeName) {
        this.host = host;
        this.converter = converter;
        this.nodeName = nodeName;
        this.omitInheritedAttribute = true;
    }

    public ConverterWrapper(String host, RestConverter<?> converter, String nodeName, boolean isInherited) {
        this.converter = converter;
        this.nodeName = nodeName;
        this.isInherited = isInherited;
        this.omitInheritedAttribute = false;
    }

    @Override
    public void marshal(Object obj, RestWriter writer) throws IOException {
        writer.object(nodeName);
        writer.end();
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        writer.startNode(nodeName);
        marshalNSAttributes(writer);
        marshalCommonAttributes((ExtensionEntityBase) source, writer);
        if (!omitInheritedAttribute) {
            writer.addAttribute("inherited", Boolean.toString(isInherited)); //$NON-NLS-1$
        }
        converter.marshal(source, writer, context);
        writer.endNode();
    }

    @SuppressWarnings("nls")
    private void marshalNSAttributes(HierarchicalStreamWriter writer) {
        writer.addAttribute("xmlns", getNamespace());
        writer.addAttribute("xmlns:xsi", XSI_INSTANCE_NS);
        writer.addAttribute("xsi:schemaLocation", converter.getNamespace() + " " +
                host + URL_SCHEMAS + converter.getXsdFileName());
    }

    @SuppressWarnings("nls")
    private void marshalCommonAttributes(ExtensionEntityBase ext, HierarchicalStreamWriter writer) {
        writer.addAttribute("derived", Boolean.toString(ext.getClass().isAnnotationPresent(Derived.class)));
        writer.addAttribute("apiVersion", converter.getApiVersion());
        String lastModified = ext.getLastModified();
        if (StringUtils.isNotBlank(lastModified)) {
            writer.addAttribute("lastModified", lastModified);
        }
        String modifiedBy = ext.getLastModifiedBy();
        if (StringUtils.isNotBlank(lastModified)) {
            writer.addAttribute("modifiedBy", modifiedBy);
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader arg0, UnmarshallingContext arg1) {
        return converter.unmarshal(arg0, arg1);
    }

    @Override
    public boolean canConvert(Class arg0) {
        return converter.canConvert(arg0);
    }

    @Override
    public String getAlias() {
        return converter.getAlias();
    }

    @Override
    public Class<?> getConversionClass() {
        return converter.getConversionClass();
    }

    @Override
    public String getApiVersion() {
        return converter.getApiVersion();
    }

    @Override
    public String getNamespace() {
        return converter.getNamespace();
    }

    @Override
    public String getXsdFileName() {
        return converter.getXsdFileName();
    }
}
