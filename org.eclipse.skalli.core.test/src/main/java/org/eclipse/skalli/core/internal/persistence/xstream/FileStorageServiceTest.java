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
package org.eclipse.skalli.core.internal.persistence.xstream;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.eclipse.skalli.services.persistence.StorageService;
import org.eclipse.skalli.testutil.AbstractStorageServiceTest;
import org.eclipse.skalli.testutil.TestUtils;

@SuppressWarnings("nls")
public class FileStorageServiceTest extends AbstractStorageServiceTest {

    private File storageBase;

    @Override
    public void setUp() throws Exception {
        storageBase = TestUtils.createTempDir("FileStorageServiceTest.Storage");
        super.setUp();

    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        if (storageBase != null) {
            FileUtils.forceDelete(storageBase);
        }
    }

    @Override
    protected StorageService createNewStorageServiceForTest() {
        return new FileStorageService(storageBase);

    }

}
