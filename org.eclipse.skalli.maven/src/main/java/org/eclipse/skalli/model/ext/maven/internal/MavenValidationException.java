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
package org.eclipse.skalli.model.ext.maven.internal;

import java.util.Collection;
import java.util.UUID;

import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Issuer;
import org.eclipse.skalli.model.ValidationException;

/**
 * Validation exception to report Maven related issues, e.g. during the
 * resolution of Maven POM files from an SCM system.
 */
public class MavenValidationException extends ValidationException {

    private static final long serialVersionUID = 3359414384751958058L;

    public MavenValidationException() {
        super();
    }

    public MavenValidationException(Class<? extends Issuer> issuer, UUID entityId,
            Class<? extends ExtensionEntityBase> extension, String propertyId, String message) {
        super(issuer, entityId, extension, propertyId, message);
    }

    public MavenValidationException(Class<? extends Issuer> issuer, UUID entityId,
            Class<? extends ExtensionEntityBase> extension, String propertyId) {
        super(issuer, entityId, extension, propertyId);
    }

    public MavenValidationException(Class<? extends Issuer> issuer, UUID entityId,
            Class<? extends ExtensionEntityBase> extension) {
        super(issuer, entityId, extension);
    }

    public MavenValidationException(Collection<Issue> issues) {
        super(issues);
    }

    public MavenValidationException(Issue... issues) {
        super(issues);
    }

    public MavenValidationException(String message, Collection<Issue> issues) {
        super(message, issues);
    }

    public MavenValidationException(String message, Issue... issues) {
        super(message, issues);
    }

    public MavenValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public MavenValidationException(String message) {
        super(message);
    }

    public MavenValidationException(Throwable cause) {
        super(cause);
    }
}
