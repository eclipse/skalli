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

import java.text.MessageFormat;
import java.util.TreeSet;

import org.eclipse.skalli.testutil.PropertyHelperUtils;
import org.eclipse.skalli.testutil.TestExtension;
import org.eclipse.skalli.testutil.TestExtension1;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("nls")
public class IssueTest implements Issuer {

    private static final Class<? extends Issuer> ISSUER = IssueTest.class;

    private class SomeIssuer implements Issuer {
    }

    private class AnotherIssuer implements Issuer {
    }

    @Test
    public void testBasics() {
        Issue issue = new Issue(Severity.WARNING, ISSUER, PropertyHelperUtils.TEST_UUIDS[0]);
        Assert.assertEquals(Severity.WARNING, issue.getSeverity());
        Assert.assertEquals(ISSUER, issue.getIssuer());
        Assert.assertEquals(PropertyHelperUtils.TEST_UUIDS[0], issue.getEntityId());
        Assert.assertEquals(
                MessageFormat.format("Entity {0} is invalid", PropertyHelperUtils.TEST_UUIDS[0].toString()),
                issue.getMessage());
        Assert.assertNull(issue.getExtension());
        Assert.assertNull(issue.getPropertyId());
        Assert.assertNull(issue.getDescription());

        issue = new Issue(Severity.WARNING, ISSUER, PropertyHelperUtils.TEST_UUIDS[0], "message");
        Assert.assertEquals(Severity.WARNING, issue.getSeverity());
        Assert.assertEquals(ISSUER, issue.getIssuer());
        Assert.assertEquals(PropertyHelperUtils.TEST_UUIDS[0], issue.getEntityId());
        Assert.assertEquals("message", issue.getMessage());
        Assert.assertEquals("message", issue.toString());
        Assert.assertNull(issue.getExtension());
        Assert.assertNull(issue.getPropertyId());
        Assert.assertNull(issue.getDescription());

        issue = new Issue(Severity.WARNING, ISSUER, PropertyHelperUtils.TEST_UUIDS[0], TestExtension.class, null);
        Assert.assertEquals(Severity.WARNING, issue.getSeverity());
        Assert.assertEquals(ISSUER, issue.getIssuer());
        Assert.assertEquals(PropertyHelperUtils.TEST_UUIDS[0], issue.getEntityId());
        Assert.assertEquals(MessageFormat.format("Extension {0} of entity {1} is invalid",
                TestExtension.class.getName(), PropertyHelperUtils.TEST_UUIDS[0].toString()),
                issue.getMessage());
        Assert.assertEquals(TestExtension.class.getName(), issue.getExtension().getName());
        Assert.assertNull(issue.getPropertyId());
        Assert.assertNull(issue.getDescription());

        issue = new Issue(Severity.WARNING, ISSUER, PropertyHelperUtils.TEST_UUIDS[0], TestExtension.class,
                TestExtension.PROPERTY_STR);
        Assert.assertEquals(Severity.WARNING, issue.getSeverity());
        Assert.assertEquals(ISSUER, issue.getIssuer());
        Assert.assertEquals(PropertyHelperUtils.TEST_UUIDS[0], issue.getEntityId());
        Assert.assertEquals(
                MessageFormat.format("Property {0} of extension {1} of entity {2} is invalid",
                        TestExtension.PROPERTY_STR, TestExtension.class.getName(),
                        PropertyHelperUtils.TEST_UUIDS[0].toString()),
                issue.getMessage());
        Assert.assertEquals(TestExtension.class.getName(), issue.getExtension().getName());
        Assert.assertEquals(TestExtension.PROPERTY_STR, issue.getPropertyId());
        Assert.assertNull(issue.getDescription());

        issue = new Issue(Severity.WARNING, ISSUER, PropertyHelperUtils.TEST_UUIDS[0], TestExtension.class,
                TestExtension.PROPERTY_STR, "message");
        issue.setDescription("foobar");
        Assert.assertEquals(Severity.WARNING, issue.getSeverity());
        Assert.assertEquals(ISSUER, issue.getIssuer());
        Assert.assertEquals(PropertyHelperUtils.TEST_UUIDS[0], issue.getEntityId());
        Assert.assertEquals("message", issue.getMessage());
        Assert.assertEquals(TestExtension.class.getName(), issue.getExtension().getName());
        Assert.assertEquals(TestExtension.PROPERTY_STR, issue.getPropertyId());
        Assert.assertEquals("foobar", issue.getDescription());

        issue.setExtension(TestExtension1.class);
        Assert.assertEquals(TestExtension1.class.getName(), issue.getExtension().getName());

        issue.setPropertyId(TestExtension1.PROPERTY_ITEMS);
        Assert.assertEquals(TestExtension1.PROPERTY_ITEMS, issue.getPropertyId());

        issue.setDescription("abc");
        Assert.assertEquals("abc", issue.getDescription());

        issue.setItem(4711);
        Assert.assertEquals(4711, issue.getItem());

        long timestamp = System.currentTimeMillis();
        issue.setTimestamp(timestamp);
        Assert.assertEquals(timestamp, issue.getTimestamp());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBasics_noSeverity() {
        new Issue(null, ISSUER, PropertyHelperUtils.TEST_UUIDS[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBasics_noIssuer() {
        new Issue(Severity.WARNING, null, PropertyHelperUtils.TEST_UUIDS[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBasics_noEntityId() {
        new Issue(Severity.WARNING, ISSUER, null);
    }

    @Test
    public void testCompareToEquals() {
        Issue issue = new Issue(Severity.WARNING, ISSUER, PropertyHelperUtils.TEST_UUIDS[0]);

        Assert.assertTrue(issue.equals(issue));
        Assert.assertEquals(0, issue.compareTo(issue));

        Issue issue1 = new Issue(Severity.FATAL, ISSUER, PropertyHelperUtils.TEST_UUIDS[0]);
        Assert.assertFalse(issue.equals(issue1));
        Assert.assertFalse(issue1.equals(issue));
        Assert.assertTrue(issue.compareTo(issue1) > 0);
        Assert.assertTrue(issue1.compareTo(issue) < 0);

        issue1 = new Issue(Severity.FATAL, ISSUER, PropertyHelperUtils.TEST_UUIDS[1]);
        Assert.assertFalse(issue.equals(issue1));
        Assert.assertFalse(issue1.equals(issue));
        Assert.assertTrue(issue.compareTo(issue1) > 0);
        Assert.assertTrue(issue1.compareTo(issue) < 0);

        issue1 = new Issue(Severity.WARNING, ISSUER, PropertyHelperUtils.TEST_UUIDS[1]);
        Assert.assertFalse(issue.equals(issue1));
        Assert.assertFalse(issue1.equals(issue));
        Assert.assertEquals(PropertyHelperUtils.TEST_UUIDS[0].compareTo(PropertyHelperUtils.TEST_UUIDS[1]),
                issue.compareTo(issue1));
        Assert.assertEquals(PropertyHelperUtils.TEST_UUIDS[1].compareTo(PropertyHelperUtils.TEST_UUIDS[0]),
                issue1.compareTo(issue));

        issue = new Issue(Severity.WARNING, ISSUER, PropertyHelperUtils.TEST_UUIDS[0], TestExtension.class, null);
        issue1 = new Issue(Severity.WARNING, ISSUER, PropertyHelperUtils.TEST_UUIDS[0], TestExtension1.class, null);
        Assert.assertFalse(issue.equals(issue1));
        Assert.assertFalse(issue1.equals(issue));
        Assert.assertTrue(issue.compareTo(issue1) < 0);
        Assert.assertTrue(issue1.compareTo(issue) > 0);

        issue = new Issue(Severity.WARNING, ISSUER, PropertyHelperUtils.TEST_UUIDS[0], TestExtension.class, 456, null);
        issue1 = new Issue(Severity.WARNING, ISSUER, PropertyHelperUtils.TEST_UUIDS[0], TestExtension.class, 123, null);
        Assert.assertFalse(issue.equals(issue1));
        Assert.assertFalse(issue1.equals(issue));
        Assert.assertTrue(issue.compareTo(issue1) > 0);
        Assert.assertTrue(issue1.compareTo(issue) < 0);

        issue1.setExtension(null);
        Assert.assertFalse(issue.equals(issue1));
        Assert.assertFalse(issue1.equals(issue));
        Assert.assertTrue(issue.compareTo(issue1) > 0);
        Assert.assertTrue(issue1.compareTo(issue) < 0);

        issue1.setExtension(TestExtension1.class);
        issue.setExtension(null);
        Assert.assertFalse(issue.equals(issue1));
        Assert.assertFalse(issue1.equals(issue));
        Assert.assertTrue(issue.compareTo(issue1) < 0);
        Assert.assertTrue(issue1.compareTo(issue) > 0);

        issue = new Issue(Severity.WARNING, ISSUER, PropertyHelperUtils.TEST_UUIDS[0], TestExtension.class,
                TestExtension.PROPERTY_BOOL);
        issue1 = new Issue(Severity.WARNING, ISSUER, PropertyHelperUtils.TEST_UUIDS[0], TestExtension.class,
                TestExtension.PROPERTY_STR);
        Assert.assertFalse(issue.equals(issue1));
        Assert.assertFalse(issue1.equals(issue));
        Assert.assertTrue(issue.compareTo(issue1) < 0);
        Assert.assertTrue(issue1.compareTo(issue) > 0);

        issue1.setPropertyId(null);
        Assert.assertFalse(issue.equals(issue1));
        Assert.assertFalse(issue1.equals(issue));
        Assert.assertTrue(issue.compareTo(issue1) > 0);
        Assert.assertTrue(issue1.compareTo(issue) < 0);

        issue.setPropertyId(null);
        issue1.setPropertyId(TestExtension.PROPERTY_BOOL);
        Assert.assertFalse(issue.equals(issue1));
        Assert.assertFalse(issue1.equals(issue));
        Assert.assertTrue(issue.compareTo(issue1) < 0);
        Assert.assertTrue(issue1.compareTo(issue) > 0);

        issue = new Issue(Severity.WARNING, ISSUER, PropertyHelperUtils.TEST_UUIDS[0], TestExtension.class,
                TestExtension.PROPERTY_BOOL, "foo");
        issue1 = new Issue(Severity.WARNING, ISSUER, PropertyHelperUtils.TEST_UUIDS[0], TestExtension.class,
                TestExtension.PROPERTY_BOOL, "bar");
        Assert.assertTrue(issue.equals(issue1));
        Assert.assertTrue(issue1.equals(issue));

        issue = new Issue(Severity.WARNING, ISSUER, PropertyHelperUtils.TEST_UUIDS[0], TestExtension.class,
                TestExtension.PROPERTY_BOOL, "foo");
        issue1 = new Issue(Severity.WARNING, ISSUER, PropertyHelperUtils.TEST_UUIDS[0], TestExtension.class,
                TestExtension.PROPERTY_BOOL, null);
        Assert.assertTrue(issue.equals(issue1));
        Assert.assertTrue(issue1.equals(issue));

        issue = new Issue(Severity.WARNING, ISSUER, PropertyHelperUtils.TEST_UUIDS[0], TestExtension.class,
                TestExtension.PROPERTY_BOOL, null);
        issue1 = new Issue(Severity.WARNING, ISSUER, PropertyHelperUtils.TEST_UUIDS[0], TestExtension.class,
                TestExtension.PROPERTY_BOOL, "bar");
        Assert.assertTrue(issue.equals(issue1));
        Assert.assertTrue(issue1.equals(issue));

        issue = new Issue(Severity.WARNING, SomeIssuer.class, PropertyHelperUtils.TEST_UUIDS[0], TestExtension.class,
                TestExtension.PROPERTY_BOOL);
        issue1 = new Issue(Severity.WARNING, AnotherIssuer.class, PropertyHelperUtils.TEST_UUIDS[0],
                TestExtension.class, TestExtension.PROPERTY_BOOL);
        Assert.assertFalse(issue.equals(issue1));
        Assert.assertFalse(issue1.equals(issue));
        Assert.assertTrue(issue.compareTo(issue1) > 0);
        Assert.assertTrue(issue1.compareTo(issue) < 0);
    }

    @Test
    public void testGetMessage() {
        TreeSet<Issue> issues = new TreeSet<Issue>();
        issues.add(new Issue(Severity.WARNING, ISSUER, PropertyHelperUtils.TEST_UUIDS[0], "IssueWarn"));
        issues.add(new Issue(Severity.ERROR, ISSUER, PropertyHelperUtils.TEST_UUIDS[1], "IssueError"));
        issues.add(new Issue(Severity.INFO, ISSUER, PropertyHelperUtils.TEST_UUIDS[2], "IssueInfo"));
        issues.add(new Issue(Severity.ERROR, ISSUER, PropertyHelperUtils.TEST_UUIDS[3], "IssueAnotherError"));
        Assert.assertEquals(
                "Message\n - IssueAnotherError\n - IssueError\n - IssueWarn\n - IssueInfo",
                Issue.getMessage("Message", issues));

        issues = new TreeSet<Issue>();
        issues.add(new Issue(Severity.WARNING, ISSUER, PropertyHelperUtils.TEST_UUIDS[0], ""));
        issues.add(new Issue(Severity.ERROR, ISSUER, PropertyHelperUtils.TEST_UUIDS[1], null));
        issues.add(new Issue(Severity.INFO, ISSUER, PropertyHelperUtils.TEST_UUIDS[2], "IssueInfo"));
        issues.add(new Issue(Severity.ERROR, ISSUER, PropertyHelperUtils.TEST_UUIDS[3], ""));
        Assert.assertEquals("Message\n" +
                " - Entity " + PropertyHelperUtils.TEST_UUIDS[3] + " is invalid\n" +
                " - Entity " + PropertyHelperUtils.TEST_UUIDS[1] + " is invalid\n" +
                " - Entity " + PropertyHelperUtils.TEST_UUIDS[0] + " is invalid\n" +
                " - IssueInfo",
                Issue.getMessage("Message", issues));

        issues = new TreeSet<Issue>();
        issues.add(new Issue(Severity.WARNING, ISSUER, PropertyHelperUtils.TEST_UUIDS[0], null));
        issues.add(new Issue(Severity.ERROR, ISSUER, PropertyHelperUtils.TEST_UUIDS[1], ""));
        Assert.assertEquals(
                " - Entity " + PropertyHelperUtils.TEST_UUIDS[1] + " is invalid\n" +
                        " - Entity " + PropertyHelperUtils.TEST_UUIDS[0] + " is invalid",
                        Issue.getMessage("", issues));

        issues = new TreeSet<Issue>();
        issues.add(new Issue(Severity.FATAL, ISSUER, PropertyHelperUtils.TEST_UUIDS[0], null));
        Assert.assertEquals(
                "Message\n" +
                        " - Entity " + PropertyHelperUtils.TEST_UUIDS[0] + " is invalid",
                        Issue.getMessage("Message", issues));

        issues = new TreeSet<Issue>();
        issues.add(new Issue(Severity.WARNING, ISSUER, PropertyHelperUtils.TEST_UUIDS[0], null));
        Assert.assertEquals(
                "Entity " + PropertyHelperUtils.TEST_UUIDS[0] + " is invalid",
                Issue.getMessage(null, issues));

        Assert.assertEquals("Message", Issue.getMessage("Message", new TreeSet<Issue>()));
        Assert.assertEquals("Message", Issue.getMessage("Message", null));
        Assert.assertEquals("", Issue.getMessage("", new TreeSet<Issue>()));
        Assert.assertEquals("", Issue.getMessage("", null));
    }

}
