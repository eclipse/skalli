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

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Issuer;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.services.extension.PropertyValidator;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

/**
 * <p>Validates the description of a project.</p>
 * <p>The following issue severities are covered:
 *   <ul>
 *     <li><strong>FATAL</strong> never</li>
 *     <li><strong>ERROR</strong> if description is empty</li>
 *     <li><strong>INFO</strong> if description is rather short (< 25 characters)</li>
 *   </ul>
 * </p>
 */
public class ProjectDescriptionValidator implements Issuer, PropertyValidator {

    private static final String TXT_DESCRIPTION_EMPTY = "The project description is empty. Let others know what this is about.";
    private static final String TXT_DESCRIPTION_SHORT = "The project description is quite short. Give some more context.";
    static final String[] allowedTags = { "i", "b", "u", "a", "p", "br", "hr", "tt", "code", "pre", "em",
            "strong", "u", "s", "strike", "big", "small", "sup", "sub", "span", "blockquote", "dl", "dt", "dd", "h1",
            "h2", "h3", "h4", "h5", "h6", "cite", "q", "ul", "ol", "li" };
    private final Class<? extends ExtensionEntityBase> extension;
    private final String propertyId;

    public ProjectDescriptionValidator(final Class<? extends ExtensionEntityBase> extension, final String propertyId) {
        this.extension = extension;
        this.propertyId = propertyId;
    }

    @Override
    public SortedSet<Issue> validate(final UUID entityId, final Object value, final Severity minSeverity) {
        final SortedSet<Issue> issues = new TreeSet<Issue>();

        String description = (value != null) ? value.toString() : StringUtils.EMPTY;

        Whitelist whitelist = new Whitelist();
        whitelist.addTags(allowedTags).addAttributes(":all", "style")
                .addAttributes("a", "href", "target", "name", "title").addAttributes("ul", "type")
                .addAttributes("ol", "start", "type").addAttributes("li", "value").addAttributes("blockquote", "cite")
                .addAttributes("q", "cite").addProtocols("a", "href", "http", "https", "mailto")
                .addProtocols("blockquote", "cite", "http", "https").addProtocols("cite", "cite", "http", "https")
                .addProtocols("q", "cite", "http", "https");

        if (!Jsoup.isValid(description, whitelist)) {
            issues.add(newIssue(Severity.FATAL, entityId, composeErrorMessage()));
        }
        if (Severity.WARNING.compareTo(minSeverity) <= 0 && StringUtils.isBlank(description)) {
            issues.add(newIssue(Severity.WARNING, entityId, TXT_DESCRIPTION_EMPTY));
        } else {
            int descriptionLength = description.length();

            if (Severity.INFO.compareTo(minSeverity) <= 0 && descriptionLength < 25) {
                issues.add(newIssue(Severity.INFO, entityId, TXT_DESCRIPTION_SHORT));
            }
        }

        return issues;
    }

    private Issue newIssue(final Severity severity, final UUID entityId, final String text) {
        return new Issue(severity, ProjectDescriptionValidator.class, entityId, extension, propertyId, 0, text);
    }

    private String composeErrorMessage() {
        StringBuilder message = new StringBuilder();
        message.append("The project description contains unsupported HTML tags. Supported tags are:");
        message.append("&lt;");
        message.append(StringUtils.join(allowedTags, "&gt;, &lt;"));
        message.append("&gt;");
        return message.toString();
    }

}
