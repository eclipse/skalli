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
package org.eclipse.skalli.services.configuration;

import org.restlet.resource.ServerResource;

/**
 * Interface of a service that defines a configuration extension.
 * Implementations of this interface must be registered as OSGI service component.
 * <br>
 * For example:
 * <pre>
 * &lt;component name="org.eclipse.skalli.core.config.groups"&gt;
 *   &lt;implementation class="org.eclipse.skalli.core.group.GroupsConfigSection"/&gt;
 *   &lt;service&gt;
 *       &lt;provide interface="org.eclipse.skalli.services.configuration.ConfigSection"/&gt;
 *   &lt;/service&gt;
 * &lt;/component&gt;
 * </pre>
 *
 * @param <T> the type of configuration associated with this configuration extension.
 */
public interface ConfigSection<T> {

    /**
     * Returns the storage key for this configuration extension.
     */
    public String getStorageKey();

    /**
     * Returns the configuration class associated with this configuration extension.
     */
    public Class<T> getConfigClass();

    /**
     * Returns the resource paths this configuration section wants to register.
     *
     * Note, resource paths are treated as <a href="http://tools.ietf.org/html/rfc6570">URI template</a>
     * in the sense of RFC 6570, i.e. may contain placeholders representing request parameters.
     * <br>
     * For example:
     * <pre>
     * /groups
     * /groups/{groupName}
     * </pre>
     *
     * @return an array of resource paths this configuration extension wants to register.
     * Note, resource paths should always start with a slash "/".
     */
    public String[] getResourcePaths();

    /**
     * Returns the server resource that can handle the given resource path.
     *
     * @param resourcePath  the requested resource path. Note, this method should return a suitable
     * server resource for all resource paths provided by {@link #getResourcePaths()}.
     */
    public Class<? extends ServerResource> getServerResource(String resourcePath);

}
