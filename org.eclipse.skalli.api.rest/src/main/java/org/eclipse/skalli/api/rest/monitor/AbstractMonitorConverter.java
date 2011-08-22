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

import java.util.logging.Logger;

import org.eclipse.skalli.common.Services;
import org.eclipse.skalli.log.Log;
import org.eclipse.skalli.model.ext.AbstractConverter;

public abstract class AbstractMonitorConverter extends AbstractConverter<Object> {

    private static final Logger LOG = Log.getLogger(AbstractMonitorConverter.class);

    protected String serviceComponentName;

    public AbstractMonitorConverter(String serviceComponentName, String resourceName, String host) {
        super(Object.class, resourceName, host);
        this.serviceComponentName = serviceComponentName;
    }

    @SuppressWarnings("nls")
    @Override
    public String getNamespace() {
        return Monitorable.NAMESPACE + "/" + serviceComponentName + "/monitors/" +getAlias();
    }

    @SuppressWarnings("nls")
    @Override
    public String getXsdFileName() {
        return serviceComponentName + "-" + getAlias() + ".xsd";
    }

    protected <SERVICE_INTERFACE,SERVICE_IMPLEMENTATION> SERVICE_IMPLEMENTATION
    getServiceInstance(Class<SERVICE_INTERFACE> serviceInterface, Class<SERVICE_IMPLEMENTATION> implementingClass) {
        SERVICE_INTERFACE boundInstance = Services.getService(serviceInterface);
        SERVICE_IMPLEMENTATION boundInstanceCasted = null;
        try {
            boundInstanceCasted = implementingClass.cast(boundInstance);
        } catch (ClassCastException e) {
            LOG.warning("Expected to find " +  implementingClass + " bound to service "
                    + serviceInterface + " but got" + boundInstance.getClass());
        }
        return boundInstanceCasted;
    }

}
