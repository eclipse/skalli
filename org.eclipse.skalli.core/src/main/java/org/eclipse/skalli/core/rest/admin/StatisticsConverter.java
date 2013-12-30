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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.eclipse.skalli.commons.FormatUtils;
import org.eclipse.skalli.commons.Statistics;
import org.eclipse.skalli.commons.Statistics.SearchInfo;
import org.eclipse.skalli.commons.Statistics.StatisticsInfo;
import org.eclipse.skalli.commons.Statistics.UsageInfo;
import org.eclipse.skalli.commons.Statistics.UserInfo;
import org.eclipse.skalli.model.Member;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.extension.rest.RestConverterBase;
import org.eclipse.skalli.services.extension.rest.RestUtils;
import org.eclipse.skalli.services.project.ProjectService;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

class StatisticsConverter extends RestConverterBase<Statistics> {

    public static final String API_VERSION = "1.0"; //$NON-NLS-1$
    public static final String NAMESPACE = "http://www.eclipse.org/skalli/2010/API/Admin"; //$NON-NLS-1$

    private final StatisticsQuery query;

    public StatisticsConverter(StatisticsQuery query) {
        super(Statistics.class);
        this.query = query;
    }

    @SuppressWarnings("nls")
    @Override
    protected void marshal(Statistics statistics) throws IOException {
        long from = Math.max(statistics.getStartDate(),
                query.getFrom() > 0? query.getFrom() : statistics.getStartupTime());
        long to = query.getTo() > 0 ? query.getTo() : System.currentTimeMillis();

        namespaces();
        apiVersion();
        writer
        .pair("instance-start", FormatUtils.formatUTCWithMillis(statistics.getStartupTime()))
        .pair("instance-uptime",  DurationFormatUtils.formatPeriodISO( //$NON-NLS-1$
                statistics.getStartupTime(), System.currentTimeMillis()))
        .pair("from", FormatUtils.formatUTCWithMillis(from))
        .pair("to", FormatUtils.formatUTCWithMillis(to))
        .pair("period", DurationFormatUtils.formatPeriodISO(from, to));

        ProjectService projectService = Services.getRequiredService(ProjectService.class);
        List<UUID> uuids = new ArrayList<UUID>(projectService.keySet());

        if (query.showSection("projects")) {
            marshalProjectsSection(projectService, uuids, from, to);
        }
        if (query.showSection("members")) {
            marshalMembersSection(projectService, uuids);
        }
        if (query.showSection("users")) {
            marshalUsersSection(statistics, from, to);
        }
        if (query.showSection("departments")) {
            marshalDepartmentsSection(statistics, from, to);
        }
        if (query.showSection("locations")) {
            marshalLocationsSection(statistics, from, to);
        }
        if (query.showSection("searches")) {
            marshalSearchesSection(statistics, from, to);
        }
        if (query.showSection("browsers")) {
            marshalBrowsersSection(statistics, from, to);
        }
        if (query.showSection("referers")) {
            marshalReferersSection(statistics, from, to);
        }
        if (query.showSection("requests")) {
            marshalRequestsSection(statistics, from, to);
        }
        if (query.showSection("tracks")) {
            marshalTracksSection(statistics, from, to);
        }
    }

