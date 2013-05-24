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
import org.eclipse.skalli.testutil.TestExtension;
import org.eclipse.skalli.testutil.TestUUIDs;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("nls")
public class EmailValidatorTest {

    @Test
    public void testIsValid() throws Exception {
        EmailValidator validator = new EmailValidator(Severity.FATAL, TestExtension.class, TestExtension.PROPERTY_STR,
                "proj.*");

        Assert.assertTrue(validator.isValid(TestUUIDs.TEST_UUIDS[0], "homer@springfield.org"));
        Assert.assertTrue(validator.isValid(TestUUIDs.TEST_UUIDS[0], "Homer@springfield.org"));
        Assert.assertTrue(validator.isValid(TestUUIDs.TEST_UUIDS[0], "Homer@server.springfield.org"));
        Assert.assertTrue(validator.isValid(TestUUIDs.TEST_UUIDS[0], "homer.simpson@springfield.org"));
        Assert.assertTrue(validator.isValid(TestUUIDs.TEST_UUIDS[0], "homer-simpson@springfield.org"));
        Assert.assertTrue(validator.isValid(TestUUIDs.TEST_UUIDS[0], "homer42@springfield.org"));
        Assert.assertTrue(validator.isValid(TestUUIDs.TEST_UUIDS[0], "Hom.Er._.4-2@springfield.org"));
        Assert.assertTrue(validator.isValid(TestUUIDs.TEST_UUIDS[0], "homer@[127.0.0.1]"));
        Assert.assertTrue(validator.isValid(TestUUIDs.TEST_UUIDS[0], "_homer@springfield.org"));
        Assert.assertTrue(validator.isValid(TestUUIDs.TEST_UUIDS[0], "-homer@springfield.org"));
        Assert.assertTrue(validator.isValid(TestUUIDs.TEST_UUIDS[0], "homer+@springfield.org"));
        Assert.assertTrue(validator.isValid(TestUUIDs.TEST_UUIDS[0], "homer&marge@springfield.org"));
        Assert.assertTrue(validator.isValid(TestUUIDs.TEST_UUIDS[0],
                "homer!#$%&'*+-/=?^_`{|}~blab.lubb@springfield.org"));
        Assert.assertFalse(validator.isValid(TestUUIDs.TEST_UUIDS[0], ".homer@springfield.org"));
        Assert.assertFalse(validator.isValid(TestUUIDs.TEST_UUIDS[0], "ho,mer@springfield.org"));
        Assert.assertFalse(validator.isValid(TestUUIDs.TEST_UUIDS[0], "homer@springfield.x"));
        Assert.assertFalse(validator.isValid(TestUUIDs.TEST_UUIDS[0], "homer°marge@springfield.org"));
    }

    @Test
    public void testValidate() throws Exception {
        EmailValidator validator = new EmailValidator(Severity.FATAL, TestExtension.class, TestExtension.PROPERTY_STR);

        SortedSet<Issue> issues = validator.validate(TestUUIDs.TEST_UUIDS[0], "homer@springfield.org",
                Severity.FATAL);
        Assert.assertNotNull(issues);
        Assert.assertEquals(0, issues.size());

        String value = ".homer@springfield.org";
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
    public void testInvalidMessages() {
        String value = ".homer@springfield.org";

        EmailValidator validator = new EmailValidator(Severity.FATAL, TestExtension.class, TestExtension.PROPERTY_STR,
                "EMail");
        SortedSet<Issue> issues = validator.validate(TestUUIDs.TEST_UUIDS[0], value, Severity.FATAL);
        Assert.assertEquals(validator.getInvalidMessageFromCaption(value), issues.first().getMessage());

        validator = new EmailValidator(Severity.FATAL, TestExtension.class, TestExtension.PROPERTY_STR,
                "Invalid EMail", "Undefined EMail");
        issues = validator.validate(TestUUIDs.TEST_UUIDS[0], value, Severity.FATAL);
        Assert.assertEquals("Invalid EMail", issues.first().getMessage());
    }

    @Test
    public void testUndefinedMessages() throws Exception {
        assertUndefinedMessages(null);
        assertUndefinedMessages("");
    }

    private void assertUndefinedMessages(String value) {
        EmailValidator validator = new EmailValidator(Severity.FATAL, TestExtension.class, TestExtension.PROPERTY_STR);
        validator.setValueRequired(true);
        SortedSet<Issue> issues = validator.validate(TestUUIDs.TEST_UUIDS[0], value, Severity.FATAL);
        PropertyValidatorTestUtils.assertIsDefaultMessage(validator, issues.first().getMessage());

        validator = new EmailValidator(Severity.FATAL, TestExtension.class, TestExtension.PROPERTY_STR, "EMail");
        validator.setValueRequired(true);
        issues = validator.validate(TestUUIDs.TEST_UUIDS[0], value, Severity.FATAL);
        PropertyValidatorTestUtils.assertIsUndefinedMessageFromCaption(validator, issues.first().getMessage());

        validator = new EmailValidator(Severity.FATAL, TestExtension.class, TestExtension.PROPERTY_STR,
                "Invalid EMail", "Undefined EMail");
        validator.setValueRequired(true);
        issues = validator.validate(TestUUIDs.TEST_UUIDS[0], value, Severity.FATAL);
        Assert.assertEquals("Undefined EMail", issues.first().getMessage());
    }
}
