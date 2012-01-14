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
import org.osgi.service.component.ComponentContext;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LDAPConfigSection implements ConfigSection {
    private static final Logger LOG = LoggerFactory.getLogger(LDAPConfigSection.class);

    protected void activate(ComponentContext context) {
        LOG.info("LDAPConfig activated");
    }

    protected void deactivate(ComponentContext context) {
    }

    @Override
    public String getName() {
        return "ldap";
    }

    @Override
    public Class<? extends ServerResource> getServerResource() {
        return LDAPResource.class;
    }

}