    @SuppressWarnings("nls")
    private void marshalProjectsSection(ProjectService projectService,
            List<UUID> uuids, long from, long to) throws IOException {
        writer.object("projects");
        List<Project> existing = new ArrayList<Project>();
        List<Project> newProjects = new ArrayList<Project>();
        List<Project> modifiedProjects = new ArrayList<Project>();
        for (UUID uuid: uuids) {
            Project project = projectService.getByUUID(uuid);
            if (project != null) {
                if (isBefore(project.getRegistered(), to)) {
                    existing.add(project);
                }
                if (isInRange(project.getRegistered(), from, to)) {
                    newProjects.add(project);
                } else if (isInRange(project.getLastModifiedMillis(), from, to)) {
                    modifiedProjects.add(project);
                }
            }
        }
        writer.pair("totalCount", existing.size());
        writer.object("created");
        writer.pair("totalCount", newProjects.size());
        if (query.showByFilter("byDate")) {
            writer.array("byDate", "project");
            for (Project newProject: newProjects) {
                writeProjectInfo(newProject, newProject.getRegistered());
            }
            writer.end();
        }
        writer.end();
        writer.object("modified");
        writer.pair("totalCount", modifiedProjects.size());
        if (query.showByFilter("byDate")) {
            writer.array("byDate", "project");
            for (Project modifiedProject: modifiedProjects) {
                writeProjectInfo(modifiedProject, modifiedProject.getLastModifiedMillis());
            }
            writer.end();
        }
        writer.end();
        writer.end();
    }

    @SuppressWarnings("nls")
    private void marshalMembersSection(ProjectService projectService, List<UUID> uuids) throws IOException {
        writer.object("members");
        Map<String, SortedSet<Member>> uniqueMembers = collectUniqueMembers(projectService, uuids);
        writer.pair("totalCount", countUniqueMembers(uniqueMembers));
        if (query.showByFilter("byRole")) {
            writer.object("byRole");
            for (Entry<String, SortedSet<Member>> entry: uniqueMembers.entrySet()) {
                writer.pair(entry.getKey(), entry.getValue().size());
            }
            writer.end();
        }
        writer.end();
    }

    @SuppressWarnings("nls")
    private void marshalUsersSection(Statistics statistics, long from, long to) throws IOException {
        writer.object("users");
        Map<String,Long> users = statistics.getUserCount(from, to);
        writeUniqueCount(users);
        writeTotalCount(users);
        if (query.showByFilter("byCount")) {
            writer.array("byCount", "user");
            writeCounts(users);
            writer.end();
        }
        if (query.showByFilter("byDate")) {
            writer.key("byDate").array();
            SortedSet<Statistics.UserInfo> userInfos = statistics.getUserInfo();
            for (Statistics.UserInfo userInfo: userInfos) {
                writeInfoEntry(userInfo);
            }
            writer.end();
        }
        writer.end();
    }

    @SuppressWarnings("nls")
    private void marshalDepartmentsSection(Statistics statistics, long from, long to) throws IOException {
        writer.object("departments");
        Map<String,Long> departments = statistics.getDepartmentCount(from, to);
        writeUniqueCount(departments);
        writeTotalCount(departments);
        if (query.showByFilter("byCount")) {
            writer.array("byCount", "department");
            writeCounts(departments);
            writer.end();
        }
        writer.end();
    }

    @SuppressWarnings("nls")
    private void marshalLocationsSection(Statistics statistics, long from, long to) throws IOException {
        writer.object("locations");
        Map<String,Long> locations = statistics.getLocationCount(from, to);
        writeUniqueCount(locations);
        writeTotalCount(locations);
        if (query.showByFilter("byCount")) {
            writer.array("byCount", "location");
            writeCounts(locations);
            writer.end();
        }
        writer.end();
    }

    @SuppressWarnings("nls")
    private void marshalSearchesSection(Statistics statistics, long from, long to) throws IOException {
        writer.object("searches");
        Map<String,Long> searches = statistics.getSearchCount(from, to);
        writeUniqueCount( searches);
        writeTotalCount(searches);
        if (query.showByFilter("byCount")) {
            writer.array("byCount", "search");
            writeCounts(searches);
            writer.end();
        }
        if (query.showByFilter("byDate")) {
            writer.key("byDate").array();
            SortedSet<Statistics.SearchInfo> searchInfos = statistics.getSearchInfo();
            for (Statistics.SearchInfo searchInfo: searchInfos) {
                writeInfoEntry(searchInfo);
            }
            writer.end();
        }
        writer.end();
    }

