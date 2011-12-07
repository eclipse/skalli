package org.eclipse.skalli.api.rest;

import org.eclipse.skalli.api.rest.internal.util.IgnoreUnknownElementsXStream;
import org.eclipse.skalli.model.ext.AliasedConverter;
import org.restlet.data.MediaType;
import org.restlet.representation.WriterRepresentation;

import com.thoughtworks.xstream.XStream;

public abstract class XStreamWriterRepresentation extends WriterRepresentation {

    private XStream xstream;
    private Class<?>[] annotatedClasses;
    private AliasedConverter[] converters;
    private ClassLoader classLoader;

    public XStreamWriterRepresentation() {
        super(MediaType.APPLICATION_XML);
    }

    public void setAnnotatedClasses(Class<?>... annotatedClasses) {
        this.annotatedClasses = annotatedClasses;
    }

    public void setConverters(AliasedConverter... converters) {
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
        XStream result = new IgnoreUnknownElementsXStream();
        if (converters != null) {
            for (AliasedConverter converter : converters) {
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
}
