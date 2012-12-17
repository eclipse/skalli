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
package org.eclipse.skalli.model;

import java.util.Arrays;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

import org.eclipse.skalli.testutil.AssertUtils;
import org.eclipse.skalli.testutil.TestUUIDs;
import org.eclipse.skalli.testutil.TestExtension;

public class ValidationExceptionTest implements Issuer {

    private static final Class<? extends Issuer> ISSUER = ValidationExceptionTest.class;

    @SuppressWarnings("nls")
    @Test
    public void testGetMessage() {
        TreeSet<Issue> issues = new TreeSet<Issue>();
        issues.add(new Issue(Severity.WARNING, ISSUER, TestUUIDs.TEST_UUIDS[0], "IssueWarn"));
        issues.add(new Issue(Severity.ERROR, ISSUER, TestUUIDs.TEST_UUIDS[1], "IssueError"));
        issues.add(new Issue(Severity.INFO, ISSUER, TestUUIDs.TEST_UUIDS[2], "IssueInfo"));
        issues.add(new Issue(Severity.ERROR, ISSUER, TestUUIDs.TEST_UUIDS[3], "IssueAnotherError"));
        ValidationException e = new ValidationException("Message", issues);
        Assert.assertEquals("Message\n - IssueAnotherError\n - IssueError\n - IssueWarn\n - IssueInfo",
                e.getMessage());
        AssertUtils.assertEquals("getIssues()", issues, e.getIssues());
        Assert.assertTrue("hasIssues()", e.hasIssues());
        Assert.assertFalse("hasIssues()", e.hasFatalIssues());

        Issue issue0 = new Issue(Severity.WARNING, ISSUER, TestUUIDs.TEST_UUIDS[0], "");
        Issue issue1 = new Issue(Severity.ERROR, ISSUER, TestUUIDs.TEST_UUIDS[1], null);
        Issue issue2 = new Issue(Severity.INFO, ISSUER, TestUUIDs.TEST_UUIDS[2], "IssueInfo");
        Issue issue3 = new Issue(Severity.ERROR, ISSUER, TestUUIDs.TEST_UUIDS[3], "");
        Issue[] issuesArray = new Issue[] { issue0, issue1, issue2, issue3 };
        Issue[] expectedIssuesArray = new Issue[] { issue3, issue1, issue0, issue2 };
        e = new ValidationException("Message", issuesArray);
        Assert.assertEquals("Message\n" +
                " - Entity " + TestUUIDs.TEST_UUIDS[3] + " is invalid\n" +
                " - Entity " + TestUUIDs.TEST_UUIDS[1] + " is invalid\n" +
                " - Entity " + TestUUIDs.TEST_UUIDS[0] + " is invalid\n" +
                " - IssueInfo",
                e.getMessage());
        AssertUtils.assertEquals("getIssues()", Arrays.asList(expectedIssuesArray), e.getIssues());
        Assert.assertTrue("hasIssues()", e.hasIssues());
        Assert.assertFalse("hasIssues()", e.hasFatalIssues());

        issues = new TreeSet<Issue>();
        issues.add(new Issue(Severity.WARNING, ISSUER, TestUUIDs.TEST_UUIDS[0], null));
        issues.add(new Issue(Severity.ERROR, ISSUER, TestUUIDs.TEST_UUIDS[1], ""));
        e = new ValidationException(issues);
        Assert.assertEquals(
                " - Entity " + TestUUIDs.TEST_UUIDS[1] + " is invalid\n" +
                        " - Entity " + TestUUIDs.TEST_UUIDS[0] + " is invalid",
                e.getMessage());
        AssertUtils.assertEquals("getIssues()", issues, e.getIssues());
        Assert.assertTrue("hasIssues()", e.hasIssues());
        Assert.assertFalse("hasIssues()", e.hasFatalIssues());

        issues = new TreeSet<Issue>();
        issues.add(new Issue(Severity.FATAL, ISSUER, TestUUIDs.TEST_UUIDS[0], null));
        e = new ValidationException("Message", issues);
        Assert.assertEquals(
                "Message\n" +
                        " - Entity " + TestUUIDs.TEST_UUIDS[0] + " is invalid",
                e.getMessage());
        AssertUtils.assertEquals("getIssues()", issues, e.getIssues());
        Assert.assertTrue("hasIssues()", e.hasIssues());
        Assert.assertTrue("hasIssues()", e.hasFatalIssues());

        issues = new TreeSet<Issue>();
        issues.add(new Issue(Severity.WARNING, ISSUER, TestUUIDs.TEST_UUIDS[0], null));
        e = new ValidationException(issues);
        Assert.assertEquals(
                "Entity " + TestUUIDs.TEST_UUIDS[0] + " is invalid",
                e.getMessage());
        AssertUtils.assertEquals("getIssues()", issues, e.getIssues());
        Assert.assertTrue("hasIssues()", e.hasIssues());
        Assert.assertFalse("hasIssues()", e.hasFatalIssues());

        e = new ValidationException("Message", new TreeSet<Issue>());
        Assert.assertEquals("Message", e.getMessage());
        Assert.assertTrue("getIssues()", e.getIssues().isEmpty());
        Assert.assertFalse("hasIssues()", e.hasIssues());
        Assert.assertFalse("hasIssues()", e.hasFatalIssues());

        e = new ValidationException("Message");
        Assert.assertEquals("Message", e.getMessage());
        Assert.assertTrue("getIssues()", e.getIssues().isEmpty());
        Assert.assertFalse("hasIssues()", e.hasIssues());
        Assert.assertFalse("hasIssues()", e.hasFatalIssues());

        e = new ValidationException("", new TreeSet<Issue>());
        Assert.assertEquals("", e.getMessage());
        Assert.assertTrue("getIssues()", e.getIssues().isEmpty());
        Assert.assertFalse("hasIssues()", e.hasIssues());
        Assert.assertFalse("hasIssues()", e.hasFatalIssues());

        e = new ValidationException();
        Assert.assertEquals("", e.getMessage());
        Assert.assertTrue("getIssues()", e.getIssues().isEmpty());
        Assert.assertFalse("hasIssues()", e.hasIssues());
        Assert.assertFalse("hasIssues()", e.hasFatalIssues());
        e.addIssue(new Issue(Severity.FATAL, ISSUER, TestUUIDs.TEST_UUIDS[0]));
        Assert.assertTrue("hasIssues()", e.hasIssues());
        Assert.assertTrue("hasIssues()", e.hasFatalIssues());

        e = new ValidationException(ISSUER, TestUUIDs.TEST_UUIDS[0], TestExtension.class);
        Assert.assertEquals(
                "Extension " + TestExtension.class.getName()
                        + " of entity " + TestUUIDs.TEST_UUIDS[0] + " is invalid",
                e.getMessage());
        Assert.assertTrue("getIssues()", e.getIssues().size() == 1);
        Assert.assertTrue("hasIssues()", e.hasIssues());
        Assert.assertTrue("hasIssues()", e.hasFatalIssues());
        Issue issue = e.getIssues().first();
        Assert.assertNotNull(issue);
        Assert.assertEquals(Severity.FATAL, issue.getSeverity());
        Assert.assertEquals(ISSUER, issue.getIssuer());
        Assert.assertEquals(TestUUIDs.TEST_UUIDS[0], issue.getEntityId());
        Assert.assertEquals(TestExtension.class, issue.getExtension());
        Assert.assertEquals(e.getMessage(), issue.getMessage());

        e = new ValidationException(ISSUER, TestUUIDs.TEST_UUIDS[0], TestExtension.class,
                TestExtension.PROPERTY_STR);
        Assert.assertEquals(
                "Property " + TestExtension.PROPERTY_STR + " of extension " + TestExtension.class.getName()
                        + " of entity " + TestUUIDs.TEST_UUIDS[0] + " is invalid",
                e.getMessage());
        Assert.assertTrue("getIssues()", e.getIssues().size() == 1);
        Assert.assertTrue("hasIssues()", e.hasIssues());
        Assert.assertTrue("hasIssues()", e.hasFatalIssues());
        issue = e.getIssues().first();
        Assert.assertNotNull(issue);
        Assert.assertEquals(Severity.FATAL, issue.getSeverity());
        Assert.assertEquals(ISSUER, issue.getIssuer());
        Assert.assertEquals(TestUUIDs.TEST_UUIDS[0], issue.getEntityId());
        Assert.assertEquals(TestExtension.class, issue.getExtension());
        Assert.assertEquals(TestExtension.PROPERTY_STR, issue.getPropertyId());
        Assert.assertEquals(e.getMessage(), issue.getMessage());

        e = new ValidationException(ISSUER, TestUUIDs.TEST_UUIDS[0], TestExtension.class,
                TestExtension.PROPERTY_STR, "Message");
        Assert.assertEquals("Message", e.getMessage());
        Assert.assertTrue("getIssues()", e.getIssues().size() == 1);
        Assert.assertTrue("hasIssues()", e.hasIssues());
        Assert.assertTrue("hasIssues()", e.hasFatalIssues());
        issue = e.getIssues().first();
        Assert.assertNotNull(issue);
        Assert.assertEquals(Severity.FATAL, issue.getSeverity());
        Assert.assertEquals(ISSUER, issue.getIssuer());
        Assert.assertEquals(TestUUIDs.TEST_UUIDS[0], issue.getEntityId());
        Assert.assertEquals(TestExtension.class, issue.getExtension());
        Assert.assertEquals(TestExtension.PROPERTY_STR, issue.getPropertyId());
        Assert.assertEquals(e.getMessage(), issue.getMessage());
    }

}
