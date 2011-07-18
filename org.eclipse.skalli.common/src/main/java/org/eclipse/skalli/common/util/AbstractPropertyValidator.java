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
package org.eclipse.skalli.common.util;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import org.eclipse.skalli.model.ext.ExtensionEntityBase;
import org.eclipse.skalli.model.ext.Issue;
import org.eclipse.skalli.model.ext.Issuer;
import org.eclipse.skalli.model.ext.PropertyName;
import org.eclipse.skalli.model.ext.PropertyValidator;
import org.eclipse.skalli.model.ext.Severity;

/**
 * Abstract base class for the implementation of {@link PropertyValidator property validators}.
 * It simplifies the implementation of property validators that perform simple yes/no validations,
 * for example check whether a given value matches a regular expression or has a certain minimum
 * length.
 * Validators derived from this class must implement {@link AbstractPropertyValidator#isValid(Object, Severity)}.
 */
public abstract class AbstractPropertyValidator implements PropertyValidator, Issuer {

    protected Class<? extends ExtensionEntityBase> extension;
    protected String propertyName;
    protected String caption;
    protected String invalidValueMessage;
    protected String undefinedValueMessage;
    protected boolean valueRequired;
    protected Severity severity;

    /**
     * Returns a validator instance for a property of a given <code>entity</code>.
     * Derived classes using this constructor should overwrite {@link #getDefaultMessage(Object)}
     * or {@link #getMessage(Object)} to provide a meaningful "validation failed" message.
     *
     * @param severity  the severity that should be assigned to reported issues.
     * @param extension  the class of the model extension the property belongs to.
     * @param propertyName  the name of a property (see {@link PropertyName}).
     */
    protected AbstractPropertyValidator(Severity severity, Class<? extends ExtensionEntityBase> extension,
            String propertyName) {
        if (severity == null) {
            throw new IllegalArgumentException("argument 'severity' must not be null");
        }
        if (extension == null) {
            throw new IllegalArgumentException("argument 'extension' must not be null");
        }
        if (StringUtils.isBlank(propertyName)) {
            throw new IllegalArgumentException("argument 'propertyName' must not be null or an empty string");
        }
        this.severity = severity;
        this.extension = extension;
        this.propertyName = propertyName;
    }

    /**
     * Returns a validator instance for a property of a given <code>entity</code>.
     * The <code>caption</code> is used to construct "validation failed" messages
     * of the form <tt>"&lt;value&gt; is not a valid &gt;caption&gt;"</tt> or
     * <tt>"&gt;caption&gt; must have a value"</tt>.<br>
     *
     * @param severity  the severity that should be assigned to reported issues.
     * @param extension  the class of the model extension the property belongs to, or <code>null</code>.
     * @param propertyName  the name of a property (see {@link PropertyName}).
     * @param caption  the caption of the property as shown to the user in the UI form.
     */
    protected AbstractPropertyValidator(Severity severity, Class<? extends ExtensionEntityBase> extension,
            String propertyName, String caption) {
        this(severity, extension, propertyName);
        this.caption = caption;
    }

    /**
     * Returns a validator instance for a property of a given <code>entity</code>.
     * This constructor allows to define custom "validation failed" and "value undefined"
     * messages, respectively. Both <code>invalidValueMessage</code> and <code>undefinedValueMessage</code>
     * may contain the placeholder <tt>"{0}"</tt> which then is substituted by the actual value
     * of the property in case of a validation failing.
     *
     * @param severity  the severity that should be assigned to reported issues.
     * @param extension  the class of the model extension the property belongs to, or <code>null</code>.
     * @param propertyName  the name of a property (see {@link PropertyName}).
     * @param invalidValueMessage  the message to return in case the value invalid.
     * @param undefinedValueMessage  the message to return in case the value is undefined.
     */
    protected AbstractPropertyValidator(Severity severity, Class<? extends ExtensionEntityBase> extension,
            String propertyName, String invalidValueMessage, String undefinedValueMessage) {
        this(severity, extension, propertyName);
        this.invalidValueMessage = invalidValueMessage;
        this.undefinedValueMessage = undefinedValueMessage;
    }

    /**
     * Returns <code>true</code>, if the validation should fail in case
     * the value passed to {@link #validate(UUID,Object,Severity)} is <code>null</code>
     * or an empty string.
     */
    public boolean isValueRequired() {
        return valueRequired;
    }

    /**
     * Specifies whether the validation should fail in case the value passed to
     * {@link #validate(UUID,Object,Severity)} is <code>null</code> or an empty string.
     *
     * @param valueRequired  if <code>true</code>, a value is required for the property.
     */
    public void setValueRequired(boolean valueRequired) {
        this.valueRequired = valueRequired;
    }

    /**
     * Returns a custom "invalid value" validation message.
     * In case a caption has been defined, {@link #getInvalidMessageFromCaption(Object)} is called
     * to construct a message. Otherwise {@link #getCustomInvalidMessage(Object)} is called to retrieve
     * a custom "validation failed" message.<br>
     * Replaces <tt>"{0}"</tt> placeholders in custom messages with <code>value</code>.
     *
     * @param value  the value of the property.
     * @return  a validation message, if either a caption or custom messages have been
     * defined, <code>null</code> otherwise.
     */
    protected String getInvalidMessage(Object value) {
        String message = null;
        if (StringUtils.isNotBlank(caption)) {
            message = getInvalidMessageFromCaption(value);
        } else {
            message = getCustomInvalidMessage(value);
            if (StringUtils.isNotBlank(message)) {
                int n = message.indexOf("{0}"); //$NON-NLS-1$
                if (n >= 0) {
                    message = MessageFormat.format(message, value);
                }
            }
        }
        return message;
    }

