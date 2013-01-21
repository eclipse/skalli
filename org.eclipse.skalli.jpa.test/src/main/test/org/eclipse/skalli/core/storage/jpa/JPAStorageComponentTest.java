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
package org.eclipse.skalli.core.storage.jpa;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.eclipse.skalli.services.persistence.StorageService;
import org.eclipse.skalli.testutil.BundleManager;
import org.eclipse.skalli.testutil.StorageServiceTestBase;
import org.junit.Test;

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

    @Test
    public void testArchive() throws Exception {
        final String TEST_CATEGORY = "test_archive";

        StorageService service = getStorageService();
        if (!(service instanceof JPAStorageComponent)) {
            return;
        }

        JPAStorageComponent pdb = (JPAStorageComponent)service;

        // initially empty
        List<HistoryStorageItem> items = pdb.getHistory(TEST_CATEGORY, TEST_ID);
        assertTrue(items.isEmpty());

        // archive non existing element, should do nothing
        pdb.archive(TEST_CATEGORY, TEST_ID);
        items = pdb.getHistory(TEST_CATEGORY, TEST_ID);
        assertTrue(items.isEmpty());

        // create item
        ByteArrayInputStream is = new ByteArrayInputStream(TEST_CONTENT.getBytes());
        pdb.write(TEST_CATEGORY, TEST_ID, is);

        // first archive step
        pdb.archive(TEST_CATEGORY, TEST_ID);
        items = pdb.getHistory(TEST_CATEGORY, TEST_ID);
        assertTrue(items.size() == 1);
        assertTrue(items.get(0).getDateCreated() != null);
        assertEquals(TEST_ID, items.get(0).getId());

        // second archive step
        pdb.archive(TEST_CATEGORY, TEST_ID);
        items = pdb.getHistory(TEST_CATEGORY, TEST_ID);
        assertTrue(items.size() == 2);
        assertTrue(items.get(0).getDateCreated() != null);
        assertEquals(TEST_ID, items.get(0).getId());
        assertTrue(items.get(1).getDateCreated() != null);
        assertEquals(TEST_ID, items.get(1).getId());
    }

}