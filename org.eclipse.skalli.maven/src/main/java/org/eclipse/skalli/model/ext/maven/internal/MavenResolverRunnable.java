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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.ComparatorUtils;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.ValidationException;
import org.eclipse.skalli.model.ext.devinf.DevInfProjectExt;
import org.eclipse.skalli.model.ext.maven.MavenPomResolver;
import org.eclipse.skalli.model.ext.maven.MavenProjectExt;
import org.eclipse.skalli.model.ext.maven.MavenReactor;
import org.eclipse.skalli.model.ext.maven.MavenReactorProjectExt;
import org.eclipse.skalli.nexus.NexusClient;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.project.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MavenResolverRunnable implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(MavenResolverRunnable.class);

    private NexusClient nexusClient;
    private String userId;
    private Project project;

    /**
     * @param configService
     * @param nexusClient - if set versions are calculated via nexusClinet. If nexusClient is null the versions cant be calculated.
     * @param userId
     */
    public MavenResolverRunnable(NexusClient nexusClient, String userId) {
        this.userId = userId;
        this.nexusClient = nexusClient;

    }

    public MavenResolverRunnable(NexusClient nexusClient, String userId, Project project) {
        this(nexusClient, userId);
        this.project = project;
    }

    @Override
    public void run() {
        ProjectService projectService = getProjectService();
        List<Project> projects;
        if (project != null) {
            projects = Collections.singletonList(project);
            LOG.info(MessageFormat.format("MavenResolver: started 1 project ({0}) to scan", project.getProjectId()));
        }
        else {
            //for each run we want to need the current project list
            projects = projectService.getAll();
            LOG.info(MessageFormat.format("MavenResolver: started ({0} projects to scan", projects.size()));
        }

        int countUpdated = 0;
        int countInvalid = 0;
        NexusVersionsResolver versionsResolver = new NexusVersionsResolver(nexusClient);
        for (int i = 0; i < projects.size(); i++) {
            if (i > 0) {
                // delay the execution for 10 seconds, otherwise we may
                // overcharge the remote systems like gitweb/gitblit/Nexus with out requests
                // but not before the first project in the loop
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    break;
                }
            }

            Project project = projects.get(i);
            MavenReactor oldReactor = getMavenReactorProperty(project);
            MavenReactor newReactor = null;
            try {
                newReactor = resolveProject(project);
            } catch (ValidationException e) {
                ++countInvalid;
                LOG.info(MessageFormat.format(
                        "Invalid Maven reactor information for project {0}:\n {1}",
                        project.getProjectId(), e.getMessage()));
                continue;
            } catch (Throwable t) {
                ++countInvalid;
                LOG.error(MessageFormat.format(
                        "Failed to resolve Maven reactor information for project {0}",
                        project.getProjectId()), t);
                continue;
            }

            try {
                versionsResolver.addVersions(newReactor, oldReactor);
                versionsResolver.setNexusVersions(newReactor);
            } catch (RuntimeException e) {
                LOG.error(MessageFormat.format(
                        "Can''t calculate Versions for project {0} . Unexpected Exception cought: {1}",
                        project.getProjectId(), e.getMessage()));
            }

            if (!ComparatorUtils.equals(newReactor, oldReactor)) {
                if (updateMavenReactorExtension(project, newReactor)) {
                    try {
                        projectService.persist(project, userId);
                        ++countUpdated;
                    } catch (ValidationException e) {
                        ++countInvalid;
                        LOG.warn(MessageFormat.format(
                                "Failed to persist Maven reactor information for project {0}",
                                project.getProjectId()), e);
                        continue;
                    }
                }
            }

            LOG.debug(MessageFormat.format(
                    "MavenResolver: ({0} projects scanned: {1} updated, {2} invalid, {3} remaining)",
                    projects.size(), countUpdated, countInvalid, projects.size() - countUpdated - countInvalid));

        }
        LOG.info(MessageFormat.format(
                "MavenResolver: finished ({0} projects scanned: {1} updated, {2} invalid)",
                projects.size(), countUpdated, countInvalid));
    }

    /**
     * Returns the Maven reactor information for a given project.
     *
     * @param project  the project to resolve.
     * @return  the Maven reactor for the project, or <code>null</code> if
     * <ul>
     * <li>the reactor POM path ({@link MavenProjectExt#PROPERTY_REACTOR_POM}) is not specified,</li>
     * <li>the SCM location ({@link DevInfProjectExt#PROPERTY_SCM_LOCATIONS}) is not defined,</li>
     * <li>the mapping from the SCM location to the source repository is not configured,</li>
     * <li>or the mapping is not applicable for the given SCM location.</li>
     * </ul>
     *
     * @throws IOException  if an i/o error occured, e.g. the connection to the source repository
     * providing POM files cannot be established or is lost.
     * @throws MavenValidationException  if any of the relevant POMs is invalid or cannot be parsed.
     */
    public MavenReactor resolveProject(Project project)
            throws IOException, MavenValidationException {
        String reactorPomPath = getReactorPomPathProperty(project);
        if (reactorPomPath == null) {
            return null;
        }
        String scmLocation = getScmLocationProperty(project);
        if (scmLocation == null) {
            return null;
        }
        MavenPomResolver pomResolver = getMavenPomResolver(scmLocation);
        if (pomResolver == null) {
            return null;
        }
        MavenResolver resolver = getMavenResolver(project.getUuid(), pomResolver);
        return resolver.resolve(scmLocation, reactorPomPath);
    }

    private boolean updateMavenReactorExtension(Project project, MavenReactor mavenReactor) {
        MavenReactorProjectExt ext = project.getExtension(MavenReactorProjectExt.class);
        if (ext == null) {
            if (mavenReactor == null) {
                return false;
            }
            ext = new MavenReactorProjectExt();
            project.addExtension(ext);
        }
        ext.setMavenReactor(mavenReactor);
        return true;
    }

    @SuppressWarnings("nls")
    private String getReactorPomPathProperty(Project project) {
        MavenProjectExt ext = project.getExtension(MavenProjectExt.class);
        if (ext == null) {
            return null;
        }
        String reactorPomPath = ext.getReactorPOM();
        if (reactorPomPath == null) {
            reactorPomPath = "";
        }
        if (reactorPomPath.endsWith("/pom.xml")) {
            reactorPomPath = reactorPomPath.substring(0, reactorPomPath.length() - 8);
        }
        if (reactorPomPath.startsWith("/")) {
            reactorPomPath = reactorPomPath.substring(1);
        }
        if (reactorPomPath.endsWith("/")) {
            reactorPomPath = reactorPomPath.substring(0, reactorPomPath.length() - 1);
        }
        return reactorPomPath;
    }

    private String getScmLocationProperty(Project project) {
        DevInfProjectExt devExtension = project.getExtension(DevInfProjectExt.class);
        if (devExtension == null) {
            return null;
        }
        String scmLocation = devExtension.getScmLocation();
        if (StringUtils.isEmpty(scmLocation)) {
            return null;
        }
        return scmLocation;
    }

    private MavenReactor getMavenReactorProperty(Project project) {
        MavenReactorProjectExt ext = project.getExtension(MavenReactorProjectExt.class);
        return ext != null ? ext.getMavenReactor() : null;
    }

    // package protected for testing purposes
    MavenPomResolver getMavenPomResolver(String scmLocation) {
        Set<MavenPomResolver> mavenPomResolvers = Services.getServices(MavenPomResolver.class);

        if (mavenPomResolvers.isEmpty()) {
            LOG.debug("no " + MavenPomResolver.class.getName() + " configuration");
            return null;
        }

        for (MavenPomResolver mavenPomResolver : mavenPomResolvers) {
            if (mavenPomResolver.canResolve(scmLocation)) {
                return mavenPomResolver;
            }
        }

        return null;
    }

    // package protected for testing purposes
    MavenResolver getMavenResolver(UUID entityId, MavenPomResolver mavenPomResolver) {
        return new MavenResolver(entityId, mavenPomResolver);
    }

    // package protected for testing purposes
    ProjectService getProjectService() {
        return Services.getRequiredService(ProjectService.class);
    }
}
