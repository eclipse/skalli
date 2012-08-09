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
package org.eclipse.skalli.api.rest.internal.resources;

import java.text.MessageFormat;

import org.eclipse.skalli.model.User;
import org.eclipse.skalli.services.extension.rest.ResourceBase;
import org.eclipse.skalli.services.extension.rest.ResourceRepresentation;
import org.eclipse.skalli.services.extension.rest.RestUtils;
import org.eclipse.skalli.services.permit.Permit;
import org.eclipse.skalli.services.user.UserUtils;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

public class UserResource extends ResourceBase {

    @Get
    public Representation retrieve() {
        String path = getReference().getPath();
        Representation result = checkAuthorization(Permit.ACTION_GET, path);
        if (result != null) {
            return result;
        }

        String id = (String) getRequestAttributes().get(RestUtils.PARAM_ID);

        User user = UserUtils.getUser(id);
        if (user == null) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND, MessageFormat.format("User {0} not found", id)); //$NON-NLS-1$
            return null;
        }

        return new ResourceRepresentation<User>(user,
                new UserConverter(getRequest().getResourceRef().getHostIdentifier()));
    }
}
