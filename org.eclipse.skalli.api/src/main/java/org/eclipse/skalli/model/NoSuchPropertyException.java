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
package org.eclipse.skalli.model;

public class NoSuchPropertyException extends RuntimeException {

    private static final long serialVersionUID = -5333642036204491847L;

    public NoSuchPropertyException() {
        super();
    }

    public NoSuchPropertyException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuchPropertyException(String message) {
        super(message);
    }

    public NoSuchPropertyException(Throwable cause) {
        super(cause);
    }

    public NoSuchPropertyException(ExtensionEntityBase extension, String propertyName) {
        this(extension, propertyName, null);
    }

    public NoSuchPropertyException(ExtensionEntityBase extension, String propertyName, Throwable cause) {
        super("Failed to retrieve property " + propertyName + " of " + extension.getClass().getName(), cause);
    }

}
