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
package org.eclipse.skalli.common.util;

import static org.apache.commons.httpclient.HttpStatus.*;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.eclipse.skalli.model.ext.ExtensionEntityBase;
import org.eclipse.skalli.model.ext.Issue;
import org.eclipse.skalli.model.ext.Issuer;
import org.eclipse.skalli.model.ext.Link;
import org.eclipse.skalli.model.ext.PropertyValidator;
import org.eclipse.skalli.model.ext.Severity;
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
            url = (URL) value;
            label = url.toExternalForm();
        } else if (value instanceof Link) {
            Link link = (Link) value;
            try {
                url = new URL(link.getUrl());
                label = link.getLabel();
            } catch (MalformedURLException e) {
                CollectionUtils.addSafe(issues, getIssueByReachableHost(minSeverity, entityId, item, link.getUrl()));
            }
        } else {
            try {
                url = new URL(value.toString());
                label = url.toExternalForm();
            } catch (MalformedURLException e) {
                CollectionUtils.addSafe(issues, getIssueByReachableHost(minSeverity, entityId, item, value.toString()));
            }
        }

        if (url == null) {
            return;
        }

        try {
            if (HttpUtils.isSupportedProtocol(url)) {
                // Was a HEAD request, but Confluence Wiki & P4Web did not support that correctly
                url = encodeURL(url);
                LOG.info("GET " + url); //$NON-NLS-1$
                GetMethod method = new GetMethod(url.toExternalForm());
                method.setFollowRedirects(false); // we want to find 301 (MOVED PERMANTENTLY)
                int status = HttpUtils.getClient(url).executeMethod(method);
                LOG.info(status + " " + HttpStatus.getStatusText(status)); //$NON-NLS-1$
                CollectionUtils.addSafe(issues,
                        getIssueByResponseCode(minSeverity, entityId, item, method.getStatusCode(), label));
            } else {
                CollectionUtils.addSafe(issues, getIssueByReachableHost(minSeverity, entityId, item, url.getHost()));
            }
        } catch (UnknownHostException e) {
            issues.add(newIssue(Severity.ERROR, entityId, item, TXT_HOST_UNKNOWN, url.getHost()));
        } catch (ConnectException e) {
            issues.add(newIssue(Severity.ERROR, entityId, item, TXT_CONNECT_FAILED, url.getHost()));
        } catch (IOException e) {
            LOG.warn(MessageFormat.format("I/O Exception on validation: {0}", e.getMessage()), e); //$NON-NLS-1$
        } catch (RuntimeException e) {
            LOG.error(MessageFormat.format("RuntimeException on validation: {0}", e.getMessage()), e); //$NON-NLS-1$
        }
    }

    // use URI to properly encode the given URL
    static URL encodeURL(URL url) throws MalformedURLException {
        try {
            URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(),
                    url.getPath(), url.getQuery(), url.getRef());
            return new URL(uri.toASCIIString());
        } catch (URISyntaxException e) {
            throw new MalformedURLException(url.toString());
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
            final int responseCode, String label) {
        // everything below HTTP 300 is OK. Do not generate issues...
        if (responseCode < 300) {
            return null;
        }

        switch (minSeverity) {
        case INFO:
            switch (responseCode) {
            case SC_MULTIPLE_CHOICES:
                // Confluence Wiki generates a 302 for anonymous requests (for ANY page). This would mess up the entries using SAPs wiki.
                // case SC_MOVED_TEMPORARILY:
            case SC_SEE_OTHER:
            case SC_TEMPORARY_REDIRECT:
                return newIssue(Severity.INFO, entityId, item, TXT_RESOURCE_FOUND_REDIRECT, label, responseCode,
                        getStatusText(responseCode));
            case SC_REQUEST_TIMEOUT:
                return newIssue(Severity.INFO, entityId, item, TXT_VALIDATOR_NEEDS_UPDATE, label, responseCode,
                        getStatusText(responseCode));
            }
        case WARNING:
            switch (responseCode) {
            case SC_MOVED_PERMANENTLY:
                return newIssue(Severity.ERROR, entityId, item, TXT_RESOURCE_MOVED, label, responseCode,
                        getStatusText(responseCode));
            case SC_USE_PROXY:
            case SC_PROXY_AUTHENTICATION_REQUIRED:
                return newIssue(Severity.WARNING, entityId, item, TXT_MISSING_PROXY, label, responseCode,
                        getStatusText(responseCode));
            case SC_UNAUTHORIZED:
                // do not create an issue, as the link might be checked with an anonymous user;
                // project members might have the rights, you can't know.
                return null;
            case SC_LOCKED:
                return newIssue(Severity.WARNING, entityId, item, TXT_RESOURCE_LOCKED, label, responseCode,
                        getStatusText(responseCode));
            case SC_INTERNAL_SERVER_ERROR:
            case SC_SERVICE_UNAVAILABLE:
            case SC_GATEWAY_TIMEOUT:
            case SC_INSUFFICIENT_STORAGE:
                return newIssue(Severity.WARNING, entityId, item, TXT_TEMP_SERVER_PROBLEM, label, responseCode,
                        getStatusText(responseCode));
            }
        case ERROR:
            switch (responseCode) {
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
                return newIssue(Severity.ERROR, entityId, item, TXT_PERMANENT_REQUEST_PROBLEM, label, responseCode,
                        getStatusText(responseCode));
            case SC_NOT_IMPLEMENTED:
            case SC_BAD_GATEWAY:
                return newIssue(Severity.ERROR, entityId, item, TXT_PERMANENT_SERVER_PROBLEM, label, responseCode,
                        getStatusText(responseCode));
            }
        }

        return null;
    }

    /**
     * centralized issue generation (w/ message arguments)
     */
    private Issue newIssue(Severity severity, UUID entityId, int item, String message, Object... messageArguments) {
        return newIssue(severity, entityId, item, MessageFormat.format(message, messageArguments));
    }

    /**
     * centralized issue generation
     */
    private Issue newIssue(Severity severity, UUID entityId, int item, String message) {
        return new Issue(severity, HostReachableValidator.class, entityId, extension, propertyId, item, message);
    }
}
