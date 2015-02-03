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

import java.io.Reader;
import java.io.Writer;

/**
 * Interface for a service that provides writers for REST resources.
 */
public interface RestService {

    /**
     * Returns <code>true</code>, if there is a suitable REST reader/writer available
     * based on the HTTP action and media type found in the given request context.
     *
     * @param context  the request context providing the source/target media type,
     * HTTP action and additional request parameters.
     */
    public boolean isSupported(RequestContext context);

    /**
     * Retrives a REST reader for the given request context.
     *
     * @param reader  the reader to wrap with a REST reader.
     * @param context  the request context providing the source media type, HTTP action
     * and additional request parameters.
     *
     * @return  a preconfigured REST reader, never <code>null</code>.
     */
    public RestReader getRestReader(Reader reader, RequestContext context);

    /**
     * Retrieves a REST writer for the given media type.
     *
     * @param writer  the writer to wrap with a REST writer.
     * @param context  the request context providing the target media type
     * and additional request parameters.
     *
     * @return  a preconfigured REST writer, never <code>null</code>.
     *
     * @throws IllegalArgumentException  if the given media type is not supported.
     */
    public RestWriter getRestWriter(Writer writer, RequestContext context);
}
