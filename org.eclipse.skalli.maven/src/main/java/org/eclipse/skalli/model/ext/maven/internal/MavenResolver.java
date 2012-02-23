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
     * @throws MavenValidationException  if any of the relevant POMs is invalid or cannot be parsed.
     * @throws IllegalArgumentException  if the given SCM location cannot be resolved by
     * the path resolver assigned to this <code>MavenResolver</code> instance.
     */
    public MavenReactor resolve(String scmLocation, String reactorPomPath)
            throws IOException, MavenValidationException {
        MavenReactor mavenReactor = new MavenReactor();
        MavenPom reactorPom = pomResolver.getMavenPom(project, scmLocation, reactorPomPath);
        if (reactorPom == null) {
            throw new MavenValidationException(MessageFormat.format(
                    "no pom for scm location {0} and reactorPomPath {1}", scmLocation, reactorPomPath));
        }
        MavenModule parent = reactorPom.getParent();
        MavenModule self = getSelf(reactorPom, parent);
        mavenReactor.setCoordinate(self);

        Set<String> moduleTags = reactorPom.getModuleTags();
        for (String moduleTag : moduleTags) {
            String normalizedPath = getNormalizedPath(reactorPomPath, moduleTag);
            List<String> visitedPaths = new ArrayList<String>();
            visitedPaths.add(normalizedPath);
            mavenReactor.addModules(getModules(visitedPaths, scmLocation, normalizedPath, self));
        }
        return mavenReactor;
    }

    private Set<MavenModule> getModules(List<String> visitedPaths, String scmLocation,
            String relativePath, MavenModule parent)
            throws IOException, MavenValidationException {
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
            if (!visitedPaths.contains(normalizedPath)) {
                visitedPaths.add(normalizedPath);
                result.addAll(getModules(visitedPaths, scmLocation, normalizedPath, self));
            }
        }
        return result;
    }

    private String getNormalizedPath(String pathPrefix, String path) {
        String normalizedPath = FilenameUtils.separatorsToUnix(FilenameUtils.normalize(pathPrefix + "/" + path)); //$NON-NLS-1$
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
