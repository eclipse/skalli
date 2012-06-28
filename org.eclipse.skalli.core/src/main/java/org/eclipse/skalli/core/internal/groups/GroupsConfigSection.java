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
package org.eclipse.skalli.core.internal.groups;

import org.eclipse.skalli.services.configuration.rest.ConfigSection;
import org.restlet.resource.ServerResource;

/**
 * Configuration API for user groups located at <tt>/api/config/groups</tt>.
 */
public class GroupsConfigSection implements ConfigSection {

    private static final String NAME = "groups"; //$NON-NLS-1$

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Class<? extends ServerResource> getServerResource() {
        return GroupsResource.class;
    }

}