    @SuppressWarnings("nls")
    private void marshalBrowsersSection(Statistics statistics, long from, long to) throws IOException {
        writer.object("browsers");
        Map<String,Long> browsers = statistics.getBrowserCount(from, to);
        writeUniqueCount(browsers);
        writeTotalCount(browsers);
        if (query.showByFilter("byCount")) {
            writer.array("byCount", "browser");
            writeCounts(browsers);
            writer.end();
        }
        writer.end();
    }

    @SuppressWarnings("nls")
    private void marshalReferersSection(Statistics statistics, long from, long to) throws IOException {
        writer.object("referers");
        Map<String,Long> referers = statistics.getRefererCount(from, to);
        writeUniqueCount(referers);
        writeTotalCount(referers);
        if (query.showByFilter("byCount")) {
            writer.array("byCount", "referer");
            writeCounts(referers);
            writer.end();
        }
        writer.end();
    }

    @SuppressWarnings("nls")
    private void marshalRequestsSection(Statistics statistics, long from, long to) throws IOException {
        Map<String,Long> avgResponseTimes = statistics.getAverageResponseTimes(from, to);
        writer.object("requests");
        Map<String,Long> usages = statistics.getUsageCount(from, to);
        writeUniqueCount(usages);
        writeTotalCount(usages);
        if (query.showByFilter("byCount")) {
            writer.array("byCount", "request");
            writeRequestCounts(usages, avgResponseTimes);
            writer.end();
        }
        if (query.showByFilter("byAvgResponseTime")) {
            writer.array("byAvgResponseTime", "request");
            writeAverageResponseTimes(avgResponseTimes);
            writer.end();
        }
        if (query.showByFilter("byDate")) {
            writer.key("byDate").array();
            for (UsageInfo usageInfo: statistics.getUsageInfo()) {
                writeInfoEntry(usageInfo);
            }
            writer.end();
        }
        writer.end();
    }

    @SuppressWarnings("nls")
    private void marshalTracksSection(Statistics statistics, long from, long to) throws IOException {
        writer.array("tracks", "track");
        Map<String, SortedSet<StatisticsInfo>> tracks = statistics.getUsageTracks(from, to);
        for (Entry<String, SortedSet<StatisticsInfo>> entries: tracks.entrySet()) {
            writer.object();
            SortedSet<StatisticsInfo> track = entries.getValue();
            writer.pair("user", entries.getKey());
            writer.array();
            for (StatisticsInfo info: track) {
                writeInfoEntry(info);
            }
            writer.end();
            writer.end();
        }
        writer.end();
    }

    @SuppressWarnings("nls")
    void writeCounts(Map<String,Long> entries) throws IOException {
        for (Entry<String, Long> entry : entries.entrySet()) {
            writer
            .object()
              .attribute("count", entry.getValue())
              .value(entry.getKey())
            .end();
        }
    }

    @SuppressWarnings("nls")
    void writeRequestCounts(Map<String,Long> requests, Map<String,Long> avgResponseTimes) throws IOException {
        for (Entry<String, Long> request : requests.entrySet()) {
            Long avgResponseTime = avgResponseTimes.get(request.getKey());
            writer.object();
                writer.attribute("count", request.getValue());
                if (avgResponseTime != null) {
                    writer.attribute("avgResponseTime", avgResponseTime);
                }
                writer.value(request.getKey());
            writer.end();
        }
    }

    @SuppressWarnings("nls")
    void writeAverageResponseTimes(Map<String,Long> avgResponseTimes) throws IOException {
        for (Entry<String, Long> entry : avgResponseTimes.entrySet()) {
            writer
            .object()
              .attribute("avgResponseTime", entry.getValue())
              .value(entry.getKey())
            .end();
        }
    }

    @SuppressWarnings("nls")
    void writeProjectInfo(Project project, long timestamp) throws IOException {
        writer
        .object()
          .attribute("date", FormatUtils.formatUTCWithMillis(timestamp))
          .attribute("timestamp", timestamp)
          .pair("uuid", project.getUuid())
          .pair("id", project.getProjectId())
          .pair("name", project.getName())
          .link(PROJECT_RELATION, RestUtils.URL_PROJECTS, project.getUuid())
          .link(BROWSE_RELATION, RestUtils.URL_BROWSE, project.getProjectId())
        .end();
    }

