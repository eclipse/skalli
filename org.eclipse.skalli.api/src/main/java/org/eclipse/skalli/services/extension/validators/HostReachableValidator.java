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
package org.eclipse.skalli.services.extension.validators;

import static org.apache.http.HttpStatus.*;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.params.HttpParams;
import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.commons.Link;
import org.eclipse.skalli.commons.URLUtils;
import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Issuer;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.services.destination.Destinations;
import org.eclipse.skalli.services.destination.HttpUtils;
import org.eclipse.skalli.services.extension.PropertyValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Validates that a host / URL is reachable by trying to establish a connection.</p>
 * <p>The following issue severities are covered:
 *   <ul>
 *     <li><strong>FATAL</strong> never</li>
 *     <li><strong>ERROR</strong> possible permanent problems</li>
 *     <li><strong>WARNING</strong> possible temporary problems</li>
 *     <li><strong>INFO</strong> informational / problems w/ validator implementation</li>
 *   </ul>
 * </p>
 */
public class HostReachableValidator implements Issuer, PropertyValidator {

    private static final Logger LOG = LoggerFactory.getLogger(HostReachableValidator.class);

    // TODO: I18N
    private static final String TXT_RESOURCE_FOUND_REDIRECT = "''{0}'' found, but a redirect is necessary ({1} {2}).";
    private static final String TXT_VALIDATOR_NEEDS_UPDATE = "Could not valdiate ''{0}''. Validator might need an update ({1} {2}).";
    private static final String TXT_MISSING_PROXY = "''{0}'' not found due to missing proxy ({1} {2}).";
    private static final String TXT_AUTH_REQUIRED = "''{0}'' found, but authentication required ({1} {2}).";
    private static final String TXT_RESOURCE_MOVED = "''{0}'' moved permanently ({1} {2}).";
    private static final String TXT_RESOURCE_LOCKED = "''{0}'' found, but locked ({1} {2}).";
    private static final String TXT_TEMP_SERVER_PROBLEM = "''{0}'' not found due to temporary problem on target server ({1} {2}).";
    private static final String TXT_PERMANENT_SERVER_PROBLEM = "''{0}'' not found due to a permanent problem on target server ({1} {2}).";
    private static final String TXT_PERMANENT_REQUEST_PROBLEM = "''{0}'' not found due to a permanent problem with the request ({1} {2}).";
    private static final String TXT_HOST_NOT_REACHABLE = "''{0}'' is not reachable.";
    private static final String TXT_HOST_UNKNOWN = "''{0}'' is unknown.";
    private static final String TXT_CONNECT_FAILED = "Could not connect to host ''{0}''.";
    private static final String TXT_MALFORMED_URL = "''{0}'' is malformed.";

    // general timeout for connection requests
    private static final int TIMEOUT = 10000;

    private final Class<? extends ExtensionEntityBase> extension;
    private final String propertyId;

    public HostReachableValidator(final Class<? extends ExtensionEntityBase> extension, final String propertyId) {
        this.extension = extension;
        this.propertyId = propertyId;
    }

    @Override
    public SortedSet<Issue> validate(final UUID entityId, final Object value, final Severity minSeverity) {
        final SortedSet<Issue> issues = new TreeSet<Issue>();

        // Do not participate in checks with Severity.FATAL & ignore null
        if (minSeverity.equals(Severity.FATAL) || value == null) {
            return issues;
        }

        if (value instanceof Collection) {
            int item = 0;
            for (Object collectionEntry : (Collection<?>) value) {
                validate(issues, entityId, collectionEntry, minSeverity, item);
                ++item;
            }
        } else {
            validate(issues, entityId, value, minSeverity, 0);
        }

        return issues;
    }

