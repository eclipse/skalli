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
package org.eclipse.skalli.services.extension;

import java.util.SortedSet;
import java.util.UUID;

import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.testutil.TestUUIDs;
import org.eclipse.skalli.testutil.TestExtension;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("nls")
public class PropertyValidatorBaseTest {

    class TestPropertyValidator extends PropertyValidatorBase {

        public TestPropertyValidator(Class<? extends ExtensionEntityBase> extension, String propertyName,
                String invalidValueMessage, String undefinedValueMessage) {
            super(Severity.FATAL, extension, propertyName, invalidValueMessage, undefinedValueMessage);
        }

        public TestPropertyValidator(Class<? extends ExtensionEntityBase> extension, String propertyName,
                String caption) {
            super(Severity.FATAL, extension, propertyName, caption);
        }

        public TestPropertyValidator(Class<? extends ExtensionEntityBase> extension, String propertyName) {
            super(Severity.FATAL, extension, propertyName);
        }

        @Override
        protected boolean isValid(UUID entity, Object value) {
            return "valid".equals(value) ? true : false;
        }
    }

    @Test
    public void testDefaultMessages() {
        TestPropertyValidator validator = new TestPropertyValidator(TestExtension.class, TestExtension.PROPERTY_STR);
        Assert.assertEquals(TestExtension.PROPERTY_STR, validator.property);
        Assert.assertEquals(TestExtension.class, validator.extension);
        Assert.assertNull(validator.caption);
        Assert.assertNull(validator.invalidValueMessage);
        Assert.assertNull(validator.undefinedValueMessage);

        assertValidIssue(validator);
        Issue issue = assertInvalidIssue(validator, "value");
        Assert.assertEquals(validator.getDefaultInvalidMessage("value"), issue.getMessage());

        issue = assertUndefinedIssue(validator, false);
        Assert.assertEquals(validator.getDefaultUndefinedMessage(), issue.getMessage());
        issue = assertUndefinedIssue(validator, true);
        Assert.assertEquals(validator.getDefaultUndefinedMessage(), issue.getMessage());
    }

    @Test
    public void testInvalidMessageFromCaption() {
        TestPropertyValidator validator = new TestPropertyValidator(TestExtension.class, TestExtension.PROPERTY_STR,
                "CAPTION");
        Assert.assertEquals(TestExtension.PROPERTY_STR, validator.property);
        Assert.assertEquals(TestExtension.class, validator.extension);
        Assert.assertEquals("CAPTION", validator.caption);
        Assert.assertNull(validator.invalidValueMessage);
        Assert.assertNull(validator.undefinedValueMessage);

        assertValidIssue(validator);
        Issue issue = assertInvalidIssue(validator, "value");
        Assert.assertEquals(validator.getInvalidMessageFromCaption("value"), issue.getMessage());

        issue = assertUndefinedIssue(validator, false);
        Assert.assertEquals(validator.getUndefinedMessageFromCaption(), issue.getMessage());
        issue = assertUndefinedIssue(validator, true);
        Assert.assertEquals(validator.getUndefinedMessageFromCaption(), issue.getMessage());
    }

    @Test
    public void testCustomInvalidMessage() {
        TestPropertyValidator validator = new TestPropertyValidator(TestExtension.class, TestExtension.PROPERTY_STR,
                "invalid {0}", "undefined {0}"); //$NON-NLS-1$ //$NON-NLS-2$
        Assert.assertEquals(TestExtension.PROPERTY_STR, validator.property);
        Assert.assertEquals(TestExtension.class, validator.extension);
        Assert.assertNull(validator.caption);
        Assert.assertEquals("invalid {0}", validator.invalidValueMessage);
        Assert.assertEquals("undefined {0}", validator.undefinedValueMessage);

        assertValidIssue(validator);
        Issue issue = assertInvalidIssue(validator, "value");
        Assert.assertEquals("invalid value", issue.getMessage());

        issue = assertUndefinedIssue(validator, false);
        Assert.assertEquals("undefined {0}", issue.getMessage());
        issue = assertUndefinedIssue(validator, true);
        Assert.assertEquals("undefined {0}", issue.getMessage());
    }

    @Test
    public void testHtmlEnocdingOfMessage() {
        final String VALUE = "<script>alert('Gotcha!')</script>";
        final String ENCODED_VALUE = "&lt;script&gt;alert('Gotcha!')&lt;/script&gt;";

        TestPropertyValidator validator = new TestPropertyValidator(TestExtension.class, TestExtension.PROPERTY_STR);
        Issue issue = assertInvalidIssue(validator, VALUE);
        Assert.assertTrue(issue.getMessage().contains(ENCODED_VALUE));

        validator = new TestPropertyValidator(TestExtension.class, TestExtension.PROPERTY_STR, "CAPTION");
        issue = assertInvalidIssue(validator, VALUE);
        Assert.assertTrue(issue.getMessage().contains(ENCODED_VALUE));

        validator = new TestPropertyValidator(TestExtension.class, TestExtension.PROPERTY_STR,
                "invalid {0}", "undefined {0}"); //$NON-NLS-1$ //$NON-NLS-2$
        issue = assertInvalidIssue(validator, VALUE);
        Assert.assertEquals("invalid " + ENCODED_VALUE, issue.getMessage());
    }

    private void assertValidIssue(TestPropertyValidator validator) {
        SortedSet<Issue> issues = validator.validate(TestUUIDs.TEST_UUIDS[0], "valid", Severity.FATAL);
        Assert.assertNotNull(issues);
        Assert.assertEquals(0, issues.size());
    }

    private Issue assertInvalidIssue(TestPropertyValidator validator, String testValue) {
        // validation failes => assert issue set with one entry, assert issue correctly initialized
        SortedSet<Issue> issues = validator.validate(TestUUIDs.TEST_UUIDS[0], testValue, Severity.ERROR);
        Assert.assertNotNull(issues);
        Assert.assertEquals(1, issues.size());
        Issue issue = issues.first();
        Assert.assertNotNull(issue);
        Assert.assertEquals(Severity.FATAL, issue.getSeverity());
        Assert.assertEquals(TestPropertyValidator.class, issue.getIssuer());
        Assert.assertEquals(TestUUIDs.TEST_UUIDS[0], issue.getEntityId());
        Assert.assertEquals(TestExtension.class, issue.getExtension());
        Assert.assertEquals(TestExtension.PROPERTY_STR, issue.getPropertyId());
        return issue;
    }

    private Issue assertUndefinedIssue(TestPropertyValidator validator, boolean emptyString) {
        validator.setValueRequired(true);
        SortedSet<Issue> issues = validator.validate(TestUUIDs.TEST_UUIDS[0], emptyString ? "" : null,
                Severity.FATAL);
        Assert.assertNotNull(issues);
        Assert.assertEquals(1, issues.size());
        Issue issue = issues.first();
        Assert.assertNotNull(issue);
        Assert.assertEquals(Severity.FATAL, issue.getSeverity());
        Assert.assertEquals(TestPropertyValidator.class, issue.getIssuer());
        Assert.assertEquals(TestUUIDs.TEST_UUIDS[0], issue.getEntityId());
        Assert.assertEquals(TestExtension.class, issue.getExtension());
        Assert.assertEquals(TestExtension.PROPERTY_STR, issue.getPropertyId());
        validator.setValueRequired(false);
        return issue;
    }
}
