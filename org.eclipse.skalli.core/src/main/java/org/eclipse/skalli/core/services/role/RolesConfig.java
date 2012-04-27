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
package org.eclipse.skalli.core.services.role;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("roles")
public class RolesConfig {

    @XStreamImplicit
    private ArrayList<RoleConfig> roles = new ArrayList<RoleConfig>();

    public RolesConfig() {
    }

    RolesConfig(List<RoleConfig> roles) {
        getRoles().addAll(roles);
    }

    public synchronized List<RoleConfig> getRoles() {
        if (roles == null) {
            roles = new ArrayList<RoleConfig>();
        }
        return roles;
    }
}
