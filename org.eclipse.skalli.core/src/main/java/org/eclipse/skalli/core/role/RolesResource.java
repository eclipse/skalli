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
package org.eclipse.skalli.core.role;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Issuer;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.services.configuration.ConfigResourceBase;

public class RolesResource extends ConfigResourceBase<RolesConfig> implements Issuer {

    @Override
    protected Class<RolesConfig> getConfigClass() {
        return RolesConfig.class;
    }

    @Override
    protected List<Class<?>> getAdditionalConfigClasses() {
        List<Class<?>> ret = new LinkedList<Class<?>>();
        ret.add(RoleConfig.class);
        return ret;
    }

    @Override
    public SortedSet<Issue> validate(RolesConfig configObject, String loggedInUser) {
        TreeSet<Issue> issues = new TreeSet<Issue>();
        for (RoleConfig roleConfig: configObject.getRoles()) {
            if (StringUtils.isBlank(roleConfig.getRoleId())) {
                issues.add(new Issue(Severity.FATAL, this.getClass(),
                        "Roles must have non-empty role identifiers"));
                return issues;
            }
        }
        //check that roles identifiers are unique
        List<RoleConfig> roleConfigs = configObject.getRoles();
        if (roleConfigs.size() > 1) {
            for (int i = 0; i < roleConfigs.size(); i++) {
                RoleConfig role = roleConfigs.get(i);
                String roleId = role.getRoleId();
                if (StringUtils.isNotBlank(roleId)) {
                    for (int j = i + 1; j < roleConfigs.size(); j++) {
                        if (roleId.equals(roleConfigs.get(j).getRoleId())) {
                            issues.add(new Issue(Severity.FATAL, this.getClass(),
                                    "Role names must be unique"));
                            return issues;
                        }
                    }
                }
            }
        }
        return issues;
    }
}
