/*******************************************************************************
 * Copyright (c) 2010 - 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.services.extension.rest;

import org.restlet.resource.ServerResource;

/**
 * Interface describing services that can add new REST API calls.
 */
public interface RestExtension {

    /**
     * Returns the resource paths this REST extension serves.
     *
     * Note, the resource path is treated as <a href="http://tools.ietf.org/html/rfc6570">URI template</a>
     * in the sense of RFC 6570, i.e. may contain placeholders representing request parameters.
     *
     * For example:
     * <pre>
     * /projects/{id}
     * /projects/{id}/subprojects
     * /updatesites/{userId}/{id}
     * </pre>
     *
     * @return a (non-null, non-empty) array of resource paths.
     */
    public String[] getResourcePaths();

    /**
     * Returns the server resource that can handle the given resource path.
     * @param resourcePath  the requested resource path.
     * @return
     */
    public Class<? extends ServerResource> getServerResource(String resourcePath);
}
