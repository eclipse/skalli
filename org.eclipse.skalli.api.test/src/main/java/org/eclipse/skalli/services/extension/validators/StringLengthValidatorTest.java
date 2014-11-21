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
package org.eclipse.skalli.services.extension.validators;

import java.util.SortedSet;

import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.testutil.TestExtension;
import org.eclipse.skalli.testutil.TestUUIDs;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("nls")
public class StringLengthValidatorTest {

    @Test
    public void testIsValid() throws Exception {
        StringLengthValidator validator = new StringLengthValidator(Severity.FATAL, TestExtension.class,
                TestExtension.PROPERTY_STR, 3, 10);

        Assert.assertTrue(validator.isValid(TestUUIDs.TEST_UUIDS[0], "123"));
        Assert.assertTrue(validator.isValid(TestUUIDs.TEST_UUIDS[0], "1234567890"));
        Assert.assertFalse(validator.isValid(TestUUIDs.TEST_UUIDS[0], "12345678901"));
        Assert.assertFalse(validator.isValid(TestUUIDs.TEST_UUIDS[0], "1234567890123456789"));
        Assert.assertFalse(validator.isValid(TestUUIDs.TEST_UUIDS[0], "12"));
        Assert.assertFalse(validator.isValid(TestUUIDs.TEST_UUIDS[0], "1"));

        validator = new StringLengthValidator(Severity.FATAL, TestExtension.class, TestExtension.PROPERTY_STR, -1, 10);
        Assert.assertTrue(validator.isValid(TestUUIDs.TEST_UUIDS[0], "123"));
        Assert.assertTrue(validator.isValid(TestUUIDs.TEST_UUIDS[0], "1234567890"));
        Assert.assertFalse(validator.isValid(TestUUIDs.TEST_UUIDS[0], "12345678901"));
        Assert.assertFalse(validator.isValid(TestUUIDs.TEST_UUIDS[0], "1234567890123456789"));
        Assert.assertTrue(validator.isValid(TestUUIDs.TEST_UUIDS[0], "12"));
        Assert.assertTrue(validator.isValid(TestUUIDs.TEST_UUIDS[0], "1"));

        validator = new StringLengthValidator(Severity.FATAL, TestExtension.class, TestExtension.PROPERTY_STR, 3, -1);
        Assert.assertTrue(validator.isValid(TestUUIDs.TEST_UUIDS[0], "123"));
        Assert.assertTrue(validator.isValid(TestUUIDs.TEST_UUIDS[0], "1234567890"));
        Assert.assertTrue(validator.isValid(TestUUIDs.TEST_UUIDS[0], "12345678901"));
        Assert.assertTrue(validator.isValid(TestUUIDs.TEST_UUIDS[0], "1234567890123456789"));
        Assert.assertFalse(validator.isValid(TestUUIDs.TEST_UUIDS[0], "12"));
        Assert.assertFalse(validator.isValid(TestUUIDs.TEST_UUIDS[0], "1"));

        validator = new StringLengthValidator(Severity.FATAL, TestExtension.class, TestExtension.PROPERTY_STR, -1, -1);
        Assert.assertTrue(validator.isValid(TestUUIDs.TEST_UUIDS[0], "123"));
        Assert.assertTrue(validator.isValid(TestUUIDs.TEST_UUIDS[0], "1234567890"));
        Assert.assertTrue(validator.isValid(TestUUIDs.TEST_UUIDS[0], "12345678901"));
        Assert.assertTrue(validator.isValid(TestUUIDs.TEST_UUIDS[0], "1234567890123456789"));
        Assert.assertTrue(validator.isValid(TestUUIDs.TEST_UUIDS[0], "12"));
        Assert.assertTrue(validator.isValid(TestUUIDs.TEST_UUIDS[0], "1"));

        validator = new StringLengthValidator(Severity.FATAL, TestExtension.class, TestExtension.PROPERTY_STR, 3, 3);
        Assert.assertTrue(validator.isValid(TestUUIDs.TEST_UUIDS[0], "123"));
        Assert.assertFalse(validator.isValid(TestUUIDs.TEST_UUIDS[0], "1234567890"));
        Assert.assertFalse(validator.isValid(TestUUIDs.TEST_UUIDS[0], "12345678901"));
        Assert.assertFalse(validator.isValid(TestUUIDs.TEST_UUIDS[0], "1234567890123456789"));
        Assert.assertFalse(validator.isValid(TestUUIDs.TEST_UUIDS[0], "12"));
        Assert.assertFalse(validator.isValid(TestUUIDs.TEST_UUIDS[0], "1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsValid_tooShort() throws Exception {
        new StringLengthValidator(Severity.FATAL, TestExtension.class, TestExtension.PROPERTY_STR, -1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsValid_impossible() throws Exception {
        new StringLengthValidator(Severity.FATAL, TestExtension.class, TestExtension.PROPERTY_STR, 10, 3);
    }

    @Test
    public void testValidate() throws Exception {
        StringLengthValidator validator = new StringLengthValidator(Severity.FATAL, TestExtension.class,
                TestExtension.PROPERTY_STR, 3, 10);

        SortedSet<Issue> issues = validator.validate(TestUUIDs.TEST_UUIDS[0], "1234", Severity.FATAL);
        Assert.assertNotNull(issues);
        Assert.assertEquals(0, issues.size());

        String value = "12";
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
        String value = "12";
        StringLengthValidator validator = new StringLengthValidator(Severity.FATAL, TestExtension.class,
                TestExtension.PROPERTY_STR, 3, 10);
        SortedSet<Issue> issues = validator.validate(TestUUIDs.TEST_UUIDS[0], value, Severity.FATAL);
        String defaultInvalidMessage = validator.getDefaultInvalidMessage(value);
        Assert.assertEquals(defaultInvalidMessage, issues.first().getMessage());
        Assert.assertTrue(defaultInvalidMessage.startsWith("Value"));
        Assert.assertTrue(defaultInvalidMessage.indexOf(TestExtension.PROPERTY_STR) > 0);
        Assert.assertTrue(defaultInvalidMessage.indexOf("at least") > 0);
        Assert.assertTrue(defaultInvalidMessage.indexOf("at most") > 0);

        value = "1234567890123";
        validator = new StringLengthValidator(Severity.FATAL, TestExtension.class, TestExtension.PROPERTY_STR, 0, 10);
        issues = validator.validate(TestUUIDs.TEST_UUIDS[0], value, Severity.FATAL);
        defaultInvalidMessage = validator.getDefaultInvalidMessage(value);
        Assert.assertEquals(defaultInvalidMessage, issues.first().getMessage());
        Assert.assertTrue(defaultInvalidMessage.startsWith("Value"));
        Assert.assertTrue(defaultInvalidMessage.indexOf(TestExtension.PROPERTY_STR) > 0);
        Assert.assertFalse(defaultInvalidMessage.indexOf("at least") > 0);
        Assert.assertTrue(defaultInvalidMessage.indexOf("at most") > 0);

        value = "12";
        validator = new StringLengthValidator(Severity.FATAL, TestExtension.class, TestExtension.PROPERTY_STR, 3, -1);
        issues = validator.validate(TestUUIDs.TEST_UUIDS[0], value, Severity.FATAL);
        defaultInvalidMessage = validator.getDefaultInvalidMessage(value);
        Assert.assertEquals(defaultInvalidMessage, issues.first().getMessage());
        Assert.assertTrue(defaultInvalidMessage.startsWith("Value"));
        Assert.assertTrue(defaultInvalidMessage.indexOf(TestExtension.PROPERTY_STR) > 0);
        Assert.assertTrue(defaultInvalidMessage.indexOf("at least") > 0);
        Assert.assertFalse(defaultInvalidMessage.indexOf("at most") > 0);

        value = "12";
        validator = new StringLengthValidator(Severity.FATAL, TestExtension.class, TestExtension.PROPERTY_STR, 3, 3);
        issues = validator.validate(TestUUIDs.TEST_UUIDS[0], value, Severity.FATAL);
        defaultInvalidMessage = validator.getDefaultInvalidMessage(value);
        Assert.assertEquals(defaultInvalidMessage, issues.first().getMessage());
        Assert.assertTrue(defaultInvalidMessage.startsWith("Value"));
        Assert.assertTrue(defaultInvalidMessage.indexOf(TestExtension.PROPERTY_STR) > 0);
        Assert.assertTrue(defaultInvalidMessage.indexOf("exactly") > 0);

        value = "12";
        String caption = "String Caption";
        validator = new StringLengthValidator(Severity.FATAL, TestExtension.class, TestExtension.PROPERTY_STR, caption,
                3, 10);
        issues = validator.validate(TestUUIDs.TEST_UUIDS[0], value, Severity.FATAL);
        String invalidMessage = validator.getInvalidMessageFromCaption(value);
        Assert.assertEquals(invalidMessage, issues.first().getMessage());
        Assert.assertTrue(invalidMessage.startsWith(caption));
        Assert.assertTrue(invalidMessage.indexOf("at least") > 0);
        Assert.assertTrue(invalidMessage.indexOf("at most") > 0);

        value = "12";
        validator = new StringLengthValidator(Severity.FATAL, TestExtension.class, TestExtension.PROPERTY_STR,
                "Invalid String Length", "Undefined String", 3, 10);
        issues = validator.validate(TestUUIDs.TEST_UUIDS[0], value, Severity.FATAL);
        Assert.assertEquals("Invalid String Length", issues.first().getMessage());
    }
}
