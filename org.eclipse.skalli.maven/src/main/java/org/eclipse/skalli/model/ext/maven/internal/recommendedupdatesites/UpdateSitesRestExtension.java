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
package org.eclipse.skalli.model.ext.maven.internal.recommendedupdatesites;

import org.eclipse.skalli.services.extension.rest.RestExtension;
import org.restlet.resource.ServerResource;

public class UpdateSitesRestExtension implements RestExtension {

    private static final String UPDATESITES_USER_ID_ID = "/updatesites/{userId}/{id}";

    @Override
    public String[] getResourcePaths() {
        return new String[] { UPDATESITES_USER_ID_ID }; //$NON-NLS-1$
    }

    @Override
    public Class<? extends ServerResource> getServerResource(String resourcePath) {
        if (UPDATESITES_USER_ID_ID.equals(resourcePath)) {
            return UpdateSitesServerResource.class;
        }
        return null;
    }

}
