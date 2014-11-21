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
package org.eclipse.skalli.nexus;

public class NexusClientException extends Exception {

    private static final long serialVersionUID = 134321657804321567L;

    public NexusClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public NexusClientException(String message) {
        super(message);
    }
}
