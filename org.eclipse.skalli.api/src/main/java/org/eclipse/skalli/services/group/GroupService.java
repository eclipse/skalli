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
package org.eclipse.skalli.services.group;

import java.util.List;

import org.eclipse.skalli.model.Group;
import org.eclipse.skalli.services.entity.EntityService;

/**
 * Service that allows to retrieve user groups and check whether a certain
 * user is member of a certain group.
 */
public interface GroupService extends EntityService<Group> {

    /** Unique identifier of the admiminstrators group */
    public static final String ADMIN_GROUP = "administrators"; //$NON-NLS-1$

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
     * Checks whether a user is member of the given group.
     *
     * @param userId  unique identifier of the user to check.
     * @param groupId  unique identifier of the group, for example
     * {@link #ADMIN_GROUP}.
     */
    public boolean isMemberOfGroup(String userId, String groupId);


    /**
     * Returs the groups the given user is member of.
     *
     * @param userId  unique identifier of the user to check.
     * @return the groups of the user, or an empty list.
     */
    public List<Group> getGroups(String userId);
}