    @SuppressWarnings("nls")
    void writeInfoEntry(StatisticsInfo info) throws IOException {
        if (info instanceof UsageInfo) {
            UsageInfo usageInfo = (UsageInfo)info;
            writer
            .item("request")
            .object()
                .attribute("type", "usage")
                .attribute("date", FormatUtils.formatUTCWithMillis(usageInfo.getTimestamp()))
                .attribute("timestamp", usageInfo.getTimestamp())
                .attribute("user", usageInfo.getUserHash());
                if (usageInfo.getReferer() != null) {
                    writer.attribute("referer", usageInfo.getReferer());
                }
                writer.value(usageInfo.getPath())
            .end();
        } else if (info instanceof SearchInfo) {
            SearchInfo searchInfo = (SearchInfo)info;
            writer
            .item("search")
            .object()
              .attribute("type", "search")
              .attribute("date", FormatUtils.formatUTCWithMillis(searchInfo.getTimestamp()))
              .attribute("timestamp", searchInfo.getTimestamp())
              .attribute("user", searchInfo.getUserHash())
              .attribute("resultCount", searchInfo.getResultCount())
              .attribute("duration", searchInfo.getDuration())
              .value(searchInfo.getQueryString())
            .end();
        } else if (info instanceof UserInfo) {
            UserInfo userInfo = (UserInfo)info;
            writer
            .item("user")
            .object()
              .attribute("type", "user")
              .attribute("date", FormatUtils.formatUTCWithMillis(userInfo.getTimestamp()))
              .attribute("timestamp", userInfo.getTimestamp());
              if (userInfo.getDepartment() != null) {
                  writer.attribute("department", userInfo.getDepartment());
              }
              if (userInfo.getLocation() != null) {
                  writer.attribute("location", userInfo.getLocation());
              }
              writer.value(userInfo.getUserHash())
            .end();
        }
    }

    @SuppressWarnings("nls")
    void writeUniqueCount(Map<String,Long> entries) throws IOException {
        writer.attribute("uniqueCount", entries.size());
    }

    @SuppressWarnings("nls")
    void writeTotalCount(Map<String,Long> entries) throws IOException {
        Iterator<Long> it = entries.values().iterator();
        int count = 0;
        for (; it.hasNext(); count += it.next()) {
        }
        writer.attribute("totalCount", count);
    }

    @Deprecated
    public StatisticsConverter(String host, StatisticsQuery query) {
        super(Statistics.class, "statistics", host); //$NON-NLS-1$
        this.query = query;
    }

    @Deprecated
    @SuppressWarnings("nls")
    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        Statistics statistics = (Statistics) source;

        marshalNSAttributes(writer);
        marshalApiVersion(writer);

        writeNode(writer, "instance-start", FormatUtils.formatUTCWithMillis(statistics.getStartupTime()));
        writeNode(writer, "instance-uptime",  DurationFormatUtils.formatPeriodISO( //$NON-NLS-1$
                statistics.getStartupTime(), System.currentTimeMillis()));
        long from = Math.max(statistics.getStartDate(),
                query.getFrom() > 0? query.getFrom() : statistics.getStartupTime());
        long to = query.getTo() > 0 ? query.getTo() : System.currentTimeMillis();
        writeNode(writer, "from", FormatUtils.formatUTCWithMillis(from));
        writeNode(writer, "to", FormatUtils.formatUTCWithMillis(to));
        writeNode(writer, "period", DurationFormatUtils.formatPeriodISO(from, to));

        ProjectService projectService = Services.getRequiredService(ProjectService.class);
        List<UUID> uuids = new ArrayList<UUID>(projectService.keySet());

