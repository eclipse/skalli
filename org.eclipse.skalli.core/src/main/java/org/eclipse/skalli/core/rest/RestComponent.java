/*******************************************************************************
 * Copyright (c) 2010-2015 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.core.rest;

import java.io.Writer;
import java.text.MessageFormat;

import org.eclipse.skalli.services.rest.RequestContext;
import org.eclipse.skalli.services.rest.RestService;
import org.eclipse.skalli.services.rest.RestWriter;
import org.restlet.data.MediaType;

/**
 * Service providing REST writers for various media types.
 */
public class RestComponent implements RestService {

    @Override
    public boolean isSupported(RequestContext context) {
        MediaType mediaType = context.getMediaType();
        return  MediaType.TEXT_XML.equals(mediaType) ||  MediaType.APPLICATION_JSON.equals(mediaType);
    }

    @Override
    public RestWriter getRestWriter(Writer writer, RequestContext context) {
        MediaType mediaType = context.getMediaType();
        String host = context.getHost();
        if (MediaType.TEXT_XML.equals(mediaType)) {
            return new XMLRestWriter(writer, host);
        } else if (MediaType.APPLICATION_JSON.equals(mediaType)) {
            return new JSONRestWriter(writer, host);
        } else {
            throw new IllegalArgumentException(MessageFormat.format("Unsupported media type ''{0}''", mediaType));
        }
    }
}
