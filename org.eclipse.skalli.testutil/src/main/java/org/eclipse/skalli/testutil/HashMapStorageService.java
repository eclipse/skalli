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
package org.eclipse.skalli.testutil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.eclipse.skalli.api.java.StorageException;
import org.eclipse.skalli.api.java.StorageService;

/**
 * Simple implementation of a storage service based on a hash map
 * for testing purposes.
 */
public class HashMapStorageService implements StorageService {

    public static class Key {
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((category == null) ? 0 : category.hashCode());
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Key other = (Key) obj;
            if (category == null) {
                if (other.category != null) {
                    return false;
                }
            } else if (!category.equalsIgnoreCase((other.category))) {
                return false;
            }
            if (key == null) {
                if (other.key != null) {
                    return false;
                }
            } else if (!key.equals(other.key)) {
                return false;
            }
            return true;
        }

        private String category;
        private String key;

        public String getCategory() {
            return category;
        }

        public String getKey() {
            return key;
        }

        public Key(String category, String key) {
            this.category = category;
            this.key = key;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return category + "/" + key; //$NON-NLS-1$
        }

    }

    private Map<Key, byte[]> blobStore = new HashMap<Key, byte[]>();

    public Map<Key, byte[]> getBlobStore() {
        return blobStore;
    }

    @Override
    public void write(String category, String key, InputStream blob) throws StorageException {
        try {
            blobStore.put(new Key(category, key), IOUtils.toByteArray(blob));
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    @Override
    public InputStream read(String category, String key) throws StorageException {
        byte[] content = blobStore.get(new Key(category, key));
        if (content == null) {
            return null;
        }
        else {
            return new ByteArrayInputStream(content);
        }
    }

    @Override
    public void archive(String category, String key) throws StorageException {
        //yet there is no way to read out the history again, so do nothing now
        return;
    }

    @Override
    public List<String> keys(String category) throws StorageException {
        List<String> result = new ArrayList<String>();
        Set<Key> allKeys = blobStore.keySet();
        for (Key key : allKeys) {
            if (key.getCategory().equalsIgnoreCase(category)) {
                result.add(key.getKey());
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "HashMapStorageService [blobStore=" + blobStore + "]";
    }


}