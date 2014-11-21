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

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.skalli.commons.HtmlUtils;
import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.PropertyName;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.services.extension.PropertyValidatorBase;

/**
 * Property validator that matches a string with a given regular expression.
 * This validator can be applied to single-valued properties and to {@link java.util.Collection collections}.
 */
public class RegularExpressionValidator extends PropertyValidatorBase {

    private Pattern pattern;

    /**
     * Creates a regular expression validator for a given regular expression.
     *
     * @param severity  the severity that should be assigned to reported issues.
     * @param extension  the class of the model extension the property belongs to, or <code>null</code>.
     * @param property  the name of a property (see {@link PropertyName}).
     * @param pattern  the {@link java.util.regexp.Pattern regular expression}.
     */
    public RegularExpressionValidator(Severity severity, Class<? extends ExtensionEntityBase> extension,
            String property, String pattern) {
        this(severity, extension, property, null, pattern);
    }

    /**
     * Creates a regular expression validator for a given regular expression.
     *
     * @param severity  the severity that should be assigned to reported issues.
     * @param extension  the class of the model extension the property belongs to, or <code>null</code>.
     * @param property  the name of a property (see {@link PropertyName}).
     * @param caption  the caption of the property as shown to the user in the UI form.
     * @param pattern  the {@link java.util.regexp.Pattern regular expression}.
     */
    public RegularExpressionValidator(Severity severity, Class<? extends ExtensionEntityBase> extension,
            String property, String caption, String pattern) {
        super(severity, extension, property, caption);
        this.pattern = Pattern.compile(pattern);
    }

    /**
     * Creates a regular expression validator for a given regular expression.
     *
     * @param severity  the severity that should be assigned to reported issues.
     * @param extension  the class of the model extension the property belongs to, or <code>null</code>.
     * @param propertyName  the name of a property (see {@link PropertyName}).
     * @param invalidValueMessage  the message to return in case the value invalid.
     * @param undefinedValueMessage  the message to return in case the value is undefined.
     * @param regex  the {@link java.util.regexp.Pattern regular expression}.
     */
    public RegularExpressionValidator(Severity severity, Class<? extends ExtensionEntityBase> extension,
            String property, String invalidValueMessage, String undefinedValueMessage, String pattern) {
        super(severity, extension, property, invalidValueMessage, undefinedValueMessage);
        this.pattern = Pattern.compile(pattern);
    }

    @Override
    protected String getInvalidMessageFromCaption(Object value) {
        return HtmlUtils.formatEscaped("{0}: ''{1}'' does not match the pattern ''{2}''", caption, value, pattern);
    }

    @Override
    protected String getDefaultInvalidMessage(Object value) {
        return HtmlUtils.formatEscaped("{0}: ''{1}'' does not match the pattern ''{2}''", property, value, pattern);
    }

    @Override
    public boolean isValid(UUID extension, Object value) {
        Matcher matcher = pattern.matcher(value.toString());
        return matcher.matches();
    }
}
