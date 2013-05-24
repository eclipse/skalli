/*******************************************************************************
 * Copyright (c) 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.core.storage;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.eclipse.skalli.services.persistence.StorageService;
import org.eclipse.skalli.testutil.StorageServiceTestBase;
import org.eclipse.skalli.testutil.TestUtils;
import org.junit.After;
import org.junit.Before;

@SuppressWarnings("nls")
public class FileStorageServiceTest extends StorageServiceTestBase {

    private File storageBase;

    @Before
    public void setUp() throws Exception {
        storageBase = TestUtils.createTempDir("FileStorageServiceTest.Storage");
    }

    @After
    public void tearDown() throws Exception {
        if (storageBase != null) {
            FileUtils.forceDelete(storageBase);
        }
    }

    @Override
    protected StorageService getStorageService() throws Exception {
        return new FileStorageComponent(storageBase);
    }

}
