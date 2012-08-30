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
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.services.extension.PropertyValidatorBase;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

public class WhitelistValidator extends PropertyValidatorBase {

    private Whitelist whitelist;

    public WhitelistValidator(Severity severity, Class<? extends ExtensionEntityBase> extension, String propertyName,
            Whitelist whitelist) {
        super(severity, extension, propertyName);
        this.whitelist = whitelist;
    }

    public WhitelistValidator(Severity severity, Class<? extends ExtensionEntityBase> extension, String propertyName,
            String caption, Whitelist whitelist) {
        super(severity, extension, propertyName, caption);
        this.whitelist = whitelist;
    }

    public WhitelistValidator(Severity severity, Class<? extends ExtensionEntityBase> extension, String propertyName,
            String invalidValueMessage, String undefinedValueMessage, Whitelist whitelist) {
        super(severity, extension, propertyName, invalidValueMessage, undefinedValueMessage);
        this.whitelist = whitelist;
    }

    @Override
    protected String getInvalidMessageFromCaption(Object value) {
        return HtmlUtils.formatEscaped("{0} contains forbidden or unsafe HTML tags", caption);
    }

    @Override
    protected String getDefaultInvalidMessage(Object value) {
        return HtmlUtils.formatEscaped("''{0}'' contains forbidden or unsafe HTML tags", value);
    }

    @Override
    protected boolean isValid(UUID entity, Object value) {
        String name = (String)value;
        return Jsoup.isValid(name, whitelist);
    }

}
