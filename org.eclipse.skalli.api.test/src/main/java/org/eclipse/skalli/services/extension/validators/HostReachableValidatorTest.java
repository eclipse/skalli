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
package org.eclipse.skalli.services.extension.validators;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.testutil.BundleManager;
import org.eclipse.skalli.testutil.HttpServerMock;
import org.eclipse.skalli.testutil.PropertyHelperUtils;
import org.eclipse.skalli.testutil.TestExtension;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

@SuppressWarnings("nls")
public class HostReachableValidatorTest {

    private HostReachableValidator hrv;
    private static HttpServerMock mmus;

    private static Map<String, Severity> expectedSeverities;

    @BeforeClass
    public static void setUpOnce() throws Exception {
        BundleManager.startBundles();
        mmus = new HttpServerMock();
        mmus.start();

        // mapping between possible test values and the expected severity per value
        String host = mmus.getHost();
        int port = mmus.getPort();
        expectedSeverities = new HashMap<String, Severity>();
        expectedSeverities.put("http://" + host + ":" + port + "/200", null);
        expectedSeverities.put("http://" + host + ":" + port + "/201", null);
        expectedSeverities.put("http://" + host + ":" + port + "/202", null);
        expectedSeverities.put("http://" + host + ":" + port + "/203", null);
        expectedSeverities.put("http://" + host + ":" + port + "/204", null);
        expectedSeverities.put("http://" + host + ":" + port + "/205", null);
        expectedSeverities.put("http://" + host + ":" + port + "/206", null);
        expectedSeverities.put("http://" + host + ":" + port + "/207", null);
        expectedSeverities.put("http://" + host + ":" + port + "/300", Severity.INFO);
        expectedSeverities.put("http://" + host + ":" + port + "/303", Severity.INFO);
        expectedSeverities.put("http://" + host + ":" + port + "/307", Severity.INFO);
        expectedSeverities.put("http://" + host + ":" + port + "/408", Severity.INFO);
        expectedSeverities.put("http://" + host + ":" + port + "/301", Severity.WARNING);
        expectedSeverities.put("http://" + host + ":" + port + "/305", Severity.WARNING);
        expectedSeverities.put("http://" + host + ":" + port + "/401", Severity.WARNING);
        expectedSeverities.put("http://" + host + ":" + port + "/407", Severity.WARNING);
        expectedSeverities.put("http://" + host + ":" + port + "/423", Severity.WARNING);
        expectedSeverities.put("http://" + host + ":" + port + "/500", Severity.WARNING);
        expectedSeverities.put("http://" + host + ":" + port + "/503", Severity.WARNING);
        expectedSeverities.put("http://" + host + ":" + port + "/503", Severity.WARNING);
        expectedSeverities.put("http://" + host + ":" + port + "/504", Severity.WARNING);
        expectedSeverities.put("http://" + host + ":" + port + "/507", Severity.WARNING);
        expectedSeverities.put("http://" + host + ":" + port + "/400", Severity.ERROR);
        expectedSeverities.put("http://" + host + ":" + port + "/403", Severity.ERROR);
        expectedSeverities.put("http://" + host + ":" + port + "/404", Severity.ERROR);
        expectedSeverities.put("http://" + host + ":" + port + "/405", Severity.ERROR);
        expectedSeverities.put("http://" + host + ":" + port + "/406", Severity.ERROR);
        expectedSeverities.put("http://" + host + ":" + port + "/409", Severity.ERROR);
        expectedSeverities.put("http://" + host + ":" + port + "/410", Severity.ERROR);
        expectedSeverities.put("http://" + host + ":" + port + "/411", Severity.ERROR);
        expectedSeverities.put("http://" + host + ":" + port + "/412", Severity.ERROR);
        expectedSeverities.put("http://" + host + ":" + port + "/413", Severity.ERROR);
        expectedSeverities.put("http://" + host + ":" + port + "/414", Severity.ERROR);
        expectedSeverities.put("http://" + host + ":" + port + "/415", Severity.ERROR);
        expectedSeverities.put("http://" + host + ":" + port + "/416", Severity.ERROR);
        expectedSeverities.put("http://" + host + ":" + port + "/417", Severity.ERROR);
        expectedSeverities.put("http://" + host + ":" + port + "/422", Severity.ERROR);
        expectedSeverities.put("http://" + host + ":" + port + "/424", Severity.ERROR);
        expectedSeverities.put("http://" + host + ":" + port + "/501", Severity.ERROR);
        expectedSeverities.put("http://" + host + ":" + port + "/502", Severity.ERROR);
        expectedSeverities.put("some_unknown_host", Severity.ERROR);
    }

    @AfterClass
    public static void tearDownOnce() throws Exception {
        mmus.stop();
    }

    @Before
    public void setUp() throws Exception {
        hrv = new HostReachableValidator(TestExtension.class, TestExtension.PROPERTY_STR);
    }

    @Test
    public void testIssuesINFO() throws Exception {
        testIssues(Severity.INFO);
    }

    @Test
    public void testIssuesWARNING() throws Exception {
        testIssues(Severity.WARNING);
    }

    @Test
    public void testIssuesERROR() throws Exception {
        testIssues(Severity.ERROR);
    }

    @Test
    public void testIssuesFATAL() throws Exception {
        testIssues(Severity.FATAL);
    }

    @Test
    public void testEncodeURL() throws Exception {
        Assert.assertEquals("http://host:8080/first%20second",
                HostReachableValidator.encodeURL(new URL("http://host:8080/first second")).toExternalForm());
        Assert.assertEquals("http://host:8080/first%20second/third",
                HostReachableValidator.encodeURL(new URL("http://host:8080/first second/third")).toExternalForm());
        Assert.assertEquals("http://host:8080/=$%20%25:%C3%A4",
                HostReachableValidator.encodeURL(new URL("http://host:8080/=$ %:ä")).toExternalForm());
    }

    private void testIssues(final Severity minSeverity) throws Exception {
        for (Map.Entry<String, Severity> entry : expectedSeverities.entrySet()) {
            String expectedHost = entry.getKey();
            Severity expectedSeverity = entry.getValue();

            if (expectedSeverity != null && expectedSeverity.compareTo(minSeverity) <= 0) {
                assertIssues(hrv.validate(PropertyHelperUtils.TEST_UUIDS[0], expectedHost, minSeverity), minSeverity);
            } else {
                assertNoIssues(hrv.validate(PropertyHelperUtils.TEST_UUIDS[0], expectedHost, minSeverity));
            }
        }
    }

    private void assertIssues(SortedSet<Issue> issues, Severity minSeverity) {
        SortedSet<Issue> issuesOk = new TreeSet<Issue>();
        SortedSet<Issue> issuesNotOk = new TreeSet<Issue>();

        for (Issue issue : issues) {
            if (issue.getSeverity().compareTo(minSeverity) > 0) {
                issuesNotOk.add(issue);
            } else {
                issuesOk.add(issue);
            }
        }

        Assert.assertEquals(Collections.<Issue> emptySet(), issuesNotOk);
    }

    private void assertNoIssues(SortedSet<Issue> issues) {
        Assert.assertEquals(Collections.<Issue> emptySet(), issues);
    }
}
