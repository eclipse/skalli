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
package org.eclipse.skalli.model.core.internal;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.skalli.model.Member;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.extension.IndexerBase;
import org.eclipse.skalli.services.role.RoleService;
import org.eclipse.skalli.services.user.UserService;
import org.eclipse.skalli.services.user.UserServices;

public class ProjectIndexer extends IndexerBase<Project> {

    private static final String MEMBERS_DISPLAY_NAME = "membersDisplayName"; //$NON-NLS-1$
    private static final String ALL_MEMBERS = "allMembers"; //$NON-NLS-1$

    @Override
    protected void indexFields(Project project) {
        UserService userService = UserServices.getUserService();
        TreeSet<Member> members = new TreeSet<Member>();
        for (RoleService roleService : Services.getServices(RoleService.class)) {
            members.addAll(roleService.getMembers(project));
        }

        addField(Project.PROPERTY_UUID, project.getUuid().toString(), true, true);
        addField(Project.PROPERTY_PROJECTID, project.getProjectId(), true, true);
        addField(Project.PROPERTY_NAME, project.getName(), true, true);
        addField(Project.PROPERTY_DESCRIPTION, project.getDescription(), true, true);
        addField(ALL_MEMBERS, members, true, true);
        if (project.getParentProject() != null) {
            addField(Project.PROPERTY_PARENT_PROJECT, project.getParentProject().toString(), true, true);
        }
        addField(Project.PROPERTY_TEMPLATEID, project.getProjectTemplateId(), true, true);

        for (Member member : members) {
            User user = userService.getUserById(member.getUserID());
            if (user != null) {
                addField(MEMBERS_DISPLAY_NAME, user.getDisplayName(), false, true);
            }
        }
    }

    @Override
    public Set<String> getDefaultSearchFields() {
        Set<String> ret = new HashSet<String>();
        ret.add(Project.PROPERTY_PROJECTID);
        ret.add(Project.PROPERTY_NAME);
        ret.add(Project.PROPERTY_DESCRIPTION);
        ret.add(ALL_MEMBERS);
        ret.add(MEMBERS_DISPLAY_NAME);
        return ret;
    }

}