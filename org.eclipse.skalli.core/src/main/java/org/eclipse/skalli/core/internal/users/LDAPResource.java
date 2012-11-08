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
package org.eclipse.skalli.core.internal.users;

import org.eclipse.skalli.services.configuration.rest.CustomizingResource;

public class LDAPResource extends CustomizingResource<LDAPConfig> {

    public static final String KEY = "ldap"; //$NON-NLS-1$

    @Override
    protected String getKey() {
        return KEY;
    }

    @Override
    protected Class<LDAPConfig> getConfigClass() {
        return LDAPConfig.class;
    }

}
