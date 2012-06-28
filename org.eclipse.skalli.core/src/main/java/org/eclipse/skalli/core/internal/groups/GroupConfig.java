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
package org.eclipse.skalli.core.internal.groups;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.model.Group;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Configuration of a single user group.
 */
@XStreamAlias("group")
public class GroupConfig {

    private String groupId;

    @XStreamImplicit(itemFieldName = "member")
    private TreeSet<String> groupMembers = new TreeSet<String>();

    public GroupConfig() {
    }

    public GroupConfig(String groupId, Collection<String> groupMembers) {
        this.groupId = groupId;
        if (CollectionUtils.isNotBlank(groupMembers)) {
            this.groupMembers.addAll(groupMembers);
        }
    }

    public GroupConfig(org.eclipse.skalli.model.Group group) {
        this(group.getGroupId(), group.getGroupMembers());
    }

    public String getGroupId() {
        return groupId;
    }

    public synchronized SortedSet<String> getGroupMembers() {
        if (groupMembers == null) {
            groupMembers = new TreeSet<String>();
        }
        return groupMembers;
    }

    public boolean hasGroupMember(String userId) {
        for (String g : getGroupMembers()) {
            if (g.equals(userId)) {
                return true;
            }
        }
        return false;
    }

    public Group getAsModelGroup() {
        return new Group(this.getGroupId(), this.getGroupMembers());
    }
}
