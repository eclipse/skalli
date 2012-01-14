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
package org.eclipse.skalli.model;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This class represents a generic user group.
 */
public class Group extends EntityBase {

    @PropertyName(position = 0)
    public static final String PROPERTY_GROUP_ID = "groupId"; //$NON-NLS-1$

    @PropertyName(position = 1)
    public static final String PROPERTY_GROUP_MEMBERS = "groupMembers"; //$NON-NLS-1$

    private String groupId;
    private TreeSet<String> groupMembers = new TreeSet<String>();

    public Group() {
    }

    public Group(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public synchronized SortedSet<String> getGroupMembers() {
        if (groupMembers == null) {
            groupMembers = new TreeSet<String>();
        }
        return groupMembers;
    }

    public void addGroupMember(String userId) {
        if (userId != null) {
            getGroupMembers().add(userId);
        }
    }

    public void removeGroupMember(String userId) {
        if (userId != null) {
            getGroupMembers().remove(userId);
        }
    }

    public boolean hasGroupMember(String userId) {
        for (String g : getGroupMembers()) {
            if (g.equals(userId)) {
                return true;
            }
        }
        return false;
    }
}
