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
package org.eclipse.skalli.commands;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.Group;
import org.eclipse.skalli.model.ValidationException;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.group.GroupService;

public class AdminCommand {

    private static final String MESSAGE_USER_LIST_EMPTY = "admin user list is empty.";
    private static final String MESSAGE_USER_ID_INVALID = "admin user id is invalid.";
    private static final String MESSAGE_USER_REMOVED = "admin user removed successfully.";
    private static final String MESSAGE_USER_ADDED = "admin user added successfully.";
    private static final String MESSAGE_USER_IS_BLANK = "user id must not be blank.";

    /** Unique identifier of the administrators group */
    private static final String ADMIN_GROUP = "administrators"; //$NON-NLS-1$

    private AdminCommand() {
    };

    protected static String list() {
        StringBuffer strb = new StringBuffer();
        Group adminGroup = getAdminGroup();
        if (adminGroup != null) {
            strb.append(adminGroup.getGroupMembers());
        } else {
            strb.append("[]"); //$NON-NLS-1$
        }
        return strb.toString();
    }

    protected static String add(String adminId) throws ValidationException {
        if (StringUtils.isNotBlank(adminId)) {
            Group adminGroup = getAdminGroup();
            if (adminGroup == null) {
                adminGroup = new Group(ADMIN_GROUP);
            }
            adminGroup.addGroupMember(adminId);
            persistAdminGroup(adminGroup);
            return MESSAGE_USER_ADDED;
        } else {
            return MESSAGE_USER_IS_BLANK;
        }
    }

    protected static String remove(String adminId) throws ValidationException {
        if (StringUtils.isNotBlank(adminId)) {
            Group adminGroup = getAdminGroup();
            if (adminGroup == null) {
                return MESSAGE_USER_LIST_EMPTY;
            }
            if (adminGroup.hasGroupMember(adminId)) {
                adminGroup.removeGroupMember(adminId);
                persistAdminGroup(adminGroup);
                return MESSAGE_USER_REMOVED;
            } else {
                return MESSAGE_USER_ID_INVALID;
            }
        } else {
            return MESSAGE_USER_IS_BLANK;
        }
    }

    private static void persistAdminGroup(Group adminGroup) throws ValidationException {
        GroupService groupService = Services.getRequiredService(GroupService.class);
        groupService.persist(adminGroup, AdminCommand.class.getName());
    }

    private static Group getAdminGroup() {
        GroupService groupService = Services.getRequiredService(GroupService.class);
        return groupService.getGroup(ADMIN_GROUP);
    }
}
