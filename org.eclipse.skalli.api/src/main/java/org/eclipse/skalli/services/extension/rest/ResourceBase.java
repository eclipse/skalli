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

/**
 * Base class for the implementation of {@link RestExtensions REST extensions} providing
 * convenience methods for creating certain common {@link ErrorRepresentation error representations}
 * and for permit checks.
 */
public abstract class ResourceBase extends ServerResource {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceBase.class);

    private static final String ERROR_ID_MISSING_AUTHORIZATION = "rest:permit({0}):00"; //$NON-NLS-1$

    /**
     * Checks whether the currently logged in user has the necessary permits to
     * perform a certain <code>action</code> on a resource given by its <code>path</code>.
     *
     * @param action  the action to perform.
     * @param path  path of the resource on which the action is to be performed.
     *
     * @return <code>null</code>, if the user is authorized to perform the action,
     * otherwise an {@link ErrorRepresenation}.
     */
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

    /**
     * Creates an {@link ErrorRepresentation error representation} for i/o errors
     * and writes a corresponding log entry (severity ERROR). Sets the response status to
     * <tt>500 Internal Server Error</tt>.
     *
     * @param errorId  a unique identifier for the error that has happened.
     * @param e  the exception describing the i/o error.
     */
    protected Representation createIOErrorRepresentation(String errorId, IOException e) {
        String message = "I/O error when reading the request entity. Please report this error " +
                "response to the server administrators.";
        LOG.error(MessageFormat.format("{0} ({1})", message, errorId), e);
        return createErrorRepresentation(Status.SERVER_ERROR_INTERNAL, errorId, message);
    }

    /**
     * Creates an {@link ErrorRepresentation error representation} for unexpected exceptions
     * and writes a corresponding log entry (severity ERROR). Sets the response status to
     * <tt>500 Internal Server Error</tt>.
     *
     * @param errorId  a unique identifier for the error that has happened.
     * @param e  the exception to wrap as error representation.
     */
    protected Representation createUnexpectedErrorRepresentation(String errorId, Exception e) {
        String message = "An unexpected exception happend. Please report this error " +
                "response to the server administrators.";
        LOG.error(MessageFormat.format("{0} ({1})", message, errorId), e);
        return createErrorRepresentation(Status.SERVER_ERROR_INTERNAL, errorId, message);
    }

    /**
     * Creates an {@link ErrorRepresentation error representation} for parsing errors
     * and writes a corresponding log entry (severity WARN). Sets the response status to
     * <tt>400 Bad Request</tt>.
     *
     * @param errorId  a unique identifier for the error that has happened.
     * @param e  the exception to wrap as error representation.
     */
    protected Representation createParseErrorRepresentation(String errorId, Exception e) {
        String message = MessageFormat.format("Failed to parse request entity: {0}", e.getMessage());
        LOG.warn(MessageFormat.format("{0} ({1})", message, errorId), e);
        return createErrorRepresentation(Status.CLIENT_ERROR_BAD_REQUEST, errorId, message);
    }

    /**
     * Creates an {@link ErrorRepresentation error representation} for validation exceptions
     * and writes a corresponding log entry (severity WARN). Sets the response status to
     * <tt>400 Bad Request</tt>.
     *
     * @param errorId  a unique identifier for the error that has happened.
     * @param entityId  the entity, e.g. a project, that has validation problems.
     * @param e  the exception to wrap as error representation.
     */
    protected Representation createValidationFailedRepresentation(String errorId, String entityId,  ValidationException e) {
        return createValidationFailedRepresentation(errorId, entityId, e.getIssues());
    }

    /**
     * Creates an {@link ErrorRepresentation error representation} for given set of issues
     * and writes a corresponding log entry (severity WARN). Sets the response status to
     * <tt>400 Bad Request</tt>.
     *
     * @param errorId  a unique identifier for the error that has happened.
     * @param entityId  the entity, e.g. a project, that has validation problems.
     * @param issues  the issues to report.
     */
    protected Representation createValidationFailedRepresentation(String errorId, String entityId, SortedSet<Issue> issues) {
        String message = Issue.getMessage(MessageFormat.format(
                "Validation of {0} failed", entityId), issues);
        LOG.warn(MessageFormat.format("{0} ({1})", message, errorId));
        return createErrorRepresentation(Status.CLIENT_ERROR_BAD_REQUEST, errorId, message);
    }

    /**
     * Creates an {@link ErrorRepresentation error representation} for an unavailable service
     * and writes a corresponding log entry (severity WARN). Sets the response status to
     * <tt>503 Service Unavailable</tt>.
     *
     * @param errorId  a unique identifier for the error that has happened.
     * @param serviceName  the name of the service that is not available.
     */
    protected Representation createServiceUnavailableRepresentation(String errorId, String serviceName) {
        String message = MessageFormat.format("Services is not available: {0}", serviceName);
        LOG.warn(MessageFormat.format("{0} ({1})", message, errorId));
        return createErrorRepresentation(Status.SERVER_ERROR_SERVICE_UNAVAILABLE, errorId, message);
    }

    /**
     * Creates a generic {@link ErrorRepresentation error representation} from a status, an error identifier
     * and a message that is composed from a pattern and arguments. Sets the status of the REST response to
     * that given as argument, but writes no log entries.
     *
     * @param status  the status assigned to the error.
     * @param errorId  a unique identifier for the error that has happened.
     * @param pattern  the error message with wildcards (see {@link MessageFormat#format(String, Object...)}).
     * @param arguments  arguments to replace the wildcards in the message with.
     */
    protected Representation createErrorRepresentation(Status status, String errorId, String pattern, Object... arguments) {
        String message = MessageFormat.format(pattern, arguments);
        return createErrorRepresentation(status, errorId, message);
    }

    /**
     * Creates a generic {@link ErrorRepresentation error representation} with from a status, an error identifier
     * and a message. Sets the status of the REST response to that given as argument, but writes no log entries.
     *
     * @param status  the status assigned to the error.
     * @param errorId  a unique identifier for the error that has happened.
     * @param message  the error message.
     */
    protected Representation createErrorRepresentation(Status status, String errorId, String message) {
        ErrorRepresentation representation = new ErrorRepresentation(getHostRef().getHostIdentifier(), status, errorId, message);
        setStatus(status);
        return representation;
    }
}
