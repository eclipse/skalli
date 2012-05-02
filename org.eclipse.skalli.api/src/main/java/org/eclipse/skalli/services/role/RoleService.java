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
package org.eclipse.skalli.services.role;

import java.util.List;

/**
 * Service that provides roles of individual users and user groups.
 */
public interface RoleService {

    /**
     * Returns the roles of the given individual user. Note, project related
     * roles for individual users are provided by {@link RoleProvider role providers}.
     *
     * @param userId  the unique identifier of the user for whom the roles are retrieved.
     *
     * @return a list of roles associated with the given user, or an empty list.
     */
    public List<String> getRoles(String userId);

    /**
     * Returns the roles of the given group of users.
     *
     * @param groups  a list of groups (each group specified by its group name) for which
     * to retrieve roles. If more than one group is provided, roles are added to the result
     * by evaluating the groups in the given order.
     *
     * @return a list of roles associated with the given groups, or an empty list.
     */
    public List<String> getRoles(String... groups);
}

