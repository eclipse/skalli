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
package org.eclipse.skalli.services.extension.rest;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.FormatUtils;
import org.eclipse.skalli.commons.XMLUtils;
import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.services.rest.RestWriter;

import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Base class for the implementation of {@link RestConverter REST converters}
 * for entities, extensions or similar beans.
 *
 * The main purpose of this class is to provide some convenience methods
 * for common recurring rendering tasks.
 */
public abstract class RestConverterBase<T> implements RestConverter<T> {

    /** Relation type for a link of a resource to itself (<tt>rel={@value}</tt>) */
    protected static final String SELF_RELATION = "self"; //$NON-NLS-1$

    /** Relation type for a link to another project's resource (<tt>rel={@value}</tt>) */
    protected static final String PROJECT_RELATION = "project"; //$NON-NLS-1$

    /** Relation type for a link to another project's detail page (<tt>rel={@value}</tt>) */
    protected static final String BROWSE_RELATION = "browse"; //$NON-NLS-1$

    /** Relation type for a permant link to another project's detail page (<tt>rel={@value}</tt>) */
    protected static final String PROJECT_PERMALINK = "permalink"; //$NON-NLS-1$

    /** Relation type for a link to the project's issues resource (<tt>rel={@value}</tt>) */
    protected static final String ISSUES_RELATION = "issues"; //$NON-NLS-1$

    /** Relation type for a link to the project's parent project (<tt>rel={@value}</tt>) */
    protected static final String PARENT_RELATION = "parent"; //$NON-NLS-1$

    /** Relation type for a link to a subproject of a project (<tt>rel={@value}</tt>) */
    protected static final String SUBPROJECT_RELATION = "subproject"; //$NON-NLS-1$

    /** Relation type for a link to the collection of subprojects of a project (<tt>rel={@value}</tt>) */
    protected static final String SUBPROJECTS_RELATION = "subprojects"; //$NON-NLS-1$

    /** Relation type for a link to a user resource (<tt>rel={@value}</tt>) */
    protected static final String USER_RELATION = "user"; //$NON-NLS-1$

    private static final String MODIFIED_BY = "modifiedBy"; //$NON-NLS-1$
    private static final String LAST_MODIFIED = "lastModified"; //$NON-NLS-1$
    private static final String LAST_MODIFIED_MILLIS = "lastModifiedMillis"; //$NON-NLS-1$
    private static final String API_VERSION = "apiVersion"; //$NON-NLS-1$
    private static final String HREF = "href"; //$NON-NLS-1$
    private static final String REL = "rel"; //$NON-NLS-1$
    private static final String LINK = "link"; //$NON-NLS-1$

    private final String host;
    private final String alias;
    private final Class<T> clazz;

    /** The Rest writer to use for marshalling. */
    protected RestWriter writer;

