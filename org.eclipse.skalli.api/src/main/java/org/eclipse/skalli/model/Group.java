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
package org.eclipse.skalli.model;

import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.skalli.services.group.GroupService;

/**
 * Class for user groups. To work with groups use the {@link GroupService}.
 */

public class Group {

    private String groupId;

    private SortedSet<String> groupMembers = new TreeSet<String>();

    public Group() {
    }

    public Group(String groupId, SortedSet<String> groupMembers) {
        super();
        this.groupId = groupId;
        this.groupMembers = groupMembers;
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

    public void setGroupMembers(SortedSet<String> groupMembers) {
        this.groupMembers = groupMembers;
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
