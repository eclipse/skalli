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
package org.eclipse.skalli.services.project;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.Member;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.services.Services;

public class ProjectUtils {

    // no instances, please!
    private ProjectUtils() {
    }

    /**
     * Returns <code>true</code> if the currently authenticated user belongs
     * to the group of administrators of the given project.
     * <p>
     * Note: The current implementation simply treats all project members
     * as project administrators (see @link {@link ProjectService#getAllPeople(Project)}).
     *
     * @param userId  the unique identifier of a user.
     * @param project  a project.
     *
     * @return <code>true</code>if the given user is a project administrator,
     * <code>false</code> otherwise. If any of the arguments is <code>null</code>,
     * or the <code>userId</code> is blank, <code>false</code> is returned.
     */
    // TODO authorization with configurable project admin group
    public static boolean isProjectAdmin(String userId, Project project) {
        if (StringUtils.isBlank(userId) || project == null) {
            return false;
        }
        for (Member member : Services.getRequiredService(ProjectService.class).getMembers(project)) {
            if (StringUtils.equalsIgnoreCase(member.getUserID(), userId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns <code>true</code> if the currently authenticated user belongs
     * to the group of administrators of the given project.
     * <p>
     * This method is a shortcut for {@link #isProjectAdmin(String, Project)
     * isProjectAdmin(user.getUserId(), project)}.
     *
     * @param user  the user to check.
     * @param project  a project.
     *
     * @return <code>true</code>if the given user is a project administrator,
     * <code>false</code> otherwise. If any of the arguments is <code>null</code>
     * or the given user has no unique identfier, <code>false</code> is returned.
     */
    public static boolean isProjectAdmin(User user, Project project) {
        if (user == null || project == null) {
            return false;
        }
        return isProjectAdmin(user.getUserId(), project);
    }

    /**
     * Returns <code>true</code> if the currently authenticated user is a
     * project administrator of any of the parent projects of the given project.
     * <p>
     * Note: The current implementation simply treats all project members
     * as project administrators (see @link {@link ProjectService#getAllPeople(Project)}).
     *
     * @param userId  the unique identifier of a user.
     * @param project  a project.
     *
     * @return <code>true</code>if the given user is a project administrator
     * of any of the parent projects of the given project,
     * <code>false</code> otherwise. If any of the arguments is <code>null</code>,
     * or the <code>userId</code> is blank, <code>false</code> is returned.
     */
    public static boolean isProjectAdminInParentChain(String userId, Project project) {
        if (StringUtils.isBlank(userId) && project == null) {
            return false;
        }
        for (Project parent : Services.getRequiredService(ProjectService.class).getParentChain(project.getUuid())) {
            if (isProjectAdmin(userId, parent)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns <code>true</code> if the currently authenticated user is a
     * project administrator of any of the parent projects of the given project.
     * <p>
     * This method is a shortcut for {@link #isProjectAdminInParentChain(String, Project)
     * isProjectAdminInParentChain(user.getUserId(), project)}.
     *
     * @param user  the user to check.
     * @param project  a project.
     *
     * @return <code>true</code>if the given user is a project administrator
     * of any of the parent projects of the given project,
     * <code>false</code> otherwise. If any of the arguments is <code>null</code>,
     *  or the given user has no unique identfier <code>false</code> is returned.
     */
    public static boolean isProjectAdminInParentChain(User user, Project project) {
        if (user == null) {
            return false;
        }
        return isProjectAdminInParentChain(user.getUserId(), project);
    }
}
