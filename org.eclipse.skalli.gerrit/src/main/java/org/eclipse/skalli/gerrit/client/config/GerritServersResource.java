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
package org.eclipse.skalli.gerrit.client.config;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.skalli.services.configuration.ConfigResourceBase;

public class GerritServersResource extends ConfigResourceBase<GerritServersConfig> {

    @Override
    protected Class<GerritServersConfig> getConfigClass() {
        return GerritServersConfig.class;
    }

    @Override
    protected List<Class<?>> getAdditionalConfigClasses() {
        List<Class<?>> ret = new ArrayList<Class<?>>();
        ret.add(GerritServerConfig.class);
        return ret;
    }

}