    protected void validate(final SortedSet<Issue> issues, final UUID entityId, final Object value,
            final Severity minSeverity, int item) {
        if (value == null) {
            return;
        }

        URL url = null;
        String label = null;
        if (value instanceof URL) {
            url = (URL)value;
            label = url.toExternalForm();
        } else if (value instanceof Link) {
            Link link = (Link) value;
            try {
                url = URLUtils.stringToURL(link.getUrl());
                label = link.getLabel();
            } catch (MalformedURLException e) {
                CollectionUtils.addSafe(issues, getIssueByReachableHost(minSeverity, entityId, item, link.getUrl()));
            }
        } else {
            try {
                url = URLUtils.stringToURL(value.toString());
                label = url != null ? url.toExternalForm() : value.toString();
            } catch (MalformedURLException e) {
                CollectionUtils.addSafe(issues, getIssueByReachableHost(minSeverity, entityId, item, value.toString()));
            }
        }

        if (url == null) {
            return;
        }

        HttpClient client = Destinations.getClient(url);
        if (client != null) {
            HttpResponse response = null;
            try {
                HttpParams params = client.getParams();
                HttpClientParams.setRedirecting(params, false); // we want to find 301 MOVED PERMANTENTLY
                HttpGet method = new HttpGet(url.toExternalForm());
                LOG.info("GET " + url); //$NON-NLS-1$
                response = client.execute(method);
                int status = response.getStatusLine().getStatusCode();
                LOG.info(status + " " + response.getStatusLine().getReasonPhrase()); //$NON-NLS-1$
                CollectionUtils.addSafe(issues,
                        getIssueByResponseCode(minSeverity, entityId, item, response.getStatusLine(), label));
            } catch (UnknownHostException e) {
                issues.add(newIssue(Severity.ERROR, entityId, item, TXT_HOST_UNKNOWN, url.getHost()));
            } catch (ConnectException e) {
                issues.add(newIssue(Severity.ERROR, entityId, item, TXT_CONNECT_FAILED, url.getHost()));
            } catch (MalformedURLException e) {
                issues.add(newIssue(Severity.ERROR, entityId, item, TXT_MALFORMED_URL, url));
            } catch (IOException e) {
                LOG.warn(MessageFormat.format("I/O Exception on validation: {0}", e.getMessage()), e); //$NON-NLS-1$
            } catch (RuntimeException e) {
                LOG.error(MessageFormat.format("RuntimeException on validation: {0}", e.getMessage()), e); //$NON-NLS-1$
            } finally {
                HttpUtils.consumeQuietly(response);
            }
        } else {
            CollectionUtils.addSafe(issues, getIssueByReachableHost(minSeverity, entityId, item, url.getHost()));
        }
    }

    /**
    * Returning an issue (Severity.ERROR) if host was not reachable, might be null
    */
    private Issue getIssueByReachableHost(final Severity minSeverity, final UUID entityId, final int item,
            final String host) {
        if (Severity.ERROR.compareTo(minSeverity) <= 0) {
            try {
                if (!InetAddress.getByName(host).isReachable(TIMEOUT)) {
                    return newIssue(Severity.ERROR, entityId, item, TXT_HOST_NOT_REACHABLE, host);
                }
            } catch (UnknownHostException e) {
                return newIssue(Severity.ERROR, entityId, item, TXT_HOST_UNKNOWN, host);
            } catch (IOException e) {
                LOG.warn(MessageFormat.format("I/O Exception on validation: {0}", e.getMessage()), e); //$NON-NLS-1$
                return null;
            }
        }
        return null;
    }

