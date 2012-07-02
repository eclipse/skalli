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
package org.eclipse.skalli.services.extension.rest;

import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.services.permit.Permits;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ServerResource;

public abstract class ResourceBase extends ServerResource {

    protected Representation checkAuthorization(String action, String path) {
        if (!Permits.isAllowed(action, path)) {
            String loggedInUser = Permits.getLoggedInUser();
            String msg = StringUtils.isBlank(loggedInUser)?
                    "Access denied for anonymous users" :
                    MessageFormat.format("Access denied for user {0}", loggedInUser);
            Representation result = new StringRepresentation(msg, MediaType.TEXT_PLAIN);
            setStatus(Status.CLIENT_ERROR_FORBIDDEN, msg);
            return result;
        }
        return null;
    }

    protected Representation createStatusMessage(Status status, String message, Object... args) {
        getResponse().setStatus(status);
        return new StringRepresentation(MessageFormat.format(message, args));
    }

}
