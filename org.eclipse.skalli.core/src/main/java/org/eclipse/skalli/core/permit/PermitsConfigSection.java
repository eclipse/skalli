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
package org.eclipse.skalli.core.permit;

import org.eclipse.skalli.services.configuration.ConfigSection;
import org.restlet.resource.ServerResource;

public class PermitsConfigSection implements ConfigSection<PermitsConfig> {

    private static final String STORAGE_KEY = "permissions"; //$NON-NLS-1$

    private static final String PERMISSIONS_PATH = "/permissions"; //$NON-NLS-1$
    private static final String PERMITS_PATH = "/permits"; //$NON-NLS-1$
    private static final String PERMIT_PATH = "/permits/{id}"; //$NON-NLS-1$
    private static final String[] RESOURCE_PATHS = new String[] { PERMISSIONS_PATH, PERMITS_PATH, PERMIT_PATH };

    @Override
    public String getStorageKey() {
        return STORAGE_KEY;
    }

    @Override
    public Class<PermitsConfig> getConfigClass() {
        return PermitsConfig.class;
    }

    @Override
    public String[] getResourcePaths() {
        return RESOURCE_PATHS;
    }

    @Override
    public Class<? extends ServerResource> getServerResource(String resourePath) {
        if (PERMISSIONS_PATH.equals(resourePath) || PERMITS_PATH.equals(resourePath)) {
            return PermitsResource.class;
        }
        if (PERMIT_PATH.equals(resourePath)) {
            return PermitResource.class;
        }
        return null;
    }
}
