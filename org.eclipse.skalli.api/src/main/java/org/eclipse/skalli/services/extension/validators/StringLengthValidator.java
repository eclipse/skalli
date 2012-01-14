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

import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.PropertyName;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.services.extension.PropertyValidatorBase;

/**
 * Property validator to check a string for a minimal and/or maximal length.
 */
public class StringLengthValidator extends PropertyValidatorBase {

    private int minLength = 0;
    private int maxLength = Integer.MAX_VALUE;

    /**
     * Creates a string length validator.
     *
     * @param severity  the severity that should be assigned to reported issues.
     * @param extension  the class of the model extension the property belongs to, or <code>null</code>.
     * @param propertyName  the name of a property (see {@link PropertyName}).
     * @param minLength  the minimal length of the string, or -1 if there is no minimal length.
     * @param maxLength  the maximal length of the string, or -1 if there is no maximal length.
     */
    public StringLengthValidator(Severity severity, Class<? extends ExtensionEntityBase> extension, String property,
            int minLength, int maxLength) {
        this(severity, extension, property, null, minLength, maxLength);
    }

    /**
     * Creates a string length validator.
     *
     * @param severity  the severity that should be assigned to reported issues.
     * @param extension  the class of the model extension the property belongs to, or <code>null</code>.
     * @param propertyName  the name of a property (see {@link PropertyName}).
     * @param caption  the caption of the property as shown to the user in the UI form.
     * @param minLength  the minimal length of the string, or -1 if there is no minimal length.
     * @param maxLength  the maximal length of the string, or -1 if there is no maximal length.
     */
    public StringLengthValidator(Severity severity, Class<? extends ExtensionEntityBase> extension, String property,
            String caption, int minLength, int maxLength) {
        super(severity, extension, property, caption);
        setMinMaxLength(minLength, maxLength);
    }

    /**
     * Creates a string length validator.
     *
     * @param severity  the severity that should be assigned to reported issues.
     * @param extension  the class of the model extension the property belongs to, or <code>null</code>.
     * @param propertyName  the name of a property (see {@link PropertyName}).
     * @param invalidValueMessage  the message to return in case the value invalid.
     * @param undefinedValueMessage  the message to return in case the value is undefined.
     * @param minLength  the minimal length of the string, or -1 if there is no minimal length.
     * @param maxLength  the maximal length of the string, or -1 if there is no maximal length.
     */
    public StringLengthValidator(Severity severity, Class<? extends ExtensionEntityBase> extension, String property,
            String invalidValueMessage, String undefinedValueMessage, int minLength, int maxLength) {
        super(severity, extension, property, invalidValueMessage, undefinedValueMessage);
        setMinMaxLength(minLength, maxLength);
    }

    private void setMinMaxLength(int minLength, int maxLength) {
        this.minLength = minLength < 0 ? 0 : minLength;
        this.maxLength = maxLength < 0 ? Integer.MAX_VALUE : maxLength;
        if (this.maxLength == 0) {
            throw new IllegalArgumentException("maxLength = 0");
        }
        if (this.maxLength < this.minLength) {
            throw new IllegalArgumentException("maxLength < minLength");
        }
    }

    @Override
    protected String getInvalidMessageFromCaption(Object value) {
        String message = null;
        if (minLength == maxLength) {
            message = MessageFormat.format("{0} must be exactly {1} characters long", caption, minLength);
        } else if (minLength == 0 && maxLength > 0) {
            message = MessageFormat.format("{0} must be at max {1} characters long", caption, maxLength);
        } else if (minLength > 0 && maxLength == Integer.MAX_VALUE) {
            message = MessageFormat.format("{0} must be at least {1} characters long", caption, minLength);
        } else if (minLength > 0 && maxLength > 0) {
            message = MessageFormat.format("{0} must be at least {1} and at max {2} characters long", caption,
                    minLength, maxLength);
        }
        return message;
    }

    @Override
    protected String getDefaultInvalidMessage(Object value) {
        String message = null;
        if (minLength == maxLength) {
            message = MessageFormat.format("Value of property ''{0}'' must be exactly {1} characters long",
                    propertyName, minLength);
        } else if (minLength == 0 && maxLength > 0) {
            message = MessageFormat.format("Value of property ''{0}'' must be at max {1} characters long",
                    propertyName, maxLength);
        } else if (minLength > 0 && maxLength == Integer.MAX_VALUE) {
            message = MessageFormat.format("Value of property ''{0}'' must be at least {1} characters long",
                    propertyName, minLength);
        } else if (minLength > 0 && maxLength > 0) {
            message = MessageFormat.format(
                    "Value of property ''{0}'' must be at least {1} and at max {2} characters long", propertyName,
                    minLength, maxLength);
        }
        return message;
    }

    @Override
    public boolean isValid(UUID entity, Object value) {
        int len = value.toString().length();
        return (minLength == 0 || len >= minLength) && (maxLength == Integer.MAX_VALUE || len <= maxLength);
    }
}
