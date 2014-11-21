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
package org.eclipse.skalli.core.rest.admin;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.core.storage.FileStorageComponent;
import org.eclipse.skalli.services.BundleProperties;
import org.eclipse.skalli.services.ServiceFilter;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.extension.rest.ResourceBase;
import org.eclipse.skalli.services.permit.Permits;
import org.eclipse.skalli.services.persistence.PersistenceService;
import org.eclipse.skalli.services.persistence.StorageException;
import org.eclipse.skalli.services.persistence.StorageService;
import org.eclipse.skalli.services.search.SearchQuery;
import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectBackupResource extends ResourceBase {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectBackupResource.class);

    private static final String FILE_NAME = "backup.zip"; //$NON-NLS-1$

    private static final String EXCLUDE_PARAM = "exclude"; //$NON-NLS-1$
    private static final String INCLUDE_PARAM = "include"; //$NON-NLS-1$

    private static final String ACTION_PARAM = "action"; //$NON-NLS-1$
    private static final String ACTION_OVERWRITE = "overwrite"; //$NON-NLS-1$

    // error codes for logging and error responses
    private static final String ID_PREFIX = "rest:api/admin/backup:"; //$NON-NLS-1$
    private static final String ERROR_ID_IO_ERROR = ID_PREFIX + "00"; //$NON-NLS-1$
    private static final String ERROR_ID_NO_STORAGE_SERVICE = ID_PREFIX + "10"; //$NON-NLS-1$
    private static final String ERROR_ID_FAILED_TO_RETRIEVE_KEYS = ID_PREFIX + "20"; //$NON-NLS-1$
    private static final String ERROR_ID_FAILED_TO_STORE = ID_PREFIX + "30"; //$NON-NLS-1$
    private static final String ERROR_ID_OVERWRITE_EXISTING_DATA = ID_PREFIX + "40"; //$NON-NLS-1$

    @SuppressWarnings("nls")
    private static final Set<String> CATEGORIES = CollectionUtils.asSet("customization", "Project", "Issues",
            "Favorites", "User", "Group");

    @Get
    public Representation backup() {
        if (!Permits.isAllowed(getAction(), getPath())) {
            return createUnauthorizedRepresentation();
        }

        StorageService storageService = getStorageService();
        if (storageService == null) {
            return createServiceUnavailableRepresentation(ERROR_ID_NO_STORAGE_SERVICE, "Storage Service");
        }
        Set<String> included = getCategories(INCLUDE_PARAM);
        Set<String> excluded = getCategories(EXCLUDE_PARAM);
        ZipOutputRepresentation zipRepresentation = new ZipOutputRepresentation(storageService, included, excluded);
        Disposition disposition = new Disposition(Disposition.TYPE_ATTACHMENT);
        disposition.setFilename(FILE_NAME);
        zipRepresentation.setDisposition(disposition);
        return zipRepresentation;
    }

    @Put
    public Representation restore(Representation entity) {
        if (!Permits.isAllowed(getAction(), getPath())) {
            return createUnauthorizedRepresentation();
        }

        StorageService storageService = getStorageService();
        if (storageService == null) {
            return createServiceUnavailableRepresentation(ERROR_ID_NO_STORAGE_SERVICE, "Storage Service");
        }

        String action = getQuery().getValues(ACTION_PARAM);
        Set<String> included = getCategories(INCLUDE_PARAM);
        Set<String> excluded = getCategories(EXCLUDE_PARAM);

        Set<String> accepted = new HashSet<String>();
        Set<String> rejected = new HashSet<String>();
        for (String category: CATEGORIES) {
            if (included.size() > 0 && !included.contains(category)) {
                continue;
            }
            if (excluded.size() > 0 && excluded.contains(category)) {
                continue;
            }
            try {
                if (ACTION_OVERWRITE.equals(action) || storageService.keys(category).isEmpty()) {
                    accepted.add(category);
                } else {
                    rejected.add(category);
                }
            } catch (StorageException e) {
                LOG.error(MessageFormat.format("Failed to retrieve keys for category {0} ({1})",
                        category, ERROR_ID_FAILED_TO_RETRIEVE_KEYS), e);
                return createErrorRepresentation(Status.SERVER_ERROR_INTERNAL, ERROR_ID_FAILED_TO_RETRIEVE_KEYS ,
                        "Failed to store the attached backup resource");
            }
        }
        if (rejected.size() > 0) {
            return createErrorRepresentation(
                    Status.CLIENT_ERROR_PRECONDITION_FAILED,
                    ERROR_ID_OVERWRITE_EXISTING_DATA,
                    MessageFormat.format(
                            "Restore might overwrite existing project data in the folling categories:\n{0}\n" +
                            "Either exclude these categories from the restore with a \"exclude=<comma-separated-list>\" " +
                            "parameter or enforce the restore with \"action=overwrite\".",
                             CollectionUtils.toString(rejected, '\n')));
        }
        if (accepted.isEmpty()) {
            setStatus(Status.SUCCESS_NO_CONTENT);
            return null;
        }

        ZipInputStream zipStream = null;
        try {
            zipStream = new ZipInputStream(entity.getStream());
            ZipEntry entry = zipStream.getNextEntry();
            while (entry != null) {
                try {
                    if (!entry.isDirectory()) {
                        String entryName = entry.getName().replace('\\', '/');
                        String[] parts = StringUtils.split(entryName, '/');
                        if (parts.length != 2) {
                            LOG.info(MessageFormat.format("Restore: {0} is not recognized as entity key", entryName));
                            continue;
                        }
                        // ensure that the category of the entry, i.e. the directory name,
                        // is in the set of accepted categories
                        String category = parts[0];
                        String key = parts[1];
                        if (accepted.contains(category)) {
                            if (key.endsWith(".xml")) { //$NON-NLS-1$
                                key = key.substring(0, key.length() - 4);
                            }
                            try {
                                storageService.write(category, key, zipStream);
                            } catch (StorageException e) {
                                LOG.error(MessageFormat.format(
                                        "Failed to store entity with key {0} and category {1} ({2})",
                                        key, category, ERROR_ID_FAILED_TO_STORE), e);
                                return createErrorRepresentation(Status.SERVER_ERROR_INTERNAL, ERROR_ID_FAILED_TO_STORE,
                                        "Failed to store the attached backup");
                            }
                        } else {
                            LOG.info(MessageFormat.format("Restore: Excluded {0} (category ''{1}'' not accepted)",
                                    key, category));
                        }
                    }
                } finally {
                    zipStream.closeEntry();
                    entry = zipStream.getNextEntry();
                }
            }
        } catch (IOException e) {
            return createIOErrorRepresentation(ERROR_ID_IO_ERROR, e);
        } finally {
            IOUtils.closeQuietly(zipStream);
        }

        // ensure that the persistence service attached to the storage
        // refreshes all caches and reloads all entities
        PersistenceService persistenceService = Services.getService(PersistenceService.class);
        if (persistenceService != null) {
            persistenceService.refreshAll();
        }
        setStatus(Status.SUCCESS_NO_CONTENT);
        return null;
    }

    private Set<String> getCategories(String paramId) {
        String[] categories = null;
        String ignoreAttribute = getQuery().getValues(paramId);
        if (ignoreAttribute != null) {
            categories = StringUtils.split(ignoreAttribute, SearchQuery.PARAM_LIST_SEPARATOR);
        }
        return CollectionUtils.asSet(categories);
    }

    private StorageService getStorageService() {
        final String storageServiceClassName = BundleProperties.getProperty(
                BundleProperties.PROPERTY_STORAGE_SERVICE, FileStorageComponent.class.getName());
        Set<StorageService> storageServices = Services.getServices(StorageService.class, new ServiceFilter<StorageService>() {
            @Override
            public boolean accept(StorageService instance) {
                return instance.getClass().getName().equals(storageServiceClassName);
            }
        });
        return storageServices.size() > 0? storageServices.iterator().next() : null;
    }

    private static class ZipOutputRepresentation extends OutputRepresentation {

        private static final int BUFFER = 2048;

        private StorageService storageService;
        private Set<String> included;
        private Set<String> excluded;

        public ZipOutputRepresentation(StorageService storageService, Set<String> included, Set<String> excluded) {
            super(MediaType.APPLICATION_ZIP);
            this.storageService = storageService;
            this.included = included;
            this.excluded = excluded;
        }

        @Override
        public void write(OutputStream paramOutputStream) throws IOException {
            ZipOutputStream out = null;
            try {
                out = new ZipOutputStream(new BufferedOutputStream(paramOutputStream));
                for (String category: CATEGORIES) {
                    // if there is an explicit include list, ensure that the
                    // category is contained in that list
                    if (included.size() > 0 && !included.contains(category)) {
                        continue;
                    }
                    // if there is an explicit exclude list, ensure that the
                    // category is not contained in that list
                    if (excluded.size() > 0 && excluded.contains(category)) {
                        continue;
                    }
                    write(category, out);
                }
            } catch (StorageException e) {
                throw new IOException(e);
            } finally {
                IOUtils.closeQuietly(out);
            }
        }

        private void write(String category, ZipOutputStream out) throws StorageException, IOException {
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
