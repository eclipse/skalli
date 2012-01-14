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

import java.text.MessageFormat;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.PropertyName;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.services.extension.PropertyValidatorBase;

/**
 * Property validator that matches a string with a given regular expression.
 *
 */
public class RegularExpressionValidator extends PropertyValidatorBase {

    private Pattern pattern;

    /**
     * Creates a regular expression validator for a given regular expression.
     *
     * @param severity  the severity that should be assigned to reported issues.
     * @param extension  the class of the model extension the property belongs to, or <code>null</code>.
     * @param propertyName  the name of a property (see {@link PropertyName}).
     * @param regex  the {@link java.util.regexp.Pattern regular expression}.
     */
    public RegularExpressionValidator(Severity severity, Class<? extends ExtensionEntityBase> extension,
            String property, String regex) {
        this(severity, extension, property, null, regex);
    }

    /**
     * Creates a regular expression validator for a given regular expression.
     *
     * @param severity  the severity that should be assigned to reported issues.
     * @param extension  the class of the model extension the property belongs to, or <code>null</code>.
     * @param propertyName  the name of a property (see {@link PropertyName}).
     * @param caption  the caption of the property as shown to the user in the UI form.
     * @param regex  the {@link java.util.regexp.Pattern regular expression}.
     */
    public RegularExpressionValidator(Severity severity, Class<? extends ExtensionEntityBase> extension,
            String property,
            String caption, String regex) {
        super(severity, extension, property, caption);
        this.pattern = Pattern.compile(regex);
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
            String property,
            String invalidValueMessage, String undefinedValueMessage, String regex) {
        super(severity, extension, property, invalidValueMessage, undefinedValueMessage);
        this.pattern = Pattern.compile(regex);
    }

    @Override
    protected String getInvalidMessageFromCaption(Object value) {
        return MessageFormat.format("''{0}'' does not match the pattern {1}", caption, pattern);
    }

    @Override
    protected String getDefaultInvalidMessage(Object value) {
        return MessageFormat.format("Value of property ''{0}'' does not match the pattern {1}", propertyName, pattern);
    }

    @Override
    public boolean isValid(UUID extension, Object value) {
        Matcher matcher = pattern.matcher(value.toString());
        return matcher.matches();
    }
}