        if (query.showSection("projects")) {
            marshalProjectsSection(writer, projectService, uuids, from, to);
        }
        if (query.showSection("members")) {
            marshalMembersSection(writer, projectService, uuids);
        }
        if (query.showSection("users")) {
            marshalUsersSection(writer, statistics, from, to);
        }
        if (query.showSection("departments")) {
            marshalDepartmentsSection(writer, statistics, from, to);
        }
        if (query.showSection("locations")) {
            marshalLocationsSection(writer, statistics, from, to);
        }
        if (query.showSection("searches")) {
            marshalSearchesSection(writer, statistics, from, to);
        }
        if (query.showSection("browsers")) {
            marshalBrowsersSection(writer, statistics, from, to);
        }
        if (query.showSection("referers")) {
            marshalReferersSection(writer, statistics, from, to);
        }
        if (query.showSection("requests")) {
            marshalRequestsSection(writer, statistics, from, to);
        }
        if (query.showSection("tracks")) {
            marshalTracksSection(writer, statistics, from, to);
        }
    }

    @Deprecated
    @SuppressWarnings("nls")
    private void marshalProjectsSection(HierarchicalStreamWriter writer, ProjectService projectService,
            List<UUID> uuids, long from, long to) {
        writer.startNode("projects");
        List<Project> existing = new ArrayList<Project>();
        List<Project> newProjects = new ArrayList<Project>();
        List<Project> modifiedProjects = new ArrayList<Project>();
        for (UUID uuid: uuids) {
            Project project = projectService.getByUUID(uuid);
            if (project != null) {
                if (isBefore(project.getRegistered(), to)) {
                    existing.add(project);
                }
                if (isInRange(project.getRegistered(), from, to)) {
                    newProjects.add(project);
                } else if (isInRange(project.getLastModifiedMillis(), from, to)) {
                    modifiedProjects.add(project);
                }
            }
        }
        writeNode(writer, "totalCount", existing.size());
        writer.startNode("created");
        writeNode(writer, "totalCount", newProjects.size());
        if (query.showByFilter("byDate")) {
            writer.startNode("byDate");
            for (Project newProject: newProjects) {
                writeProjectInfo(writer, newProject, newProject.getRegistered());
            }
            writer.endNode();
        }
        writer.endNode();
        writer.startNode("modified");
        writeNode(writer, "totalCount", modifiedProjects.size());
        if (query.showByFilter("byDate")) {
            writer.startNode("byDate");
            for (Project modifiedProject: modifiedProjects) {
                writeProjectInfo(writer, modifiedProject, modifiedProject.getLastModifiedMillis());
            }
            writer.endNode();
        }
        writer.endNode();
        writer.endNode();
    }

    @Deprecated
    @SuppressWarnings("nls")
    private void marshalMembersSection(HierarchicalStreamWriter writer, ProjectService projectService,
            List<UUID> uuids) {
        writer.startNode("members");
        Map<String, SortedSet<Member>> uniqueMembers = collectUniqueMembers(projectService, uuids);
        writeNode(writer, "totalCount", countUniqueMembers(uniqueMembers));
        if (query.showByFilter("byRole")) {
            writer.startNode("byRole");
            for (Entry<String, SortedSet<Member>> entry: uniqueMembers.entrySet()) {
                writeNode(writer, entry.getKey(), entry.getValue().size());
            }
            writer.endNode();
        }
        writer.endNode();
    }

    private Map<String, SortedSet<Member>> collectUniqueMembers(ProjectService projectService, List<UUID> uuids) {
        Map<String, SortedSet<Member>> uniqueMembers = new HashMap<String, SortedSet<Member>>();
        for (UUID uuid: uuids) {
            Map<String, SortedSet<Member>> membersByRole = projectService.getMembersByRole(uuid);
            for (Entry<String,SortedSet<Member>> entry: membersByRole.entrySet()) {
                SortedSet<Member> members = uniqueMembers.get(entry.getKey());
                if (members == null) {
                    members = new TreeSet<Member>();
                    uniqueMembers.put(entry.getKey(), members);
                }
                members.addAll(entry.getValue());
            }
        }
        return uniqueMembers;
    }

    private int countUniqueMembers(Map<String, SortedSet<Member>> uniqueMembers) {
        int count = 0;
        for (SortedSet<Member> members: uniqueMembers.values()) {
            count += members.size();
        }
        return count;
    }

    @Deprecated
    @SuppressWarnings("nls")
    private void marshalUsersSection(HierarchicalStreamWriter writer, Statistics statistics, long from, long to) {
        writer.startNode("users");
        Map<String,Long> users = statistics.getUserCount(from, to);
        writeUniqueCount(writer, users);
        writeTotalCount(writer, users);
        if (query.showByFilter("byCount")) {
            writer.startNode("byCount");
            writeCounts(writer, "user", users);
            writer.endNode();
        }
        if (query.showByFilter("byDate")) {
            writer.startNode("byDate");
            SortedSet<Statistics.UserInfo> userInfos = statistics.getUserInfo();
            for (Statistics.UserInfo userInfo: userInfos) {
                writeInfoEntry(writer, userInfo);
            }
            writer.endNode();
        }
        writer.endNode();
    }

    @Deprecated
    @SuppressWarnings("nls")
    private void marshalDepartmentsSection(HierarchicalStreamWriter writer, Statistics statistics, long from, long to) {
        writer.startNode("departments");
        Map<String,Long> departments = statistics.getDepartmentCount(from, to);
        writeUniqueCount(writer, departments);
        writeTotalCount(writer, departments);
        if (query.showByFilter("byCount")) {
            writer.startNode("byCount");
            writeCounts(writer, "department", departments);
            writer.endNode();
        }
        writer.endNode();
    }

    @Deprecated
    @SuppressWarnings("nls")
    private void marshalLocationsSection(HierarchicalStreamWriter writer, Statistics statistics, long from, long to) {
        writer.startNode("locations");
        Map<String,Long> locations = statistics.getLocationCount(from, to);
        writeUniqueCount(writer, locations);
        writeTotalCount(writer, locations);
        if (query.showByFilter("byCount")) {
            writer.startNode("byCount");
            writeCounts(writer, "location", locations);
            writer.endNode();
        }
        writer.endNode();
    }

    @Deprecated
    @SuppressWarnings("nls")
    private void marshalSearchesSection(HierarchicalStreamWriter writer, Statistics statistics, long from, long to) {
        writer.startNode("searches");
        Map<String,Long> searches = statistics.getSearchCount(from, to);
        writeUniqueCount(writer, searches);
        writeTotalCount(writer, searches);
        if (query.showByFilter("byCount")) {
            writer.startNode("byCount");
            writeCounts(writer, "search", searches);
            writer.endNode();
        }
        if (query.showByFilter("byDate")) {
            writer.startNode("byDate");
            SortedSet<Statistics.SearchInfo> searchInfos = statistics.getSearchInfo();
            for (Statistics.SearchInfo searchInfo: searchInfos) {
                writeInfoEntry(writer, searchInfo);
            }
            writer.endNode();
        }
        writer.endNode();
    }

    @Deprecated
    @SuppressWarnings("nls")
    private void marshalBrowsersSection(HierarchicalStreamWriter writer, Statistics statistics, long from, long to) {
        writer.startNode("browsers");
        Map<String,Long> browsers = statistics.getBrowserCount(from, to);
        writeUniqueCount(writer, browsers);
        writeTotalCount(writer, browsers);
        if (query.showByFilter("byCount")) {
            writer.startNode("byCount");
            writeCounts(writer, "browser", browsers);
            writer.endNode();
        }
        writer.endNode();
    }

    @Deprecated
    @SuppressWarnings("nls")
    private void marshalReferersSection(HierarchicalStreamWriter writer, Statistics statistics, long from, long to) {
        writer.startNode("referers");
        Map<String,Long> referers = statistics.getRefererCount(from, to);
        writeUniqueCount(writer, referers);
        writeTotalCount(writer, referers);
        if (query.showByFilter("byCount")) {
            writer.startNode("byCount");
            writeCounts(writer, "referer", referers);
            writer.endNode();
        }
        writer.endNode();
    }

    @Deprecated
    @SuppressWarnings("nls")
    private void marshalRequestsSection(HierarchicalStreamWriter writer, Statistics statistics, long from, long to) {
        Map<String,Long> avgResponseTimes = statistics.getAverageResponseTimes(from, to);
        writer.startNode("requests");
        Map<String,Long> usages = statistics.getUsageCount(from, to);
        writeUniqueCount(writer, usages);
        writeTotalCount(writer, usages);
        if (query.showByFilter("byCount")) {
            writer.startNode("byCount");
            writeRequestCounts(writer, usages, avgResponseTimes);
            writer.endNode();
        }
        if (query.showByFilter("byAvgResponseTime")) {
            writer.startNode("byAvgResponseTime");
            writeAverageResponseTimes(writer, avgResponseTimes);
            writer.endNode();
        }
        if (query.showByFilter("byDate")) {
            writer.startNode("byDate");
            writeRequestInfos(writer, statistics.getUsageInfo());
            writer.endNode();
        }
        writer.endNode();
    }

    @Deprecated
    @SuppressWarnings("nls")
    private void marshalTracksSection(HierarchicalStreamWriter writer, Statistics statistics, long from, long to) {
        writer.startNode("tracks");
        Map<String, SortedSet<StatisticsInfo>> tracks = statistics.getUsageTracks(from, to);
        for (Entry<String, SortedSet<StatisticsInfo>> entries: tracks.entrySet()) {
            writer.startNode("track");
            SortedSet<StatisticsInfo> track = entries.getValue();
            writeNode(writer, "user", entries.getKey());
            for (StatisticsInfo info: track) {
                writeInfoEntry(writer, info);
            }
            writer.endNode();
        }
        writer.endNode();
    }

    @Deprecated
    @SuppressWarnings("nls")
    void writeCounts(HierarchicalStreamWriter writer, String nodeName, Map<String,Long> entries) {
        for (Entry<String, Long> entry : entries.entrySet()) {
            writer.startNode(nodeName);
            writer.addAttribute("count", entry.getValue().toString());
            writeValue(writer, entry.getKey());
            writer.endNode();
        }
    }

    @Deprecated
    @SuppressWarnings("nls")
    void writeRequestCounts(HierarchicalStreamWriter writer,
            Map<String,Long> requests, Map<String,Long> avgResponseTimes) {
        for (Entry<String, Long> request : requests.entrySet()) {
            writer.startNode("request");
            writer.addAttribute("count", request.getValue().toString());
            Long avgResponseTime = avgResponseTimes.get(request.getKey());
            if (avgResponseTime != null) {
                writer.addAttribute("avgResponseTime", Long.toString(avgResponseTime));
            }
            writeValue(writer, request.getKey());
            writer.endNode();
        }
    }

    @Deprecated
    @SuppressWarnings("nls")
    void writeAverageResponseTimes(HierarchicalStreamWriter writer, Map<String,Long> avgResponseTimes) {
        for (Entry<String, Long> entry : avgResponseTimes.entrySet()) {
            writer.startNode("request");
            writer.addAttribute("avgResponseTime", Long.toString(entry.getValue()) );
            writeValue(writer, entry.getKey());
            writer.endNode();
        }
    }

    @Deprecated
    void writeRequestInfos(HierarchicalStreamWriter writer, SortedSet<UsageInfo> usageInfos) {
        for (UsageInfo usageInfo: usageInfos) {
            writeInfoEntry(writer, usageInfo);
        }
    }

    @Deprecated
    @SuppressWarnings("nls")
    void writeProjectInfo(HierarchicalStreamWriter writer, Project project, long timestamp) {
        writer.startNode("project");
        writer.addAttribute("date", FormatUtils.formatUTCWithMillis(timestamp));
        writer.addAttribute("timestamp", Long.toString(timestamp));
        UUID uuid = project.getUuid();
        writeNode(writer, "uuid", uuid.toString()); //$NON-NLS-1$
        writeNode(writer, "id", project.getProjectId()); //$NON-NLS-1$
        writeNode(writer, "name", project.getName()); //$NON-NLS-1$
        writeProjectLink(writer, PROJECT_RELATION, uuid);
        writeLink(writer, BROWSE_RELATION, getHost() + RestUtils.URL_BROWSE + project.getProjectId());
        writer.endNode();
    }

    @Deprecated
    @SuppressWarnings("nls")
    void writeInfoEntry(HierarchicalStreamWriter writer, StatisticsInfo info) {
        if (info instanceof UsageInfo) {
            UsageInfo usageInfo = (UsageInfo)info;
            writer.startNode("request");
            writer.addAttribute("date", FormatUtils.formatUTCWithMillis(usageInfo.getTimestamp()));
            writer.addAttribute("timestamp", Long.toString(usageInfo.getTimestamp()));
            writer.addAttribute("user", usageInfo.getUserHash());
            if (usageInfo.getReferer() != null) {
                writer.addAttribute("referer", usageInfo.getReferer());
            }
            writeValue(writer, usageInfo.getPath());
            writer.endNode();
        } else if (info instanceof SearchInfo) {
            SearchInfo searchInfo = (SearchInfo)info;
            writer.startNode("search");
            writer.addAttribute("date", FormatUtils.formatUTCWithMillis(searchInfo.getTimestamp()));
            writer.addAttribute("timestamp", Long.toString(searchInfo.getTimestamp()));
            writer.addAttribute("user", searchInfo.getUserHash());
            writer.addAttribute("resultCount", Integer.toString(searchInfo.getResultCount()));
            writer.addAttribute("duration", Long.toString(searchInfo.getDuration()));
            writeValue(writer, searchInfo.getQueryString());
            writer.endNode();
        } else if (info instanceof UserInfo) {
            UserInfo userInfo = (UserInfo)info;
            writer.startNode("user");
            writer.addAttribute("date", FormatUtils.formatUTCWithMillis(userInfo.getTimestamp()));
            writer.addAttribute("timestamp", Long.toString(userInfo.getTimestamp()));
            if (userInfo.getDepartment() != null) {
                writer.addAttribute("department", userInfo.getDepartment());
            }
            if (userInfo.getLocation() != null) {
                writer.addAttribute("location", userInfo.getLocation());
            }
            writeValue(writer, userInfo.getUserHash());
            writer.endNode();
        }
    }

    @Deprecated
    @SuppressWarnings("nls")
    void writeUniqueCount(HierarchicalStreamWriter writer, Map<String,Long> entries) {
        writer.addAttribute("uniqueCount", Integer.toString(entries.size()));
    }

    @Deprecated
    @SuppressWarnings("nls")
    void writeTotalCount(HierarchicalStreamWriter writer, Map<String,Long> entries) {
        Iterator<Long> it = entries.values().iterator();
        int count = 0;
        for (; it.hasNext(); count += it.next()) {
        }
        writer.addAttribute("totalCount", Integer.toString(count));
    }

    @Deprecated
    void writeValue(HierarchicalStreamWriter writer, String value) {
        writer.setValue(value != null? value : ""); //$NON-NLS-1$
    }

    private boolean isInRange(long timestamp, long from, long to) {
        return (from <= 0 || from <=  timestamp) && (to <= 0 || timestamp <= to);
    }

    private boolean isBefore(long timestamp, long to) {
        return to <= 0 || timestamp <= to;
    }


    @Override
    public Object unmarshal(HierarchicalStreamReader arg0, UnmarshallingContext arg1) {
        // not supported
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
