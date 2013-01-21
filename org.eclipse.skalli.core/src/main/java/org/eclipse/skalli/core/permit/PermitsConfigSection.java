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
package org.eclipse.skalli.core.permit;

import org.eclipse.skalli.services.configuration.rest.ConfigSection;
import org.eclipse.skalli.services.configuration.rest.ConfigSectionBase;
import org.restlet.resource.ServerResource;

public class PermitsConfigSection extends ConfigSectionBase implements ConfigSection {

    private static final String NAME = "permissions"; //$NON-NLS-1$

    private static final String PERMISSIONS_PATH = "/permissions"; //$NON-NLS-1$
    private static final String PERMITS_PATH = "/permits"; //$NON-NLS-1$
    private static final String PERMIT_PATH = "/permits/{id}"; //$NON-NLS-1$

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String[] getResourcePaths() {
        return new String[] {
                PERMISSIONS_PATH, PERMITS_PATH, PERMIT_PATH
        };
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
