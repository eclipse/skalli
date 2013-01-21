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
package org.eclipse.skalli.core.validation;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.skalli.services.configuration.rest.CustomizingResource;

public class ValidationsResource extends CustomizingResource<ValidationsConfig> {

    public static final String MAPPINGS_KEY = "validation"; //$NON-NLS-1$

    @Override
    protected String getKey() {
        return MAPPINGS_KEY;
    }

    @Override
    protected Class<ValidationsConfig> getConfigClass() {
        return ValidationsConfig.class;
    }

    @Override
    protected List<Class<?>> getAdditionalConfigClasses() {
        List<Class<?>> ret = new LinkedList<Class<?>>();
        ret.add(ValidationConfig.class);
        return ret;
    }

}
