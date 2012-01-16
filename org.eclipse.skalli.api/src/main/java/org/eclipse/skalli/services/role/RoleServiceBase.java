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
package org.eclipse.skalli.services.role;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.skalli.model.Member;
import org.eclipse.skalli.model.Project;

public abstract class RoleServiceBase implements RoleService {

    @Override
    public SortedSet<Member> getMembers(Project project, String... roles) {
        TreeSet<Member> result = new TreeSet<Member>();
        if (roles == null || roles.length == 0) {
            return result;
        }
        Map<String, SortedSet<Member>> members = getMembersByRole(project);
        if (members.isEmpty()) {
            return result;
        }
        for (String role: roles) {
            Set<Member> membersWithRole = members.get(role);
            if (membersWithRole != null) {
                result.addAll(membersWithRole);
            }
        }
        return result;
    }

}
