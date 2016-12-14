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
package org.eclipse.skalli.core.storage.jpa;

import static org.junit.Assert.fail;

import org.eclipse.skalli.services.persistence.StorageService;
import org.eclipse.skalli.testutil.BundleManager;
import org.eclipse.skalli.testutil.StorageServiceTestBase;

@SuppressWarnings("nls")
public class JPAStorageComponentTest extends StorageServiceTestBase {

    @Override
    protected StorageService getStorageService() throws Exception {
        StorageService jpaStorageService = BundleManager.waitService(StorageService.class,
                JPAStorageComponent.class, 1000);
        if (jpaStorageService == null) {
            fail(JPAStorageComponent.class.getName() + " is not active");
        }
        return jpaStorageService;
    }
}