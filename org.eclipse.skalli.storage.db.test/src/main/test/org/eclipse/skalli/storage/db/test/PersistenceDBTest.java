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
package org.eclipse.skalli.storage.db.test;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Set;

import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.persistence.StorageService;
import org.eclipse.skalli.storage.db.PersistenceDB;
import org.eclipse.skalli.storage.db.entities.HistoryStorageItem;
import org.eclipse.skalli.testutil.AbstractStorageServiceTest;
import org.eclipse.skalli.testutil.BundleManager;
import org.junit.Test;

public class PersistenceDBTest extends AbstractStorageServiceTest {

    @Override
    protected StorageService createNewStorageServiceForTest() throws Exception {
        BundleManager.startBundles();
        Set<StorageService> services = Services.getServices(StorageService.class);
        for (StorageService storageService : services) {
            if (storageService instanceof PersistenceDB) {
                return storageService;
            }
        }
        fail("no storageService for " + PersistenceDB.class.getName() + " found!");
        return null;
    }

    @Test
    public void testArchive() throws Exception {
        final String TEST_CATEGORY = "test_archive";

        PersistenceDB pdb = (PersistenceDB) createNewStorageServiceForTest();

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