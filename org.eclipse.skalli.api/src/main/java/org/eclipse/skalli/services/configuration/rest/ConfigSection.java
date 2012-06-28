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
package org.eclipse.skalli.services.configuration.rest;

import org.restlet.resource.ServerResource;

public interface ConfigSection {

    /**
     * Returns the name of the configuration section. If the configuration section
     * provides no {@link #getResourcePaths() resource paths}, it is registered with its name, i.e.
     * as <tt>/api/config/&lt;name&gt;</tt>.
     */
    public String getName();

    /**
     * Returns the resource paths this configuration section want to register.
     *
     * Note, the resource path is treated as <a href="http://tools.ietf.org/html/rfc6570">URI template</a>
     * in the sense of RFC 6570, i.e. may contain placeholders representing request parameters.
     *
     * For example:
     * <pre>
     * /groups
     * /groups/{groupName}
     * </pre>
     *
     * @return an array of resource paths, or <code>null</code>. If the configuration section
     * provides no resource paths, it is registered with its {@link #getName() name}, i.e.
     * as <tt>/api/config/&lt;name&gt;</tt>.
     */
    public String[] getResourcePaths();

    /**
     * Returns the server resource that can handle the given configuration section.
     * @param resourcePath  the requested resource path.
     */
    public Class<? extends ServerResource> getServerResource(String resourePath);

}
