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

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.model.ext.maven.MavenProjectExt;
import org.eclipse.skalli.services.extension.PropertyValidatorBase;

public class RelativePomPathValidator extends PropertyValidatorBase {
    private static final long serialVersionUID = 1L;

    public RelativePomPathValidator(Severity severity, String caption) {
        super(severity, MavenProjectExt.class, MavenProjectExt.PROPERTY_REACTOR_POM, caption);
    }

    @Override
    protected String getInvalidMessageFromCaption(Object value) {
        return MessageFormat.format("''{0}'' is not a valid value for {1} - it must be a valid path, " +
                "must not contain backslashes and /../ segments, and must not end with /pom.xml or a trailing slash",
                value, caption);
    }

    @Override
    public boolean isValid(UUID entity, Object value) {
        String relativePomPath = (String) value;
        if (StringUtils.isBlank(relativePomPath)) {
            return true;
        }

        // must have forward slashes
        // must not be relative (i.e. point outside the project)
        // must not include the pom.xml
        // must be valid filenames
        if (relativePomPath.indexOf('\\') >= 0) {
            return false;
        }
        if (relativePomPath.charAt(0) == '/') {
            return false;
        }
        if (relativePomPath.charAt(relativePomPath.length() - 1) == '/') {
            return false;
        }
        if (relativePomPath.endsWith("pom.xml")) { //$NON-NLS-1$
            return false;
        }

        if (relativePomPath.indexOf("..") >= 0 || //$NON-NLS-1$
                relativePomPath.startsWith("./") || //$NON-NLS-1$
                relativePomPath.endsWith("/.") || //$NON-NLS-1$
                relativePomPath.indexOf("/./") >= 0) { //$NON-NLS-1$
            return false;
        }

        try {
            File f = new File(relativePomPath);
            // http://stackoverflow.com/questions/468789/is-there-a-way-in-java-to-determine-if-a-path-is-valid-without-attempting-to-crea/469105#469105
            f.getCanonicalPath();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
