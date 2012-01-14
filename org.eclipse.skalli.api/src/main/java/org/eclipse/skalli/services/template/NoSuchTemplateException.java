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
package org.eclipse.skalli.services.template;

import java.util.UUID;

public class NoSuchTemplateException extends RuntimeException {

    private static final long serialVersionUID = 8191688438685219574L;

    public NoSuchTemplateException(String message) {
        super(message);
    }

    public NoSuchTemplateException(Throwable cause) {
        super(cause);
    }

    public NoSuchTemplateException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuchTemplateException(UUID projectId, String templateId) {
        super("Project " + projectId + " references template " + templateId
                + ", but there is no such template registered");
    }

}
