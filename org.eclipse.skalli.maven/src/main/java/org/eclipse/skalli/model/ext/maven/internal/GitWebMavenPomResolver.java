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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;

/**
 * Maven pom resolver that provides the {@link MavenPom} for a Git repository
 * specified by the location of the SCM repository and the path to the resource within
 * that repository. For example, for a Git repository with the SCM location
 * <tt>scm:git:git://git.example.org/project.git</tt> and the path <tt>"."</tt>
 * the resolver parses the maven pom from
 * <tt>http://git.example.org:50000/git/?p=project.git;a=blob_plain;f=pom.xml;hb=HEAD</tt>
 */
public class GitWebMavenPomResolver extends HttpMavenPomResolverBase {

    @Override
    protected String getProvider() {
        return "gitweb";
    }

    @Override
    protected URL resolvePath(String scmLocation, String relativePath) throws MalformedURLException {
        StringBuilder sb = new StringBuilder();
        if (!isValidNormalizedPath(relativePath)) {
            throw new IllegalArgumentException("not a valid path: " + relativePath);
        }

        String repositoryRoot = getRepositoryRoot(scmLocation);
        if (StringUtils.isBlank(repositoryRoot)) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "{0} is not applicable for scmLocation={1}", getClass(), scmLocation));
        }
        sb.append(repositoryRoot);
        sb.append(";a=blob_plain;f="); //$NON-NLS-1$
        if (StringUtils.isBlank(relativePath) || ".".equals(relativePath)) { //$NON-NLS-1$
            sb.append(DEFAULT_POM_FILENAME);
        }
        else if (!relativePath.endsWith(DEFAULT_POM_FILENAME)) {
            appendPath(sb, relativePath);
            if (!relativePath.endsWith("/")) { //$NON-NLS-1$
                sb.append("/"); //$NON-NLS-1$
            }
            sb.append(DEFAULT_POM_FILENAME);
        }
        else {
            appendPath(sb, relativePath);
        }
        sb.append(";hb=HEAD"); //$NON-NLS-1$
        return new URL(sb.toString());
    }

    @Override
    protected InputStream asPomInputStream(HttpEntity entity, String relativePath) throws IOException {
        return entity.getContent();
    }
}
