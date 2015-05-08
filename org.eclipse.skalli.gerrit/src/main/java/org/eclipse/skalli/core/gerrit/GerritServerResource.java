/*******************************************************************************
 * Copyright (c) 2010-2015 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.core.gerrit;

import org.eclipse.skalli.services.configuration.ConfigResourceBase;
import org.eclipse.skalli.services.gerrit.GerritServerConfig;

public class GerritServerResource extends ConfigResourceBase<GerritServerConfig> {

    @Override
    protected Class<GerritServerConfig> getConfigClass() {
        return GerritServerConfig.class;
    }
}
