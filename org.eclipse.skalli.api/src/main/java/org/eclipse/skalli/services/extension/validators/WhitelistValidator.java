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

import java.util.UUID;

import org.eclipse.skalli.commons.HtmlUtils;
import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.PropertyName;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.services.extension.PropertyValidatorBase;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

/**
 * Property validator to check HTML fragments against a whitelist of allowed/safe tags and attributes.
 * This validator uses {@link Jsoup#isValid(String, Whitelist)} to check the content of a given property.
 */
public class WhitelistValidator extends PropertyValidatorBase {

    private Whitelist whitelist;

    /**
     * Creates a whitelist validator.
     *
     * @param severity  the severity that should be assigned to reported issues.
     * @param extension  the class of the model extension the property belongs to, or <code>null</code>.
     * @param propertyName  the name of a property (see {@link PropertyName}).
     * @param whitelist  the {@link Whitelist} with allowed tags and attributes to use.
     */
    public WhitelistValidator(Severity severity, Class<? extends ExtensionEntityBase> extension, String propertyName,
            Whitelist whitelist) {
        super(severity, extension, propertyName);
        this.whitelist = whitelist;
    }

    /**
     * Creates a whitelist validator.
     *
     * @param severity  the severity that should be assigned to reported issues.
     * @param extension  the class of the model extension the property belongs to, or <code>null</code>.
     * @param propertyName  the name of a property (see {@link PropertyName}).
     * @param caption  the caption of the property as shown to the user in the UI form.
     * @param whitelist  the {@link Whitelist} with allowed tags and attributes to use.
     */
    public WhitelistValidator(Severity severity, Class<? extends ExtensionEntityBase> extension, String propertyName,
            String caption, Whitelist whitelist) {
        super(severity, extension, propertyName, caption);
        this.whitelist = whitelist;
    }

    /**
     * Creates a whitelist validator.
     *
     * @param severity  the severity that should be assigned to reported issues.
     * @param extension  the class of the model extension the property belongs to, or <code>null</code>.
     * @param propertyName  the name of a property (see {@link PropertyName}).
     * @param invalidValueMessage  the message to return in case the value is invalid.
     * @param undefinedValueMessage  the message to return in case the value is undefined.
     * @param whitelist  the {@link Whitelist} with allowed tags and attributes to use.
     */
    public WhitelistValidator(Severity severity, Class<? extends ExtensionEntityBase> extension, String propertyName,
            String invalidValueMessage, String undefinedValueMessage, Whitelist whitelist) {
        super(severity, extension, propertyName, invalidValueMessage, undefinedValueMessage);
        this.whitelist = whitelist;
    }

    @Override
    protected String getInvalidMessageFromCaption(Object value) {
        return HtmlUtils.formatEscaped("{0}: ''{1}'' contains forbidden or unsafe HTML tags", caption, value);
    }

    @Override
    protected String getDefaultInvalidMessage(Object value) {
        return HtmlUtils.formatEscaped("{0}: ''{1}'' contains forbidden or unsafe HTML tags", property, value);
    }

    @Override
    protected boolean isValid(UUID entity, Object value) {
        String name = (String)value;
        return Jsoup.isValid(name, whitelist);
    }

}
