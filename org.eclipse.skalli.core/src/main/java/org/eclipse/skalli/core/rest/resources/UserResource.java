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
package org.eclipse.skalli.core.rest.resources;

import java.text.MessageFormat;

import org.eclipse.skalli.model.User;
import org.eclipse.skalli.services.extension.rest.ResourceBase;
import org.eclipse.skalli.services.extension.rest.ResourceRepresentation;
import org.eclipse.skalli.services.permit.Permits;
import org.eclipse.skalli.services.user.UserServices;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

public class UserResource extends ResourceBase {

    private static final String PARAM_USERID = "userId"; //$NON-NLS-1$

    @Get
    public Representation retrieve() {
        if (!Permits.isAllowed(getAction(), getPath())) {
            return createUnauthorizedRepresentation();
        }

        String id = (String) getRequestAttributes().get(PARAM_USERID);

        User user = UserServices.getUser(id);
        if (user.isUnknown()) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND, MessageFormat.format("User \"{0}\" not found", id)); //$NON-NLS-1$
            return null;
        }

        if (enforceOldStyleConverters()) {
            return new ResourceRepresentation<User>(user, new UserConverter(getHost()));
        }
        return new ResourceRepresentation<User>(getResourceContext(), user, new UserConverter());
    }
}
