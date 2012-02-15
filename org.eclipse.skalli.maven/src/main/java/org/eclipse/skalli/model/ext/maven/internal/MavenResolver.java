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

import static org.apache.http.HttpStatus.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.params.HttpParams;
import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Issuer;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.model.ext.devinf.DevInfProjectExt;
import org.eclipse.skalli.model.ext.maven.MavenModule;
import org.eclipse.skalli.model.ext.maven.MavenPathResolver;
import org.eclipse.skalli.model.ext.maven.MavenProjectExt;
import org.eclipse.skalli.model.ext.maven.MavenReactor;
import org.eclipse.skalli.model.ext.maven.MavenReactorProjectExt;
import org.eclipse.skalli.services.destination.DestinationService;
import org.eclipse.skalli.services.destination.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MavenResolver implements Issuer {

    private static final Logger LOG = LoggerFactory.getLogger(MavenResolver.class);

    protected final UUID project;
    protected final MavenPomParser parser;
    protected final MavenPathResolver pathResolver;
    protected final DestinationService destinationService;

    private HttpClient client;

    /**
     * Creates a resolver for a given project.
     * @param project  the unique identifier of the project for which reactor information is to be calculated.
     * @param pathResolver  the path resolver to use to convert resource paths to download URLs.
     */
    public MavenResolver(UUID project, MavenPathResolver pathResolver, DestinationService destinationService) {
        this(project, new MavenPomParserImpl(), pathResolver, destinationService);
    }

    // package protected for testing purposes
    MavenResolver(UUID project, MavenPomParser parser, MavenPathResolver pathResolver,
            DestinationService destinationService) {
        this.project = project;
        this.parser = parser;
        this.pathResolver = pathResolver;
        this.destinationService = destinationService;
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
        if (destinationService == null) {
            throw new IllegalArgumentException("destination service not available");
        }
        if (!pathResolver.canResolve(scmLocation)) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "path resolver {0} is not applicable to scmLocation={1}", pathResolver.getClass(), scmLocation));
        }
        MavenReactor mavenReactor = new MavenReactor();
        MavenPom reactorPom = getMavenPom(scmLocation, reactorPomPath);
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
        MavenPom modulePom = getMavenPom(scmLocation, relativePath);
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

    // package protected for testing purposes
    MavenPom getMavenPom(String scmLocation, String relativePath)
            throws IOException, MavenValidationException {
        URL url = pathResolver.resolvePath(scmLocation, relativePath);
        if (url == null) {
            throw new MavenValidationException(MessageFormat.format(
                    "url to read the pom for scm {0}, relativePath {1} is null.", scmLocation, relativePath));
        }
        return getMavenPom(url);
    }

    // package protected for testing purposes
    MavenPom getMavenPom(URL url) throws IOException, MavenValidationException {
        MavenPom mavenPom = null;

        HttpClient client = getClient(url);
        HttpResponse response = null;
        try {
            LOG.info("GET " + url); //$NON-NLS-1$
            HttpGet method = new HttpGet(url.toExternalForm());
            response = client.execute(method);
            int status = response.getStatusLine().getStatusCode();
            LOG.info(status + " " + response.getStatusLine().getReasonPhrase()); //$NON-NLS-1$
            if (status == HttpStatus.SC_OK) {
                InputStream in = response.getEntity().getContent();
                mavenPom = parser.parse(in);
            }
            else {
                String statusText = response.getStatusLine().getReasonPhrase();
                switch (status) {
                case SC_NOT_FOUND:
                    throw new MavenValidationException(
                            new Issue(Severity.ERROR, MavenResolver.class, project,
                                    MavenProjectExt.class, MavenReactorProjectExt.PROPERTY_MAVEN_REACTOR,
                                    MessageFormat.format("{0} not found", url)));
                case SC_UNAUTHORIZED:
                    throw new MavenValidationException(
                            new Issue(Severity.WARNING, MavenResolver.class, project,
                                    MavenProjectExt.class, MavenReactorProjectExt.PROPERTY_MAVEN_REACTOR,
                                    MessageFormat.format("{0} found but authentication required", url, status,
                                            statusText)));
                case SC_INTERNAL_SERVER_ERROR:
                case SC_SERVICE_UNAVAILABLE:
                case SC_GATEWAY_TIMEOUT:
                case SC_INSUFFICIENT_STORAGE:
                    throw new MavenValidationException(
                            new Issue(Severity.WARNING, MavenResolver.class, project,
                                    MavenProjectExt.class, MavenReactorProjectExt.PROPERTY_MAVEN_REACTOR,
                                    MessageFormat.format("{0} not found. Host reports a temporary problem: {1} {2}",
                                            url, status, statusText)));
                case SC_MOVED_PERMANENTLY:
                    throw new MavenValidationException(
                            new Issue(Severity.WARNING, MavenResolver.class, project,
                                    MavenProjectExt.class, MavenReactorProjectExt.PROPERTY_MAVEN_REACTOR,
                                    MessageFormat.format("{0} not found. Resource has been moved permanently to {1}",
                                            url, response.getFirstHeader("Location"))));
                default:
                    throw new MavenValidationException(
                            new Issue(Severity.ERROR, MavenResolver.class, project,
                                    MavenProjectExt.class, MavenReactorProjectExt.PROPERTY_MAVEN_REACTOR,
                                    MessageFormat.format("{0} not found. Host responded with {1} {2}", url, status,
                                            statusText)));
                }
            }
        } finally {
            HttpUtils.consumeQuietly(response);
        }
        return mavenPom;
    }

    private HttpClient getClient(URL url) {
        if (client == null) {
            client = destinationService.getClient(url);
            HttpParams params = client.getParams();
            HttpClientParams.setRedirecting(params, false); // we want to find 301 MOVED PERMANTENTLY
        }
        return client;
    }
}
