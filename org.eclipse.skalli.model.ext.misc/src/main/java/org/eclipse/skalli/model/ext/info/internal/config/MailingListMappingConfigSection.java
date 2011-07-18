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
package org.eclipse.skalli.model.ext.info.internal.config;

import org.restlet.resource.ServerResource;

import org.eclipse.skalli.api.rest.config.ConfigSection;

public class MailingListMappingConfigSection implements ConfigSection {

    private static final String NAME = "info/mailingList"; //$NON-NLS-1$

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Class<? extends ServerResource> getServerResource() {
        return MailingListMappingResource.class;
    }

}
