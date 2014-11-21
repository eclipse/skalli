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

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

import org.eclipse.skalli.commons.URLUtils;
import org.restlet.data.MediaType;

/**
 * Base class for the implementation of REST writers.
 * This class implements the options mechanism as well as some
 * common functionality.
 */
public abstract class RestWriterBase implements RestWriter {

    protected Writer writer;
    protected String host;
    protected long options;

    protected RestWriterBase(Writer writer) {
        this.writer = writer;
    }

    protected RestWriterBase(Writer writer, String host, long options) {
        this(writer);
        this.host = URLUtils.removeSlashStartEnd(host);
        this.options = options;
    }

    @Override
    public boolean isSet(long optionsMask) {
        return (options & optionsMask) == optionsMask;
    }

    @Override
    public RestWriter set(long optionsMask) {
        options |= optionsMask;
        return this;
    }

    @Override
    public RestWriter reset(long optionsMask) {
        options &= ~optionsMask;
        return this;
    }

    @Override
    public RestWriter link(String rel, Object... pathSegments) throws IOException {
        return link(rel, hrefOf(pathSegments));
    }

    @Override
    public RestWriter collection(String key, String itemKey, Collection<String> values) throws IOException {
        array(key, itemKey);
        for (String s : values) {
            value(s);
        }
        end();
        return this;
    }

    @Override
    public RestWriter timestamp(String key, long millis) throws IOException {
        if (millis > 0) {
            object(key)
              .attribute("millis", millis) //$NON-NLS-1$
              .datetime(millis)
            .end();
        }
        return this;
    }

    @Override
    public boolean isMediaType(MediaType mediaType) {
        return mediaType != null && mediaType.equals(getMediaType());
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public String hrefOf(Object... pathSegments) {
        return URLUtils.concat(isSet(RELATIVE_LINKS)? null : host, pathSegments);
    }
}