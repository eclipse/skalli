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
package org.eclipse.skalli.services.extension.rest;

import java.util.Calendar;
import java.util.Collection;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.EntityBase;

import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public abstract class RestConverterBase<T> implements RestConverter {

    /** Relation attributes for links **/
    protected static final String SELF_RELATION = "self"; //$NON-NLS-1$
    protected static final String PROJECT_RELATION = "project"; //$NON-NLS-1$
    protected static final String BROWSE_RELATION = "browse"; //$NON-NLS-1$
    protected static final String ISSUES_RELATION = "issues"; //$NON-NLS-1$
    protected static final String PARENT_RELATION = "parent"; //$NON-NLS-1$
    protected static final String SUBPROJECT_RELATION = "subproject"; //$NON-NLS-1$
    protected static final String USER_RELATION = "user"; //$NON-NLS-1$

    private static final String MODIFIED_BY = "modifiedBy"; //$NON-NLS-1$
    private static final String LAST_MODIFIED = "lastModified"; //$NON-NLS-1$
    private static final String API_VERSION = "apiVersion"; //$NON-NLS-1$
    private static final String HREF = "href"; //$NON-NLS-1$
    private static final String REL = "rel"; //$NON-NLS-1$
    private static final String LINK = "link"; //$NON-NLS-1$

    private final String host;
    private final String alias;
    private final Class<T> clazz;

    public RestConverterBase(Class<T> clazz, String alias) {
        this(clazz, alias, null);
    }

    public RestConverterBase(Class<T> clazz, String alias, String host) {
        this.clazz = clazz;
        this.alias = alias;
        this.host = host;
    }

    @Override
    public String getAlias() {
        return alias;
    }

    @Override
    public Class<?> getConversionClass() {
        return clazz;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public boolean canConvert(Class type) {
        return type.equals(clazz);
    }

    protected void marshalNSAttributes(HierarchicalStreamWriter writer) {
        marshalNSAttributes(this, writer);
    }

    protected void marshalNSAttributes(RestConverter converter, HierarchicalStreamWriter writer) {
        writer.addAttribute(RestUtils.XMLNS, converter.getNamespace());
        writer.addAttribute(RestUtils.XMLNS_XSI, RestUtils.XSI_INSTANCE_NS);
        writer.addAttribute(RestUtils.XSI_SCHEMA_LOCATION, getSchemaLocationAttribute(converter));
    }

    private String getSchemaLocationAttribute(RestConverter converter) {
        return converter.getNamespace() + " " + converter.getHost() + RestUtils.URL_SCHEMAS + converter.getXsdFileName(); //$NON-NLS-1$
    }

    protected void marshalApiVersion(HierarchicalStreamWriter writer) {
        marshalApiVersion(this, writer);
    }

    protected void marshalApiVersion(RestConverter converter, HierarchicalStreamWriter writer) {
        writer.addAttribute(API_VERSION, converter.getApiVersion());
    }

    protected void marshalCommonAttributes(EntityBase entity, HierarchicalStreamWriter writer) {
        marshalCommonAttributes(entity, this, writer);
    }

    protected void marshalCommonAttributes(EntityBase entity, RestConverter converter,
            HierarchicalStreamWriter writer) {
        marshalApiVersion(converter, writer);
        String lastModified = entity.getLastModified();
        if (StringUtils.isNotBlank(lastModified)) {
            writer.addAttribute(LAST_MODIFIED, lastModified);
        }
        String modifiedBy = entity.getLastModifiedBy();
        if (StringUtils.isNotBlank(modifiedBy)) {
            writer.addAttribute(MODIFIED_BY, modifiedBy);
        }
    }

    protected void writeLink(HierarchicalStreamWriter writer, String relation, String url) {
        writer.startNode(LINK);
        if (StringUtils.isNotBlank(relation)) {
            writer.addAttribute(REL, relation);
        }
        writer.addAttribute(HREF, url);
        writer.endNode();
    }

    protected void writeProjectLink(HierarchicalStreamWriter writer, String relation, UUID uuid) {
        writeLink(writer, relation, host + RestUtils.URL_PROJECTS + uuid.toString());
    }

    protected void writeUserLink(HierarchicalStreamWriter writer, String relation, String userId) {
        writeLink(writer, relation, host + RestUtils.URL_USER + userId);
    }

    protected void writeNode(HierarchicalStreamWriter writer, String nodeName) {
        writeNode(writer, nodeName, (String) null);
    }

    protected void writeNode(HierarchicalStreamWriter writer, String nodeName, long value) {
        writeNode(writer, nodeName, Long.toString(value));
    }

    protected void writeNode(HierarchicalStreamWriter writer, String nodeName, String value) {
        if (StringUtils.isNotBlank(value)) {
            writer.startNode(nodeName);
            writer.setValue(value);
            writer.endNode();
        }
    }

    protected void writeNode(HierarchicalStreamWriter writer, String nodeName, String itemName,
            Collection<String> values) {
        if (values != null && values.size() > 0) {
            writer.startNode(nodeName);
            for (String value : values) {
                writeNode(writer, itemName, value);
            }
            writer.endNode();
        }
    }

    protected void writeDateTime(HierarchicalStreamWriter writer, String nodeName, long millis) {
        if (millis >= 0) {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.ENGLISH); //$NON-NLS-1$
            calendar.setTimeInMillis(millis);
            writeNode(writer, nodeName, DatatypeConverter.printDateTime(calendar));
        }
    }
}
