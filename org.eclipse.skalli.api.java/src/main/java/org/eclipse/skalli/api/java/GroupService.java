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
package org.eclipse.skalli.api.java;

import java.util.List;

import org.eclipse.skalli.common.Group;

/**
 * Service that allows to retrieve user groups and check whether a certain
 * user is member of a certain group.
 */
public interface GroupService extends EntityService<Group> {

    /**
     * Returns all currently available groups.
     */
    public List<Group> getGroups();

    /**
     * Returns a group for a unique group id.
     *
     * @param groupId  unique identifier of the group, for example
     * {@link #ADMIN_GROUP}.
     */
    public Group getGroup(String groupId);

    /**
     * Checks whether the given user is member of the portal administrator group.
     *
     * @param userId  unique identifier of the user to check.
     */
    public boolean isAdministrator(String userId);

    /**
     * Checks whether a is member of the given group.
     *
     * @param userId  unique identifier of the user to check.
     * @param groupId  unique identifier of the group, for example
     * {@link #ADMIN_GROUP}.
     */
    public boolean isMemberOfGroup(String userId, String groupId);
}
