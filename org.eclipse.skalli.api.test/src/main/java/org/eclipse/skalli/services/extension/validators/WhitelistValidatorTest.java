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

import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.testutil.PropertyHelperUtils;
import org.eclipse.skalli.testutil.TestExtension;
import org.jsoup.safety.Whitelist;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("nls")
public class WhitelistValidatorTest {

    @Test
    public void testIsValid() throws Exception {
        WhitelistValidator validator = new WhitelistValidator(Severity.FATAL, TestExtension.class,
                TestExtension.PROPERTY_STR, Whitelist.basic());

        Assert.assertTrue(validator.isValid(PropertyHelperUtils.TEST_UUIDS[0], "foobar"));
        Assert.assertTrue(validator.isValid(PropertyHelperUtils.TEST_UUIDS[0], "<b>foobar</b>"));
        Assert.assertFalse(validator.isValid(PropertyHelperUtils.TEST_UUIDS[0], "<script>alert('Gotcha!')</script>"));
    }

    @Test
    public void testValidate() throws Exception {
        assertNoIssues("foobar");
        assertNoIssues("<b>foobar</b>");
        assertNoIssues("");
        assertNoIssues(null);

        assertHasIssue("<script>alert('Gotcha!')</script>");
    }

    private void assertNoIssues(String s) {
        WhitelistValidator validator = new WhitelistValidator(Severity.FATAL, TestExtension.class,
                TestExtension.PROPERTY_STR, Whitelist.basic());
        SortedSet<Issue> issues = validator.validate(PropertyHelperUtils.TEST_UUIDS[0], s, Severity.FATAL);
        Assert.assertNotNull(issues);
        Assert.assertEquals(0, issues.size());
    }

    private void assertHasIssue(String s) {
        WhitelistValidator validator = new WhitelistValidator(Severity.FATAL, TestExtension.class,
                TestExtension.PROPERTY_STR, Whitelist.basic());
        SortedSet<Issue> issues = validator.validate(PropertyHelperUtils.TEST_UUIDS[0], s, Severity.FATAL);
        Assert.assertNotNull(issues);
        Assert.assertEquals(1, issues.size());
        Assert.assertEquals(PropertyHelperUtils.TEST_UUIDS[0], issues.first().getEntityId());
        Assert.assertEquals(TestExtension.class, issues.first().getExtension());
        Assert.assertEquals(TestExtension.PROPERTY_STR, issues.first().getPropertyId());
        Assert.assertEquals(Severity.FATAL, issues.first().getSeverity());
        Assert.assertTrue(issues.first().getMessage().contains(StringEscapeUtils.escapeHtml(s)));
    }

}
