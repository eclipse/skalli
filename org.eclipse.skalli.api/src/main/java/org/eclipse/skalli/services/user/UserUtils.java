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
package org.eclipse.skalli.services.user;

import org.eclipse.skalli.model.User;

public class UserUtils {

    // no instances, please!
    private UserUtils() {
    }

    /**
     * Returns the {@link User} matching the given <code>userId<code>-
     *
     * @param userId  the unique identifier of the user.
     * @return a user, or <code>null</code> if no known user matched
     * the given unique identifier.
     *
     * @throws IllegalStateException if no user service is available.
     */
    public static User getUser(String userId) {
        User user = null;
        if (userId != null) {
            UserService userService = UserServices.getUserService();
            user = userService.getUserById(userId.toString());
        }
        return user;
    }
}
