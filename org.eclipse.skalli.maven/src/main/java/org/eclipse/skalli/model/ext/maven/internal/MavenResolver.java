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

import static org.apache.commons.httpclient.HttpStatus.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FilenameUtils;

import org.eclipse.skalli.common.util.HttpUtils;
import org.eclipse.skalli.model.ext.Issue;
import org.eclipse.skalli.model.ext.Issuer;
import org.eclipse.skalli.model.ext.Severity;
import org.eclipse.skalli.model.ext.devinf.DevInfProjectExt;
import org.eclipse.skalli.model.ext.maven.MavenCoordinate;
import org.eclipse.skalli.model.ext.maven.MavenPathResolver;
import org.eclipse.skalli.model.ext.maven.MavenProjectExt;
import org.eclipse.skalli.model.ext.maven.MavenReactor;
import org.eclipse.skalli.model.ext.maven.MavenReactorProjectExt;

public class MavenResolver implements Issuer {

    protected final UUID project;
    protected final MavenPomParser parser;
    protected final MavenPathResolver pathResolver;

    /**
     * Creates a resolver for a given project.
     * @param project  the unique identifier of the project for which reactor information is to be calculated.
     * @param pathResolver  the path resolver to use to convert resource paths to download URLs.
     */
    public MavenResolver(UUID project, MavenPathResolver pathResolver) {
        this(project, new MavenPomParserImpl(), pathResolver);
    }

    // package protected for testing purposes
    MavenResolver(UUID project, MavenPomParser parser, MavenPathResolver pathResolver) {
        this.project = project;
        this.parser = parser;
        this.pathResolver = pathResolver;
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
        if (!pathResolver.canResolve(scmLocation)) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "path resolver {0} is not applicable to scmLocation={1}", pathResolver.getClass(), scmLocation));
        }
        MavenReactor project = new MavenReactor();
        MavenPom reactorPom = getMavenPom(scmLocation, reactorPomPath);
        MavenCoordinate parent = reactorPom.getParent();
        MavenCoordinate self = getSelf(reactorPom, parent);
        project.setCoordinate(self);
        Set<String> moduleTags = reactorPom.getModuleTags();
        for (String moduleTag : moduleTags) {
            String normalizedPath = getNormalizedPath(reactorPomPath, moduleTag);
            project.addModules(getModules(scmLocation, normalizedPath, self));
        }
        return project;
    }

    private Set<MavenCoordinate> getModules(String scmLocation, String relativePath, MavenCoordinate parent)
            throws IOException, MavenValidationException {
        TreeSet<MavenCoordinate> result = new TreeSet<MavenCoordinate>();
        MavenPom modulePom = getMavenPom(scmLocation, relativePath);
        MavenCoordinate self = getSelf(modulePom, parent);
        result.add(self);
        Set<String> moduleTags = modulePom.getModuleTags();
        for (String moduleTag : moduleTags) {
            String normalizedPath = getNormalizedPath(relativePath, moduleTag);
            result.addAll(getModules(scmLocation, normalizedPath, self));
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

    private MavenCoordinate getSelf(MavenPom mavenPom, MavenCoordinate parent) {
        MavenCoordinate self = mavenPom.getSelf();
        if (parent != null) {
            if (self.getGroupId() == null) {
                self.setGroupId(parent.getGroupId());
            }
        }
        return self;
    }

    // package protected for testing purposes
    MavenPom getMavenPom(String scmLocation, String relativePath)
            throws IOException, MavenValidationException {
        URL url = pathResolver.resolvePath(scmLocation, relativePath);
        return getMavenPom(url);
    }

    // package protected for testing purposes
    MavenPom getMavenPom(URL url)
            throws IOException, MavenValidationException {
        MavenPom mavenPom = null;
        String externalForm = url.toExternalForm();
        GetMethod method = new GetMethod(externalForm);
        method.setFollowRedirects(false);
        try {
            int statusCode = HttpUtils.getClient(url).executeMethod(method);
            if (statusCode == HttpStatus.SC_OK) {
                InputStream in = method.getResponseBodyAsStream();
                mavenPom = parser.parse(in);
            }
            else {
                String statusText = method.getStatusText();
                switch (statusCode) {
                case SC_NOT_FOUND:
                    throw new MavenValidationException(
                            new Issue(Severity.ERROR, MavenResolver.class, project,
                                    MavenProjectExt.class, MavenReactorProjectExt.PROPERTY_MAVEN_REACTOR,
                                    url + " not found"));
                case SC_UNAUTHORIZED:
                    throw new MavenValidationException(
                            new Issue(Severity.WARNING, MavenResolver.class, project,
                                    MavenProjectExt.class, MavenReactorProjectExt.PROPERTY_MAVEN_REACTOR,
                                    MessageFormat.format("{0} found but authentication required", url, statusCode,
                                            statusText)));
                case SC_INTERNAL_SERVER_ERROR:
                case SC_SERVICE_UNAVAILABLE:
                case SC_GATEWAY_TIMEOUT:
                case SC_INSUFFICIENT_STORAGE:
                    throw new MavenValidationException(
                            new Issue(Severity.WARNING, MavenResolver.class, project,
                                    MavenProjectExt.class, MavenReactorProjectExt.PROPERTY_MAVEN_REACTOR,
                                    MessageFormat.format("{0} not found. Host reports a temporary problem: {1} {2}",
                                            url, statusCode, statusText)));
                case SC_MOVED_PERMANENTLY:
                    throw new MavenValidationException(
                            new Issue(Severity.WARNING, MavenResolver.class, project,
                                    MavenProjectExt.class, MavenReactorProjectExt.PROPERTY_MAVEN_REACTOR,
                                    MessageFormat.format("{0} not found. Resource has been moved permanently to {1}",
                                            url, method.getResponseHeader("Location"))));
                default:
                    throw new MavenValidationException(
                            new Issue(Severity.ERROR, MavenResolver.class, project,
                                    MavenProjectExt.class, MavenReactorProjectExt.PROPERTY_MAVEN_REACTOR,
                                    MessageFormat.format("{0} not found. Host responded with {1} {2}", url, statusCode,
                                            statusText)));
                }
            }
        } finally {
            method.releaseConnection();
        }
        return mavenPom;
    }
}
