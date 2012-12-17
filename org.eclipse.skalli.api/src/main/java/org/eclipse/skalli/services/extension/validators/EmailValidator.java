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

import org.eclipse.skalli.commons.HtmlUtils;
import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.PropertyName;
import org.eclipse.skalli.model.Severity;

/**
 * Property validator to check email (<tt>mailto:</tt>) addresses.
 * This validator can be applied to single-valued properties and to {@link java.util.Collection collections}.
 */
public class EmailValidator extends RegularExpressionValidator {

    private static final String EMAIL_PATTERN = "([a-zA-Z0-9_\\-])([a-zA-Z0-9_\\-\\.+!#$%&'*/=?^`{|}~]*)@(\\[((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|" + //$NON-NLS-1$
            "[1-9][0-9]|[0-9])\\.){3}|((([a-zA-Z0-9\\-]+)\\.)+))([a-zA-Z]{2,}|(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[0-9])\\])"; //$NON-NLS-1$

    /**
     * Creates an email validator.
     *
     * @param severity  the severity that should be assigned to reported issues.
     * @param extension  the class of the model extension the property belongs to, or <code>null</code>.
     * @param property  the name of a property (see {@link PropertyName}).
     */
    public EmailValidator(Severity severity, Class<? extends ExtensionEntityBase> extension, String property) {
        super(severity, extension, property, EMAIL_PATTERN);
    }

    /**
     * Creates an email validator.
     *
     * @param severity  the severity that should be assigned to reported issues.
     * @param extension  the class of the model extension the property belongs to, or <code>null</code>.
     * @param property  the name of a property (see {@link PropertyName}).
     * @param caption  the caption of the property as shown to the user in the UI form.
     */
    public EmailValidator(Severity severity, Class<? extends ExtensionEntityBase> extension,
            String property, String caption) {
        super(severity, extension, property, caption, EMAIL_PATTERN);
    }

    /**
     * Creates an email validator.
     *
     * @param severity  the severity that should be assigned to reported issues.
     * @param extension  the class of the model extension the property belongs to, or <code>null</code>.
     * @param property  the name of a property (see {@link PropertyName}).
     * @param invalidValueMessage  the message to return in case the value invalid.
     * @param undefinedValueMessage  the message to return in case the value is undefined.
     */
    public EmailValidator(Severity severity, Class<? extends ExtensionEntityBase> extension, String property,
            String invalidValueMessage, String undefinedValueMessage) {
        super(severity, extension, property, invalidValueMessage, undefinedValueMessage, EMAIL_PATTERN);
    }

    @Override
    protected String getInvalidMessageFromCaption(Object value) {
        return HtmlUtils.formatEscaped("{0}: ''{1}'' is not a valid e-mail address", caption, value);
    }

    @Override
    protected String getDefaultInvalidMessage(Object value) {
        return HtmlUtils.formatEscaped("{0}: ''{1}'' is not a valid e-mail address", property, value);
    }
}
