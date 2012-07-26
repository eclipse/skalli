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

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.eclipse.skalli.commons.FormatUtils;
import org.eclipse.skalli.commons.Statistics;
import org.eclipse.skalli.commons.Statistics.SearchInfo;
import org.eclipse.skalli.commons.Statistics.StatisticsInfo;
import org.eclipse.skalli.commons.Statistics.UsageInfo;
import org.eclipse.skalli.services.extension.rest.RestConverterBase;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

class StatisticsConverter extends RestConverterBase<Statistics> {

    public static final String API_VERSION = "1.0"; //$NON-NLS-1$
    public static final String NAMESPACE = "http://www.eclipse.org/skalli/2010/API/Admin"; //$NON-NLS-1$

    private final long startDate;
    private final long endDate;

    public StatisticsConverter(String host, long startDate, long endDate) {
        super(Statistics.class, "statistics", host); //$NON-NLS-1$
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        Statistics statistics = (Statistics) source;
        marshalNSAttributes(writer);
        marshalApiVersion(writer);
        writer.startNode("info"); //$NON-NLS-1$
        writeNode(writer, "instance-start", FormatUtils.formatUTCWithMillis(statistics.getStartTimestamp())); //$NON-NLS-1$
        writeNode(writer, "instance-uptime",  DurationFormatUtils.formatPeriodISO( //$NON-NLS-1$
                statistics.getStartTimestamp(), System.currentTimeMillis()));
        writeNode(writer, "from", FormatUtils.formatUTCWithMillis(startDate > 0? startDate : statistics.getStartTimestamp())); //$NON-NLS-1$
        writeNode(writer, "to", FormatUtils.formatUTCWithMillis(endDate > 0 ? endDate : System.currentTimeMillis())); //$NON-NLS-1$
        writeNode(writer, "period", DurationFormatUtils.formatPeriodISO( //$NON-NLS-1$
                (startDate > 0? startDate : statistics.getStartTimestamp()),
                (endDate > 0? endDate : System.currentTimeMillis())));
        writer.endNode();

        writer.startNode("users"); //$NON-NLS-1$
        Map<String,Integer> users = statistics.getUserCount(startDate, endDate);
        appendUniqueCount(writer, users);
        appendTotalCount(writer, users);
        writeEntries(writer, "user", users); //$NON-NLS-1$
        writer.endNode();

        writer.startNode("usages"); //$NON-NLS-1$
        Map<String,Integer> usages = statistics.getUsageCount(startDate, endDate);
        appendUniqueCount(writer, usages);
        appendTotalCount(writer, usages);
        writeEntries(writer, "usage", usages); //$NON-NLS-1$
        writer.endNode();

        writer.startNode("searches"); //$NON-NLS-1$
        Map<String,Integer> searches = statistics.getSearchCount(startDate, endDate);
        appendUniqueCount(writer, searches);
        appendTotalCount(writer, searches);
        writeEntries(writer, "search", searches); //$NON-NLS-1$
        writer.endNode();

        writer.startNode("locations"); //$NON-NLS-1$
        Map<String,Integer> locations = statistics.getLocationCount(startDate, endDate);
        appendUniqueCount(writer, locations);
        appendTotalCount(writer, locations);
        writeEntries(writer, "location", locations); //$NON-NLS-1$
        writer.endNode();

        writer.startNode("departments"); //$NON-NLS-1$
        Map<String,Integer> departments = statistics.getDepartmentCount(startDate, endDate);
        appendUniqueCount(writer, departments);
        appendTotalCount(writer, departments);
        writeEntries(writer, "department", departments); //$NON-NLS-1$
        writer.endNode();

        writer.startNode("browsers"); //$NON-NLS-1$
        Map<String,Integer> browsers = statistics.getBrowserCount(startDate, endDate);
        appendUniqueCount(writer, browsers);
        appendTotalCount(writer, browsers);
        writeEntries(writer, "browser", browsers); //$NON-NLS-1$
        writer.endNode();

        writer.startNode("referers"); //$NON-NLS-1$
        Map<String,Integer> referers = statistics.getRefererCount(startDate, endDate);
        appendUniqueCount(writer, referers);
        appendTotalCount(writer, referers);
        writeEntries(writer, "referer", referers); //$NON-NLS-1$
        writer.endNode();

        writer.startNode("tracks"); //$NON-NLS-1$
        Map<String, SortedSet<StatisticsInfo>> tracks = statistics.getUsageTracks(startDate, endDate);
        for (Entry<String, SortedSet<StatisticsInfo>> entries: tracks.entrySet()) {
            writer.startNode("track");
            SortedSet<StatisticsInfo> track = entries.getValue();
            writeNode(writer, "user", entries.getKey());
            for (StatisticsInfo info: track) {
                writeEntry(writer, info);
            }
            writer.endNode();
        }
        writer.endNode();
    }

    private void writeEntries(HierarchicalStreamWriter writer, String nodeName, Map<String,Integer> entries) {
        for (Entry<String, Integer> entry : entries.entrySet()) {
            writer.startNode(nodeName);
            writer.addAttribute("count", entry.getValue().toString()); //$NON-NLS-1$
            writer.setValue(entry.getKey() != null ? entry.getKey() : ""); //$NON-NLS-1$
            writer.endNode();
        }
    }

    private void writeEntry(HierarchicalStreamWriter writer, StatisticsInfo info) {
        if (info instanceof UsageInfo) {
            writer.startNode("request");
            writer.addAttribute("timestamp", FormatUtils.formatUTCWithMillis(info.getTimestamp())); //$NON-NLS-1$
            writeNode(writer, "path", (((UsageInfo)info).getPath()));
            writeNode(writer, "referer", (((UsageInfo)info).getReferer()));
            writer.endNode();
        } else if (info instanceof SearchInfo) {
            writer.startNode("search");
            writer.addAttribute("timestamp", FormatUtils.formatUTCWithMillis(info.getTimestamp())); //$NON-NLS-1$
            writeNode(writer, "queryString", ((SearchInfo)info).getQueryString());
            writeNode(writer, "resultCount", ((SearchInfo)info).getResultCount());
            writeNode(writer, "duration", ((SearchInfo)info).getDuration());
            writer.endNode();
        }
    }

    private void appendUniqueCount(HierarchicalStreamWriter writer, Map<String,Integer> entries) {
        writer.addAttribute("uniqueCount", Integer.toString(entries.size())); //$NON-NLS-1$
    }

    private void appendTotalCount(HierarchicalStreamWriter writer, Map<String,Integer> entries) {
        Iterator<Integer> it = entries.values().iterator();
        int count = 0;
        for (; it.hasNext(); count += it.next()) {
        }
        writer.addAttribute("totalCount", Integer.toString(count)); //$NON-NLS-1$
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
