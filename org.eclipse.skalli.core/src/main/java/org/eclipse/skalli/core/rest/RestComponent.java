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

import java.io.Reader;
import java.io.Writer;
import java.text.MessageFormat;

import org.eclipse.skalli.services.rest.RequestContext;
import org.eclipse.skalli.services.rest.RestReader;
import org.eclipse.skalli.services.rest.RestService;
import org.eclipse.skalli.services.rest.RestWriter;

/**
 * Service providing REST writers for various media types.
 */
public class RestComponent implements RestService {

    @Override
    public boolean isSupported(RequestContext context) {
        String action = context.getAction();
        if ("GET".equalsIgnoreCase(action)) { //$NON-NLS-1$
            return context.isXML() || context.isJSON();
        } else {
            return context.isJSON();
        }
    }

    @Override
    public RestReader getRestReader(Reader reader, RequestContext context) {
        if (context.isJSON()) {
            return new JSONRestReader(reader);
        } else {
            throw new IllegalArgumentException(MessageFormat.format("Unsupported media type ''{0}''", context.getMediaType()));
        }
    }

    @Override
    public RestWriter getRestWriter(Writer writer, RequestContext context) {
        String host = context.getHost();
        if (context.isXML()) {
            return new XMLRestWriter(writer, host);
        } else if (context.isJSON()) {
            return new JSONRestWriter(writer, host);
        } else {
            throw new IllegalArgumentException(MessageFormat.format("Unsupported media type ''{0}''", context.getMediaType()));
        }
    }
}
