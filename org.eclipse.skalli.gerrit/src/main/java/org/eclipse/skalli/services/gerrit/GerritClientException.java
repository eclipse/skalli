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
 * General exception for problems with a Gerrit client.
 *
 * It's recommend to handle the specific sub-classed exceptions.
 */
public class GerritClientException extends Exception {

    private static final long serialVersionUID = 636572388227939464L;

    public GerritClientException() {
        super();
    }

    public GerritClientException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public GerritClientException(String message) {
        super(message);
    }

    public GerritClientException(Throwable throwable) {
        super(throwable);
    }

}
