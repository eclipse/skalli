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
package org.eclipse.skalli.core.internal.persistence.xstream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.api.java.StorageException;
import org.eclipse.skalli.api.java.StorageService;
import org.eclipse.skalli.common.Consts;
import org.eclipse.skalli.log.Log;

/**
 * Implementation of a storage service based on a local file system.
 */
public class FileStorageService implements StorageService {

    private static final Logger LOG = Log.getLogger(XStreamPersistence.class);
    private static final String STORAGE_BASE = "storage" + IOUtils.DIR_SEPARATOR; //$NON-NLS-1$

    private final File storageBase;

    /**
     * This constructor determines the storage directory by searching for the property <tt>"workdir"</tt>
     * in the resource file <tt>skalli.properties</tt>. Alternatively the storage directory can be
     * defined with the system property <tt>"workdir"</tt>. If so such property is defined, the current
     * directory is used.
     */
    public FileStorageService() {
        this.storageBase = getDefaultStorageDirectory();
    }

    /**
     *  This constructor allows to specify the storage directory explicitly, e.g. for testing purposes.
     */
    FileStorageService(File storageBase) {
        this.storageBase = storageBase;
    }

    private String getPath(String category, String key) {
        return category + "/" + key; //$NON-NLS-1$
    }

    private File getFile(String category, String key) {
        File path = new File(storageBase, category);
        if (!path.exists()) {
            path.mkdirs();
        }
        return new File(path, key + ".xml"); //$NON-NLS-1$
    }

    @Override
   public void write(String category, String key, InputStream blob) throws StorageException {
        File file = getFile(category, key);
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e1) {
            throw new StorageException("Failed to write " + getPath(category, key), e1);
        }
        try {
            IOUtils.copy(blob, fos);
        } catch (FileNotFoundException e) {
            throw new StorageException("Failed to write " + getPath(category, key), e);
        } catch (IOException e) {
            throw new StorageException("Failed to write " + getPath(category, key), e);
        } finally {
            IOUtils.closeQuietly(fos);
        }
        LOG.fine(getPath(category, key) + " successfully written to " + file.getAbsolutePath()); //$NON-NLS-1$
    }

    @Override
    public void archive(String category, String key) {
        File oldEntityFile = getFile(category, key);
        new Historian().historize(oldEntityFile, true);
    }

    @Override
    public InputStream read(String category, String key) throws StorageException {
        File file = getFile(category, key);
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            return null;
        }
    }


    private final File getDefaultStorageDirectory() {
        File storageDirectory = null;

        String workdir = null;
        try {
            // try to get working directory from configuration file
            Properties properties = new Properties();
            properties.load(getClass().getResourceAsStream(Consts.PROPERTIES_RESOURCE));
            workdir = properties.getProperty(Consts.PROPERTY_WORKDIR);
            if (StringUtils.isBlank(workdir)) {
                LOG.warning("Property '" + Consts.PROPERTY_WORKDIR + "' not defined in configuration file '" + Consts.PROPERTIES_RESOURCE
                        + "' - falling back to system property '" + Consts.PROPERTY_WORKDIR + "'");
            }
        } catch (Exception e) {
            LOG.warning("Cannot read configuration file '" + Consts.PROPERTIES_RESOURCE +
                    "' - falling back to system property '" + Consts.PROPERTY_WORKDIR + "'");
        }

        if (StringUtils.isBlank(workdir)) {
            // fall back: get working directory from system property
            workdir = System.getProperty(Consts.PROPERTY_WORKDIR);
            if (StringUtils.isBlank(workdir)) {
                LOG.warning("Cannot get system property '" + Consts.PROPERTY_WORKDIR + "' - " +
                        "falling back to current directory");
            }
        }

        if (workdir != null) {
            File workingDirectory = new File(workdir);
            if (workingDirectory.exists() && workingDirectory.isDirectory()) {
                storageDirectory = new File(workingDirectory, STORAGE_BASE);
            } else {
                LOG.warning("Working directory '" + workingDirectory.getAbsolutePath()
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

    @Override
    public List<String> keys(String category) throws StorageException {
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
}
