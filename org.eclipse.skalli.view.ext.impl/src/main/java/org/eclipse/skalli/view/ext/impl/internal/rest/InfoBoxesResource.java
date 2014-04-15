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
package org.eclipse.skalli.view.ext.impl.internal.rest;

import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.extension.rest.ResourceBase;
import org.eclipse.skalli.services.extension.rest.ResourceRepresentation;
import org.eclipse.skalli.services.permit.Permits;
import org.eclipse.skalli.view.ext.InfoBox;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

public class InfoBoxesResource extends ResourceBase {

    @Get
    public Representation retrieve() {
        if (!Permits.isAllowed(getAction(), getPath())) {
            return createUnauthorizedRepresentation();
        }

        InfoBoxes infoboxes = new InfoBoxes(Services.getServices(InfoBox.class));

        if (enforceOldStyleConverters()) {
            return new ResourceRepresentation<InfoBoxes>(infoboxes,
                   new InfoBoxesConverter(getHost()));
        }
        return new ResourceRepresentation<InfoBoxes>(getResourceContext(), infoboxes,
                new InfoBoxesConverter());
    }
}
