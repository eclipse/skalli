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
package org.eclipse.skalli.core.user;

import org.eclipse.skalli.services.configuration.ConfigResourceBase;
import org.eclipse.skalli.services.user.UserStoreConfig;

public class UserStoreResource extends ConfigResourceBase<UserStoreConfig> {

    @Override
    protected Class<UserStoreConfig> getConfigClass() {
        return UserStoreConfig.class;
    }
}