    /**
     * Returning an issue depending on the HTTP response code, might be null
     */
    private Issue getIssueByResponseCode(final Severity minSeverity, final UUID entityId, int item,
            StatusLine statusLine, String label) {
        // everything below HTTP 300 is OK. Do not generate issues...
        if (statusLine.getStatusCode() < 300) {
            return null;
        }

        switch (minSeverity) {
        case INFO:
            switch (statusLine.getStatusCode()) {
            case SC_MULTIPLE_CHOICES:
                // Confluence Wiki generates a 302 for anonymous requests (for ANY page). This would mess up the entries using SAPs wiki.
                // case SC_MOVED_TEMPORARILY:
            case SC_SEE_OTHER:
            case SC_TEMPORARY_REDIRECT:
                return newIssue(Severity.INFO, entityId, item, TXT_RESOURCE_FOUND_REDIRECT, label,
                        statusLine.getStatusCode(), statusLine.getReasonPhrase());
            case SC_REQUEST_TIMEOUT:
                return newIssue(Severity.INFO, entityId, item, TXT_VALIDATOR_NEEDS_UPDATE, label,
                        statusLine.getStatusCode(), statusLine.getReasonPhrase());
            }
        case WARNING:
            switch (statusLine.getStatusCode()) {
            case SC_MOVED_PERMANENTLY:
                return newIssue(Severity.ERROR, entityId, item, TXT_RESOURCE_MOVED, label, statusLine.getStatusCode(),
                        statusLine.getReasonPhrase());
            case SC_USE_PROXY:
            case SC_PROXY_AUTHENTICATION_REQUIRED:
                return newIssue(Severity.WARNING, entityId, item, TXT_MISSING_PROXY, label, statusLine.getStatusCode(),
                        statusLine.getReasonPhrase());
            case SC_UNAUTHORIZED:
                // do not create an issue, as the link might be checked with an anonymous user;
                // project members might have the rights, you can't know.
                return null;
            case SC_LOCKED:
                return newIssue(Severity.WARNING, entityId, item, TXT_RESOURCE_LOCKED, label,
                        statusLine.getStatusCode(), statusLine.getReasonPhrase());
            case SC_INTERNAL_SERVER_ERROR:
            case SC_SERVICE_UNAVAILABLE:
            case SC_GATEWAY_TIMEOUT:
            case SC_INSUFFICIENT_STORAGE:
                return newIssue(Severity.WARNING, entityId, item, TXT_TEMP_SERVER_PROBLEM, label,
                        statusLine.getStatusCode(), statusLine.getReasonPhrase());
            }
        case ERROR:
            switch (statusLine.getStatusCode()) {
            case SC_BAD_REQUEST:
            case SC_FORBIDDEN:
            case SC_NOT_FOUND:
            case SC_METHOD_NOT_ALLOWED:
            case SC_NOT_ACCEPTABLE:
            case SC_CONFLICT:
            case SC_GONE:
            case SC_LENGTH_REQUIRED:
            case SC_PRECONDITION_FAILED:
            case SC_REQUEST_TOO_LONG:
            case SC_REQUEST_URI_TOO_LONG:
            case SC_UNSUPPORTED_MEDIA_TYPE:
            case SC_REQUESTED_RANGE_NOT_SATISFIABLE:
            case SC_EXPECTATION_FAILED:
            case SC_UNPROCESSABLE_ENTITY:
            case SC_FAILED_DEPENDENCY:
                return newIssue(Severity.ERROR, entityId, item, TXT_PERMANENT_REQUEST_PROBLEM, label,
                        statusLine.getStatusCode(), statusLine.getReasonPhrase());
            case SC_NOT_IMPLEMENTED:
            case SC_BAD_GATEWAY:
                return newIssue(Severity.ERROR, entityId, item, TXT_PERMANENT_SERVER_PROBLEM, label,
                        statusLine.getStatusCode(), statusLine.getReasonPhrase());
            }
        }

        return null;
    }

    /**
     * centralized issue generation (w/ message arguments)
     */
    protected Issue newIssue(Severity severity, UUID entityId, int item, String message, Object... messageArguments) {
        return newIssue(severity, entityId, item, MessageFormat.format(message, messageArguments));
    }

    /**
     * centralized issue generation
     */
    protected Issue newIssue(Severity severity, UUID entityId, int item, String message) {
        return new Issue(severity, getClass(), entityId, extension, propertyId, item, message);
    }
}
