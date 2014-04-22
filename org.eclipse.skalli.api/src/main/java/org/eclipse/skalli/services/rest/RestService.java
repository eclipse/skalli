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
package org.eclipse.skalli.services.rest;

import java.io.Writer;

import org.eclipse.skalli.services.extension.rest.RequestContext;
import org.restlet.data.MediaType;

/**
 * Interface for a service that provides writers for REST resources.
 */
public interface RestService {

    /**
     * Returns <code>true</code>, if there is a suitable REST writer available
     * for the given media type.
     *
     * @param mediaType  the media type, for which to retrieve a REST writer
     */
    public boolean isSupportedMediaType(MediaType mediaType);

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
