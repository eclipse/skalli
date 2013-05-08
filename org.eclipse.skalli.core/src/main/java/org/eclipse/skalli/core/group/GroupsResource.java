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
package org.eclipse.skalli.core.group;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Issuer;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.services.configuration.ConfigResourceBase;
import org.eclipse.skalli.services.group.GroupService;

public class GroupsResource extends ConfigResourceBase<GroupsConfig> implements Issuer {

    @Override
    protected Class<GroupsConfig> getConfigClass() {
        return GroupsConfig.class;
    }

    @Override
    protected List<Class<?>> getAdditionalConfigClasses() {
        List<Class<?>> ret = new LinkedList<Class<?>>();
        ret.add(GroupConfig.class);
        return ret;
    }

    @Override
    public SortedSet<Issue> validate(GroupsConfig configObject, String loggedInUser) {
        TreeSet<Issue> issues = new TreeSet<Issue>();

        boolean adminGroupExists = false;
        boolean memberEmptyErrorFound = false;

        for (GroupConfig group : configObject.getGroups()) {

            //goupId is mandatory
            if (StringUtils.isBlank(group.getGroupId())) {
                issues.add(new Issue(Severity.FATAL, this.getClass(),
                        "Groups must have a name"));
            }

            if (GroupService.ADMIN_GROUP.equals(group.getGroupId())) {
                //admin group: warn if empty group
                adminGroupExists = true;
                if (group.getGroupMembers().size() == 0) {
                    issues.add(new Issue(Severity.WARNING, this.getClass(),
                            "Administrators group has no members. All users will be administrators!"));
                }
            } else {
                //warn: each group should have at least one members
                if (group.getGroupMembers().size() == 0) {
                    issues.add(new Issue(Severity.WARNING, this.getClass(),
                            MessageFormat.format("Group {0} has no members.", group.getGroupId())));
                }
            }

            //members value is mandatory
            for (String member : group.getGroupMembers()) {
                if (StringUtils.isBlank(member) && !memberEmptyErrorFound) {
                    memberEmptyErrorFound = true;
                    issues.add(new Issue(Severity.FATAL, this.getClass(),
                            "All members must have a non empty value"));
                }
            }
        }

        if (!adminGroupExists) {
            issues.add(new Issue(Severity.WARNING, this.getClass(),
                    "No administrators group configured. All users will be administrators!"));
        }

        //check that groups Ids are unique
        List<GroupConfig> groups = configObject.getGroups();
        if (groups.size() > 1) {
            boolean unique = true;
            for (int i = 0; i < groups.size() && unique; i++) {
                GroupConfig group = groups.get(i);
                String groupId = group.getGroupId();
                if (StringUtils.isNotBlank(groupId)) {

                    for (int j = i + 1; j < groups.size() && unique; j++) {
                        if (groupId.equals(groups.get(j).getGroupId())) {
                            unique = false;
                        }
                    }
                }

            }
            if (!unique) {
                issues.add(new Issue(Severity.FATAL, this.getClass(), "Group names must be unique"));
            }
        }

        return issues;
    }
}
