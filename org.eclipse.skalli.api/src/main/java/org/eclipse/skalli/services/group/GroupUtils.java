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
package org.eclipse.skalli.services.group;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.services.Services;

public class GroupUtils {

    // no instances, please!
    private GroupUtils() {
    }

    /**
     * Returns <code>true</code> if the given user belongs
     * to the group of administrators (see {@link GroupService#isAdministrator(String)}).
     * <p>
     * Certain administrators can be "banned" temporaily by defining the
     * system property <tt>"-DbannedAdmins=&lt;comma-separated list of user ids&gt;"</tt>.
     *
     * @param userId  the unique identifier of a user.
     *
     * @return <code>true</code>if the given user is in the group of administrators,
     * <code>false</code> otherwise. If the argument is <code>null</code>,
     * <code>false</code> is returned.
     */
    public static boolean isAdministrator(String userId) {
        if (userId == null) {
            return false;
        }
        String bannedAdmins = System.getProperty("bannedAdmins"); //$NON-NLS-1$
        if (StringUtils.isNotBlank(bannedAdmins)) {
            String[] bannedAdminsList = StringUtils.split(bannedAdmins, ',');
            for (String bannedAdmin : bannedAdminsList) {
                if (bannedAdmin.equals(userId)) {
                    return false;
                }
            }
        }
        GroupService groupService = Services.getRequiredService(GroupService.class);
        return groupService.isAdministrator(userId);
    }

    /**
     * Returns <code>true</code> if the given user belongs
     * to the group of administrators (see {@link GroupService#isAdministrator(String)}).
     *
     * This method is a shortcut for {@link #isAdministrator(String)
     * isAdministrator(user.getUserId())}.
     *
     * @param user  the user to check.
     *
     * @return <code>true</code>if the given user is in the group of administrators,
     * <code>false</code> otherwise. If the argument is <code>null</code> or the given
     * user has no unique identfier, <code>false</code> is returned.
     */
    public static boolean isAdministrator(User user) {
        if (user == null) {
            return false;
        }
        return isAdministrator(user.getUserId());
    }
}
