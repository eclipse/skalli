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
import java.util.SortedSet;

import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.services.extension.PropertyValidatorTestUtils;
import org.eclipse.skalli.testutil.PropertyHelperUtils;
import org.eclipse.skalli.testutil.TestExtension;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("nls")
public class URLValidatorTest {

    @Test
    public void testIsValid() throws Exception {
        URLValidator validator = new URLValidator(Severity.FATAL, TestExtension.class, TestExtension.PROPERTY_STR);

        Assert.assertTrue(validator.isValid(PropertyHelperUtils.TEST_UUIDS[0], new URL("http://example.org")));
        Assert.assertTrue(validator.isValid(PropertyHelperUtils.TEST_UUIDS[0], "http://example.org"));
        Assert.assertFalse(validator.isValid(PropertyHelperUtils.TEST_UUIDS[0], "foobar"));
    }

    @Test
    public void testValidate() throws Exception {
        URLValidator validator = new URLValidator(Severity.FATAL, TestExtension.class, TestExtension.PROPERTY_STR);

        SortedSet<Issue> issues = validator.validate(PropertyHelperUtils.TEST_UUIDS[0], new URL("http://example.org"),
                Severity.FATAL);
        Assert.assertNotNull(issues);
        Assert.assertEquals(0, issues.size());

        issues = validator.validate(PropertyHelperUtils.TEST_UUIDS[0], "http://example.org", Severity.FATAL);
        Assert.assertNotNull(issues);
        Assert.assertEquals(0, issues.size());

        String value = "foobar";
        issues = validator.validate(PropertyHelperUtils.TEST_UUIDS[0], value, Severity.FATAL);
        Assert.assertNotNull(issues);
        Assert.assertEquals(1, issues.size());
        Assert.assertEquals(validator.getDefaultInvalidMessage(value), issues.first().getMessage());
        Assert.assertEquals(PropertyHelperUtils.TEST_UUIDS[0], issues.first().getEntityId());
        Assert.assertEquals(TestExtension.class, issues.first().getExtension());
        Assert.assertEquals(TestExtension.PROPERTY_STR, issues.first().getPropertyId());
        Assert.assertEquals(Severity.FATAL, issues.first().getSeverity());

        issues = validator.validate(PropertyHelperUtils.TEST_UUIDS[0], null, Severity.FATAL);
        Assert.assertNotNull(issues);
        Assert.assertEquals(0, issues.size());

        issues = validator.validate(PropertyHelperUtils.TEST_UUIDS[0], "", Severity.FATAL);
        Assert.assertNotNull(issues);
        Assert.assertEquals(0, issues.size());

        validator.setValueRequired(true);
        issues = validator.validate(PropertyHelperUtils.TEST_UUIDS[0], null, Severity.FATAL);
        Assert.assertNotNull(issues);
        Assert.assertEquals(1, issues.size());

        issues = validator.validate(PropertyHelperUtils.TEST_UUIDS[0], "", Severity.FATAL);
        Assert.assertNotNull(issues);
        Assert.assertEquals(1, issues.size());
    }

    @Test
    public void testInvalidMessages() throws Exception {
        String value = "foobar";

        URLValidator validator = new URLValidator(Severity.FATAL, TestExtension.class, TestExtension.PROPERTY_STR);
        SortedSet<Issue> issues = validator.validate(PropertyHelperUtils.TEST_UUIDS[0], value, Severity.FATAL);
        Assert.assertEquals(validator.getDefaultInvalidMessage(value), issues.first().getMessage());

        validator = new URLValidator(Severity.FATAL, TestExtension.class, TestExtension.PROPERTY_STR, "Foobar Caption");
        issues = validator.validate(PropertyHelperUtils.TEST_UUIDS[0], value, Severity.FATAL);
        Assert.assertEquals(validator.getInvalidMessageFromCaption(value), issues.first().getMessage());

        validator = new URLValidator(Severity.FATAL, TestExtension.class, TestExtension.PROPERTY_STR, "Invalid Foobar",
                "Undefined Foobar");
        issues = validator.validate(PropertyHelperUtils.TEST_UUIDS[0], value, Severity.FATAL);
        Assert.assertEquals("Invalid Foobar", issues.first().getMessage());
    }

    @Test
    public void testUndefinedMessages() throws Exception {
        assertUndefinedMessages(null);
        assertUndefinedMessages("");
    }

    private void assertUndefinedMessages(String value) {
        URLValidator validator = new URLValidator(Severity.FATAL, TestExtension.class, TestExtension.PROPERTY_STR);
        validator.setValueRequired(true);
        SortedSet<Issue> issues = validator.validate(PropertyHelperUtils.TEST_UUIDS[0], value, Severity.FATAL);
        PropertyValidatorTestUtils.assertIsDefaultMessage(validator, issues.first().getMessage());

        validator = new URLValidator(Severity.FATAL, TestExtension.class, TestExtension.PROPERTY_STR, "Foobar Caption");
        validator.setValueRequired(true);
        issues = validator.validate(PropertyHelperUtils.TEST_UUIDS[0], value, Severity.FATAL);
        PropertyValidatorTestUtils.assertIsUndefinedMessageFromCaption(validator, issues.first().getMessage());

        validator = new URLValidator(Severity.FATAL, TestExtension.class, TestExtension.PROPERTY_STR, "Invalid Foobar",
                "Undefined Foobar");
        validator.setValueRequired(true);
        issues = validator.validate(PropertyHelperUtils.TEST_UUIDS[0], value, Severity.FATAL);
        Assert.assertEquals("Undefined Foobar", issues.first().getMessage());
    }
}
