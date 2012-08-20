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
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.ComparatorUtils;
import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Issuer;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.Severity;
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

public class MavenResolverRunnable implements Runnable, Issuer {

    private static final Logger LOG = LoggerFactory.getLogger(MavenResolverRunnable.class);

    private NexusClient nexusClient;
    private String userId;
    private UUID uuid;

    /**
     * Creates a Maven resolver runable.
     *
     * @param nexusClient the Nexus client to use for retrieving artifact versions, or <code>null</code>
     * @param userId  the unique identifier of the user requesting the resolving of Maven artifacts
     */
    public MavenResolverRunnable(NexusClient nexusClient, String userId) {
        this.userId = userId;
        this.nexusClient = nexusClient;

    }

    /**
     * Creates a Maven resolver runable.
     *
     * @param nexusClient the Nexus client to use for retrieving artifact versions, or <code>null</code>
     * @param userId  the unique identifier of the user requesting the resolving of Maven artifacts
     * @param uuid  the unique identifier of the project to evaluate, or <code>null</code>.
     * If no project is specified, the {@link #run()} method will iterate over all existing projects.
     */
    public MavenResolverRunnable(NexusClient nexusClient, String userId, UUID uuid) {
        this(nexusClient, userId);
        this.uuid = uuid;
    }

    @Override
    public void run() {
        ProjectService projectService = getProjectService();
        Set<UUID> uuids = uuid != null? Collections.singleton(uuid) : projectService.keySet();
        LOG.info(MessageFormat.format("MavenResolver: Started ({0} projects to scan)", uuids.size()));

        NexusVersionsResolver versionsResolver = new NexusVersionsResolver(nexusClient);

        int count = 0;
        int countUpdated = 0;
        int countInvalidPom = 0;
        int countIOExceptions = 0;
        int countUnexpectedException = 0;
        int countPersistingProblem = 0;
        SortedSet<Issue> issues = new TreeSet<Issue>();

        for (UUID uuid: uuids) {
            if (count > 0) {
                // delay the execution for 10 seconds, otherwise we may
                // overcharge the remote systems with out requests;
                // but not before the first project in the loop
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    break;
                }
            }
            ++count;

            Project project = projectService.getByUUID(uuid);
            if (project == null) {
                handleIssue(Severity.ERROR, uuid, MessageFormat.format(
                        "MavenResolver: Unknown project {0}", uuid),
                        null, issues);
                continue;
            }
            LOG.info(MessageFormat.format("MavenResolver: Processing {0}", project.getProjectId()));

            MavenReactor oldReactor = getMavenReactorProperty(project);
            MavenReactor newReactor = null;
            try {
                newReactor = resolveProject(project, oldReactor, issues);
            } catch (ValidationException e) {
                ++countInvalidPom;
                handleIssue(Severity.WARNING, uuid, MessageFormat.format(
                        "MavenResolver: Invalid POM found for project {0}",
                        project.getProjectId()), e, issues);
                continue;
            } catch (IOException e) {
                ++countIOExceptions;
                handleIssue(Severity.ERROR, uuid, MessageFormat.format(
                        "MavenResolver: Failed to retrieve POM for project {0}",
                        project.getProjectId()), e, issues);
                continue;
            } catch (RuntimeException e) {
                ++countUnexpectedException;
                handleIssue(Severity.FATAL, uuid, MessageFormat.format(
                        "MavenResolver: Unexpected exception when resolving POMs for project {0}",
                        project.getProjectId()), e, issues);
                continue;
            }

            try {
                versionsResolver.addVersions(newReactor, oldReactor);
                versionsResolver.setNexusVersions(newReactor);
            } catch (RuntimeException e) {
                ++countUnexpectedException;
                handleIssue(Severity.FATAL, uuid, MessageFormat.format(
                        "MavenResolver: Unexpected exception when retrieving artifact versions for project {0}",
                        project.getProjectId()), e, issues);
                continue;
            }

            if (!ComparatorUtils.equals(newReactor, oldReactor)) {
                if (updateMavenReactorExtension(project, newReactor)) {
                    try {
                        projectService.persist(project, userId);
                        handleIssue(Severity.INFO, uuid, MessageFormat.format(
                                "MavenResolver: Updated project {0}",
                                project.getProjectId()), null, issues);
                        ++countUpdated;
                    } catch (ValidationException e) {
                        ++countPersistingProblem;
                        handleIssue(Severity.FATAL, uuid, MessageFormat.format(
                                "MavenResolver: Failed to persist project {0}",
                                project.getProjectId()), e, issues);
                        continue;
                    } catch (RuntimeException e) {
                        ++countUnexpectedException;
                        handleIssue(Severity.FATAL, uuid, MessageFormat.format(
                                "MavenResolver: Unexpected exception when persisting project {0}",
                                project.getProjectId()), e, issues);
                        continue;
                    }
                }
            }
            LOG.info(MessageFormat.format("MavenResolver: Processed {0} ({1} projects processed, {2} remaining)",
                    project.getProjectId(), count, uuids.size() - count));
        }
        LOG.info(MessageFormat.format(
                "MavenResolver: Finished ({0} projects processed, {1} updated, {2} with invalid POM, {3} persisting problems, " +
                        "{4} i/o exceptions, {5} unexpected exceptions)",
                uuids.size(), countUpdated, countInvalidPom, countPersistingProblem, countIOExceptions,
                countUnexpectedException));
        if (issues.size() > 0) {
            LOG.info(Issue.getMessage("MavenResolver: Issue Summary", issues));
        }
    }

    /**
     * Returns the Maven reactor information for a given project.
     *
     * @param project  the project to resolve.
     * @param oldReactor  the Maven information from a previous run.
     * @param issues  set of issues found during the resolving.
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
     * @throws ValidationException  if any of the relevant POMs is invalid or cannot be parsed.
     */
    MavenReactor resolveProject(Project project, MavenReactor oldReactor, SortedSet<Issue> issues)
            throws IOException, ValidationException {
        String reactorPomPath = getReactorPomPathProperty(project);
        if (reactorPomPath == null) {
            if (oldReactor != null) {
                handleIssue(Severity.WARNING, project.getUuid(), MessageFormat.format(
                        "MavenResolver: Rector POM Path property has been removed from project {0}",
                        project.getProjectId()), null, issues);
            }
            return null;
        }
        String scmLocation = getScmLocationProperty(project);
        if (scmLocation == null) {
            if (oldReactor != null) {
                handleIssue(Severity.WARNING, project.getUuid(), MessageFormat.format(
                        "MavenResolver: SCM Location property has been removed from project {0}",
                        project.getProjectId()), null, issues);
            }
            return null;
        }
        MavenPomResolver pomResolver = getMavenPomResolver(scmLocation);
        if (pomResolver == null) {
            handleIssue(Severity.ERROR, project.getUuid(), MessageFormat.format(
                    "MavenResolver: No mapping to source repository available for SCM location {0} of project {1}",
                    scmLocation, project.getProjectId()), null, issues);
            return null;
        }
        MavenResolver resolver = getMavenResolver(project.getUuid(), pomResolver);
        return resolver.resolve(scmLocation, reactorPomPath);
    }

    private void handleIssue(Severity severity, UUID uuid, String message, Exception e, SortedSet<Issue> issues) {
        if (e != null) {
            LOG.error(message, e);
            message = MessageFormat.format("{0} [{1}]", message, e.getMessage()); //$NON-NLS-1$
            if (e instanceof ValidationException) {
                issues.addAll(((ValidationException) e).getIssues());
            }
        } else {
            LOG.error(message);
        }
        issues.add(new Issue(severity, MavenResolverRunnable.class, uuid, MavenReactorProjectExt.class, null, message));
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
