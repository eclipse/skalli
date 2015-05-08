/*******************************************************************************
 * Copyright (c) 2010-2015 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.services.gerrit;

/**
 * Exception for connection / communication problems.
 */
public class ConnectionException extends GerritClientException {

    private static final long serialVersionUID = 6082875880219352467L;

    public ConnectionException() {
    }

    public ConnectionException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public ConnectionException(String message) {
        super(message);
    }

    public ConnectionException(Throwable throwable) {
        super(throwable);
    }

}
