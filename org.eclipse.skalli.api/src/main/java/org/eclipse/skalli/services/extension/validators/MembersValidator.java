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

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.HtmlUtils;
import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Issuer;
import org.eclipse.skalli.model.Member;
import org.eclipse.skalli.model.PropertyName;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.services.extension.PropertyValidator;
import org.eclipse.skalli.services.user.UserService;
import org.eclipse.skalli.services.user.UserServices;

/**
 * Validator that checks a collection of {@link Member members} against the currently active
 * user store. Issues are created for {@link User#UNKNOWN unknown members}.
 */
public class MembersValidator implements Issuer, PropertyValidator {

    protected Severity severity;
    protected Class<? extends ExtensionEntityBase> extension;
    protected String property;
    protected String caption;

    /**
     * Creates a URL validator.
     *
     * @param severity  the severity that should be assigned to reported issues.
     * @param extension  the class of the model extension the property belongs to, or <code>null</code>.
     * @param property  the name of a property (see {@link PropertyName}).
     */
    public MembersValidator(Severity severity, Class<? extends ExtensionEntityBase> extension, String property) {
        this(severity, extension, property, null);
    }

    /**
     * Creates a URL validator.
     *
     * @param severity  the severity that should be assigned to reported issues.
     * @param extension  the class of the model extension the property belongs to, or <code>null</code>.
     * @param property  the name of a property (see {@link PropertyName}).
     * @param caption  the caption of the property as shown to the user in the UI form.
     */
    public MembersValidator(Severity severity, Class<? extends ExtensionEntityBase> extension,
            String property, String caption) {
        if (severity == null) {
            throw new IllegalArgumentException("argument 'severity' must not be null");
        }
        if (extension == null) {
            throw new IllegalArgumentException("argument 'extension' must not be null");
        }
        if (StringUtils.isBlank(property)) {
            throw new IllegalArgumentException("argument 'property' must not be null or an empty string");
        }
        this.severity = severity;
        this.extension = extension;
        this.property = property;
        this.caption = caption;
    }

    protected UserService getUserService() {
        return UserServices.getUserService();
    }

    protected String getInvalidMessageFromCaption(String userId) {
        if (StringUtils.isNotBlank(caption)) {
            return HtmlUtils.formatEscaped("{0} list references user ''{1}'' that is unknown or no longer valid. " +
                    "Either remove that user from the list or add it to the active user store.",
                    caption, userId);
        } else {
            return HtmlUtils.formatEscaped("Property ''{0}'' references user ''{1}'' that is unknown or no " +
                    "longer valid. Either remove that user from the list or add it to the active user store.",
                    property, userId);
        }
    }

    @Override
    public SortedSet<Issue> validate(UUID entity, Object value, Severity minSeverity) {
        TreeSet<Issue> issues = new TreeSet<Issue>();
        if (severity.compareTo(minSeverity) <= 0) {
            UserService userService = getUserService();
            if (userService != null) {
                if (value instanceof Collection) {
                    int item = 0;
                    for (Object entry : (Collection<?>) value) {
                        validate(issues, entity, entry, minSeverity, item, userService);
                        ++item;
                    }
                } else {
                    validate(issues, entity, value, minSeverity, 0, userService);
                }
            }
        }
        return issues;
    }

    private void validate(SortedSet<Issue> issues, UUID entity, Object value, Severity minSeverity, int item,
            UserService userService) {
        if (value instanceof Member) {
            Member member = (Member)value;
            String userId = member.getUserID();
            User user = userService.getUserById(userId);
            if (user == null || user.isUnknown()) {
                String msg = getInvalidMessageFromCaption(userId);
                issues.add(new Issue(severity, getClass(), entity, extension, property, item, msg));
            }
        }
    }
}
