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
package org.eclipse.skalli.model.core.internal;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.HtmlUtils;
import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Issuer;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.services.extension.ExtensionValidator;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

/**
 * <p>Validates the description of a project.</p>
 * <p>The following issue severities are covered:
 *   <ul>
 *     <li><strong>FATAL</strong> if the description contains forbidden HTML tags</li>
 *     <li><strong>ERROR</strong> if description is empty</li>
 *     <li><strong>INFO</strong> if description is rather short (< 25 characters)</li>
 *   </ul>
 * </p>
 */
public class ProjectDescriptionValidator implements Issuer, ExtensionValidator<Project> {

    public static final int DESCRIPTION_RECOMMENDED_LENGHT = 25;

    private static final String TXT_DESCRIPTION_EMPTY = "The project description is empty. Let others know what this is about.";
    private static final String TXT_DESCRIPTION_SHORT = "The project description is quite short. Give some more context.";
    private static final String TXT_NO_TAGS_ALLOWED = "The project description must not contain any HTML tags for description " +
            "format 'text'. Remove all tags or select another description format.";
    private static final String TXT_ALLOWED_TAGS = "The project description contains unsupported HTML tags. " +
            "Supported tags are: &lt;" + StringUtils.join(HtmlUtils.ALLOWED_TAGS, "&gt;, &lt;") + "&gt;.";

    @Override
    public Class<Project> getExtensionClass() {
        return Project.class;
    }

    @SuppressWarnings("nls")
    @Override
    public SortedSet<Issue> validate(UUID entity, ExtensionEntityBase extension, Severity minSeverity) {
        SortedSet<Issue> issues = new TreeSet<Issue>();
        Project project = (Project)extension;

        String description = project.getDescription();
        if (description == null) {
            description = StringUtils.EMPTY;
        }

        Whitelist whitelist = null;
        String fatalMessage = null;
        String format = project.getDescriptionFormat();
        if ("html".equals(format)) {
            whitelist = HtmlUtils.getWhiteList();
            fatalMessage = TXT_ALLOWED_TAGS;
        } else {
            whitelist = Whitelist.none();
            fatalMessage = TXT_NO_TAGS_ALLOWED;
        }
        if (!Jsoup.isValid(description, whitelist)) {
            issues.add(newIssue(Severity.FATAL, entity, fatalMessage));
        }

        if (Severity.WARNING.compareTo(minSeverity) <= 0 && StringUtils.isBlank(description)) {
            issues.add(newIssue(Severity.WARNING, entity, TXT_DESCRIPTION_EMPTY));
        } else {
            int descriptionLength = description.length();
            if (Severity.INFO.compareTo(minSeverity) <= 0 && descriptionLength < DESCRIPTION_RECOMMENDED_LENGHT) {
                issues.add(newIssue(Severity.INFO, entity, TXT_DESCRIPTION_SHORT));
            }
        }

        return issues;
    }

    private Issue newIssue(final Severity severity, final UUID entityId, final String text) {
        return new Issue(severity, ProjectDescriptionValidator.class, entityId, null, Project.PROPERTY_DESCRIPTION, 0, text);
    }
}
