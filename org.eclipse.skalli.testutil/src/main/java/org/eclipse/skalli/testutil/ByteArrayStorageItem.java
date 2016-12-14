/*******************************************************************************
 * Copyright (c) 2010-2016 SAP AG and others.
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class ByteArrayStorageItem {
    private StorageKey key;
    private byte[] buf;
    private long lastModified;

    public ByteArrayStorageItem(String category, String id, long lastModified, byte[] buf) {
        this(keyOf(category, id), lastModified, buf);
    }

    public ByteArrayStorageItem(StorageKey key, long lastModified,  byte[] buf) {
        this.key = key;
        this.buf = buf;
        this.lastModified = lastModified;
    }

    public ByteArrayStorageItem(StorageKey key, byte[] buf) {
        this(key, System.currentTimeMillis(), buf);
    }

    public String getCategory() {
        return key.getCategory();
    }

    public String getId() {
        return key.getKey();
    }

    public long lastModified() {
        return lastModified;
    }

    public InputStream getContent() {
        return buf != null ? new ByteArrayInputStream(buf) : null;
    }

    public byte[] toByteArray() {
        return buf;
    }
}