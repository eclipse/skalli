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
package org.eclipse.skalli.core.group;

import org.eclipse.skalli.services.configuration.ConfigSection;
import org.restlet.resource.ServerResource;

/**
 * Configuration API for user groups located at <tt>/api/config/groups</tt>
 * and <tt>/api/config/groups/&lt;groupName&gt;</tt>.
 */
public class GroupsConfigSection implements ConfigSection<GroupsConfig> {

    private static final String STORAGE_KEY =  "groups"; //$NON-NLS-1$

    private static final String GROUPS_PATH = "/groups"; //$NON-NLS-1$
    private static final String GROUPS_GROUP_NAME_PATH = "/groups/{groupName}"; //$NON-NLS-1$
    private static final String[] RESOURCE_PATHS = new String[] { GROUPS_PATH, GROUPS_GROUP_NAME_PATH };

    @Override
    public String getStorageKey() {
        return STORAGE_KEY;
    }

    @Override
    public Class<GroupsConfig> getConfigClass() {
        return GroupsConfig.class;
    }

    @Override
    public String[] getResourcePaths() {
        return RESOURCE_PATHS;
    }

    @Override
    public Class<? extends ServerResource> getServerResource(String resourePath) {
        if (GROUPS_PATH.equals(resourePath)) {
            return GroupsResource.class;
        }
        if (GROUPS_GROUP_NAME_PATH.equals(resourePath)) {
            return GroupResource.class;
        }
        return null;
    }

}
