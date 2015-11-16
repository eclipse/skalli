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

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.XMLUtils;
import org.eclipse.skalli.model.Derived;
import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.services.extension.ExtensionService;
import org.eclipse.skalli.services.extension.rest.RestConverter;
import org.eclipse.skalli.services.extension.rest.RestConverterBase;
import org.eclipse.skalli.services.extension.rest.RestException;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class InheritableExtensionConverter extends RestConverterBase<InheritableExtension> {

    public static final String API_VERSION = "1.4"; //$NON-NLS-1$
    public static final String NAMESPACE = "http://www.eclipse.org/skalli/2010/API"; //$NON-NLS-1$

    private ExtensionService<? extends ExtensionEntityBase> extensionService;
    private RestConverter<? extends ExtensionEntityBase> extensionConverter;

    public InheritableExtensionConverter(ExtensionService<? extends ExtensionEntityBase> extensionService) {
        super(InheritableExtension.class);
        this.extensionService = extensionService;
        this.extensionConverter = extensionService.getRestConverter();
    }

    @Override
    public String getApiVersion() {
        return extensionConverter != null? extensionConverter.getApiVersion() : API_VERSION;
    }

    @Override
    public String getNamespace() {
        return extensionConverter != null? extensionConverter.getNamespace() : NAMESPACE;
    }

    @Override
    public String getXsdFileName() {
        return extensionConverter != null? extensionConverter.getXsdFileName() : null;
    }

    @SuppressWarnings("nls")
    @Override
    protected void marshal(InheritableExtension inheritable) throws IOException {
        ExtensionEntityBase extension = inheritable.getExtension();
        Class<? extends ExtensionEntityBase> extensionClass = extension.getClass();
        if (extensionConverter != null && extensionConverter.getConversionClass().isAssignableFrom(extensionClass)) {
            writer.object(extensionService.getShortName());
                namespaces(extensionConverter);
                commonAttributes(extension, extensionConverter);
                writer.attribute("inherited", inheritable.isInherited() == Boolean.TRUE);
                writer.attribute("derived", extensionClass.isAnnotationPresent(Derived.class));
                extensionConverter.marshal(extension, writer);
            writer.end();
        }
    }

    @Override
    protected InheritableExtension unmarshal() throws IOException, RestException {
        InheritableExtension inheritable = new InheritableExtension();
        reader.object();
        unmarshallCommonAttributes(inheritable);
        inheritable.setExtension(extensionConverter.unmarshal(reader));
        reader.end();
        return inheritable;
    }

    @SuppressWarnings("nls")
    private void unmarshallCommonAttributes(InheritableExtension inheritable) throws IOException, RestException {
        String apiVersion = null;
        String namespace = null;
        while (reader.hasMore()) {
            if (reader.isKeyAnyOf("inherited")) {
                inheritable.setInherited(reader.attributeBoolean());
            } else if (reader.isKeyAnyOf("apiVersion")) {
                apiVersion = reader.attributeString();
            } else if (reader.isKeyAnyOf(XMLUtils.XMLNS, "namespace")) {
                namespace = reader.attributeString();
            } else if (reader.isKeyAnyOf(XMLUtils.XMLNS_XSI, XMLUtils.XSI_SCHEMA_LOCATION,
                    "lastModified", "lastModifiedMillis", "modifiedBy", "derived")) {
                // ignore these attributes
                reader.skip();
            } else {
                // first unknown attribute indicates begin of extension attributes
                break;
            }
        }
        if (StringUtils.isBlank(apiVersion)) {
            throw new RestException("Missing required apiVersion attribute");
        }
        if (!getApiVersion().equals(apiVersion)) {
            throw new RestException(MessageFormat.format(
                    "Unsupported API version (requested: ''{0}'', expected: ''{1}'')",
                    apiVersion, getApiVersion()));
        }
        if (StringUtils.isBlank(namespace)) {
            throw new RestException("Missing required namespace attribute");
        }
        if (!getNamespace().equals(namespace)) {
            throw new RestException(MessageFormat.format(
                    "Unsupported namespace (requested: ''{0}'', expected: ''{1}'')",
                    namespace, getNamespace()));
        }
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        throw new UnsupportedOperationException();
    }
}
