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
package org.eclipse.skalli.services.search;

public class QueryParseException extends Exception {
    private static final long serialVersionUID = 1L;

    public QueryParseException() {
        super();
    }

    public QueryParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public QueryParseException(String message) {
        super(message);
    }

    public QueryParseException(final Throwable cause) {
        super(cause);
    }

}
