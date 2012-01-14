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

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.UUID;

import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.PropertyName;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.services.extension.PropertyValidatorBase;

public class URLValidator extends PropertyValidatorBase {

    /**
     * Creates a URL validator.
     *
     * @param severity  the severity that should be assigned to reported issues.
     * @param extension  the class of the model extension the property belongs to, or <code>null</code>.
     * @param propertyName  the name of a property (see {@link PropertyName}).
     */
    public URLValidator(Severity severity, Class<? extends ExtensionEntityBase> extension, String propertyName) {
        super(severity, extension, propertyName);
    }

    /**
     * Creates a URL validator.
     *
     * @param severity  the severity that should be assigned to reported issues.
     * @param extension  the class of the model extension the property belongs to, or <code>null</code>.
     * @param propertyName  the name of a property (see {@link PropertyName}).
     * @param caption  the caption of the property as shown to the user in the UI form.
     */
    public URLValidator(Severity severity, Class<? extends ExtensionEntityBase> extension, String propertyName,
            String caption) {
        super(severity, extension, propertyName, caption);
    }

    /**
     * Creates a URL validator.
     *
     * @param severity  the severity that should be assigned to reported issues.
     * @param extension  the class of the model extension the property belongs to, or <code>null</code>.
     * @param propertyName  the name of a property (see {@link PropertyName}).
     * @param invalidValueMessage  the message to return in case the value invalid.
     * @param undefinedValueMessage  the message to return in case the value is undefined.
     */
    public URLValidator(Severity severity, Class<? extends ExtensionEntityBase> extension, String propertyName,
            String invalidValueMessage, String undefinedValueMessage) {
        super(severity, extension, propertyName, invalidValueMessage, undefinedValueMessage);
    }

    @Override
    protected String getInvalidMessageFromCaption(Object value) {
        return MessageFormat.format("{0} link must be a valid URL", caption);
    }

    @Override
    protected String getDefaultInvalidMessage(Object value) {
        return MessageFormat.format("''{0}'' is not a valid URL", value);
    }

    @Override
    public boolean isValid(UUID entity, Object value) {
        if (value instanceof URL) {
            return true;
        }
        try {
            new URL(value.toString());
        } catch (MalformedURLException e) {
            return false;
        }
        return true;
    }
}
