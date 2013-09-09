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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.skalli.model.Issuer;
import org.eclipse.skalli.model.ValidationException;
import org.eclipse.skalli.model.ext.devinf.DevInfProjectExt;
import org.eclipse.skalli.model.ext.maven.MavenModule;
import org.eclipse.skalli.model.ext.maven.MavenPomResolver;
import org.eclipse.skalli.model.ext.maven.MavenReactor;

public class MavenResolver implements Issuer {


    protected final UUID project;
    protected final MavenPomResolver pomResolver;

    /**
     * Creates a resolver for a given project.
     * @param project  the unique identifier of the project for which reactor information is to be calculated.
     * @param pomResolver  the path resolver to use to convert resource paths to download URLs.
     */
    public MavenResolver(UUID project, MavenPomResolver pomResolver) {
        this.project = project;
        this.pomResolver = pomResolver;
    }

    /**
     * Resolves a Maven reactor project and its modules.
     *
     * Note, this method assumes, that the POM files downloaded from the SCM system are
     * syntactically and semantically correct and complete. No attempt is made to validate
     * the returned <code>MavenReactor</code> instance. It may therefore contain incomplete
     * or invalid information.
     *
     * @param reactorPomPath  the path relative to the repository root of the reactor POM file
     * (without leading or trailing slashes and without file namne).
     * @param scmLocation  the SCM location provided by the
     * project (see {@link DevInfProjectExt#getScmLocation()}.
     *
     * @throws IOException  if an i/o error occured, e.g. the connection to the server
     * providing POM files cannot be established or is lost.
     * @throws ValidationException  if any of the relevant POMs is invalid or cannot be parsed.
     * @throws IllegalArgumentException  if the given SCM location cannot be resolved by
     * the path resolver assigned to this <code>MavenResolver</code> instance.
     */
    public MavenReactor resolve(String scmLocation, String reactorPomPath)
            throws IOException, ValidationException {
        MavenReactor mavenReactor = new MavenReactor();
        MavenPom reactorPom = pomResolver.getMavenPom(project, scmLocation, reactorPomPath);
        if (reactorPom == null) {
            throw new ValidationException(MessageFormat.format(
                    "no pom for scm location {0} and reactorPomPath {1}", scmLocation, reactorPomPath));
        }
        MavenModule parent = reactorPom.getParent();
        MavenModule self = getSelf(reactorPom, parent);
        mavenReactor.setCoordinate(self);

        Set<String> moduleTags = reactorPom.getModuleTags();
        for (String moduleTag : moduleTags) {
            String normalizedPath = getNormalizedPath(reactorPomPath, moduleTag);
            if (normalizedPath != null) {
                List<String> visitedPaths = new ArrayList<String>();
                visitedPaths.add(normalizedPath);
                mavenReactor.addModules(getModules(visitedPaths, scmLocation, normalizedPath, self));
            }
        }
        return mavenReactor;
    }

    private Set<MavenModule> getModules(List<String> visitedPaths, String scmLocation,
            String relativePath, MavenModule parent)
            throws IOException, ValidationException {
        TreeSet<MavenModule> result = new TreeSet<MavenModule>();
        MavenPom modulePom = pomResolver.getMavenPom(project, scmLocation, relativePath);
        if (modulePom == null) {
            return result;
        }
        MavenModule self = getSelf(modulePom, parent);
        result.add(self);
        Set<String> moduleTags = modulePom.getModuleTags();
        for (String moduleTag : moduleTags) {
            String normalizedPath = getNormalizedPath(relativePath, moduleTag);
            if (normalizedPath != null && !visitedPaths.contains(normalizedPath)) {
                visitedPaths.add(normalizedPath);
                result.addAll(getModules(visitedPaths, scmLocation, normalizedPath, self));
            }
        }
        return result;
    }

    /**
     * Concats <code>pathPrefix</code> and <code>path</code>, normalizes the result by
     * removing double and single dot path segments, converts all file separators to forward slashes
     * and removes a leading slash, if any.
     *
     * @param pathPrefix  the path prefix.
     * @param path  the path relative to the path prefix.
     * @return  the bnormalized path, or <code>null</code> of removing double and single dot path
     * segments yielded an invalid path, e.g. a path like <tt>"foo/../../bar"</tt> would be treated
     * as invalid.
     */
    private String getNormalizedPath(String pathPrefix, String path) {
        String normalizedPath = FilenameUtils.normalize(pathPrefix + "/" + path); //$NON-NLS-1$
        if (normalizedPath == null) {
            return null;
        }
        normalizedPath = FilenameUtils.separatorsToUnix(normalizedPath);
        if (normalizedPath.charAt(0) == '/') {
            normalizedPath = normalizedPath.substring(1);
        }
        return normalizedPath;
    }

    private MavenModule getSelf(MavenPom mavenPom, MavenModule parent) {
        MavenModule self = mavenPom.getSelf();
        if (parent != null) {
            if (self.getGroupId() == null) {
                self.setGroupId(parent.getGroupId());
            }
        }
        return self;
    }

}
