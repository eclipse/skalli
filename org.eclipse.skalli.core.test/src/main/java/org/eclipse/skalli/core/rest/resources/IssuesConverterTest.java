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
package org.eclipse.skalli.core.rest.resources;

import java.text.MessageFormat;

import org.eclipse.skalli.core.rest.JSONRestWriter;
import org.eclipse.skalli.core.rest.XMLRestWriter;
import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.services.issues.Issues;
import org.eclipse.skalli.services.rest.RestWriter;
import org.eclipse.skalli.testutil.RestWriterTestBase;
import org.eclipse.skalli.testutil.TestEntityService;
import org.eclipse.skalli.testutil.TestExtension;
import org.eclipse.skalli.testutil.TestUUIDs;
import org.junit.Test;

@SuppressWarnings("nls")
public class IssuesConverterTest extends RestWriterTestBase {

    private static final String NAMESPACE_ATTRIBUTES = MessageFormat.format(ATTRIBUTES_PATTERN,
            IssuesConverter.NAMESPACE, "issues", IssuesConverter.API_VERSION);

    private static final String COMMON_PART_XML = MessageFormat.format(
            "<issues {0}>"
            + "<link rel=\"self\" href=\"http://example.org/api/projects/{1}/issues\"/>"
            + "<link rel=\"project\" href=\"http://example.org/api/projects/{1}\"/>"
            + "<uuid>{1}</uuid>",
            NAMESPACE_ATTRIBUTES, TestUUIDs.TEST_UUIDS[0]);

    private static final String COMMON_PART_JSON = MessageFormat.format(
            "'{'"
            + "\"apiVersion\":\"{0}\",\"links\":"
            + "['{'\"rel\":\"self\",\"href\":\"http://example.org/api/projects/{1}/issues\"},"
            + "'{'\"rel\":\"project\",\"href\":\"http://example.org/api/projects/{1}\"}],"
            + "\"uuid\":\"{1}\",",
            IssuesConverter.API_VERSION, TestUUIDs.TEST_UUIDS[0]);

    private static final Issue[] ISSUES = {
        new Issue(Severity.INFO, TestEntityService.class, TestUUIDs.TEST_UUIDS[2],
                null, null, 0, "msg0"),
        new Issue(Severity.ERROR, TestEntityService.class, TestUUIDs.TEST_UUIDS[1],
                TestExtension.class, TestExtension.PROPERTY_STR, 5, "msg1")};
    static {
        long now = System.currentTimeMillis();
        ISSUES[0].setTimestamp(now);
        ISSUES[1].setTimestamp(now + 1000L);
        ISSUES[1].setDescription("descr1");
    }

    @Test
    public void testMarshalEmptyIssuesXML() throws Exception {
        Issues issues = new Issues(TestUUIDs.TEST_UUIDS[0]);
        XMLRestWriter restWriter = new XMLRestWriter(writer, "http://example.org");
        marshalIssues(issues, restWriter);
        assertEqualsXML(COMMON_PART_XML + "<isStale>false</isStale></issues>");
    }

    @Test
    public void testMarshalIssuesXML() throws Exception {
        Issues issues = newIssues();
        XMLRestWriter restWriter = new XMLRestWriter(writer, "http://example.org");
        marshalIssues(issues, restWriter);
        assertEqualsXML(COMMON_PART_XML
                + "<isStale>true</isStale>"
                + nextIssueXML(1)
                + nextIssueXML(0)
                + "</issues>");
    }

    @Test
    public void testMarshalEmptyIssuesJSON() throws Exception {
        Issues issues = new Issues(TestUUIDs.TEST_UUIDS[0]);
        JSONRestWriter restWriter = new JSONRestWriter(writer, "http://example.org");
        marshalIssues(issues, restWriter);
        assertEqualsJSON(COMMON_PART_JSON + "\"isStale\":false,\"items\":[]}");
    }

    @Test
    public void testMarshalIssuesJSON() throws Exception {
        Issues issues = newIssues();
        JSONRestWriter restWriter = new JSONRestWriter(writer, "http://example.org");
        marshalIssues(issues, restWriter);
        assertEqualsJSON(COMMON_PART_JSON
                + "\"isStale\":true,\"items\":["
                + nextIssueJSON(1) + ","
                + nextIssueJSON(0)
                + "]}");
    }

    private String nextIssueXML(int i) {
        Issue issue = ISSUES[i];
        return MessageFormat.format(
                "<issue>"
                + "<timestamp>{0}</timestamp>"
                + "<severity>{1}</severity>"
                + (i==1? "<extension>{2}</extension><propertyId>{3}</propertyId>" : "")
                + "<issuer>{4}</issuer>"
                + "<item>{5}</item>"
                + "<message>{6}</message>"
                + (i==1? "<description>{7}</description>" : "")
                + "</issue>",
                Long.toString(issue.getTimestamp()), issue.getSeverity(), TestExtension.class.getName(),
                TestExtension.PROPERTY_STR, issue.getIssuer().getName(), Integer.toString(issue.getItem()),
                issue.getMessage(), issue.getDescription());
    }

    private String nextIssueJSON(int i) {
        Issue issue = ISSUES[i];
        return MessageFormat.format(
                "'{'"
                + "\"timestamp\":{0}"
                + ",\"severity\":\"{1}\""
                + (i==1? ",\"extension\":\"{2}\",\"propertyId\":\"{3}\"" : "")
                + ",\"issuer\":\"{4}\""
                + ",\"item\":{5}"
                + ",\"message\":\"{6}\""
                + (i==1? ",\"description\":\"{7}\"" : "")
                + "}",
                Long.toString(issue.getTimestamp()), issue.getSeverity(), TestExtension.class.getName(),
                TestExtension.PROPERTY_STR, issue.getIssuer().getName(), Integer.toString(issue.getItem()),
                issue.getMessage(), issue.getDescription());
    }
    private Issues newIssues() {
        Issues issues = new Issues(TestUUIDs.TEST_UUIDS[0]);
        issues.setStale(true);
        issues.addIssue(ISSUES[0]);
        issues.addIssue(ISSUES[1]);
        return issues;
    }

    private void marshalIssues(Issues issues, RestWriter restWriter) throws Exception {
        IssuesConverter converter = new IssuesConverter();
        converter.marshal(issues, restWriter);
        restWriter.flush();
    }
}
