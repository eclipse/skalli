package org.eclipse.skalli.api.rest;

import java.io.IOException;
import java.io.Writer;

import org.eclipse.skalli.model.ext.AliasedConverter;
import org.restlet.representation.Representation;

public class ResourceRepresentation<T> extends XStreamWriterRepresentation {

    private T object;

    public ResourceRepresentation() {
        super();
    }

    public ResourceRepresentation(T object) {
        super();
        this.object = object;
        if (object != null) {
            setAnnotatedClasses(object.getClass());
        }
    }

    public ResourceRepresentation(T object, AliasedConverter... converters) {
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
}
