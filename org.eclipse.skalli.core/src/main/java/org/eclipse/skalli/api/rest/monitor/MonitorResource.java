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
package org.eclipse.skalli.api.rest.monitor;

import org.eclipse.skalli.services.extension.rest.ResourceRepresentation;
import org.eclipse.skalli.services.extension.rest.RestConverter;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public abstract class MonitorResource extends ServerResource {

    @Get
    public Representation retrieve() {
        RestConverter converter = getConverter(getRequest().getResourceRef().getHostIdentifier());
        if (converter == null) {
            return new EmptyRepresentation();
        }
        return new ResourceRepresentation<Object>(new Object(), converter);
    }

    protected abstract RestConverter getConverter(String host);
}