    /**
     * Returns a custom "value undefined" validation message.
     * In case a caption has been defined, {@link #getUndefinedMessageFromCaption()} is called
     * to construct a message. Otherwise {@link #getCustomUndefinedMessage()} is called to retrieve
     * a custom "value undefined" message.
     *
     * @return  a validation message, if either a caption or custom messages have been
     * defined, <code>null</code> otherwise.
     */
    protected String getUndefinedMessage() {
        String message = null;
        if (StringUtils.isNotBlank(caption)) {
            message = getUndefinedMessageFromCaption();
        } else {
            message = getCustomUndefinedMessage();
        }
        return message;
    }

    /**
     * Constructs a "invalid value" message from the caption of the property.
     * @param value  the value of the property.
     */
    protected String getInvalidMessageFromCaption(Object value) {
        return MessageFormat.format("''{0}'' is not a valid {1}", value, caption);
    }

    /**
     * Constructs a "value undefined" message from the caption of the property.
     */
    protected String getUndefinedMessageFromCaption() {
        return MessageFormat.format("{0} must have a value", caption);
    }

    /**
     * Returns a custom "invalid value" message.
     * @param value  the value of the property.
     */
    protected String getCustomInvalidMessage(Object value) {
        return invalidValueMessage;
    }

    /**
     * Returns a custom "value undefined" message.
     */
    protected String getCustomUndefinedMessage() {
        return undefinedValueMessage;
    }

    /**
     * Returns a default "invalid value" message constructed from the
     * property name and the actual property value.
     * @param value  the value of the property.
     */
    protected String getDefaultInvalidMessage(Object value) {
        return extension != null ?
                MessageFormat.format("''{0}'' is not a valid value for property ''{1}'' of extension ''{2}''",
                        value, propertyName, extension.getName()) :
                MessageFormat.format("''{0}'' is not a valid value for property ''{1}''",
                        value, propertyName);
    }

    /**
     * Returns a default "value undefined" message constructed from the
     * property name and the actual property value.
     */
    protected String getDefaultUndefinedMessage() {
        return extension != null ?
                MessageFormat.format("Property ''{0}'' of extension ''{1}'' must have a value",
                        propertyName, extension.getName()) :
                MessageFormat.format("Property ''{0}'' must have a value",
                        propertyName);
    }

    /**
     * Returns <code>true</code> if the given <code>value</code> is <code>null</code>
     * or, in case of a string, an empty string.
     *
     * @param value  the value to check.
     */
    protected boolean isUndefinedOrBlank(Object value) {
        if (value instanceof String) {
            return StringUtils.isBlank((String) value);
        }
        return value == null;
    }

    /**
     * Returns <code>true</code>, if the given value is invalid
     * This method is called from within {@link #validate(Object, Severity)}
     * and implementations can assume that this method is never called
     * with <code>value=null</code> or <code>value=""</code>.
     *
     * @param entity  the unique identifier of the entity to validate.
     * @param value  the property value to validate.
     */
    protected abstract boolean isValid(UUID entity, Object value);

    /**
     * Calls {@link AbstractPropertyValidator#isValid(Object, Severity)} to determine whether
     * <code>value</code> is valid. In case the value is invalid, {@link #getInvalidMessage(Object)}
     * is called to build a suitable "invalid value" validation message.
     * In case a value is required but not provided, {@link #getUndefinedMessage()} is called to
     * build a suitable "value undefined" validation message.
     * <p>
     * If no sutiable custom message is available, the method tries to construct a default message
     * by calling {@link #getDefaultInvalidMessage(Object)} or {@link #getDefaultUndefinedMessage()},
     * respectively.
     *
     * The result set of this method contains exactly one {@link Issue} entry,
     * if the validation failed, but is empty otherwise.
     */
    @Override
    public SortedSet<Issue> validate(UUID entity, Object value, Severity minSeverity) {
        TreeSet<Issue> issues = new TreeSet<Issue>();

        if (isUndefinedOrBlank(value)) {
            if (valueRequired) {
                String message = getUndefinedMessage();
                if (StringUtils.isBlank(message)) {
                    message = getDefaultUndefinedMessage();
                }
                issues.add(new Issue(minSeverity, getClass(), entity, extension, propertyName, 0, message));
            }
        }
        else if (value instanceof Collection) {
            int item = 0;
            for (Object entry : (Collection<?>) value) {
                validate(entity, entry, minSeverity, item, issues);
                ++item;
            }
        }
        else {
            validate(entity, value, minSeverity, 0, issues);
        }
        return issues;
    }

    private void validate(UUID entity, Object value, Severity minSeverity, int item, TreeSet<Issue> issues) {
        String message = null;
        if (severity.compareTo(minSeverity) <= 0 && !isValid(entity, value)) {
            message = getInvalidMessage(value);
            if (StringUtils.isBlank(message)) {
                message = getDefaultInvalidMessage(value);
            }
        }
        if (StringUtils.isNotBlank(message)) {
            issues.add(new Issue(severity, getClass(), entity, extension, propertyName, item, message));
        }
    }
}
