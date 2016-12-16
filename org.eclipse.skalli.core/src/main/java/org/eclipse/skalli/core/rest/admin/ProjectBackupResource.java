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
import java.io.InputStream;
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
import org.eclipse.skalli.commons.ThreadPool;
import org.eclipse.skalli.core.storage.FileStorageComponent;
import org.eclipse.skalli.services.BundleProperties;
import org.eclipse.skalli.services.ServiceFilter;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.extension.rest.ResourceBase;
import org.eclipse.skalli.services.permit.Permits;
import org.eclipse.skalli.services.persistence.PersistenceService;
import org.eclipse.skalli.services.persistence.StorageConsumer;
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
            "Favorites", "User", "Group", "History");

    @Get
    public Representation backup() {
        if (!Permits.isAllowed(getAction(), getPath())) {
            return createUnauthorizedRepresentation();
        }

        StorageService storageService = getStorageService();
        if (storageService == null) {
            return createServiceUnavailableRepresentation(ERROR_ID_NO_STORAGE_SERVICE, "Storage Service");
        }
        Set<String> categories = getCategories();
        ZipOutputRepresentation zipRepresentation = new ZipOutputRepresentation(storageService, categories);
        Disposition disposition = new Disposition(Disposition.TYPE_ATTACHMENT);
        disposition.setFilename(FILE_NAME);
        zipRepresentation.setDisposition(disposition);
        setStatus(Status.SUCCESS_OK);
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

        String action = getQueryAttribute(ACTION_PARAM);
        Set<String> categories = getCategories();

        Set<String> accepted = new HashSet<String>();
        Set<String> rejected = new HashSet<String>();
        for (String category: CATEGORIES) {
            if (categories.contains(category)) {
                try {
                    if (ACTION_OVERWRITE.equals(action) || storageService.keys(category).isEmpty()) {
                        accepted.add(category);
                    } else {
                        rejected.add(category);
                    }
                } catch (IOException e) {
                    LOG.error(MessageFormat.format("Failed to retrieve keys for category {0} ({1})",
                            category, ERROR_ID_FAILED_TO_RETRIEVE_KEYS), e);
                    return createErrorRepresentation(Status.SERVER_ERROR_INTERNAL, ERROR_ID_FAILED_TO_RETRIEVE_KEYS ,
                            "Failed to store the attached backup resource");
                }
            }
        }
        if (rejected.size() > 0) {
            return createErrorRepresentation(
                    Status.CLIENT_ERROR_PRECONDITION_FAILED,
                    ERROR_ID_OVERWRITE_EXISTING_DATA,
                    MessageFormat.format(
                            "Restore might overwrite existing data in the folling categories:\n{0}\n" +
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
                            } catch (IOException e) {
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
        // refreshes all caches and reloads all entities --- do that
        // in the background, otherwise we might run into timeouts
        ThreadPool.submit(new Runnable() {
            @Override
            public void run() {
                PersistenceService persistenceService = Services.getService(PersistenceService.class);
                if (persistenceService != null) {
                    LOG.info("Refreshing all caches");
                    persistenceService.refreshAll();
                }
            }
        });

        setStatus(Status.SUCCESS_NO_CONTENT);
        return null;
    }

    /**
     * Start with a result set that contains all entries from CATEGORIES,
     * except if there is an include list; in this case start with an empty
     * result set. Then first add all categories contained in the include list
     * and remove afterwards all entries contained in the exclude list.
     */
    private Set<String> getCategories() {
        Set<String> categories = new HashSet<String>(CATEGORIES);
        Set<String> include = CollectionUtils.asSet(StringUtils.split(getQueryAttribute(INCLUDE_PARAM),
                SearchQuery.PARAM_LIST_SEPARATOR));
        Set<String> exclude = CollectionUtils.asSet(StringUtils.split(getQueryAttribute(EXCLUDE_PARAM),
                SearchQuery.PARAM_LIST_SEPARATOR));
        if (!include.isEmpty()) {
            categories.removeAll(CATEGORIES);
        }
        categories.addAll(include);
        categories.removeAll(exclude);
        return categories;
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
        private Set<String> categories;
        private boolean withHistory;

        public ZipOutputRepresentation(StorageService storageService, Set<String> categories) {
            super(MediaType.APPLICATION_ZIP);
            this.storageService = storageService;
            this.categories = categories;
            this.withHistory = categories.contains("History"); //$NON-NLS-1$
        }

        @Override
        public void write(OutputStream out) throws IOException {
            ZipOutputStream zipStream = null;
            try {
                zipStream = new ZipOutputStream(new BufferedOutputStream(out));
                for (String category: CATEGORIES) {
                    if (categories.contains(category)) {
                        write(category, zipStream);
                    }
                }
                zipStream.flush();
            } finally {
                IOUtils.closeQuietly(zipStream);
            }
        }

        private void write(String category, ZipOutputStream target) throws IOException {
            List<String> keys = storageService.keys(category);
            for (String key: keys) {
                write(category, key, target);
                if (withHistory) {
                    writeHistory(category, key, target);
                }
            }
        }

        private void write(String category, String key, final ZipOutputStream target) throws IOException {
            String entryName = MessageFormat.format("{0}/{1}.xml", category, key); //$NON-NLS-1$
            write(entryName, storageService.read(category, key), target);
        }

        private void writeHistory(String category, String key, final ZipOutputStream target) throws IOException {
            storageService.readFromArchive(category, key, new StorageConsumer() {
                @Override
                public void consume(String category, String key, long lastModified, InputStream blob)
                        throws IOException {
                    String entryName = MessageFormat.format("{0}/{1}_{2}.xml", //$NON-NLS-1$
                            category, key, Long.toString(lastModified));
                    write(entryName, blob, target);
                }
            });
        }

        private void write(String entryName, InputStream blob, ZipOutputStream target) throws IOException {
            BufferedInputStream source = null;
            try {
                source = new BufferedInputStream(blob, BUFFER);
                ZipEntry entry = new ZipEntry(entryName);
                target.putNextEntry(entry);
                int count;
                byte data[] = new byte[BUFFER];
                while ((count = source.read(data, 0, BUFFER)) != -1) {
                    target.write(data, 0, count);
                }
            } finally {
                IOUtils.closeQuietly(source);
            }
        }
    }

}
