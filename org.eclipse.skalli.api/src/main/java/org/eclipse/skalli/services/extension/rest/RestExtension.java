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
 * Interface representing extensions to the REST API. Implementations of
 * this class must be registered as OSGi services for Skalli to be able to
 * find them.
 */
public interface RestExtension {

    /**
     * Returns the resource paths this REST extension serves.
     *
     * Note, the resource paths are treated as <a href="http://tools.ietf.org/html/rfc6570">URI templates</a>
     * in the sense of RFC 6570, i.e. may contain placeholders representing request parameters.
     * <p>
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
     * Returns the resource that is able to handle the given resource path.
     * @param resourcePath  the requested resource path.
     * @return a resource, or <code>null</code> in which case nothing is registered
     * for the given resource path.
     */
    public Class<? extends ServerResource> getServerResource(String resourcePath);
}
