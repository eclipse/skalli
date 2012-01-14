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

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.ext.mapping.MapperUtil;
import org.eclipse.skalli.model.ext.maven.MavenPathResolver;

/**
 * Path resolver that provides GitWeb URLs for resources in a Git repository
 * specified by the location of the SCM repository and the path to the resource within
 * that repository. For example, for a Git repository with the SCM location
 * <tt>scm:git:git://git.example.org/project.git</tt> and the path <tt>"."</tt>
 * the resolver could return
 * <tt>http://git.example.org:50000/git/?p=project.git;a=blob_plain;f=pom.xml;hb=HEAD</tt>
 */
public class GitWebPathResolver implements MavenPathResolver {

    private static final String DEFAULT_POM_FILENAME = "pom.xml"; //$NON-NLS-1$

    private String pattern;
    private String template;

    /**
     * Creates a path resolver for a GitWeb server.
     *
     * @param pattern  a regular expression that describes SCM locations to which the resolver can be applied,
     * e.g. <tt>"^scm:git:git://(git\\.example\\.org(:\\d+)?)/(.*\\.git)$"</tt>.
     * @param template  a string with placeholders that describes the URL schema of the GitWeb,
     * e.g. <tt>"http://{1}:50000/git/?p={3}"</tt>.
     */
    public GitWebPathResolver(String pattern, String template) {
        if (StringUtils.isBlank(pattern)) {
            throw new IllegalArgumentException("argument 'pattern' must not be null or an empty string");
        }
        if (StringUtils.isBlank(template)) {
            throw new IllegalArgumentException("argument 'template' must not be null or an empty string");
        }
        this.pattern = pattern;
        this.template = template;
    }

    @Override
    public boolean canResolve(String scmLocation) {
        return MapperUtil.convert("", scmLocation, pattern, template) != null;
    }

    @Override
    public URL resolvePath(String scmLocation, String relativePath) throws MalformedURLException {
        StringBuilder sb = new StringBuilder();
        if (!isValidNormalizedPath(relativePath)) {
            throw new IllegalArgumentException("not a valid path: " + relativePath);
        }
        String repsitoryRoot = MapperUtil.convert("", scmLocation, pattern, template);
        if (StringUtils.isBlank(repsitoryRoot)) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "{0} is not applicable for scmLocation={1}", getClass(), scmLocation));
        }
        sb.append(repsitoryRoot);
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

    private void appendPath(StringBuilder sb, String relativePath) {
        if (relativePath.charAt(0) == '/') {
            sb.append(relativePath.substring(1));
        } else {
            sb.append(relativePath);
        }
    }

    @SuppressWarnings("nls")
    private boolean isValidNormalizedPath(String path) {
        if (StringUtils.isNotBlank(path)) {
            if (path.indexOf('\\') >= 0) {
                return false;
            }
            if (path.indexOf("..") >= 0 ||
                    path.startsWith("./") ||
                    path.endsWith("/.") ||
                    path.indexOf("/./") >= 0) {
                return false;
            }
        }
        return true;
    }

}
