/*******************************************************************************
 * Copyright (c) 2010-2014 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.view.internal.config;

import org.eclipse.skalli.services.configuration.ConfigResourceBase;

public class TopLinksResource extends ConfigResourceBase<TopLinksConfig> {

    @Override
    protected Class<TopLinksConfig> getConfigClass() {
        return TopLinksConfig.class;
    }

}
