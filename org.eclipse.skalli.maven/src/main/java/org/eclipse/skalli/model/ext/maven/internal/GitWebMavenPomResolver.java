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
 * Maven POM resolver that reads from a gitweb backend.
 *
 * Provides a stream for reading the pom.xml file for a given repository and path
 * within the repository. For example, for a  repository with {@code scmLocation}
 * <tt>"scm:git:git://git.example.org/project.git"</tt> and {@relativePath} path <tt>"."</tt>
 * the method would return
 * <tt>"http://git.example.org:50000/git/?p=project.git;a=blob_plain;f=pom.xml;hb=HEAD"</tt>
 */
public class GitWebMavenPomResolver extends HttpMavenPomResolverBase {

    @Override
    protected String getProvider() {
        return "gitweb"; //$NON-NLS-1$
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

        String fileName = getPomFileName(relativePath);
        sb.append(fileName).append(";hb=HEAD"); //$NON-NLS-1$

        return new URL(sb.toString());
    }

    @Override
    protected InputStream asPomInputStream(HttpEntity entity, String relativePath) throws IOException {
        return entity.getContent();
    }
}
