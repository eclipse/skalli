/*******************************************************************************
 * Copyright (c) 2010-2015 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.core.gerrit;

import org.eclipse.skalli.services.configuration.ConfigSection;
import org.eclipse.skalli.services.gerrit.GerritServersConfig;
import org.restlet.resource.ServerResource;

public class GerritServersConfigSection implements ConfigSection<GerritServersConfig> {

    private static final String STORAGE_KEY = "gerrit"; //$NON-NLS-1$
    private static final String[] RESOURCE_PATHS = new String[] { "/gerrit" }; //$NON-NLS-1$

    @Override
    public String getStorageKey() {
        return STORAGE_KEY;
    }

    @Override
    public Class<GerritServersConfig> getConfigClass() {
        return GerritServersConfig.class;
    }

    @Override
    public String[] getResourcePaths() {
        return RESOURCE_PATHS;
    }
    @Override
    public Class<? extends ServerResource> getServerResource(String resourePath) {
        return GerritServersResource.class;
    }

}
