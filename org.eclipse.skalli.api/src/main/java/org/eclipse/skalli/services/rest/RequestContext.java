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
package org.eclipse.skalli.services.rest;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.CollectionUtils;
import org.restlet.Request;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.data.Reference;
import org.restlet.resource.ServerResource;

/**
 * Helper class providing information about a request for a REST resource
 * like query attributes and request path.
 */
public class RequestContext {

    private static final String ACCEPT_ALL = "*/*"; //$NON-NLS-1$

    @SuppressWarnings("nls")
    private static final Set<String> FORMAT_JSON =
            CollectionUtils.asSet("json", "text/json", MediaType.APPLICATION_JSON.toString());

    // for backward compatibility and to facilitate browser access,
    // we also accept "html" and "text/html", but treat them as "text/xml"
    @SuppressWarnings("nls")
    private static final Set<String> FORMAT_XML =
            CollectionUtils.asSet(
                    "xml", MediaType.TEXT_XML.toString(), MediaType.APPLICATION_XML.toString(),
                    "html", MediaType.TEXT_HTML.toString());

    private static final String ACCEPT_QUERY_PARAM = "accept"; //$NON-NLS-1$

    private String action;
    private String path;
    private Reference resourceRef;
    private Form form;
    private Form headers;
    private Map<String,String> queryParams;
    private MediaType mediaType;

    /**
     * Creates a request context from a given REST request.
     * <p>
     * Extracts request parameters like {@link #getPath() path} and
     * {@link #getQueryAttributes() query attributes}.
     *
     * @param request  the REST request to evaluate.
     */
    public RequestContext(Request request) {
        action = request.getMethod().getName();
        resourceRef = request.getResourceRef();
        path = resourceRef != null ? resourceRef.getPath() : "/"; //$NON-NLS-1$
        form = resourceRef != null ? resourceRef.getQueryAsForm() : null;
        if (form != null) {
            queryParams = form.getValuesMap();
        } else {
            form = new Form();
            queryParams = Collections.emptyMap();
        }
        mediaType = getMediaType(request);
        headers = (Form) request.getAttributes().get("org.restlet.http.headers"); //$NON-NLS-1$
    }

    /**
     * Creates a request context from a given server resource.
     * <p>
     * This is a convenience method equivalent to
     * {@link #RequestContext(Request) RequestContext(serverResource.getRequest())}.
     *
     * @param serverResource  the server resource to evaluate.
     */
    public RequestContext(ServerResource serverResource) {
        this(serverResource.getRequest());
    }

    /**
     * Returns the request path including the context path
     * prefix <code>/api</code>.
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the action of this request.
     */
    public String getAction() {
        return action;
    }

    /**
     * Returns the reference of the requested resource, or <code>null</code>
     * if the requested resource could not be determined.
     *
     * @see org.restlet.Request#getResourceRef().
     */
    public Reference getResourceRef() {
        return resourceRef;
    }

    /**
     * Returns the host part of the resource's URL including scheme,
     * host name and port number, or <code>null</code> if the host
     * could not be determined.
     */
    public String getHost() {
        return resourceRef != null ? resourceRef.getHostIdentifier() : null;
    }

    /**
     * Returns the query as form, which may be an {@link Form#Form() empty form}
     * if there is no query.
     */
    public Form getQueryAsForm() {
        return form;
    }

    /**
     * Returns the query as string, which may be an empty string
     * if there is no query.
     */
    public String getQueryString() {
        return form.getQueryString();
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
    public String getQueryAttribute(String name) {
        return queryParams.get(name);
    }

    /**
     * Returns <code>true</code> if there is a query attribute
     * with the given name.
     *
     * @param name  the name of the attribute.
     */
    public boolean hasQueryAttribute(String name) {
        return queryParams.containsKey(name);
    }

    /**
     * Returns the query attributes as map.
     * @return a mapping of attribute names to their respective values,
     * or an empty map if there is no query.
     */
    public Map<String,String> getQueryAttributes() {
        return queryParams;
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
    public String getHeader(String name, String defaultValue) {
        return headers.getFirstValue(name, true, defaultValue);
    }

    /**
     * Returns the requested media type, never <code>null</code>.
     * If no media type can be determined from the request, <tt>"text/xml"</tt>
     * is returned.
     */
    public MediaType getMediaType() {
        return mediaType;
    }

    /**
     * Returns <code>true</code> if the media type is <tt>text/xml</tt>.
     */
    public boolean isXML() {
        return MediaType.TEXT_XML.equals(mediaType);
    }

    /**
     * Returns <code>true</code> if the media type is <tt>application/json</tt>.
     */
    public boolean isJSON() {
        return MediaType.APPLICATION_JSON.equals(mediaType);
    }

    private MediaType getMediaType(Request request) {
        MediaType mediaType = null;
        if (request.isEntityAvailable()) {
            mediaType = request.getEntity().getMediaType();
        }
        if (mediaType == null) {
            String accept = getQueryAttribute(ACCEPT_QUERY_PARAM);
            if (StringUtils.isBlank(accept)) {
                accept = getMaxQualityAccept(request);
            }
            if (StringUtils.isNotBlank(accept)) {
                if  (FORMAT_XML.contains(accept)) {
                    mediaType = MediaType.TEXT_XML;
                } else if (FORMAT_JSON.contains(accept)) {
                    mediaType = MediaType.APPLICATION_JSON;
                } else if (ACCEPT_ALL.equals(accept)) {
                    mediaType = MediaType.TEXT_XML;
                } else {
                    mediaType = MediaType.valueOf(accept);
                }
            }
            if (mediaType == null) {
                mediaType = MediaType.TEXT_XML;
            }
        }
        return mediaType;
    }

    /**
     * Scans the <tt>Accept</tt> header for the media type with the highest <tt>"q"</tt> attribute.
     * If the list of accepted media type contains {@value #ACCEPT_ALL}, {@value #ACCEPT_ALL} is returned.
     */
    private String getMaxQualityAccept(Request request) {
        String maxQualityAccept = null;
        float maxQuality = 0;
        for (Preference<MediaType> acceptedMediaType: request.getClientInfo().getAcceptedMediaTypes()) {
            String mediaType = acceptedMediaType.getMetadata().getName();
            if (ACCEPT_ALL.equals(mediaType)) {
                return ACCEPT_ALL;
            }
            float quality = acceptedMediaType.getQuality();
            if (quality > maxQuality) {
                maxQualityAccept = mediaType;
                maxQuality = quality;
            }
        }
        return maxQualityAccept;
    }
}
