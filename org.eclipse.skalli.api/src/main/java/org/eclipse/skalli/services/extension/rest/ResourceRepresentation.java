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

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.rest.RequestContext;
import org.eclipse.skalli.services.rest.RestService;
import org.eclipse.skalli.services.rest.RestWriter;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.WriterRepresentation;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.CompactWriter;
import com.thoughtworks.xstream.mapper.MapperWrapper;

/**
 * Base class for REST resources based on XStream as converter technology between beans
 * and their XML representation.
 *
 * @param <T>  the type of the bean with which this REST representation is associated.
 */
public class ResourceRepresentation<T> extends WriterRepresentation {

    private static final String RELATIVE_LINKS = "relative"; //$NON-NLS-1$
    private static final String LINKS_QUERY_ATTRIBUTE = "links"; //$NON-NLS-1$
    private static final String MEMBERS_QUERY_ATTRIBUTE = "members"; //$NON-NLS-1$
    private static final String ALL_MEMBERS = "all"; //$NON-NLS-1$

    private T object;
    private RequestContext context;
    private RestConverter<T> converter;

    private XStream xstream;
    private Set<Class<?>> annotatedClasses = new HashSet<Class<?>>();
    private Set<RestConverter> converters = new HashSet<RestConverter>();
    private Map<String, Class<?>> aliases = new HashMap<String, Class<?>>();
    private ClassLoader classLoader;
    private boolean compact;

    /**
     * Creates an uninitialized representation for converting an XML representation
     * into an instance of the bean.
     */
    public ResourceRepresentation() {
        super(MediaType.APPLICATION_XML);
    }

    /**
     * Creates a representation initialized with the given bean instance for
     * converting it to its XML representation.
     *
     * @param object  a bean instance.
     */
    @Deprecated
    public ResourceRepresentation(T object) {
        this();
        this.object = object;
        if (object != null) {
            addAnnotatedClass(object.getClass());
        }
    }

    /**
     * Creates a representation initialized with the given bean instance for
     * converting it to its XML representation, and adds additional converters
     * for non-standard property types.
     *
     * @param object  a bean instance.
     * @param converters  additional converters that may be necessary for
     * conversion of certain properties or inner beans of the bean.
     */
    @Deprecated
    public ResourceRepresentation(T object, RestConverter... converters) {
        this(object);
        if (converters != null) {
            for (RestConverter converter: converters) {
                addConverter(converter);
            }
        }
    }

    /**
     * Creates a resource representation for converting a given object to
     * a specific media type, e.g. to XML or JSON format.
     * <p>
     * Allows to specify additional converters for the conversion of
     * certain properties of the object.
     *
     * @param context the request context.
     * @param @param object the object to convert.
     * @param converter the converter to use.
     */
    public ResourceRepresentation(RequestContext context, T object, RestConverter<T> converter) {
        super(context.getMediaType());
        this.object = object;
        this.context = context;
        this.converter = converter;
    }

    @Override
    public void write(Writer writer) throws IOException {
        if (object != null) {
            if (context == null) {
                writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"); //$NON-NLS-1$
                XStream xstream = getXStream();
                if (compact) {
                    xstream.marshal(object, new CompactWriter(writer));
                } else {
                    xstream.toXML(object, writer);
                }
            } else if (converter.canConvert(object.getClass())) {
                RestService restService = Services.getRequiredService(RestService.class);
                MediaType mediaType = context.getMediaType();
                if (!restService.isSupportedMediaType(mediaType)) {
                    throw new IOException(MessageFormat.format("Unsupported media type ''{0}''", mediaType));
                }
                RestWriter restWriter = restService.getRestWriter(writer, context);
                String hrefQueryAttr = context.getQueryAttribute(LINKS_QUERY_ATTRIBUTE);
                if (RELATIVE_LINKS.equalsIgnoreCase(hrefQueryAttr)) {
                    restWriter.set(RestWriter.RELATIVE_LINKS);
                }
                String membersQueryAttr = context.getQueryAttribute(MEMBERS_QUERY_ATTRIBUTE);
                if (ALL_MEMBERS.equalsIgnoreCase(membersQueryAttr)) {
                    restWriter.set(RestWriter.ALL_MEMBERS);
                }
                try {
                    converter.marshal(object, restWriter);
                } catch (RuntimeException e) {
                    // don't trust the integrity of plugins!
                    throw new IOException(MessageFormat.format("Failed to render response for {0}",
                            context.getPath()), e);
                } finally {
                    restWriter.flush();
                }
            } else {
                throw new IOException("Failed to create resource representation");
            }
        }
    }

