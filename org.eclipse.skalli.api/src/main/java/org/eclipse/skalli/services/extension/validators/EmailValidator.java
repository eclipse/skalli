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

import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.Severity;

public class EmailValidator extends RegularExpressionValidator {

    private static final String EMAIL_REGEX = "([a-zA-Z0-9_\\-])([a-zA-Z0-9_\\-\\.+!#$%&'*/=?^`{|}~]*)@(\\[((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|" + //$NON-NLS-1$
            "[1-9][0-9]|[0-9])\\.){3}|((([a-zA-Z0-9\\-]+)\\.)+))([a-zA-Z]{2,}|(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[0-9])\\])"; //$NON-NLS-1$

    public EmailValidator(Severity severity, Class<? extends ExtensionEntityBase> extension, String property) {
        super(severity, extension, property, EMAIL_REGEX);
    }

    public EmailValidator(Severity severity, Class<? extends ExtensionEntityBase> extension, String property,
            String caption) {
        super(severity, extension, property, caption, EMAIL_REGEX);
    }

    public EmailValidator(Severity severity, Class<? extends ExtensionEntityBase> extension, String property,
            String invalidValueMessage, String undefinedValueMessage) {
        super(severity, extension, property, invalidValueMessage, undefinedValueMessage, EMAIL_REGEX);
    }
}