    /**
     * Constructs a REST converter for the given class.
     *
     * @param clazz  the class with which this converter is associated.
     */
    public RestConverterBase(Class<T> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("argument 'clazz' must not be null");
        }
        this.clazz = clazz;
        this.host = null;
        this.alias = null;
    }

    /**
     * Constructs a REST converter for the given class.
     *
     * @param clazz  the class with which this converter is associated.
     * @param alias  the alias used in the XML representation.
     */
    @Deprecated
    public RestConverterBase(Class<T> clazz, String alias) {
        this(clazz, alias, null);
    }

    /**
     * Constructs a REST converter for the given class.
     *
     * @param clazz  the class with which this converter is associated.
     * @param alias  the alias to use in the XML representation.
     * @param host  the host part of the web locator of the server on which this converter is running,
     * e.g. <tt>http://localhost:8080</tt>.
     */
    @Deprecated
    public RestConverterBase(Class<T> clazz, String alias, String host) {
        this.clazz = clazz;
        this.alias = alias;
        this.host = host;
    }

    @Override
    @Deprecated
    public String getAlias() {
        return alias;
    }

    @Override
    public Class<T> getConversionClass() {
        return clazz;
    }

    @Override
    @Deprecated
    public boolean canConvert(Class type) {
        return type.equals(clazz);
    }

    /**
     * Returns the host, or <code>null</code> if the host is not known.
     */
    @Deprecated
    protected String getHost() {
        return host;
    }

    @Override
    public void marshal(Object obj, RestWriter writer) throws IOException {
        if (obj == null) {
            return;
        }
        if (clazz.isAssignableFrom(obj.getClass())) {
            this.writer = writer;
            marshal(clazz.cast(obj));
        } else {
            throw new IllegalArgumentException("Cannot convert object of type " + obj.getClass());
        }
    }

    /**
     * Marshals the given object.
     *
     * @param obj  the object instance to marshal.
     *
     * @throws IOException  if an i/o error occured.
     */
    protected void marshal(T obj) throws IOException {
    }

    /**
     * Marshals the namespace attributes provided by this converter to the
     * underlying stream writer.
     *
     * @param writer  the writer to use for marshaling.
     */
    @Deprecated
    protected void marshalNSAttributes(HierarchicalStreamWriter writer) {
        marshalNSAttributes(writer, this);
    }

    /**
     * Marshals the namespace attributes provided by the given converter to the
     * underlying stream writer.
     *
     * @param converter  the converter from which to retrieve namespace information.
     * @param writer  the writer to use for marshaling.
     */
    @Deprecated
    protected void marshalNSAttributes(HierarchicalStreamWriter writer, RestConverter<?> converter) {
        writer.addAttribute(XMLUtils.XMLNS, converter.getNamespace());
        writer.addAttribute(XMLUtils.XMLNS_XSI, XMLUtils.XSI_INSTANCE_NS);
        if (StringUtils.isNotBlank(host) && StringUtils.isNotBlank(converter.getXsdFileName())) {
            writer.addAttribute(XMLUtils.XSI_SCHEMA_LOCATION, getSchemaLocationAttribute(converter));
        }
    }

    @Deprecated
    private String getSchemaLocationAttribute(RestConverter<?> converter) {
        StringBuilder sb = new StringBuilder();
        sb.append(converter.getNamespace());
        sb.append(" "); //$NON-NLS-1$
        sb.append(host);
        sb.append(RestUtils.URL_SCHEMAS);
        sb.append(converter.getXsdFileName());
        return sb.toString();
    }

    /**
     * Marshals the API version attribute provided by this converter to the
     * underlying stream writer.
     *
     * @param writer  the writer to use for marshaling.
     */
    @Deprecated
    protected void marshalApiVersion(HierarchicalStreamWriter writer) {
        marshalApiVersion(writer, this);
    }

    /**
     * Marshals the API version attribute provided by the given converter to the
     * underlying stream writer.
     *
     * @param converter  the converter from which to retrieve API version information.
     * @param writer  the writer to use for marshaling.
     */
    @Deprecated
    protected void marshalApiVersion(HierarchicalStreamWriter writer, RestConverter<?> converter) {
        writer.addAttribute(API_VERSION, converter.getApiVersion());
    }

    /**
     * Marshal common entity attributes like API version, last modified and last modifier attributes
     * provided by this converter to the underlying stream writer.
     *
     * @param entity  the entity from which to retrieve the attributes to marshal.
     * @param writer  the writer to use for marshaling.
     */
    @Deprecated
    protected void marshalCommonAttributes(HierarchicalStreamWriter writer, EntityBase entity) {
        marshalCommonAttributes(writer, entity, this);
    }

    /**
     * Marshal common entity attributes like API version, last modified and last modifier attributes
     * provided by the given converter to the underlying stream writer.
     *
     * @param entity the entity from which to retrieve the attributes to marshal.
     * @param converter the converter from which to retrieve API version information.
     * @param writer  the writer to use for marshaling.
     */
    @Deprecated
    protected void marshalCommonAttributes(HierarchicalStreamWriter writer,
            EntityBase entity, RestConverter<?> converter) {
        marshalApiVersion(writer, converter);
        String lastModified = entity.getLastModified();
        if (StringUtils.isNotBlank(lastModified)) {
            writer.addAttribute(LAST_MODIFIED, lastModified);
        }
        String modifiedBy = entity.getLastModifiedBy();
        if (StringUtils.isNotBlank(modifiedBy)) {
            writer.addAttribute(MODIFIED_BY, modifiedBy);
        }
    }

    /**
     * Marshals a &lt;link&gt; tag with the given relation (<tt>rel</tt>) attribute an URL.
     *
     * @param writer  the writer to use for marshaling.
     * @param relation  the optional relation attribute, or <code>null</code>.
     * @param url  the URL of the link.
     */
    @Deprecated
    protected void writeLink(HierarchicalStreamWriter writer, String relation, String url) {
        writer.startNode(LINK);
        if (StringUtils.isNotBlank(relation)) {
            writer.addAttribute(REL, relation);
        }
        writer.addAttribute(HREF, url);
        writer.endNode();
    }

    /**
     * Marshals a &lt;link&gt; tag with the given relation (<tt>rel</tt>) attribute and an URL
     * pointing to the project with the specified unique identifier.
     *
     * @param writer  the writer to use for marshaling.
     * @param relation  the optional relation attribute, or <code>null</code>. Typical relations
     * for projects are {@link #SELF_RELATION}, {@link #PROJECT_RELATION}, {@link #PARENT_RELATION}
     * and {@link #SUBPROJECT_RELATION}.
     * @param uuid  the unique identifier of a project.
     */
    @Deprecated
    protected void writeProjectLink(HierarchicalStreamWriter writer, String relation, UUID uuid) {
        writeLink(writer, relation, host + RestUtils.URL_PROJECTS + uuid.toString());
    }

    /**
     * Marshals a &lt;link&gt; tag with the given relation (<tt>rel</tt>) attribute and an URL
     * pointing to the resource associated with the specified user.
     *
     * @param writer  the writer to use for marshaling.
     * @param relation  the optional relation attribute, or <code>null</code>. In most cases
     * the relation should be set to {@link #USER_RELATION}, but under certain circumstances
     * it might be useful to define relations with alternative semantics.
     * @param userId  the user's unique identifier.
     */
    @Deprecated
    protected void writeUserLink(HierarchicalStreamWriter writer, String relation, String userId) {
        writeLink(writer, relation, host + RestUtils.URL_USER + userId);
    }

    /**
     * Marshals an empty node with the given name, e.g. <tt>&lt;deleted/&gt;</tt>.
     *
     * @param writer  the writer to use for marshaling.
     * @param nodeName the name of the node.
     */
    @Deprecated
    protected void writeNode(HierarchicalStreamWriter writer, String nodeName) {
        writeNode(writer, nodeName, (String) null);
    }

    /**
     * Marshals a node with a <tt>xsd:long</tt> content and a given name.
     *
     * @param writer  the writer to use for marshaling.
     * @param nodeName the name of the node.
     * @param value the value of the node.
     */
    @Deprecated
    protected void writeNode(HierarchicalStreamWriter writer, String nodeName, long value) {
        writeNode(writer, nodeName, Long.toString(value));
    }

    /**
     * Marshals a node with string content and a given name.
     *
     * @param writer  the writer to use for marshaling.
     * @param nodeName the name of the node.
     * @param value the value of the node.
     */
    @Deprecated
    protected void writeNode(HierarchicalStreamWriter writer, String nodeName, String value) {
        if (StringUtils.isNotBlank(value)) {
            writer.startNode(nodeName);
            writer.setValue(value);
            writer.endNode();
        }
    }

    /**
     * Marshals a node with string content and a given name.
     * The given value is converted with <code>value.toString()</code>.
     *
     * @param writer  the writer to use for marshaling.
     * @param nodeName the name of the node.
     * @param value the value of the node.
     */
    @Deprecated
    protected void writeNode(HierarchicalStreamWriter writer, String nodeName, Object value) {
        String s = value != null? value.toString() : null;
        writeNode(writer, nodeName, s);
    }

    /**
     * Marshals a list of nodes with given item name under a common root node.
     *
     * @param writer  the writer to use for marshaling.
     * @param nodeName the name of the root node.
     * @param itemName  the name of the nodes in the list.
     * @param values  the collection of values to marshal.
     */
    @Deprecated
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

    /**
     * Marshals a node with a <tt>xsd:dateTime</tt> content and a given name.
     * The given timestamp is first converted to a {@link Calendar date} with timezone
     * UTC and locale EN; afterwards the date is converted to <tt>xsd:dateTime</tt>
     * format. The given timestamp in milliseconds argument is rendered also
     * as attribute <tt>millis</tt>.
     *
     * @param writer  the writer to use for marshaling.
     * @param nodeName  the name of the node.
     * @param millis  the timestamp to marshal. If the value is zero ior negative,
     * nothing will be marshalled.
     */
    @Deprecated
    protected void writeDateTime(HierarchicalStreamWriter writer, String nodeName, long millis) {
        if (millis > 0) {
            writer.startNode(nodeName);
            writer.addAttribute("millis", Long.toString(millis)); //$NON-NLS-1$
            writer.setValue(FormatUtils.formatUTC(millis));
            writer.endNode();
        }
    }

    /**
     * Renders the API version provided by this converter as <tt>apiVersion</tt> attribute.
     *
     * @param writer  the writer to use for rendering.
     */
    protected void apiVersion() throws IOException {
        apiVersion(this);
    }

    /**
     * Renders the API version provided by the given converter as <tt>apiVersion</tt> attribute.
     *
     * @param writer  the writer to use for rendering.
     * @param converter  the converter from which to retrieve API version information.
     */
    protected void apiVersion(RestConverter<?> converter) throws IOException {
        writer.attribute(API_VERSION, converter.getApiVersion());
    }

    /**
     * Renders common attributes like API version, last modified and last modifier.
     *
     * @param writer  the writer to use for rendering.
     * @param entity  the entity from which to retrieve the attributes to marshal.
     */
    protected void commonAttributes(EntityBase entity) throws IOException {
        commonAttributes(entity, this);
    }

    /**
     * Renders common attributes like API version, last modified and last modifier.
     *
     * @param writer  the writer to use for rendering.
     * @param entity the entity from which to retrieve the attributes to marshal.
     * @param converter the converter from which to retrieve API version information.
     */
    protected void commonAttributes(EntityBase entity, RestConverter<?> converter) throws IOException {
        apiVersion(converter);
        long lastModifiedMillis = entity.getLastModifiedMillis();
        if (lastModifiedMillis > 0) {
            writer.attribute(LAST_MODIFIED_MILLIS, lastModifiedMillis);
        }
        String lastModified = entity.getLastModified();
        if (StringUtils.isNotBlank(lastModified)) {
            writer.attribute(LAST_MODIFIED, lastModified);
        }
        String modifiedBy = entity.getLastModifiedBy();
        if (StringUtils.isNotBlank(modifiedBy)) {
            writer.attribute(MODIFIED_BY, modifiedBy);
        }
    }

    /**
     * Marshals the namespace attributes provided by this converter to the
     * underlying stream writer.
     *
     * @param writer  the writer to use for marshaling.
     */
    protected void namespaces() throws IOException {
        namespaces(this);
    }

    /**
     * Marshals the namespace attributes provided by the given converter to the
     * underlying stream writer.
     *
     * @param converter  the converter from which to retrieve namespace information.
     * @param writer  the writer to use for marshaling.
     */
    protected void namespaces(RestConverter<?> converter) throws IOException {
        writer.namespace(XMLUtils.XMLNS, converter.getNamespace());
        writer.namespace(XMLUtils.XMLNS_XSI, XMLUtils.XSI_INSTANCE_NS);
        if (StringUtils.isNotBlank(writer.getHost()) && StringUtils.isNotBlank(converter.getXsdFileName())) {
            writer.namespace(XMLUtils.XSI_SCHEMA_LOCATION, getSchemaLocation(converter));
        }
    }

    private String getSchemaLocation(RestConverter<?> converter) {
        StringBuilder sb = new StringBuilder();
        sb.append(converter.getNamespace());
        sb.append(" "); //$NON-NLS-1$
        sb.append(writer.getHost());
        sb.append("/schemas/"); //$NON-NLS-1$
        sb.append(converter.getXsdFileName());
        return sb.toString();
    }
}
