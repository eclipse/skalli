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

import org.eclipse.skalli.services.configuration.rest.ConfigSection;
import org.eclipse.skalli.services.configuration.rest.ConfigSectionBase;
import org.restlet.resource.ServerResource;


public class LDAPConfigSection extends ConfigSectionBase implements ConfigSection {

    @Override
    public String getName() {
        return "ldap";
    }

    @Override
    public Class<? extends ServerResource> getServerResource(String resourePath) {
        return LDAPResource.class;
    }

}
