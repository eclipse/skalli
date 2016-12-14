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
package org.eclipse.skalli.core.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.skalli.core.storage.Historian.HistoryEntry;
import org.eclipse.skalli.core.storage.Historian.HistoryIterator;
import org.eclipse.skalli.services.BundleProperties;
import org.eclipse.skalli.services.persistence.StorageConsumer;
import org.eclipse.skalli.services.persistence.StorageService;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a storage service based on a local file system.
 */
public class FileStorageComponent implements StorageService {

    private static final Logger LOG = LoggerFactory.getLogger(FileStorageComponent.class);

    private static final String STORAGE_BASE = "storage" + IOUtils.DIR_SEPARATOR; //$NON-NLS-1$

    private final File storageBase;

    /**
     * This constructor determines the storage directory by searching for the property <tt>"workdir"</tt>
     * in the resource file <tt>skalli.properties</tt>. Alternatively the storage directory can be
     * defined with the system property <tt>"workdir"</tt>. If so such property is defined, the current
     * directory is used.
     */
    public FileStorageComponent() {
        this.storageBase = getDefaultStorageDirectory();
    }

    /**
     *  This constructor allows to specify the storage directory explicitly, e.g. for testing purposes.
     */
    FileStorageComponent(File storageBase) {
        this.storageBase = storageBase;
    }

    protected void activate(ComponentContext context) {
        LOG.info(MessageFormat.format("[StorageService][file] {0} : activated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void deactivate(ComponentContext context) {
        LOG.info(MessageFormat.format("[StorageService][file] {0} : deactivated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    @Override
   public void write(String category, String key, InputStream blob) throws IOException {
        File file = getFile(category, key);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            IOUtils.copy(blob, fos);
        } finally {
            IOUtils.closeQuietly(fos);
        }
        LOG.debug(getPath(category, key) + " successfully written to " + file.getAbsolutePath()); //$NON-NLS-1$
    }

    @Override
    public void archive(String category, String key) throws IOException {
        File oldEntityFile = getFile(category, key);
        new Historian(new File(storageBase, category)).historize(oldEntityFile);
    }

    @Override
    public void writeToArchive(String category, String id, long timestamp, InputStream blob) throws IOException {
        new Historian(new File(storageBase, category)).historize(id, timestamp, blob);
    }

    @Override
    public void readFromArchive(String category, String key, StorageConsumer consumer) throws IOException {
        HistoryIterator history = null;
        try {
            history = new Historian(new File(storageBase, category)).getHistory(key);
            while (history.hasNext()) {
                HistoryEntry next = history.next();
                consumer.consume(category, key, next.getTimestamp(), IOUtils.toInputStream(next.getContent()));
            }
        } finally {
            history.close();
        }
    }

    @Override
    public InputStream read(String category, String key) throws IOException {
        return toStream(getFile(category, key));
    }

    @Override
    public List<String> keys(String category) {
        List<String> list = new ArrayList<String>();

        File storageBaseEntityName = new File(storageBase, category);
        if (!storageBaseEntityName.exists()) {
            return list;
        }

        @SuppressWarnings("unchecked")
        Iterator<File> files = FileUtils.iterateFiles(storageBaseEntityName, new String[] { "xml" }, true); //$NON-NLS-1$
        while (files.hasNext()) {
            File file = files.next();
            String key = file.getName().substring(0, file.getName().length() - ".xml".length()); //$NON-NLS-1$
            list.add(key);
        }

        return list;
    }

    private static File getDefaultStorageDirectory() {
        File storageDirectory = null;
        String workdir = BundleProperties.getProperty(BundleProperties.PROPERTY_WORKDIR);
        if (workdir != null) {
            File workingDirectory = new File(workdir);
            if (workingDirectory.exists() && workingDirectory.isDirectory()) {
                storageDirectory = new File(workingDirectory, STORAGE_BASE);
            } else {
                LOG.warn("Working directory '" + workingDirectory.getAbsolutePath()
                        + "' not found - falling back to current directory");
            }
        }
        if (storageDirectory == null) {
            // fall back: use current directory as working directory
            storageDirectory = new File(STORAGE_BASE);
        }

        LOG.info("Using storage directory '" + storageDirectory.getAbsolutePath() + "'");
        return storageDirectory;
    }

    private File getFile(String category, String key) {
        File path = new File(storageBase, category);
        if (!path.exists()) {
            path.mkdirs();
        }
        return new File(path, key + ".xml"); //$NON-NLS-1$
    }

    private static String getPath(String category, String key) {
        return category + "/" + key; //$NON-NLS-1$
    }

    private static InputStream toStream(File file) {
        try {
            return file != null && file.exists() && file.isFile() && file.canRead() ? new FileInputStream(file) : null;
        } catch (FileNotFoundException e) {
            return null;
        }
    }
}
