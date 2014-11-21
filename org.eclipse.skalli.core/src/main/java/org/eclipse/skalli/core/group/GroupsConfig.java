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
package org.eclipse.skalli.core.group;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.skalli.model.Group;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Configuration of a collection of user groups.
 */
@XStreamAlias("groups")
public class GroupsConfig {

    // do not remove: required by xstream
    public GroupsConfig() {
    }

    public GroupsConfig(List<GroupConfig> groups) {
        getGroups().addAll(groups);
    }

    @XStreamImplicit
    private ArrayList<GroupConfig> groups = new ArrayList<GroupConfig>();

    public List<GroupConfig> getGroups() {
        if (groups == null) {
            groups = new ArrayList<GroupConfig>();
        }
        return groups;
    }

    public List<Group> getModelGroups() {
        ArrayList<Group> modelGroups = new ArrayList<Group>();
        for (GroupConfig skalliCoreGroup : getGroups()) {
            modelGroups.add(skalliCoreGroup.getAsModelGroup());
        }
        return modelGroups;
    }

}
