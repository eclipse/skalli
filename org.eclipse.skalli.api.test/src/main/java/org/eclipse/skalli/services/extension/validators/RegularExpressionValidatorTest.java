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

import java.util.SortedSet;

import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.services.extension.PropertyValidatorTestUtils;
import org.eclipse.skalli.testutil.TestUUIDs;
import org.eclipse.skalli.testutil.TestExtension;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("nls")
public class RegularExpressionValidatorTest {

    @Test
    public void testIsValid() throws Exception {
        RegularExpressionValidator validator = new RegularExpressionValidator(Severity.FATAL, TestExtension.class,
                TestExtension.PROPERTY_STR, "proj.*");

        Assert.assertTrue(validator.isValid(TestUUIDs.TEST_UUIDS[0], "project"));
        Assert.assertTrue(validator.isValid(TestUUIDs.TEST_UUIDS[0], "proj"));
        Assert.assertTrue(validator.isValid(TestUUIDs.TEST_UUIDS[0], "proj6%7ä"));
        Assert.assertFalse(validator.isValid(TestUUIDs.TEST_UUIDS[0], "foobar"));
        Assert.assertFalse(validator.isValid(TestUUIDs.TEST_UUIDS[0], "pr"));
    }

    @Test
    public void testValidate() throws Exception {
        RegularExpressionValidator validator = new RegularExpressionValidator(Severity.FATAL, TestExtension.class,
                TestExtension.PROPERTY_STR, "proj.*");

        SortedSet<Issue> issues = validator.validate(TestUUIDs.TEST_UUIDS[0], "project", Severity.FATAL);
        Assert.assertNotNull(issues);
        Assert.assertEquals(0, issues.size());

        String value = "foobar";
        issues = validator.validate(TestUUIDs.TEST_UUIDS[0], value, Severity.FATAL);
        Assert.assertNotNull(issues);
        Assert.assertEquals(1, issues.size());
        Assert.assertEquals(validator.getDefaultInvalidMessage(value), issues.first().getMessage());
        Assert.assertEquals(TestUUIDs.TEST_UUIDS[0], issues.first().getEntityId());
        Assert.assertEquals(TestExtension.class, issues.first().getExtension());
        Assert.assertEquals(TestExtension.PROPERTY_STR, issues.first().getPropertyId());
        Assert.assertEquals(Severity.FATAL, issues.first().getSeverity());

        issues = validator.validate(TestUUIDs.TEST_UUIDS[0], null, Severity.FATAL);
        Assert.assertNotNull(issues);
        Assert.assertEquals(0, issues.size());

        issues = validator.validate(TestUUIDs.TEST_UUIDS[0], "", Severity.FATAL);
        Assert.assertNotNull(issues);
        Assert.assertEquals(0, issues.size());

        validator.setValueRequired(true);
        issues = validator.validate(TestUUIDs.TEST_UUIDS[0], null, Severity.FATAL);
        Assert.assertNotNull(issues);
        Assert.assertEquals(1, issues.size());

        issues = validator.validate(TestUUIDs.TEST_UUIDS[0], "", Severity.FATAL);
        Assert.assertNotNull(issues);
        Assert.assertEquals(1, issues.size());
    }

    @Test
    public void testInvalidMessages() throws Exception {
        String value = "foobar";

        RegularExpressionValidator validator = new RegularExpressionValidator(Severity.FATAL, TestExtension.class,
                TestExtension.PROPERTY_STR, "proj.*");
        SortedSet<Issue> issues = validator.validate(TestUUIDs.TEST_UUIDS[0], value, Severity.FATAL);
        Assert.assertEquals(validator.getDefaultInvalidMessage(value), issues.first().getMessage());

        validator = new RegularExpressionValidator(Severity.FATAL, TestExtension.class, TestExtension.PROPERTY_STR,
                "Foobar Caption", "proj.*");
        issues = validator.validate(TestUUIDs.TEST_UUIDS[0], value, Severity.FATAL);
        Assert.assertEquals(validator.getInvalidMessageFromCaption(value), issues.first().getMessage());

        validator = new RegularExpressionValidator(Severity.FATAL, TestExtension.class, TestExtension.PROPERTY_STR,
                "Invalid Foobar", "Undefined Foobar", "proj.*");
        issues = validator.validate(TestUUIDs.TEST_UUIDS[0], value, Severity.FATAL);
        Assert.assertEquals("Invalid Foobar", issues.first().getMessage());
    }

    @Test
    public void testUndefinedMessages() throws Exception {
        assertUndefinedMessages(null);
        assertUndefinedMessages("");
    }

    private void assertUndefinedMessages(String value) {
        RegularExpressionValidator validator = new RegularExpressionValidator(Severity.FATAL, TestExtension.class,
                TestExtension.PROPERTY_STR, "proj.*");
        validator.setValueRequired(true);
        SortedSet<Issue> issues = validator.validate(TestUUIDs.TEST_UUIDS[0], value, Severity.FATAL);
        PropertyValidatorTestUtils.assertIsDefaultMessage(validator, issues.first().getMessage());

        validator = new RegularExpressionValidator(Severity.FATAL, TestExtension.class, TestExtension.PROPERTY_STR,
                "Foobar Caption", "proj.*");
        validator.setValueRequired(true);
        issues = validator.validate(TestUUIDs.TEST_UUIDS[0], value, Severity.FATAL);
        PropertyValidatorTestUtils.assertIsUndefinedMessageFromCaption(validator, issues.first().getMessage());

        validator = new RegularExpressionValidator(Severity.FATAL, TestExtension.class, TestExtension.PROPERTY_STR,
                "Invalid Foobar", "Undefined Foobar", "proj.*");
        validator.setValueRequired(true);
        issues = validator.validate(TestUUIDs.TEST_UUIDS[0], value, Severity.FATAL);
        Assert.assertEquals("Undefined Foobar", issues.first().getMessage());
    }
}
