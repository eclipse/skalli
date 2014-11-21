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
package org.eclipse.skalli.services.issues;

import java.util.Arrays;
import java.util.Map;
import java.util.SortedSet;

import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Issuer;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.testutil.AssertUtils;
import org.eclipse.skalli.testutil.PropertyTestUtil;
import org.eclipse.skalli.testutil.TestUUIDs;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("nls")
public class IssuesTest implements Issuer {

    private static final Class<? extends Issuer> ISSUER = IssuesTest.class;

    private static Issue[] ISSUES = new Issue[] {
            new Issue(Severity.ERROR, ISSUER, TestUUIDs.TEST_UUIDS[1]),
            new Issue(Severity.FATAL, ISSUER, TestUUIDs.TEST_UUIDS[0]),
            new Issue(Severity.WARNING, ISSUER, TestUUIDs.TEST_UUIDS[0]),
            new Issue(Severity.INFO, ISSUER, TestUUIDs.TEST_UUIDS[0]),
            new Issue(Severity.ERROR, ISSUER, TestUUIDs.TEST_UUIDS[0])
    };

    @Test
    public void testPropertyDefinitions() throws Exception {
        Map<String, Object> values = PropertyTestUtil.getValues();
        Map<Class<?>, String[]> requiredProperties = PropertyTestUtil.getRequiredProperties();
        values.put(Issues.PROPERTY_ISSUES, CollectionUtils.asSortedSet(ISSUES));
        values.put(Issues.PROPERTY_STALE, true);
        PropertyTestUtil.checkPropertyDefinitions(Issues.class, requiredProperties, values);
    }

    @Test
    public void testGetIssues() {
        SortedSet<Issue> set = CollectionUtils.asSortedSet(ISSUES);
        Issues issues = new Issues(TestUUIDs.TEST_UUIDS[0], set);
        AssertUtils.assertEquals("getIssues(WARNING)",
                Arrays.asList(ISSUES[1], ISSUES[0], ISSUES[4], ISSUES[2]),
                issues.getIssues(Severity.WARNING));
        AssertUtils.assertEquals("Issues.getIssues(WARNING)",
                Arrays.asList(ISSUES[1], ISSUES[0], ISSUES[4], ISSUES[2]),
                Issues.getIssues(set, Severity.WARNING));
        AssertUtils.assertEquals("getIssues(FATAL)",
                Arrays.asList(ISSUES[1]),
                issues.getIssues(Severity.FATAL));
        AssertUtils.assertEquals("getIssues(FATAL)",
                Arrays.asList(ISSUES[1]),
                issues.getIssues(Severity.FATAL));
        AssertUtils.assertEquals("getIssues(INFO)",
                Arrays.asList(ISSUES[1], ISSUES[0], ISSUES[4], ISSUES[2], ISSUES[3]),
                issues.getIssues(Severity.INFO));
        Assert.assertTrue(issues.getIssues(null).isEmpty());
    }

    @Test
    public void testLatestDurations() throws Exception {
        Issues issues = new Issues();
        Assert.assertEquals(0, issues.getLatestDurations().length);
        Assert.assertEquals(-1L, issues.getLatestDuration());
        Assert.assertEquals(-1L, issues.getAverageDuration());
        long sum = 0;
        for (int i = 1; i <= 10; ++i) {
            if (i > 5) {
                sum += i;
            }
            issues.addLatestDuration(i);
            Assert.assertEquals(i, issues.getLatestDuration());
            if (i==1) {
                Assert.assertEquals(1L, issues.getAverageDuration());
            }
        }
        Assert.assertEquals(sum/5, issues.getAverageDuration());
        Assert.assertEquals(10, issues.getLatestDuration());
    }
}
