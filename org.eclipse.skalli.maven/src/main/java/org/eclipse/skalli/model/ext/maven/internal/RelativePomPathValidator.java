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
package org.eclipse.skalli.model.ext.maven.internal;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.HtmlUtils;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.model.ext.maven.MavenProjectExt;
import org.eclipse.skalli.services.extension.PropertyValidatorBase;

public class RelativePomPathValidator extends PropertyValidatorBase {

    public RelativePomPathValidator(Severity severity, String caption) {
        super(severity, MavenProjectExt.class, MavenProjectExt.PROPERTY_REACTOR_POM, caption);
    }

    @Override
    protected String getInvalidMessageFromCaption(Object value) {
        return HtmlUtils.formatEscaped("{0}: ''{1}'' is not a valid path. If the reactor POM is in the project's " +
                "root directory, leave the field empty. Otherwise enter the path relative to the project's root " +
                "directory. Note that the path must not start or end with a slash, must not contain /../ segments " +
                "or backslashes, and must not end with /pom.xml",
                 caption, value);
    }

    @Override
    protected String getDefaultInvalidMessage(Object value) {
        return HtmlUtils.formatEscaped("{0}: ''{1}'' is not a valid path. If the reactor POM is in the project's " +
                "root directory, leave the field empty. Otherwise enter the path relative to the project's root " +
                "directory. Note that the path must not start or end with a slash, must not contain /../ segments " +
                "or backslashes, and must not end with /pom.xml",
                 property, value);
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
