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
package org.eclipse.skalli.core.extension.people;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.model.Member;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.ext.commons.PeopleExtension;
import org.eclipse.skalli.services.role.RoleProviderBase;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CoreRoleProvider extends RoleProviderBase {

    private static final Logger LOG = LoggerFactory.getLogger(CoreRoleProvider.class);

    private static final String ROLE_LEAD = "projectlead"; //$NON-NLS-1$
    private static final String ROLE_MEMBER = "projectmember"; //$NON-NLS-1$

    protected void activate(ComponentContext context) {
        LOG.info(MessageFormat.format("[RoleProvider][core] {0} : activated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void deactivate(ComponentContext context) {
        LOG.info(MessageFormat.format("[RoleProvider][core] {0} : deactivated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    private static final Set<String> SUPPORTED_ROLES = CollectionUtils.asSet(ROLE_LEAD, ROLE_MEMBER);

    @Override
    public Set<String> getSupportedRoles() {
        return SUPPORTED_ROLES;
    }

    @Override
    public Map<String, SortedSet<Member>> getMembersByRole(Project project) {
        Map<String, SortedSet<Member>> ret = new HashMap<String, SortedSet<Member>>();
        if (project != null) {
            PeopleExtension ext = project.getExtension(PeopleExtension.class);
            if (ext != null) {
                ret.put(ROLE_LEAD, ext.getLeads());
                ret.put(ROLE_MEMBER, ext.getMembers());
            }
        }
        return ret;
    }

    @Override
    public SortedSet<Member> getMembers(Project project) {
        TreeSet<Member> result = new TreeSet<Member>();
        if (project != null) {
            PeopleExtension ext = project.getExtension(PeopleExtension.class);
            if (ext == null) {
                return result;
            }
            result.addAll(ext.getLeads());
            result.addAll(ext.getMembers());
        }
        return result;
    }

    @Override
    public boolean addMember(Project project, Member person, String role) {
        if (project == null || !SUPPORTED_ROLES.contains(role)) {
            return false;
        }
        if (project.isInherited(PeopleExtension.class)) {
            return false;
        }
        PeopleExtension ext = project.getExtension(PeopleExtension.class);
        if (ext == null) {
            ext = new PeopleExtension();
            project.addExtension(ext);
        }
        if (StringUtils.equalsIgnoreCase(role, ROLE_LEAD)) {
            ext.addLead(person);
        } else {
            ext.addMember(person);
        }
        return true;
    }
}