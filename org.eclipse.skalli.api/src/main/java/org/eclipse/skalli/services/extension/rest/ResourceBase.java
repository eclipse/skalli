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
package org.eclipse.skalli.services.extension.rest;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.SortedSet;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.ValidationException;
import org.eclipse.skalli.services.BundleProperties;
import org.eclipse.skalli.services.permit.Permit;
import org.eclipse.skalli.services.permit.Permit.Level;
import org.eclipse.skalli.services.permit.Permits;
import org.eclipse.skalli.services.rest.RequestContext;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for the implementation of {@link RestExtensions REST extensions} providing
 * convenience methods for creating certain common {@link ErrorRepresentation error representations}
 * and accessing request and query attributes.
 */
public abstract class ResourceBase extends ServerResource {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceBase.class);

    private static final String ERROR_ID_MISSING_AUTHORIZATION = "rest:permit({0}):00"; //$NON-NLS-1$
    private static final String ERROR_ID_UNSUPPORTED_MEDIA_TYPE = "rest:mediaType({0}):00"; //$NON-NLS-1$

    private static final String ERROS_MSG_UNSUPPORTED_MEDIA_TYPE =
            "Unsupported media type {0}. Supported media types are 'text/xml' and 'application/json'."; //$NON-NLS-1$

    private RequestContext context;

    @Override
    protected void doInit() {
        super.doInit();
        context = new RequestContext(this);
    }

    /**
     * Returns the resource context providing information
     * about the request like query attributes and request path.
     */
    protected RequestContext getResourceContext() {
        return context;
    }

    /**
     * Returns the request path including the context path
     * prefix <code>/api</code>.
     */
    protected String getPath() {
        return context.getPath();
    }

    /**
     * Returns the action of this request.
     */
    protected String getAction() {
        return context.getAction();
    }

    /**
     * Returns the reference of the requested resource, or <code>null</code>
     * if the requested resource could not be determined.
     *
     * @see org.restlet.Request#getResourceRef().
     */
    protected Reference getResourceRef() {
        return context.getResourceRef();
    }

    /**
     * Returns the host part of the resource's URL including scheme,
     * host name and port number, or <code>null</code> if the host
     * could not be determined.
     */
    protected String getHost() {
        return context.getHost();
    }

    /**
     * Returns the query as form, which may be an {@link Form#Form() empty form}
     * if there is no query.
     */
    protected Form getQueryAsForm() {
        return context.getQueryAsForm();
    }

    /**
     * Returns the query as string, which may be an empty string
     * if there is no query.
     */
    protected String getQueryString() {
        return context.getQueryString();
    }

    /**
     * Returns the value of a given query attribute.
     *
     * @param name  the name of the attribute.
     *
     * @return  the value of the attribute, which may be
     * <code>null</code> either if there is no attribute
     * with the given name, or the attribute is a boolean
     * attribute.
     */
    protected String getQueryAttribute(String name) {
        return context.getQueryAttribute(name);
    }

    /**
     * Returns <code>true</code> if there is a query attribute
     * with the given name.
     *
     * @param name  the name of the attribute.
     */
    protected boolean hasQueryAttribute(String name) {
        return context.hasQueryAttribute(name);
    }

    /**
     * Returns the query attributes as map.
     * @return a mapping of attribute names to their respective values,
     * or an empty map if there is no query.
     */
    protected Map<String,String> getQueryAttributes() {
        return context.getQueryAttributes();
    }

    /**
     * Returns the value of the header matching the given name
     * (ignoring the case). If there are multiple headers with
     * the same name, only the first one is matched. If there
     * is no matching header, the given default value is returned.
     *
     * @param name  the header name.
     * @param defaultValue  the default value to return in case
     * there is no matching header.
     */
    protected String getHeader(String name, String defaultValue) {
        return context.getHeader(name, defaultValue);
    }

    /**
     * Returns the requested media type.
     */
    protected MediaType getMediaType() {
        return context.getMediaType();
    }

    /**
     * Returns <code>true</code>, if the requested media type is either
     * <tt>text/xml</tt> or <tt>application/json</tt>.
     */
    protected boolean isSupportedMediaType() {
        return context.isXML() || context.isJSON();
    }

    /**
     * Checks whether the XStream-based REST converters should be used for
     * rendering the response of this REST request.
     * <p>
     * This method checks whether the request has a query attribute named
     * <tt>"rest"</tt>, or whether there is a system-wide
     * {@link BundleProperties#getProperty(String) bundle/system property}
     * named <tt>"skalli.rest"</tt> with a value <i>different</i> from <tt>"v1"</tt>.
     * In that case, e.g. the request has a query attribute <tt>"rest=v2"</tt>,
     * the method returns <code>false</code> to indicate that the new
     * RestWriter-based converters should be employed. Otherwise the
     * method returns <code>true</code>.
     * <p>
     * If the requested media type is different from <tt>"text/xml"</tt>,
     * always <code>false</code> will be returned.
     *
     * @return <code>true</code>, if XStream-based converters should be used
     * for rendering the response of this REST request.
     */
    @SuppressWarnings("nls")
    protected boolean enforceOldStyleConverters() {
        if (!context.isXML()) {
            return false;
        }
        String restVersion = getQueryAttribute("rest");
        if (StringUtils.isBlank(restVersion)) {
            restVersion = BundleProperties.getProperty("skalli.rest");
        }
        if (StringUtils.isNotBlank(restVersion)) {
            return "v1".equalsIgnoreCase(restVersion);
        }
        return true;
    }

    /**
     * Creates an {@link ErrorRepresentation error representation} for authorization errors
     * and writes a corresponding audit log entry (severity WARN). Sets the response status to
     * <tt>403 Forbidden</tt>.
     *
     * @param action  the action to perform.
     * @param path  path of the resource on which the action is to be performed.
     */
    protected Representation createUnauthorizedRepresentation() {
        String loggedInUser = Permits.getLoggedInUser();
        String action = context.getAction();
        String path = context.getPath();
        String message = StringUtils.isBlank(loggedInUser)?
                MessageFormat.format("{0} {1}: Forbidden for anonymous users", action, path) :
                MessageFormat.format("{0} {1}: Forbidden for user ''{2}''", action, path, loggedInUser);
        String messageId = MessageFormat.format(ERROR_ID_MISSING_AUTHORIZATION,
                Permit.toString(action, path, Level.ALLOW));
        return createErrorRepresentation(Status.CLIENT_ERROR_FORBIDDEN, messageId, message);
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
        ErrorRepresentation representation = new ErrorRepresentation(context, status, errorId, message);
        setStatus(status);
        return representation;
    }
}
