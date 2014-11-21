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
package org.eclipse.skalli.gerrit.client.exception;

/**
 * Exception for problems during command execution.
 */
public class CommandException extends GerritClientException {

    private static final long serialVersionUID = 5017117065105508950L;

    public CommandException() {
        super();
    }

    public CommandException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public CommandException(String message) {
        super(message);
    }

    public CommandException(Throwable throwable) {
        super(throwable);
    }

}
