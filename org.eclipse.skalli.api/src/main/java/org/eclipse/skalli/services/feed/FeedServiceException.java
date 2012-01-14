/*******************************************************************************
 * Copyright (c) 2010 - 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.services.feed;

public class FeedServiceException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * @param message
     * @param cause
     */
    public FeedServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public FeedServiceException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public FeedServiceException(Throwable cause) {
        super(cause);
    }

}