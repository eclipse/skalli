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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.SortedSet;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.ValidationException;
import org.eclipse.skalli.services.permit.Permit;
import org.eclipse.skalli.services.permit.Permit.Level;
import org.eclipse.skalli.services.permit.Permits;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ResourceBase extends ServerResource {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceBase.class);

    protected static final String ERROR_ID_MISSING_AUTHORIZATION = "rest:permit({0}):00"; //$NON-NLS-1$

    protected Representation checkAuthorization(String action, String path) {
        if (!Permits.isAllowed(action, path)) {
            String loggedInUser = Permits.getLoggedInUser();
            String message = StringUtils.isBlank(loggedInUser)?
                    MessageFormat.format("{0} {1}: Forbidden for anonymous users", action, path) :
                    MessageFormat.format("{0} {1}: Forbidden for user ''{2}''", action, path, loggedInUser);
            String messageId = MessageFormat.format(ERROR_ID_MISSING_AUTHORIZATION,
                    Permit.toString(action, path, Level.ALLOW));
            return createErrorRepresentation(Status.CLIENT_ERROR_FORBIDDEN, messageId, message);
        }
        return null;
    }

    protected Representation createIOErrorRepresentation(String errorId, IOException e) {
        String message = "I/O error when reading the request entity. Please report this error " +
                "response to the server administrators.";
        LOG.error(MessageFormat.format("{0} ({1})", message, errorId), e);
        return createErrorRepresentation(Status.SERVER_ERROR_INTERNAL, errorId, message);
    }

    protected Representation createUnexpectedErrorRepresentation(String errorId, Exception e) {
        String message = "An unexpected exception happend. Please report this error " +
                "response to the server administrators.";
        LOG.error(MessageFormat.format("{0} ({1})", message, errorId), e);
        return createErrorRepresentation(Status.SERVER_ERROR_INTERNAL, errorId, message);
    }

    protected Representation createParseErrorRepresentation(String errorId, Exception e) {
        String message = MessageFormat.format("Failed to parse request entity: {0}", e.getMessage());
        LOG.warn(MessageFormat.format("{0} ({1})", message, errorId), e);
        return createErrorRepresentation(Status.CLIENT_ERROR_BAD_REQUEST, errorId, message);
    }

    protected Representation createValidationFailedRepresentation(String errorId, String entityId,  ValidationException e) {
        return createValidationFailedRepresentation(errorId, entityId, e.getIssues());
    }

    protected Representation createValidationFailedRepresentation(String errorId, String entityId, SortedSet<Issue> issues) {
        String message = Issue.getMessage(MessageFormat.format(
                "Validation of {0} failed", entityId), issues);
        LOG.warn(MessageFormat.format("{0} ({1})", message, errorId));
        return createErrorRepresentation(Status.CLIENT_ERROR_BAD_REQUEST, errorId, message);
    }

    protected Representation createServiceUnavailableRepresentation(String errorId, String serviceName) {
        String message = MessageFormat.format("Services is not available: {0}", serviceName);
        LOG.warn(MessageFormat.format("{0} ({1})", message, errorId));
        return createErrorRepresentation(Status.SERVER_ERROR_SERVICE_UNAVAILABLE, errorId, message);
    }

    protected Representation createErrorRepresentation(Status status, String errorId, String pattern, Object... arguments) {
        String message = MessageFormat.format(pattern, arguments);
        return createErrorRepresentation(status, errorId, message);
    }

    protected Representation createErrorRepresentation(Status status, String errorId, String message) {
        ErrorRepresentation representation = new ErrorRepresentation(getHostRef().getHostIdentifier(), status, errorId, message);
        setStatus(status);
        return representation;
    }
}
