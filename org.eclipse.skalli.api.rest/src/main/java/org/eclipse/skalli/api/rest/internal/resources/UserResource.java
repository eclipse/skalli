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

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import org.eclipse.skalli.api.java.authentication.UserUtil;
import org.eclipse.skalli.api.rest.internal.util.IgnoreUnknownElementsXStreamRepresentation;
import org.eclipse.skalli.common.User;
import org.eclipse.skalli.common.util.Statistics;
import org.eclipse.skalli.model.ext.AliasedConverter;

public class UserResource extends AbstractServerResource {

    @Get
    public Representation retrieve() {
        Statistics.getDefault().trackUsage("api.rest.user.get"); //$NON-NLS-1$

        String id = (String) getRequestAttributes().get("id"); //$NON-NLS-1$

        User user = UserUtil.getUser(id);
        if (user == null) {
            return createError(Status.CLIENT_ERROR_NOT_FOUND, "User \"{0}\" not found.", id); //$NON-NLS-1$
        }

        return new IgnoreUnknownElementsXStreamRepresentation<User>(user, new AliasedConverter[] { new UserConverter(
                getRequest().getResourceRef().getHostIdentifier()) });
    }
}
