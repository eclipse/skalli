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
package org.eclipse.skalli.core.extension;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.HtmlUtils;
import org.eclipse.skalli.core.extension.ProjectDescriptionValidator;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.testutil.TestUUIDs;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("nls")
public class ProjectDescriptionValidatorTest {

    private static final String ALL_TAGS = "<" + StringUtils.join(HtmlUtils.ALLOWED_TAGS, ">, <") + ">";

    @Test
    public void testIssuesFATAL() throws Exception {
        assertDescriptionInvalid("<test>", Project.FORMAT_HTML, Severity.FATAL, Severity.FATAL);
        assertDescriptionInvalid("<test>", Project.DEFAULT_FORMAT, Severity.FATAL, Severity.FATAL);
        assertDescriptionInvalid("<test>", "unknown_format", Severity.FATAL, Severity.FATAL);
        assertDescriptionInvalid("<test>", null, Severity.FATAL, Severity.FATAL);
        assertDescriptionInvalid("abc <test> def", Project.DEFAULT_FORMAT, Severity.FATAL, Severity.FATAL);

        assertDescriptionValid(ALL_TAGS, Project.FORMAT_HTML, Severity.FATAL);
        assertDescriptionValid("abc" + ALL_TAGS + "def", Project.FORMAT_HTML, Severity.FATAL);
        assertDescriptionInvalid(ALL_TAGS, Project.DEFAULT_FORMAT, Severity.FATAL, Severity.FATAL);
        assertDescriptionInvalid(ALL_TAGS, "unknown_format", Severity.FATAL, Severity.FATAL);
        assertDescriptionInvalid(ALL_TAGS, null, Severity.FATAL, Severity.FATAL);
    }

    @Test
    public void testIssuesWARNING() throws Exception {
        assertDescriptionInvalid(null, Project.FORMAT_HTML, Severity.WARNING, Severity.WARNING);
        assertDescriptionInvalid("", Project.FORMAT_HTML, Severity.WARNING, Severity.WARNING);
        assertDescriptionInvalid("    ", Project.FORMAT_HTML, Severity.WARNING, Severity.WARNING);

        assertDescriptionInvalid(null, Project.FORMAT_HTML, Severity.INFO, Severity.WARNING);
        assertDescriptionInvalid("", Project.FORMAT_HTML, Severity.INFO, Severity.WARNING);
        assertDescriptionInvalid("    ", Project.FORMAT_HTML, Severity.INFO, Severity.WARNING);
    }

    @Test
    public void testIssuesINFO() throws Exception {
        assertDescriptionInvalid(StringUtils.repeat("a", 1), Project.FORMAT_HTML, Severity.INFO, Severity.INFO);
        assertDescriptionInvalid(StringUtils.repeat("a", 10), Project.FORMAT_HTML, Severity.INFO, Severity.INFO);
        assertDescriptionInvalid(StringUtils.repeat("a", 24), Project.FORMAT_HTML, Severity.INFO, Severity.INFO);


        assertDescriptionValid(StringUtils.repeat("a", 1), Project.FORMAT_HTML, Severity.WARNING);
        assertDescriptionValid(StringUtils.repeat("a", 10), Project.FORMAT_HTML, Severity.WARNING);
        assertDescriptionValid(StringUtils.repeat("a", 24), Project.FORMAT_HTML, Severity.WARNING);
    }

    @Test
    public void testNoIssues() throws Exception {
        assertDescriptionValid(StringUtils.repeat("a", ProjectDescriptionValidator.DESCRIPTION_RECOMMENDED_LENGHT),
                Project.FORMAT_HTML, Severity.INFO);
        assertDescriptionValid(StringUtils.repeat("a", ProjectDescriptionValidator.DESCRIPTION_RECOMMENDED_LENGHT + 1),
                Project.FORMAT_HTML, Severity.INFO);
        assertDescriptionValid(StringUtils.repeat("a", ProjectDescriptionValidator.DESCRIPTION_RECOMMENDED_LENGHT + 4711),
                Project.FORMAT_HTML, Severity.INFO);

        assertDescriptionValid(null, Project.FORMAT_HTML, Severity.FATAL);
        assertDescriptionValid(null, Project.FORMAT_HTML, Severity.ERROR);
        assertDescriptionValid("", Project.FORMAT_HTML, Severity.FATAL);
        assertDescriptionValid("", Project.FORMAT_HTML, Severity.ERROR);
        assertDescriptionValid("    ", Project.FORMAT_HTML, Severity.FATAL);
        assertDescriptionValid("    ", Project.FORMAT_HTML, Severity.ERROR);
    }

    private void assertDescriptionValid(String description, String format, Severity minSeverity) {
        ProjectDescriptionValidator validator = new ProjectDescriptionValidator();
        Project project = getProject(description, format);
        Assert.assertTrue(validator.validate(project.getUuid(), project, minSeverity).isEmpty());
    }

    private void assertDescriptionInvalid(String description, String format, Severity minSeverity, Severity expected) {
        ProjectDescriptionValidator validator = new ProjectDescriptionValidator();
        Project project = getProject(description, format);
        Assert.assertEquals(expected, validator.validate(project.getUuid(), project, minSeverity).first().getSeverity());
    }

    private Project getProject(String description, String format) {
        Project project = new Project();
        project.setUuid(TestUUIDs.TEST_UUIDS[0]);
        project.setDescription(description);
        project.setDescriptionFormat(format);
        return project;
    }
}
