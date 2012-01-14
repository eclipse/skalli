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
package org.eclipse.skalli.api.rest.monitor;

import java.util.Set;

import org.restlet.resource.ServerResource;

/**
 * Interface describing services that can be monitored via REST API.
 * Monitorable services must insert a corresponding <tt>provide</tt> clause
 * <pre>
 *    &lt;service&gt;
        <provide interface="org.eclipse.skalli.api.java.Monitorable"/>
        <provide interface="..."/>
      &lt;/service&gt;
 * </pre>
 * in their service component descriptors to be found by the REST API.
 */
public interface Monitorable {

    public static final String NAMESPACE = "http://www.eclipse.org/skalli/2010/API/services"; //$NON-NLS-1$

    /**
     * The service component name of the service. This name is used as the second-last
     * part of the REST URL <code>/api/services/&lt;service-component-name&gt;/&lt;/monitors/resource-name&gt;</code>.
     */
    public String getServiceComponentName();

    /**
     * Resource names for which this service provides REST information.
     * The resource name is the right-most part of the REST URL, i.e.
     * <code>/api/services/&lt;short-name&gt;/&lt;resource-name&gt;</code>
     */
    public Set<String> getResourceNames();

    public Class<? extends ServerResource> getServerResource(String resourceName);
}
