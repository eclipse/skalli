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
package org.eclipse.skalli.services.role;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.eclipse.skalli.model.Member;
import org.eclipse.skalli.model.Project;

/**
 * Interface for a service that is able to extract project {@link Member members}
 * with given role from a project. Each implementation of this interface recognizes
 * a certain set of roles and must be registered as OSGi service.
 */
public interface RoleProvider {

    /**
     * Returns the set of roles this provider is able to recognize.
     *
     * @return a non-empty set of role names.
     */
    public Set<String> getSupportedRoles();

    /**
     * Returns the members of the project that are assigned to roles
     * recognized by this provider.
     *
     * @param project  the project to evaluate.
     * @return a map of members with role names as keys, or an empty map.
     */
    public Map<String, SortedSet<Member>> getMembersByRole(Project project);

    /**
     * Returns the members of the project that are assigned to roles
     * recognized by this provider.
     *
     * @param project  the project to evaluate.
     * @return a set of members, or an empty set.
     */
    public SortedSet<Member> getMembers(Project project);

    /**
     * Returns the members of the project that are assigned to given roles
     * recognized by this provider.
     *
     * @param project  the project to evaluate.
     * @param roles  the role names to search for.
     *
     * @return a set of members matching the given roles, or an empty set.
     * If <code>roles</code> is <code>null</code> or an empty array, an empty set
     * is returned.
     */
    public SortedSet<Member> getMembers(Project project, String... roles);

    /**
     * Adds the given member to a project and assigns a given role to this member.
     * If the role is not supported by this provider, nothing is added
     * to the project.
     *
     * @param project  the project to which to add a member.
     * @param member  the member to add.
     * @param role  the role to assign to the new member.
     * @return  <code>true</code>, if the member was added to the project,
     * <code>false</code> otherwise.
     */
    public boolean addMember(Project project, Member member, String role);
}
