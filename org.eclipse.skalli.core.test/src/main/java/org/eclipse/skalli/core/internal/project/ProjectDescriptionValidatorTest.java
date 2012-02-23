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
package org.eclipse.skalli.core.internal.project;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.testutil.PropertyHelperUtils;
import org.eclipse.skalli.testutil.TestExtension;
import org.junit.Assert;
import org.junit.Test;

public class ProjectDescriptionValidatorTest {

    @Test
    @SuppressWarnings("nls")
    public void testIssuesFATAL() throws Exception {
        ProjectDescriptionValidator validator = new ProjectDescriptionValidator(TestExtension.class,
                TestExtension.PROPERTY_STR);
        Assert.assertEquals(Severity.FATAL,
                validator.validate(PropertyHelperUtils.TEST_UUIDS[0], "<test>", Severity.FATAL).first().getSeverity());
        StringBuilder tags = new StringBuilder();
        tags.append("<");
        tags.append(StringUtils.join(ProjectDescriptionValidator.allowedTags, ">, <"));
        tags.append(">");

        Assert.assertTrue(validator.validate(PropertyHelperUtils.TEST_UUIDS[0], tags, Severity.FATAL).isEmpty());
    }

    @Test
    @SuppressWarnings("nls")
    public void testIssuesWARNING() throws Exception {
        ProjectDescriptionValidator validator = new ProjectDescriptionValidator(TestExtension.class,
                TestExtension.PROPERTY_STR);

        Assert.assertEquals(Severity.WARNING,
                validator.validate(PropertyHelperUtils.TEST_UUIDS[0], null, Severity.WARNING).first().getSeverity());
        Assert.assertEquals(Severity.WARNING,
                validator.validate(PropertyHelperUtils.TEST_UUIDS[0], "", Severity.WARNING).first().getSeverity());
        Assert.assertEquals(Severity.WARNING,
                validator.validate(PropertyHelperUtils.TEST_UUIDS[0], "    ", Severity.WARNING).first().getSeverity());

        Assert.assertEquals(Severity.WARNING, validator
                .validate(PropertyHelperUtils.TEST_UUIDS[0], null, Severity.INFO).first().getSeverity());
        Assert.assertEquals(Severity.WARNING, validator.validate(PropertyHelperUtils.TEST_UUIDS[0], "", Severity.INFO)
                .first().getSeverity());
        Assert.assertEquals(Severity.WARNING,
                validator.validate(PropertyHelperUtils.TEST_UUIDS[0], "    ", Severity.INFO).first().getSeverity());
    }

    @Test
    @SuppressWarnings("nls")
    public void testIssuesINFO() throws Exception {
        ProjectDescriptionValidator validator = new ProjectDescriptionValidator(TestExtension.class,
                TestExtension.PROPERTY_STR);

        Assert.assertEquals(Severity.INFO,
                validator.validate(PropertyHelperUtils.TEST_UUIDS[0], StringUtils.repeat("a", 1), Severity.INFO)
                        .first().getSeverity());
        Assert.assertEquals(Severity.INFO,
                validator.validate(PropertyHelperUtils.TEST_UUIDS[0], StringUtils.repeat("a", 10), Severity.INFO)
                        .first().getSeverity());
        Assert.assertEquals(Severity.INFO,
                validator.validate(PropertyHelperUtils.TEST_UUIDS[0], StringUtils.repeat("a", 24), Severity.INFO)
                        .first().getSeverity());

        Assert.assertTrue(validator.validate(PropertyHelperUtils.TEST_UUIDS[0], StringUtils.repeat("a", 1),
                Severity.WARNING).isEmpty());
        Assert.assertTrue(validator.validate(PropertyHelperUtils.TEST_UUIDS[0], StringUtils.repeat("a", 10),
                Severity.WARNING).isEmpty());
        Assert.assertTrue(validator.validate(PropertyHelperUtils.TEST_UUIDS[0], StringUtils.repeat("a", 24),
                Severity.WARNING).isEmpty());
    }

    @Test
    @SuppressWarnings("nls")
    public void testNoIssues() throws Exception {
        ProjectDescriptionValidator validator = new ProjectDescriptionValidator(TestExtension.class,
                TestExtension.PROPERTY_STR);

        Assert.assertTrue(validator.validate(PropertyHelperUtils.TEST_UUIDS[0], StringUtils.repeat("a", 25),
                Severity.INFO).isEmpty());
        Assert.assertTrue(validator.validate(PropertyHelperUtils.TEST_UUIDS[0], StringUtils.repeat("a", 26),
                Severity.INFO).isEmpty());
        Assert.assertTrue(validator.validate(PropertyHelperUtils.TEST_UUIDS[0], StringUtils.repeat("a", 101),
                Severity.INFO).isEmpty());

        // never fatal
        Assert.assertTrue(validator.validate(PropertyHelperUtils.TEST_UUIDS[0], null, Severity.FATAL).isEmpty());
        Assert.assertTrue(validator.validate(PropertyHelperUtils.TEST_UUIDS[0], StringUtils.repeat("a", 1),
                Severity.FATAL).isEmpty());
        Assert.assertTrue(validator.validate(PropertyHelperUtils.TEST_UUIDS[0], StringUtils.repeat("a", 25),
                Severity.INFO).isEmpty());
    }

}
