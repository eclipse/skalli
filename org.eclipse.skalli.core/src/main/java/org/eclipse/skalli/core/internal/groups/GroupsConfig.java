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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.skalli.model.Group;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("groups")
public class GroupsConfig {

    public GroupsConfig() {
    }

    GroupsConfig(List<SkalliCoreGroup> groups) {
        getGroups().addAll(groups);
    }

    @XStreamImplicit
    private ArrayList<SkalliCoreGroup> groups = new ArrayList<SkalliCoreGroup>();

    public List<SkalliCoreGroup> getGroups() {
        if (groups == null) {
            groups = new ArrayList<SkalliCoreGroup>();
        }
        return groups;
    }

    public List<Group> getModelGroups() {
        ArrayList<Group> modelGroups = new ArrayList<Group>();
        for (SkalliCoreGroup skalliCoreGroup : getGroups()) {
            modelGroups.add(skalliCoreGroup.getAsModelGroup());
        }
        return modelGroups;
    }

}
