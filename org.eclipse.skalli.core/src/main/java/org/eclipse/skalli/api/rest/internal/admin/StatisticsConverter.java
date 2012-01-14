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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.eclipse.skalli.commons.Statistics;
import org.eclipse.skalli.services.extension.rest.RestConverterBase;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

class StatisticsConverter extends RestConverterBase<Statistics> {

    public static final String API_VERSION = "1.0"; //$NON-NLS-1$
    public static final String NAMESPACE = "http://www.eclipse.org/skalli/2010/API/Admin"; //$NON-NLS-1$

    public StatisticsConverter(String host) {
        super(Statistics.class, "statistics", host); //$NON-NLS-1$
    }

    private static SimpleDateFormat SDF = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss"); //$NON-NLS-1$

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        Statistics statistics = (Statistics) source;
        marshalNSAttributes(writer);
        marshalApiVersion(writer);
        writer.startNode("info"); //$NON-NLS-1$
        writeNode(writer, "started", SDF.format(new Date(statistics.getStartTimestamp()))); //$NON-NLS-1$
        writeNode(writer, "snapshot", SDF.format(new Date())); //$NON-NLS-1$
        writeNode(writer, "duration", DurationFormatUtils.formatPeriod(statistics.getStartTimestamp(), //$NON-NLS-1$
                new Date().getTime(), "dd 'days' HH:mm:ss")); //$NON-NLS-1$
        writer.endNode();

        writer.startNode("users"); //$NON-NLS-1$
        writeNode(writer, "count", statistics.getUniqueUserCount()); //$NON-NLS-1$
        writer.startNode("userIds"); //$NON-NLS-1$
        for (String user : statistics.getUsers()) {
            writeNode(writer, "user", user); //$NON-NLS-1$
        }
        writer.endNode();
        writer.startNode("locations"); //$NON-NLS-1$
        for (Entry<String, Integer> entry : statistics.getLocations().entrySet()) {
            writer.startNode("location"); //$NON-NLS-1$
            writer.addAttribute("count", entry.getValue().toString()); //$NON-NLS-1$
            writer.setValue(entry.getKey() != null ? entry.getKey() : ""); //$NON-NLS-1$
            writer.endNode();
        }
        writer.endNode();
        writer.startNode("departments"); //$NON-NLS-1$
        for (Entry<String, Integer> entry : statistics.getDepartments().entrySet()) {
            writer.startNode("department"); //$NON-NLS-1$
            writer.addAttribute("count", entry.getValue().toString()); //$NON-NLS-1$
            writer.setValue(entry.getKey() != null ? entry.getKey() : ""); //$NON-NLS-1$
            writer.endNode();
        }
        writer.endNode();
        writer.endNode();

        writer.startNode("browsers"); //$NON-NLS-1$
        for (Entry<String, Long> entry : statistics.getBrowserCount().entrySet()) {
            writer.startNode("browser"); //$NON-NLS-1$
            writer.addAttribute("count", entry.getValue().toString()); //$NON-NLS-1$
            writer.setValue(entry.getKey() != null ? entry.getKey() : ""); //$NON-NLS-1$
            writer.endNode();
        }
        writer.endNode();

        writer.startNode("referers"); //$NON-NLS-1$
        for (Entry<String, Long> entry : statistics.getRefererCount().entrySet()) {
            writer.startNode("referer"); //$NON-NLS-1$
            writer.addAttribute("count", entry.getValue().toString()); //$NON-NLS-1$
            writer.setValue((entry.getKey() != null) ? entry.getKey() : ""); //$NON-NLS-1$
            writer.endNode();
        }
        writer.endNode();

        writer.startNode("usages"); //$NON-NLS-1$
        for (Entry<String, Long> entry : statistics.getHits().entrySet()) {
            writer.startNode("usage"); //$NON-NLS-1$
            writer.addAttribute("count", entry.getValue().toString()); //$NON-NLS-1$
            writer.setValue(entry.getKey() != null ? entry.getKey() : ""); //$NON-NLS-1$
            writer.endNode();
        }
        writer.endNode();

        writer.startNode("searches"); //$NON-NLS-1$
        for (Entry<String, Long> entry : statistics.getSearches().entrySet()) {
            writer.startNode("search"); //$NON-NLS-1$
            writer.addAttribute("count", entry.getValue().toString()); //$NON-NLS-1$
            writer.setValue(entry.getKey() != null ? entry.getKey() : ""); //$NON-NLS-1$
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
        return "admin-statistics.xsd"; //$NON-NLS-1$
    }
}
