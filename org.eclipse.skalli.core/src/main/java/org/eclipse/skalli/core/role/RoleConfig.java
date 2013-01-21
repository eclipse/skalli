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
package org.eclipse.skalli.core.role;

import java.util.Collection;
import java.util.TreeSet;

import org.eclipse.skalli.commons.CollectionUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("role")
public class RoleConfig {

    private String roleId;

    @XStreamImplicit(itemFieldName = "user")
    private TreeSet<String> users = new TreeSet<String>();

    @XStreamImplicit(itemFieldName = "group")
    private TreeSet<String> groups = new TreeSet<String>();

    public RoleConfig() {
    }

    public RoleConfig(String roleId, Collection<String> users, Collection<String> groups) {
        this.roleId = roleId;
        if (CollectionUtils.isNotBlank(users)) {
            getUsers().addAll(users);
        }
        if (CollectionUtils.isNotBlank(groups)) {
            getGroups().addAll(groups);
        }
    }

    public String getRoleId() {
        return roleId;
    }

    public synchronized TreeSet<String> getUsers() {
        if (users == null) {
            users = new TreeSet<String>();
        }
        return users;
    }

    public synchronized TreeSet<String> getGroups() {
        if (groups == null) {
            groups = new TreeSet<String>();
        }
        return groups;
    }
}
