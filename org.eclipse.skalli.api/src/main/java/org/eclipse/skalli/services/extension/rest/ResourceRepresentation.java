package org.eclipse.skalli.services.extension.rest;

import java.io.IOException;
import java.io.Writer;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.WriterRepresentation;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.mapper.MapperWrapper;

public class ResourceRepresentation<T> extends WriterRepresentation {

    private T object;
    private XStream xstream;
    private Class<?>[] annotatedClasses;
    private RestConverter[] converters;
    private ClassLoader classLoader;

    public ResourceRepresentation() {
        super(MediaType.APPLICATION_XML);
    }

    public ResourceRepresentation(T object) {
        this();
        this.object = object;
        if (object != null) {
            setAnnotatedClasses(object.getClass());
        }
    }

    public ResourceRepresentation(T object, RestConverter... converters) {
        this(object);
        setConverters(converters);
    }

    @Override
    public void write(Writer writer) throws IOException {
        if (object != null) {
            writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"); //$NON-NLS-1$
            getXStream().toXML(object, writer);
        }
    }

    public T read(Representation representation, Class<T> c) throws IOException {
        return c.cast(getXStream().fromXML(representation.getStream()));
    }

    public void setAnnotatedClasses(Class<?>... annotatedClasses) {
        this.annotatedClasses = annotatedClasses;
    }

    public void setConverters(RestConverter... converters) {
        this.converters = converters;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void setXStream(XStream xstream) {
        this.xstream = xstream;
    }

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

        @Override
        public boolean shouldSerializeMember(Class definedIn, String fieldName) {
            if (definedIn == Object.class) {
                return false;
            }
            return super.shouldSerializeMember(definedIn, fieldName);
        }
    }
}
