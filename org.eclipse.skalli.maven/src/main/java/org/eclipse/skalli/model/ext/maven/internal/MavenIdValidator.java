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
/**
 *
 */
package org.eclipse.skalli.model.ext.maven.internal;

import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.services.extension.validators.RegularExpressionValidator;

public class MavenIdValidator extends RegularExpressionValidator {
    private static final long serialVersionUID = 7276050513601204905L;

    /**
     * There seems to be no "official" spec for groupIds - not even the Maven XSD
     * defines, how a groupId must look like. So we assume, that the implementation in
     * {@link org.apache.maven.project.validation.DefaultModelValidator#validateId(String,ModelValidationResult,String)}
     * is normative.
     */
    private static final String MAVEN_ID_REGEX = "[A-Za-z0-9_\\-]([A-Za-z0-9_\\-.]*[A-Za-z0-9_\\-])?"; //$NON-NLS-1$

    public MavenIdValidator(Severity severity, Class<? extends ExtensionEntityBase> extension, String property) {
        super(severity, extension, property, MAVEN_ID_REGEX);
    }

    public MavenIdValidator(Severity severity, Class<? extends ExtensionEntityBase> extension, String property,
            String caption) {
        super(severity, extension, property, caption, MAVEN_ID_REGEX);
    }

    public MavenIdValidator(Severity severity, Class<? extends ExtensionEntityBase> extension, String property,
            String invalidValueMessage, String undefinedValueMessage) {
        super(severity, extension, property, invalidValueMessage, undefinedValueMessage, MAVEN_ID_REGEX);
    }

}
