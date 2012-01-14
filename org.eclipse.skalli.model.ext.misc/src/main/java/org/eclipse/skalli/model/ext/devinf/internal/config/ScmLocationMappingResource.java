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

import java.util.LinkedList;
import java.util.List;

import org.eclipse.skalli.ext.mapping.scm.ScmLocationMappingConfig;
import org.eclipse.skalli.ext.mapping.scm.ScmLocationMappingsConfig;
import org.eclipse.skalli.services.configuration.rest.CustomizingResource;

public class ScmLocationMappingResource extends CustomizingResource<ScmLocationMappingsConfig> {

    public static final String MAPPINGS_KEY = "devInf.scmLocationMappings"; //$NON-NLS-1$

    @Override
    protected String getKey() {
        return MAPPINGS_KEY;
    };

    @Override
    protected Class<ScmLocationMappingsConfig> getConfigClass() {
        return ScmLocationMappingsConfig.class;
    };

    @Override
    protected List<Class<?>> getAdditionalConfigClasses() {
        List<Class<?>> ret = new LinkedList<Class<?>>();
        ret.add(ScmLocationMappingConfig.class);
        return ret;
    }

}
