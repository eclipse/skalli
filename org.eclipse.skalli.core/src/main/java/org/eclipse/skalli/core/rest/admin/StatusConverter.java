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
package org.eclipse.skalli.core.rest.admin;

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
                writeNode(writer, "runnable", schedule.getRunnable().getClass().getName()); //$NON-NLS-1$
                writeNode(writer, "runAt", schedule.getSchedule()); //$NON-NLS-1$
                long lastRun = schedule.getLastRun();
                if (lastRun > 0) {
                    writeNode(writer, "lastRun", FormatUtils.formatUTCWithMillis(lastRun)); //$NON-NLS-1$
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
