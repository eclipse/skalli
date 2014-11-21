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
package org.eclipse.skalli.services.extension;

import java.util.Collection;
import java.util.UUID;

import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Issuer;
import org.eclipse.skalli.model.ValidationException;

public class MigrationException extends ValidationException {

    private static final long serialVersionUID = 3142467269881913439L;

    public MigrationException() {
        super();
    }

    public MigrationException(Class<? extends Issuer> issuer, UUID entityId, Class<? extends ExtensionEntityBase> extension,
            String propertyId, String message) {
        super(issuer, entityId, extension, propertyId, message);
    }

    public MigrationException(Class<? extends Issuer> issuer, UUID entityId, Class<? extends ExtensionEntityBase> extension,
            String propertyId) {
        super(issuer, entityId, extension, propertyId);
    }

    public MigrationException(Class<? extends Issuer> issuer, UUID entityId, Class<? extends ExtensionEntityBase> extension) {
        super(issuer, entityId, extension);
    }

    public MigrationException(Collection<Issue> issues) {
        super(issues);
    }

    public MigrationException(Issue... issues) {
        super(issues);
    }

    public MigrationException(String message, Collection<Issue> issues) {
        super(message, issues);
    }

    public MigrationException(String message, Issue... issues) {
        super(message, issues);
    }

    public MigrationException(String message, Throwable cause) {
        super(message, cause);
    }

    public MigrationException(String message) {
        super(message);
    }

    public MigrationException(Throwable cause) {
        super(cause);
    }
}