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
package org.eclipse.skalli.view.ext.impl.internal.rest;

import org.eclipse.skalli.services.extension.rest.RestExtension;
import org.restlet.resource.ServerResource;

public class InfoBoxesRestExtension implements RestExtension {

    private static final String INFOBOXES_PATH = "/infoboxes"; //$NON-NLS-1$

    @Override
    public String[] getResourcePaths() {
        return new String[] { INFOBOXES_PATH };
    }

    @Override
    public Class<? extends ServerResource> getServerResource(String resourcePath) {
        if (INFOBOXES_PATH.equals(resourcePath)) {
            return InfoBoxesResource.class;
        }
        return null;
    }

}
