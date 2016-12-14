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

import static org.eclipse.skalli.testutil.StorageKey.keyOf;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.eclipse.skalli.services.persistence.StorageService;

/**
 * Simple implementation of a storage service based on a hash map
 * for testing purposes.
 */
@SuppressWarnings("nls")
public class HashMapStorageService implements StorageService {

    private Map<StorageKey, ByteArrayStorageItem> store = new HashMap<StorageKey, ByteArrayStorageItem>();
    private Map<StorageKey, List<ByteArrayStorageItem>> archive = new HashMap<StorageKey, List<ByteArrayStorageItem>>();

    public Map<StorageKey, byte[]> asMap() {
        Map<StorageKey, byte[]> result = new HashMap<StorageKey, byte[]>();
        for (Entry<StorageKey, ByteArrayStorageItem> next: store.entrySet()) {
            result.put(next.getKey(), next.getValue().toByteArray());
        }
        return result;
    }

    @Override
    public void write(String category, String id, InputStream blob) throws IOException {
        StorageKey key = keyOf(category, id);
        store.put(key, new ByteArrayStorageItem(key, IOUtils.toByteArray(blob)));
    }

    @Override
    public InputStream read(String category, String id) throws IOException {
        ByteArrayStorageItem item = store.get(keyOf(category, id));
        return item != null ? item.getContent() : null;
    }

    @Override
    public void archive(String category, String id) throws IOException {
        StorageKey key = keyOf(category, id);
        ByteArrayStorageItem item = store.get(key);
        if (item != null) {
            List<ByteArrayStorageItem> items = archive.get(key);
            if (items == null) {
                items = new ArrayList<ByteArrayStorageItem>();
                archive.put(key, items);
            }
            items.add(new ByteArrayStorageItem(key, IOUtils.toByteArray(item.getContent())));
        }
        return;
    }

    @Override
    public List<String> keys(String category) throws IOException {
        List<String> result = new ArrayList<String>();
        Set<StorageKey> allKeys = store.keySet();
        for (StorageKey key : allKeys) {
            if (key.getCategory().equalsIgnoreCase(category)) {
                result.add(key.getKey());
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "HashMapStorageService [blobStore=" + store + "]";
    }
}