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
package org.eclipse.skalli.model.ext.devinf.internal.config;

import org.eclipse.skalli.services.configuration.rest.ConfigSection;
import org.eclipse.skalli.services.configuration.rest.ConfigSectionBase;
import org.restlet.resource.ServerResource;

public class ScmLocationMappingConfigSection extends ConfigSectionBase implements ConfigSection {

    private static final String NAME = "devInf/scmMappings"; //$NON-NLS-1$

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Class<? extends ServerResource> getServerResource(String resourePath) {
        return ScmLocationMappingResource.class;
    }

}
