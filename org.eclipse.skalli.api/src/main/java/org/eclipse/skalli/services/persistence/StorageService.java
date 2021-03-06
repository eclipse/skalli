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
package org.eclipse.skalli.services.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Interface representing a storage service that can be used for
 * persisting of project date, customizations etc.
 *
 * Note: For testing a StorageService there is a test helper available:
 * {@link org.eclipse.skalli.testutil.StorageServiceTestBase}.
 */
public interface StorageService {

    /**
     * Writes the content of the given stream to the storage.
     * Note, this method does not close the content stream.
     *
     * @param category  category or type of the content.
     * @param key  the unique key of the content within its category.
     * @param blob  the content.
     * @throws IOException  if an i/o error occured while writing content to the store.
     */
    public void write(String category, String key, InputStream blob) throws IOException;

    /**
     * Reads the content specified by its category and key from the storage.
     * Callers should close the returned stream after having read out the content.
     *
     * @param category  category or type of the content.
     * @param key  the unique key of the content within its category.
     * @return  the content, or <code>null</code> if no content for the specified
     * parameters is stored.
     * @throws IOException  if an i/o error occured while reading content from the store.
     */
    public InputStream read(String category, String key) throws IOException;

    /** Reads the content specified by its category and key from the storage.
     *
     * @param category  category or type of the content.
     * @param key  the unique key of the content within its category.
     * @param consumer consumer for the content.
     * @throws IOException  if an i/o error occured while reading content from the store.
     */
    public void read(String category, String key, StorageConsumer consumer) throws IOException;

    /**
     * Provides all stored content for the given category to the specified consumer.
     *
     * @param category  category of stored items to provide.
     * @param consumer  consumer for stored items.
     * @throws IOException  if an i/o error occured while reading content from the store.
     */
    public void readAll(String category, StorageConsumer consumer) throws IOException;

    /**
     * Archives the current content specified by its category and key.
     *
     * @param category  category or type of the content.
     * @param key  the unique key of the content within its category.
     * @throws IOException  if an i/o error occured while archiving the content.
     */
    public void archive(String category, String key) throws IOException;

    /**
     * Writes the given content to the archive
     *
     * @param category
     * @param key
     * @param blob
     * @throws IOException if an i/o error occured while eriting to the archive.
     */
    public void writeToArchive(String category, String key, long timestamp, InputStream blob) throws IOException;

    /**
     * Provides all archived content for the given category and key to the specified consumer.
     *
     * @param category  category of archived items to provide.
     * @param key  the unique key of the content within its category.
     * @param consumer consumer for archived items.
     * @throws IOException  if an i/o error occured while reading content from the store.
     */
    public void readFromArchive(String category, String key, StorageConsumer consumer) throws IOException;

    /**
     * Returns the keys of storage entries for the given category.
     *
     * @param category  category or type of the content.
     * @return  a list of keys, or an empty list, if no content for the given
     * catgeory has been stored yet.
     * @throws IOException  if an i/o error occured while retrieving keys from the store.
     */
    public List<String> keys(String category) throws IOException;
}
