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
import java.io.Writer;

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

    private T object;
    private XStream xstream;
    private Class<?>[] annotatedClasses;
    private RestConverter[] converters;
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
    public ResourceRepresentation(T object) {
        this();
        this.object = object;
        if (object != null) {
            setAnnotatedClasses(object.getClass());
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
    public ResourceRepresentation(T object, RestConverter... converters) {
        this(object);
        setConverters(converters);
    }

    @Override
    public void write(Writer writer) throws IOException {
        if (object != null) {
            writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"); //$NON-NLS-1$
            XStream xstream = getXStream();
            if (compact) {
                xstream.marshal(object, new CompactWriter(writer));
            } else {
                xstream.toXML(object, writer);
            }
        }
    }

    /**
     * Reads the XML representation of a bean from a given representation (e.g. a string
     * or a socket) and initializes a bean instance from it.
     *
     * @param representation  the representation from which to read the XML representation of the bean.
     * @param c  the class of the bean ton instantiate.
     *
     * @return an instance of the requested bean class initialized from it XML representation.
     *
     * @throws IOException  if reading the bean caused an i/o error.
     * @throws ClassCastExeption  if the XML representation of the bean cannot be casted to the
     * requested class.
     */
    public T read(Representation representation, Class<T> c) throws IOException {
        return c.cast(getXStream().fromXML(representation.getStream()));
    }

    /**
     * Adds additional classes with XStream annotations that represent parts of the
     * inner structure of the bean, e.g. the entries of a collection-like property,
     * to the converter. If the bean class and the classes representing its
     * inner structure live in different bundle, calling this method is mandatory.
     * <p>
     * The bean class itself is automatically added to the conversion.
     *
     * @param annotatedClasses  a list of classes to add to the conversion.
     */
    public void setAnnotatedClasses(Class<?>... annotatedClasses) {
        this.annotatedClasses = annotatedClasses;
    }

    /**
     * Sets additional converters that may be necessary for the conversion of certain properties
     * or inner beans of the bean, which are not supported by XStream out-of-box.
     *
     * @param converters  a list of converters necessary for the conversion.
     */
    public void setConverters(RestConverter... converters) {
        this.converters = converters;
    }

    /**
     * Sets an alternative classloader for the conversion.
     * @param classLoader  the classloader to use, not <code>null</code>.
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Sets an alternative {@link XStream} instance. By default, a suitable
     * <code>XStream</code> instance is constructed automatically.
     *
     * @param xstream  a <code>XStream</code> instance, not <code>null</code>.
     */
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
        if (converters != null) {
            for (RestConverter converter : converters) {
                result.registerConverter(converter);
                result.alias(converter.getAlias(), converter.getConversionClass());
            }
        }
        if (annotatedClasses != null) {
            for (Class<?> annotatedClass : annotatedClasses) {
                result.processAnnotations(annotatedClass);
            }
        }
        if (classLoader != null) {
            result.setClassLoader(classLoader);
        }
        result.setMode(XStream.NO_REFERENCES);
        return result;
    }

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
