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
package org.eclipse.skalli.testutil;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.eclipse.skalli.services.persistence.StorageConsumer;
import org.eclipse.skalli.services.persistence.StorageService;
import org.junit.Test;

@SuppressWarnings("nls")
public abstract class StorageServiceTestBase {

    protected static final String TEST_ID = "test_id";
    protected static final String TEST_NONEXISTING_ID = "some_non_existing_id";
    protected static final String TEST_NONEXISTING_CATEGORY = "some_non_existing_category";
    protected static final String TEST_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
            "<entity-project lastModified=\"2011-05-10T08:16:25.137Z\" modifiedBy=\"d044774\" version=\"16\">" +
            "  <uuid>00c5cdb4-fb0f-4f11-913c-44a7f24531bd</uuid>" +
            "  <deleted>true</deleted>" +
            "  <parentEntityId>ccefcf51-6b8b-440b-ac32-3b8f94c66590</parentEntityId>" +
            "</entity-project>";
    protected static final String TEST_CONTENT_UPDATED = "updated_data";

    /**
     * Creates the {@link StorageService} instance under test.
     */
    protected abstract StorageService getStorageService() throws Exception;

    @Test
    public void testReadWrite() throws Exception {
        final String TEST_CATEGORY = "test_readwrite";
        writeContent(TEST_CATEGORY, TEST_ID, TEST_CONTENT);
        String outputText = readContent(TEST_CATEGORY);
        assertEquals(TEST_CONTENT, outputText);
    }

    @Test
    public void testUpdate() throws Exception {
        final String TEST_CATEGORY = "test_update";

        writeContent(TEST_CATEGORY, TEST_ID, TEST_CONTENT);

        //update the same key with TEST_CONTENT_UPDATED
        ByteArrayInputStream is = new ByteArrayInputStream(TEST_CONTENT_UPDATED.getBytes("UTF-8"));

        StorageService storageService = getStorageService();
        storageService.write(TEST_CATEGORY, TEST_ID, is);

        InputStream stream = storageService.read(TEST_CATEGORY, TEST_ID);
        String outputText = IOUtils.toString(stream, "UTF-8");

        assertEquals(TEST_CONTENT_UPDATED, outputText);
        is.close();
        stream.close();
    }

    @Test
    public void testReadNonExistingId() throws Exception {
        StorageService storageService = getStorageService();
        InputStream stream = storageService.read(TEST_NONEXISTING_CATEGORY, TEST_NONEXISTING_ID);
        assertNull(stream);
    }

    @Test
    public void testKeys() throws Exception {
        final String TEST_CATEGORY = "test_keys";

        StorageService storageService = getStorageService();
        List<String> ids = storageService.keys(TEST_CATEGORY);
        assertTrue(ids.isEmpty());

        writeContent(TEST_CATEGORY, TEST_ID, TEST_CONTENT);

        ids = storageService.keys(TEST_CATEGORY);
        assertTrue(ids.size() == 1);
        assertEquals(TEST_ID, ids.get(0));
    }

    @Test
    public void testWriteBigData() throws Exception {
        final String TEST_CATEGORY = "test_writebigdata";

        char[] chars = new char[100000];
        Arrays.fill(chars, 'a');
        String bigString = String.valueOf(chars);

        writeContent(TEST_CATEGORY, TEST_ID, bigString);

        StorageService storageService = getStorageService();
        InputStream stream = storageService.read(TEST_CATEGORY, TEST_ID);
        String outputText = IOUtils.toString(stream, "UTF-8");
        assertEquals(bigString, outputText);

        stream.close();
    }

