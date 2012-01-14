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
package org.eclipse.skalli.api.rest.internal.admin;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.eclipse.skalli.core.internal.persistence.xstream.FileStorageService;
import org.eclipse.skalli.services.ServiceFilter;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.configuration.ConfigurationProperties;
import org.eclipse.skalli.services.extension.rest.ResourceBase;
import org.eclipse.skalli.services.persistence.StorageException;
import org.eclipse.skalli.services.persistence.StorageService;
import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

public class ProjectBackupResource extends ResourceBase {

    private static final String FILE_NAME = "backup.zip"; //$NON-NLS-1$

    @Get
    public Representation retrieve() {
        StorageService storageService = getStorageService();
        if (storageService == null) {
            return createStatusMessage(Status.SERVER_ERROR_INTERNAL, "No storage service available");
        }
        ZipRepresentation zipRepresentation = new ZipRepresentation(storageService);
        Disposition disposition = new Disposition(Disposition.TYPE_ATTACHMENT);
        disposition.setFilename(FILE_NAME);
        zipRepresentation.setDisposition(disposition);
        return zipRepresentation;
    }

    private StorageService getStorageService() {
        final String storageServiceClassName = ConfigurationProperties.getProperty(
                ConfigurationProperties.PROPERTY_STORAGE_SERVICE, FileStorageService.class.getName());
        Set<StorageService> storageServices = Services.getServices(StorageService.class, new ServiceFilter<StorageService>() {
            @Override
            public boolean accept(StorageService instance) {
                return instance.getClass().getName().equals(storageServiceClassName);
            }
        });
        return storageServices.size() > 0? storageServices.iterator().next() : null;
    }

    private static class ZipRepresentation extends OutputRepresentation {

        private static final int BUFFER = 2048;

        private StorageService storageService;

        public ZipRepresentation(StorageService storageService) {
            super(MediaType.APPLICATION_ZIP);
            this.storageService = storageService;
        }

        @Override
        public void write(OutputStream paramOutputStream) throws IOException {
            ZipOutputStream out = null;
            try {
                out = new ZipOutputStream(new BufferedOutputStream(paramOutputStream));
                read("customization", out); //$NON-NLS-1$
                read("Project", out); //$NON-NLS-1$
                read("Issues", out); //$NON-NLS-1$
                read("Favorites", out); //$NON-NLS-1$
                read("User", out); //$NON-NLS-1$
                read("Group", out); //$NON-NLS-1$
            } catch (StorageException e) {
                throw new IOException(e);
            } finally {
                IOUtils.closeQuietly(out);
            }
        }

        private void read(String category, ZipOutputStream out) throws StorageException, IOException {
            List<String> keys = storageService.keys(category);
            BufferedInputStream origin = null;
            for (String key: keys) {
                try {
                    origin = new BufferedInputStream(storageService.read(category, key), BUFFER);
                    ZipEntry entry = new ZipEntry(MessageFormat.format("{0}/{1}.xml", category, key)); //$NON-NLS-1$
                    out.putNextEntry(entry);
                    int count;
                    byte data[] = new byte[BUFFER];
                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                    }
                } finally {
                    IOUtils.closeQuietly(origin);
                }
            }
        }
    }
}
