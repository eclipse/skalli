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
import java.util.zip.ZipInputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;

/**
 * Maven pom resolver that reads from a <a href="http://gitblit.com/">gitblit</a> backend.
 *
 * Provides a stream for reading the pom.xml file for a given repository and path
 * within the repository. For example, for a  repository with {@code scmLocation}
 * <tt>"scm:git:git://git.example.org/project.git"</tt> and {@relativePath} path <tt>"."</tt>
 * the method would return
 * <tt>"http://git.example.org/zip/?r=projectt&p=pom.xml&h=HEAD"</tt>
 */
public class GitBlitMavenPomResolver extends HttpMavenPomResolverBase {

    @Override
    protected String getProvider() {
        return "gitblit"; //$NON-NLS-1$
    }

    @Override
    protected URL resolvePath(String scmLocation, String relativePath) throws MalformedURLException {
        if (!isValidNormalizedPath(relativePath)) {
            throw new IllegalArgumentException("not a valid path: " + relativePath);
        }

        String repositoryRoot = getRepositoryRoot(scmLocation);
        if (StringUtils.isBlank(repositoryRoot)) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "{0} is not applicable for scmLocation={1}", getClass(), scmLocation));
        }

        StringBuilder sb = new StringBuilder();
        sb.append(repositoryRoot);
        sb.append("&p="); //$NON-NLS-1$

        String fileName = getPomFileName(relativePath);
        sb.append(fileName).append("&h=HEAD"); //$NON-NLS-1$

        return new URL(sb.toString());
    }

    @Override
    protected InputStream asPomInputStream(HttpEntity entity, String relativePath) throws IOException {
        ZipInputStream zipInputStream = new ZipInputStream(entity.getContent());
        return ZipHelper.getEntry(zipInputStream, getPomFileName(relativePath));
    }
}