    @Test
    public void testArchive() throws Exception {
        final String TEST_CATEGORY = "test_archive";

        StorageService storageService = getStorageService();
        // initially empty
        List<ByteArrayStorageItem> items = getHistory(storageService, TEST_CATEGORY, TEST_ID);
        assertTrue(items.isEmpty());

        // archive non existing element, should do nothing
        storageService.archive(TEST_CATEGORY, TEST_ID);
        items = getHistory(storageService, TEST_CATEGORY, TEST_ID);
        assertTrue(items.isEmpty());

        // create item
        ByteArrayInputStream is = new ByteArrayInputStream(TEST_CONTENT.getBytes("UTF-8"));
        storageService.write(TEST_CATEGORY, TEST_ID, is);

        // first archive step
        storageService.archive(TEST_CATEGORY, TEST_ID);
        items = getHistory(storageService, TEST_CATEGORY, TEST_ID);
        assertEquals(1, items.size());
        assertEquals(TEST_CATEGORY, items.get(0).getCategory());
        assertEquals(TEST_ID, items.get(0).getId());
        assertEquals(TEST_CONTENT, IOUtils.toString(items.get(0).getContent()));
        assertTrue(items.get(0).lastModified() != 0);

        // second archive step
        storageService.archive(TEST_CATEGORY, TEST_ID);
        items = getHistory(storageService, TEST_CATEGORY, TEST_ID);
        assertEquals(2, items.size());
        assertTrue(items.get(0).lastModified() != 0);
        assertEquals(TEST_ID, items.get(0).getId());
        assertTrue(items.get(1).lastModified() != 0);
        assertTrue(items.get(1).lastModified() >= items.get(0).lastModified());
        assertEquals(TEST_ID, items.get(1).getId());
    }

    @Test
    public void testReadWriteArchive() throws Exception {
        final String TEST_CATEGORY = "test_large_archive";

        StorageService storageService = getStorageService();

        // create item
        ByteArrayInputStream is = new ByteArrayInputStream(TEST_CONTENT.getBytes("UTF-8"));
        storageService.write(TEST_CATEGORY, TEST_ID, is);

        // create some archive entries
        for (int i = 0; i < 10; ++i) {
            storageService.archive(TEST_CATEGORY, TEST_ID);
        }

        // read these entries and check that the timestamps form a series
        List<ByteArrayStorageItem> items = getHistory(storageService, TEST_CATEGORY, TEST_ID);
        assertEquals(10, items.size());
        for (int i = 0; i < 10; ++i) {
            assertEquals(TEST_CATEGORY, items.get(i).getCategory());
            assertEquals(TEST_ID, items.get(i).getId());
            assertEquals(TEST_CONTENT, IOUtils.toString(items.get(i).getContent()));
            if (i > 0) {
                assertTrue(items.get(i).lastModified() >= items.get(i-1).lastModified());
            }
        }

        // write an additional entry
        long now = System.currentTimeMillis() + 4711;
        storageService.writeToArchive(TEST_CATEGORY, TEST_ID, now, IOUtils.toInputStream(TEST_CONTENT_UPDATED));
        items = getHistory(storageService, TEST_CATEGORY, TEST_ID);
        assertEquals(11, items.size());

        assertEquals(TEST_CATEGORY, items.get(10).getCategory());
        assertEquals(TEST_ID, items.get(10).getId());
        assertEquals(TEST_CONTENT_UPDATED, IOUtils.toString(items.get(10).getContent()));
        assertEquals(now, items.get(10).lastModified());
    }

    private String readContent(final String TEST_CATEGORY) throws Exception {
        StorageService storageService = getStorageService();
        InputStream stream = storageService.read(TEST_CATEGORY, TEST_ID);
        String outputText = IOUtils.toString(stream, "UTF-8");
        stream.close();
        return outputText;
    }

    private void writeContent(final String TEST_CATEGORY, String id, String content) throws Exception {
        StorageService storageService = getStorageService();
        ByteArrayInputStream is = new ByteArrayInputStream(content.getBytes("UTF-8"));
        storageService.write(TEST_CATEGORY, TEST_ID, is);
        is.close();
        return;
    }

    private List<ByteArrayStorageItem> getHistory(StorageService storageService, String category, String id)
            throws IOException {
        final List<ByteArrayStorageItem> items = new ArrayList<ByteArrayStorageItem>();
        storageService.readFromArchive(category, id, new StorageConsumer(){
            @Override
            public void consume(String category, String key, long lastModified, InputStream blob) throws IOException {
                items.add(new ByteArrayStorageItem(category, key, lastModified, IOUtils.toByteArray(blob)));
            }
        });
        return items;
    }
}
