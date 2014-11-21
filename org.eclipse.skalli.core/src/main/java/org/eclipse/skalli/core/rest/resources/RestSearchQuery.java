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
            representation.addConverter(new PropertyUpdateConverter());
            representation.addAnnotatedClass(PropertyUpdate.class);
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