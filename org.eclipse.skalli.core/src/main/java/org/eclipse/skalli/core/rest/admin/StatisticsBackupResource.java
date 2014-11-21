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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.eclipse.skalli.commons.Statistics;
import org.eclipse.skalli.commons.Statistics.BrowserInfo;
import org.eclipse.skalli.commons.Statistics.RefererInfo;
import org.eclipse.skalli.commons.Statistics.ResponseTimeInfo;
import org.eclipse.skalli.commons.Statistics.SearchInfo;
import org.eclipse.skalli.commons.Statistics.UsageInfo;
import org.eclipse.skalli.commons.Statistics.UserInfo;
import org.eclipse.skalli.services.extension.rest.ResourceBase;
import org.eclipse.skalli.services.extension.rest.ResourceRepresentation;
import org.eclipse.skalli.services.permit.Permits;
import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Put;

import com.thoughtworks.xstream.XStreamException;

public class StatisticsBackupResource extends ResourceBase {

    private static final String FILE_NAME = "statistics.zip"; //$NON-NLS-1$
    private static final String ENTRY_NAME = "statistics.xml"; //$NON-NLS-1$

    private static final String ID_PREFIX = "rest:api/admin/statistics/backup:"; //$NON-NLS-1$
    private static final String ERROR_ID_IO_ERROR = ID_PREFIX + "00"; //$NON-NLS-1$
    private static final String ERROR_ID_INVALID_BACKUP = ID_PREFIX + ":30"; //$NON-NLS-1$
    private static final String ERROR_ID_PARSING_FAILED = ID_PREFIX + ":40"; //$NON-NLS-1$
    private static final String ERROR_ID_CLASS_MISMATCH = ID_PREFIX + ":50"; //$NON-NLS-1$


    @Get
    public Representation backup() {
        if (!Permits.isAllowed(getAction(), getPath())) {
            return createUnauthorizedRepresentation();
        }
        StatisticsQuery query = new StatisticsQuery(getQueryAttributes());
        Statistics statistics = new Statistics(Statistics.getDefault(), query.getFrom(), query.getTo());
        ZipOutputRepresentation zipRepresentation = new ZipOutputRepresentation(statistics);
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

        ZipInputStream zipStream = null;
        try {
            zipStream = new ZipInputStream(entity.getStream());
            ZipEntry entry = zipStream.getNextEntry();
            try {
                String entryName = entry.getName();
                if (!entryName.equals(ENTRY_NAME)) {
                    return createErrorRepresentation(Status.CLIENT_ERROR_BAD_REQUEST, ERROR_ID_INVALID_BACKUP,
                            "Invalid backup file: Expected entry ''statistics.xml'', but found ''{0}''", entryName); //$NON-NLS-1$
                }
                ResourceRepresentation<Statistics> representation = new ResourceRepresentation<Statistics>();
                addAliases(representation);
                Statistics.getDefault().restore(representation.read(zipStream, Statistics.class));
            } finally {
                zipStream.closeEntry();
            }
        } catch (IOException e) {
            return createIOErrorRepresentation(ERROR_ID_IO_ERROR, e);
        } catch (XStreamException e) {
            return createErrorRepresentation(Status.CLIENT_ERROR_BAD_REQUEST, ERROR_ID_PARSING_FAILED,
                    "Invalid backup file: {0}", e.getMessage()); //$NON-NLS-1$
        } catch (ClassCastException e) {
            return createErrorRepresentation(Status.CLIENT_ERROR_BAD_REQUEST, ERROR_ID_CLASS_MISMATCH,
                    "Invalid backup file: {0}", e.getMessage()); //$NON-NLS-1$
        } finally {
            IOUtils.closeQuietly(zipStream);
        }
        setStatus(Status.SUCCESS_NO_CONTENT);
        return null;
    }

    private static class ZipOutputRepresentation extends OutputRepresentation {

        private Statistics statistics;

        public ZipOutputRepresentation(Statistics statistics) {
            super(MediaType.APPLICATION_ZIP);
            this.statistics = statistics;
        }

        @Override
        public void write(OutputStream out) throws IOException {
            ZipOutputStream zipStream = null;
            try {
                zipStream = new ZipOutputStream(new BufferedOutputStream(out));
                ZipEntry entry = new ZipEntry(ENTRY_NAME);
                zipStream.putNextEntry(entry);
                ResourceRepresentation<Statistics> representation = new ResourceRepresentation<Statistics>(statistics);
                addAliases(representation);
                representation.write(zipStream);
            } finally {
                IOUtils.closeQuietly(zipStream);
            }
        }
    }

    @SuppressWarnings("nls")
    private static void addAliases(ResourceRepresentation<Statistics> representation) {
        representation.addAlias("statistics", Statistics.class);
        representation.addAlias("user", UserInfo.class);
        representation.addAlias("usage", UsageInfo.class);
        representation.addAlias("referer", RefererInfo.class);
        representation.addAlias("browser", BrowserInfo.class);
        representation.addAlias("search", SearchInfo.class);
        representation.addAlias("responseTime", ResponseTimeInfo.class);
    }
}
