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
package org.eclipse.skalli.core.services.permit;

import java.util.Collections;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("permits")
public class PermitsConfig {

    private List<PermitConfig> globalPermits;

    private List<PermitConfig> templatePermits;

    private List<PermitConfig> rolePermits;

    private List<PermitConfig> groupPermits;

    private List<PermitConfig> userPermits;


    public List<PermitConfig> getGlobalPermits() {
        if (globalPermits == null) {
            return Collections.emptyList();
        }
        return globalPermits;
    }

    public List<PermitConfig> getTemplatePermits() {
        if (templatePermits == null) {
            return Collections.emptyList();
        }
        return templatePermits;
    }

    public List<PermitConfig> getRolePermits() {
        if (rolePermits == null) {
            return Collections.emptyList();
        }
        return rolePermits;
    }

    public List<PermitConfig> getGroupPermits() {
        if (groupPermits == null) {
            return Collections.emptyList();
        }
        return groupPermits;
    }

    public List<PermitConfig> getUserPermits() {
        if (userPermits == null) {
            return Collections.emptyList();
        }
        return userPermits;
    }

}
