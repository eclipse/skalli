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
package org.eclipse.skalli.model;

import java.text.MessageFormat;

public class PropertyUpdateException extends RuntimeException {

    private static final long serialVersionUID = -5333642036204491847L;

    public PropertyUpdateException() {
        super();
    }

    public PropertyUpdateException(String message, Throwable cause) {
        super(message, cause);
    }

    public PropertyUpdateException(String message) {
        super(message);
    }

    public PropertyUpdateException(Throwable cause) {
        super(cause);
    }

    public PropertyUpdateException(EntityBase entity, String propertyName) {
        this(entity, propertyName, null);
    }

    public PropertyUpdateException(EntityBase entity, String propertyName, Throwable cause) {
        super(MessageFormat.format("Failed to update property {0} of {1}", propertyName, entity.getClass().getName()), cause);
    }
}
