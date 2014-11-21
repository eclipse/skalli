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

import java.io.IOException;

import org.eclipse.skalli.commons.FormatUtils;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.extension.rest.RestConverterBase;
import org.eclipse.skalli.services.scheduler.RunnableSchedule;
import org.eclipse.skalli.services.scheduler.SchedulerService;
import org.osgi.framework.Bundle;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

class StatusConverter extends RestConverterBase<Object> {

    public static final String API_VERSION = "1.0"; //$NON-NLS-1$
    public static final String NAMESPACE = "http://www.eclipse.org/skalli/2010/API/Admin"; //$NON-NLS-1$

    public StatusConverter() {
        super(Object.class);
    }

    @SuppressWarnings("nls")
    @Override
    protected void marshal(Object obj) throws IOException {
        writer.object("status");
            namespaces();
            apiVersion();
            writer.array("schedules", "schedule");
            SchedulerService schedulerService = Services.getService(SchedulerService.class);
            if (schedulerService != null) {
                for (RunnableSchedule schedule : schedulerService.getSchedules()) {
                    long lastStarted = schedule.getLastStarted();
                    long lastCompleted = schedule.getLastCompleted();
                    writer
                    .pair("name", schedule.getCaption())
                    .pair("runnable", schedule.getClass().getName())
                    .pair("runAt", schedule.getSchedule());
                    if (lastStarted > 0) {
                        writer.pair("lastStarted", FormatUtils.formatUTCWithMillis(lastStarted));
                    }
                    if (lastCompleted > 0) {
                        writer.pair("lastCompleted", FormatUtils.formatUTCWithMillis(lastCompleted));
                    }
                }
            }
            writer.end();
            writer.array("bundles", "bundle");
            for (Bundle bundle : Services.getBundles()) {
                writer
                .pair("name", bundle.getSymbolicName())
                .pair("version", bundle.getVersion().toString())
                .pair("state", getBundleState(bundle.getState()));
            }
            writer.end();
        writer.end();
    }

    @Deprecated
    public StatusConverter(String host) {
        super(Object.class, "status", host); //$NON-NLS-1$
    }

    private String getBundleState(int state) {
        switch (state) {
        case Bundle.ACTIVE:
            return "Active"; //$NON-NLS-1$
        case Bundle.INSTALLED:
            return "Installed"; //$NON-NLS-1$
        case Bundle.UNINSTALLED:
            return "Uninstalled"; //$NON-NLS-1$
        case Bundle.STARTING:
            return "Starting"; //$NON-NLS-1$
        case Bundle.STOPPING:
            return "Stopping"; //$NON-NLS-1$
        case Bundle.RESOLVED:
            return "Resolved"; //$NON-NLS-1$
        default:
            return "(unknown)"; //$NON-NLS-1$
        }
    }

    @Deprecated
    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        marshalNSAttributes(writer);
        marshalApiVersion(writer);
        writer.startNode("schedules"); //$NON-NLS-1$
        SchedulerService schedulerService = Services.getService(SchedulerService.class);
        if (schedulerService != null) {
            for (RunnableSchedule schedule : schedulerService.getSchedules()) {
                writer.startNode("schedule"); //$NON-NLS-1$
                writeNode(writer, "name", schedule.getCaption()); //$NON-NLS-1$
                writeNode(writer, "runnable", schedule.getClass().getName()); //$NON-NLS-1$
                writeNode(writer, "runAt", schedule.getSchedule()); //$NON-NLS-1$
                long lastStarted = schedule.getLastStarted();
                if (lastStarted > 0) {
                    writeNode(writer, "lastStarted", FormatUtils.formatUTCWithMillis(lastStarted)); //$NON-NLS-1$
                }
                long lastCompleted = schedule.getLastCompleted();
                if (lastCompleted > 0) {
                    writeNode(writer, "lastCompleted", FormatUtils.formatUTCWithMillis(lastCompleted)); //$NON-NLS-1$
                }
                writer.endNode();
            }
        }
        writer.endNode();
        writer.startNode("bundles"); //$NON-NLS-1$
        for (Bundle bundle : Services.getBundles()) {
            writer.startNode("bundle"); //$NON-NLS-1$
            writeNode(writer, "name", bundle.getSymbolicName()); //$NON-NLS-1$
            writeNode(writer, "version", bundle.getVersion().toString()); //$NON-NLS-1$
            writeNode(writer, "state", getBundleState(bundle.getState())); //$NON-NLS-1$
            writer.endNode();
        }
        writer.endNode();
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader arg0, UnmarshallingContext arg1) {
        // don't support that yet
        return null;
    }

    @Override
    public String getApiVersion() {
        return API_VERSION;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public String getXsdFileName() {
        return "admin-status.xsd"; //$NON-NLS-1$
    }
}
