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

import static org.junit.Assert.assertEquals;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.skalli.commons.FormatUtils;
import org.eclipse.skalli.commons.Statistics.SearchInfo;
import org.eclipse.skalli.commons.Statistics.UsageInfo;
import org.eclipse.skalli.commons.Statistics.UserInfo;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.testutil.TestUUIDs;
import org.eclipse.skalli.testutil.StringBufferHierarchicalStreamWriter;
import org.junit.Before;
import org.junit.Test;

//@SuppressWarnings("nls")
public class StatisticsConverterTest {

    private StringBufferHierarchicalStreamWriter writer;
    private StatisticsConverter converter;

    @Before
    public void setup() throws Exception {
        writer = new StringBufferHierarchicalStreamWriter();
        converter = new StatisticsConverter("http://localhost", new StatisticsQuery(new HashMap<String,String>()));
    }

    @Test
    public void testWriteValue() throws Exception {
        writer.startNode("tag");
        converter.writeValue(writer, "foobar");
        writer.endNode();
        assertWriter(writer, "<tag>foobar</tag>");
    }

    @Test
    public void testWriteNullValue() throws Exception {
        writer.startNode("tag");
        converter.writeValue(writer, null);
        writer.endNode();
        assertWriter(writer, "<tag></tag>");
    }

    @Test
    public void testWriteTotalCount() throws Exception {
        writer.startNode("tag");
        Map<String,Long> entries = new HashMap<String,Long>();
        entries.put("a", 1L);
        entries.put("b", 2L);
        entries.put("c", 3L);
        converter.writeTotalCount(writer, entries);
        converter.writeValue(writer, null);
        writer.endNode();
        assertWriter(writer, "<tag totalCount=\"6\"></tag>");
    }

    @Test
    public void testWriteUniqueCount() throws Exception {
        writer.startNode("tag");
        Map<String,Long> entries = new HashMap<String,Long>();
        entries.put("a", 1L);
        entries.put("b", 2L);
        entries.put("c", 3L);
        converter.writeUniqueCount(writer, entries);
        converter.writeValue(writer, null);
        writer.endNode();
        assertWriter(writer, "<tag uniqueCount=\"3\"></tag>");
    }

    @Test
    public void testWriteUsageInfoEntry() throws Exception {
        UsageInfo info = new UsageInfo("hugo", "/projects", "/referer", 4711L);
        long timestamp = info.getTimestamp();
        converter.writeInfoEntry(writer, info);
        assertWriter(writer, MessageFormat.format(
                "<request date=\"{0}\" timestamp=\"{1}\" user=\"{2}\" referer=\"/referer\">/projects</request>",
                FormatUtils.formatUTCWithMillis(timestamp), Long.toString(timestamp), info.getUserHash()));
    }

    @Test
    public void testWriteSearchInfoEntry() throws Exception {
        SearchInfo info = new SearchInfo("hugo", "query", 42, 123L, 4711L);
        long timestamp = info.getTimestamp();
        converter.writeInfoEntry(writer, info);
        assertWriter(writer, MessageFormat.format(
                "<search date=\"{0}\" timestamp=\"{1}\" user=\"{2}\" resultCount=\"42\" duration=\"123\">query</search>",
                FormatUtils.formatUTCWithMillis(timestamp), Long.toString(timestamp), info.getUserHash()));
    }

    @Test
    public void testWriteRequestInfos() throws Exception {
        UsageInfo info1 = new UsageInfo("foo", "/project1", "/referer1", 1L);
        UsageInfo info2 = new UsageInfo("bar", "/project2", "/referer2", 2L);
        TreeSet<UsageInfo> set = new TreeSet<UsageInfo>();
        set.add(info1);
        set.add(info2);
        long timestamp1 = info1.getTimestamp();
        long timestamp2 = info2.getTimestamp();
        converter.writeRequestInfos(writer, set);
        assertWriter(writer, MessageFormat.format(
                "<request date=\"{0}\" timestamp=\"{1}\" user=\"{2}\" referer=\"/referer1\">/project1</request>\n" +
                "<request date=\"{3}\" timestamp=\"{4}\" user=\"{5}\" referer=\"/referer2\">/project2</request>",
                FormatUtils.formatUTCWithMillis(timestamp1), Long.toString(timestamp1), info1.getUserHash(),
                FormatUtils.formatUTCWithMillis(timestamp2), Long.toString(timestamp2), info2.getUserHash()));
    }

    @Test
    public void testWriteUserInfoEntry() throws Exception {
        UserInfo info = new UserInfo("hugo", "department", "location", 4711L);
        long timestamp = info.getTimestamp();
        converter.writeInfoEntry(writer, info);
        assertWriter(writer, MessageFormat.format(
                "<user date=\"{0}\" timestamp=\"{1}\" department=\"department\" location=\"location\">{2}</user>",
                FormatUtils.formatUTCWithMillis(timestamp), Long.toString(timestamp), info.getUserHash()));
    }

    @Test
    public void testWriteProjectInfo() throws Exception {
        Project project = new Project();
        project.setUuid(TestUUIDs.TEST_UUIDS[0]);
        long now = System.currentTimeMillis();
        project.setRegistered(now);
        project.setProjectId("testproject");
        project.setName("Test Project");
        converter.writeProjectInfo(writer, project, 4711L);
        assertWriter(writer, MessageFormat.format(
                "<project date=\"{0}\" timestamp=\"{1}\">\n" +
                "  <uuid>{2}</uuid>\n" +
                "  <id>testproject</id>\n" +
                "  <name>Test Project</name>\n" +
                "  <link rel=\"project\" href=\"http://localhost/api/projects/{2}\"/>\n" +
                "  <link rel=\"browse\" href=\"http://localhost/projects/testproject\"/>\n" +
                "</project>",
                FormatUtils.formatUTCWithMillis(4711L), Long.toString(4711L),
                TestUUIDs.TEST_UUIDS[0].toString()));
    }

    private void assertWriter(StringBufferHierarchicalStreamWriter writer, String expected) {
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + expected, writer.toString());
    }

    @Test
    public void testWriteAverageResponseTimes() throws Exception {
        TreeMap<String,Long> map = new TreeMap<String,Long>();
        map.put("/project2", 1234L);
        map.put("/project1", 5678L);
        converter.writeAverageResponseTimes(writer, map);
        assertWriter(writer,
                "<request avgResponseTime=\"5678\">/project1</request>\n" +
                "<request avgResponseTime=\"1234\">/project2</request>");
    }

    @Test
    public void testWriteRequestCounts() throws Exception {
        TreeMap<String,Long> requests = new TreeMap<String,Long>();
        requests.put("/project2", 2L);
        requests.put("/project1", 1L);
        TreeMap<String,Long> avgResponseTimes = new TreeMap<String,Long>();
        avgResponseTimes.put("/project2", 1234L);
        avgResponseTimes.put("/project1", 5678L);
        converter.writeRequestCounts(writer, requests, avgResponseTimes);
        assertWriter(writer,
                "<request count=\"1\" avgResponseTime=\"5678\">/project1</request>\n" +
                "<request count=\"2\" avgResponseTime=\"1234\">/project2</request>");
    }

    @Test
    public void testWriteCounts() throws Exception {
        TreeMap<String,Long> entries = new TreeMap<String,Long>();
        entries.put("project2", 2L);
        entries.put("project1", 1L);
        converter.writeCounts(writer, "foobar", entries);
        assertWriter(writer,
                "<foobar count=\"1\">project1</foobar>\n" +
                "<foobar count=\"2\">project2</foobar>");
    }
}
