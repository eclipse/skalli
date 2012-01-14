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
package org.eclipse.skalli.services.project;

import java.util.UUID;

public class InvalidParentChainException extends RuntimeException {

    private static final long serialVersionUID = -1985554640339070234L;

    public InvalidParentChainException(String message) {
        super(message);
    }

    public InvalidParentChainException(Throwable cause) {
        super(cause);
    }

    public InvalidParentChainException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidParentChainException(UUID projectId, UUID parentId) {
        super("Parent hierarchy of project " + projectId + " references project " + parentId
                + ", but there is no correponding Project instance");
    }

}
