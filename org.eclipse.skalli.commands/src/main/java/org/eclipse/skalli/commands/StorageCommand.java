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
package org.eclipse.skalli.commands;

import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.skalli.commons.UUIDUtils;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.persistence.PersistenceService;
import org.eclipse.skalli.services.persistence.StorageException;
import org.eclipse.skalli.services.persistence.StorageService;
import org.osgi.framework.Constants;

public class StorageCommand {
    public static void copy(String sourceType, String destType, String category, CommandInterpreter intr) {
        StorageService source = null;
        StorageService destination = null;

        try {
            source = getStorageService(sourceType);
            destination = getStorageService(destType);
        } catch (IllegalStateException e) {
            intr.println("ERROR: more than one'" + StorageService.class.getSimpleName() + "' found.");
            intr.printStackTrace(e);
            return;
        }

        if (source == null) {
            intr.println("ERROR: no '" + StorageService.class.getSimpleName() + "' found with type '" + sourceType
                    + "'");
            return;
        }

        if (destination == null) {
            intr.println("ERROR: no '" + StorageService.class.getSimpleName() + "' found with type '" + destType + "'");
            return;
        }

        copy(source, destination, category, intr);

        // ensure that the persistence service attached to the destination storage
        // refreshes all caches and reloads the entities
        Services.getRequiredService(PersistenceService.class).refreshAll();
    }

    static void copy(StorageService source, StorageService destination, String category, CommandInterpreter intr) {
        List<String> keys;
        try {
            keys = source.keys(category);
        } catch (StorageException e) {
            intr.printStackTrace(e);
            return;
        }
        if (keys == null || keys.size() == 0) {
            intr.println("No data for category '" + category + "' found. Nothing to copy.");
            return;
        }

        int copiedRecords = 0;
        StringBuffer noUUIDKeys = new StringBuffer();
        for (String key : keys) {
            InputStream blob = null;
            try {
                blob = source.read(category, key);
                // only copy records with valid uuids, expect for customization (these are "normal" strings!)
                if (UUIDUtils.isUUID(key) || "customization".equalsIgnoreCase(category)) {
                    destination.write(category, key, blob);
                    copiedRecords++;
                    if ((copiedRecords % 10) == 0) {
                        intr.println(copiedRecords + " data copied for category '" + category + "'.");
                    }
                } else {
                    noUUIDKeys.append("'");
                    noUUIDKeys.append(key);
                    noUUIDKeys.append("';");
                }
            } catch (StorageException e) {
                intr.printStackTrace(e);
            } finally {
                IOUtils.closeQuietly(blob);
            }
        }

        intr.println(copiedRecords + " data records of category '" + category
                + "' successfully copied from source with "
                + keys.size() + " records.");
        if (copiedRecords != keys.size()) {
            intr.println("Warning: could not copy " + (keys.size() - copiedRecords) + " record(s). This are: "
                    + noUUIDKeys);
        }
    }

    /**
     * @param sourceType
     * @return
     */
    private static StorageService getStorageService(String type) {
        String filter = "(&(" + Constants.OBJECTCLASS + "=" + StorageService.class.getName() + ")(storageService.type=" + type + "))"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        StorageService service = Services.getService(StorageService.class, filter);
        return service;
    }
}