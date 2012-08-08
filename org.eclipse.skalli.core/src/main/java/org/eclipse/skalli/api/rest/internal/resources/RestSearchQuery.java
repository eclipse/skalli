package org.eclipse.skalli.api.rest.internal.resources;

import java.io.IOException;

import org.eclipse.skalli.services.extension.rest.ResourceRepresentation;
import org.eclipse.skalli.services.search.QueryParseException;
import org.eclipse.skalli.services.search.SearchQuery;
import org.restlet.data.Form;
import org.restlet.representation.Representation;

public class RestSearchQuery extends SearchQuery {

    public static final String PARAM_PERSIST = "persist"; //$NON-NLS-1$

    private boolean doPersist;
    private String template;

    public RestSearchQuery(Form form) throws QueryParseException {
        super(form.getValuesMap());
        doPersist = form.getValuesMap().containsKey(PARAM_PERSIST);
    }

    public RestSearchQuery(Form form, Representation entity) throws QueryParseException, IOException {
        this(form);
        if (entity != null) {
            ResourceRepresentation<PropertyUpdate> representation = new ResourceRepresentation<PropertyUpdate>();
            representation.setConverters(new PropertyUpdateConverter());
            representation.setAnnotatedClasses(PropertyUpdate.class);
            PropertyUpdate propertyUpdate = null;
            propertyUpdate = representation.read(entity, PropertyUpdate.class);
            template = propertyUpdate.getTemplate();
        }
    }

    public boolean doPersist() {
        return doPersist;
    }

    public String getTemplate() {
        return template;
    }
}