    /**
     * Reads the XML representation of a bean from a given representation (e.g. a string
     * or a socket) and initializes a bean instance from it.
     *
     * @param representation  the representation from which to read the XML representation of the bean.
     * @param c  the class of the bean to instantiate.
     *
     * @return an instance of the requested bean class initialized from it XML representation.
     *
     * @throws IOException  if reading the bean caused an i/o error.
     * @throws ClassCastExeption  if the XML representation of the bean cannot be casted to the
     * requested class.
     */
    @Deprecated
    public T read(Representation representation, Class<T> c) throws IOException {
        return read(representation.getStream(), c);
    }

    /**
     * Reads the XML representation of a bean from a given input stream and initializes
     * a bean instance from it.
     *
     * @param in  the stream to read.
     * @param c  the class of the bean to instantiate.
     *
     * @return an instance of the requested bean class initialized from it XML representation.
     *
     * @throws IOException  if reading the bean caused an i/o error.
     * @throws ClassCastExeption  if the XML representation of the bean cannot be casted to the
     * requested class.
     */
    @Deprecated
    public T read(InputStream in, Class<T> c) throws IOException {
        return c.cast(getXStream().fromXML(in));
    }

    /**
     * Adds an additional class with XStream annotations that represents an inner structure
     * of the class to convert, e.g. the entries of a collection-like property. If the class
     * to convert and the classes representing its inner structure live in different bundles,
     * calling this method is mandatory.
     * <p>
     * The class to convert is automatically added.
     *
     * @param annotatedClass  the class to add to the conversion.
     */
    @Deprecated
    public void addAnnotatedClass(Class<?> annotatedClass) {
        if (annotatedClass != null) {
            annotatedClasses.add(annotatedClass);
        }
    }

    /**
     * Adds an alias name for a given type.
     *
     * @param name  the alias.
     * @param type  the class for which to use the alias.
     */
    @Deprecated
    public void addAlias(String name, Class<?> type) {
        if (StringUtils.isNotBlank(name) && type != null) {
            aliases.put(name,  type);
        }
    }

    /**
     * Adds an additional converter for the conversion of certain properties
     * or inner classes, which are not supported by XStream out-of-the-box.
     *
     * @param converter  the REST converter to add.
     */
    @Deprecated
    public void addConverter(RestConverter converter) {
        if (converter != null) {
            converters.add(converter);
        }
    }

    /**
     * Sets an alternative classloader for the conversion.
     * @param classLoader  the classloader to use, or <code>null</code>
     * if the default classloader should be used.
     */
    @Deprecated
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Sets an alternative {@link XStream} instance. By default, a suitable
     * <code>XStream</code> instance is constructed automatically.
     *
     * @param xstream  a <code>XStream</code> instance, or <code>null</code>
     * if the default <code>XStream</code> instance should be used.
     */
    @Deprecated
    public void setXStream(XStream xstream) {
        this.xstream = xstream;
    }

    /**
     * Switches compact output on or off (default is off). Switching on
     * compact output will remove line breaks and indentations from the output
     * performing slightly faster and reducing the amount of data written
     * to the underlying socket.
     *
     * @param compact if <code>true</code> the output is compacted.
     */
    @Deprecated
    public void setCompact(boolean compact) {
        this.compact = compact;
    }

    /**
     * Creates an <code>XStream</code> instance for rendering/parsing the XML
     * representation of the bean.
     *
     * @return an <code>XStream</code> instance pre-initialized with the
     * specified {@link #setClassLoader(ClassLoader) class loader},
     * {@link #setConverters(RestConverter...) converters} and
     * {@link #setAnnotatedClasses(Class...) (inner) bean classes}. A special
     * wrapper is used to skip unknown tags in the XML representation.
     * {@link XStream#NO_REFERENCES} is set so that references betweem tags
     * are always resolved.
     */
    @Deprecated
    protected XStream getXStream() {
        if (xstream != null) {
            return xstream;
        }
        XStream result = new XStream() {
            @Override
            protected MapperWrapper wrapMapper(MapperWrapper next) {
                return new MapperWrapperIgnoreUnknownElements(next);
            }
        };
        for (RestConverter converter : converters) {
            result.registerConverter(converter);
            result.alias(converter.getAlias(), converter.getConversionClass());
        }
        for (Class<?> annotatedClass : annotatedClasses) {
            result.processAnnotations(annotatedClass);
        }
        for (Entry<String, Class<?>> alias: aliases.entrySet()) {
            result.alias(alias.getKey(), alias.getValue());
        }
        if (classLoader != null) {
            result.setClassLoader(classLoader);
        }
        result.setMode(XStream.NO_REFERENCES);
        return result;
    }

    @Deprecated
    private static class MapperWrapperIgnoreUnknownElements extends MapperWrapper {
        public MapperWrapperIgnoreUnknownElements(MapperWrapper next) {
            super(next);
        }

        @SuppressWarnings("rawtypes")
        @Override
        public boolean shouldSerializeMember(Class definedIn, String fieldName) {
            if (definedIn == Object.class) {
                return false;
            }
            return super.shouldSerializeMember(definedIn, fieldName);
        }
    }
}
