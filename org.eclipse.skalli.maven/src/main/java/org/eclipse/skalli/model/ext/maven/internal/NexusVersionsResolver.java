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

import java.text.MessageFormat;

import org.eclipse.skalli.commons.ComparatorUtils;
import org.eclipse.skalli.model.ext.maven.MavenCoordinate;
import org.eclipse.skalli.model.ext.maven.MavenModule;
import org.eclipse.skalli.model.ext.maven.MavenReactor;
import org.eclipse.skalli.nexus.NexusArtifact;
import org.eclipse.skalli.nexus.NexusClient;
import org.eclipse.skalli.nexus.NexusSearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NexusVersionsResolver {
    private static final Logger LOG = LoggerFactory.getLogger(NexusVersionsResolver.class);
    private NexusClient nexusClient;

    public NexusVersionsResolver(NexusClient nexusClient) {
        this.nexusClient = nexusClient;
        if (this.nexusClient == null) {
            LOG.warn("Can't calculate artifact versions: No Nexus client available");
        }
    }

    public void setNexusVersions(MavenReactor mavenReactor) {
        if (nexusClient == null || mavenReactor == null) {
            return;
        }
        setNexusVersion(mavenReactor.getCoordinate());
        for (MavenModule mavenCoordinate : mavenReactor.getModules()) {
            if (mavenCoordinate != null) {
                setNexusVersion(mavenCoordinate);
            }
        }
        return;
    }

    void setNexusVersion(MavenModule mavenCoordinate) {
        if (mavenCoordinate == null) {
            return;
        }

        try {
            NexusSearchResult searchResult = nexusClient.searchArtifactVersions(mavenCoordinate.getGroupId(),
                    mavenCoordinate.getArtefactId());

            if (searchResult.getArtifacts().size() < mavenCoordinate.getVersions().size()) {
                LOG.warn(MessageFormat.format(
                        "Nexus returned less versions for artifact {0}:{1} than in previous runs. Nexus index might be broken.",
                                mavenCoordinate.getGroupId(), mavenCoordinate.getArtefactId()));
            }

            for (NexusArtifact nexusArtifact : searchResult.getArtifacts()) {
                mavenCoordinate.getVersions().add(nexusArtifact.getVersion());
            }

        } catch (Exception e) {
            LOG.warn(MessageFormat.format("Failed to retrieve versions of artifact {0}:{1}: {2}",
                    mavenCoordinate.getGroupId(), mavenCoordinate.getArtefactId(), e.getMessage()), e);
        }
    }

    public void addVersions(MavenReactor newReactor, MavenReactor oldReactor) {
        if (newReactor == null || oldReactor == null) {
            return;
        }

        if (newReactor.getCoordinate() != null
                && haveSameGroupArtifact(newReactor.getCoordinate(), oldReactor.getCoordinate())) {
            newReactor.getCoordinate().getVersions().addAll(oldReactor.getCoordinate().getVersions());
        }

        for (MavenModule mavenCoordinate : newReactor.getModules()) {
            if (mavenCoordinate != null) {
                MavenModule oldCoordinate = findModuleCoordinate(oldReactor, mavenCoordinate);
                if (oldCoordinate != null) {
                    mavenCoordinate.getVersions().addAll(oldCoordinate.getVersions());
                }
            }
        }
        return;

    }

    private MavenModule findModuleCoordinate(MavenReactor reactor, MavenCoordinate mavenCoordinate) {
        if (reactor == null || mavenCoordinate == null) {
            return null;
        }
        for (MavenModule reactorCoordinate : reactor.getModules()) {
            if (haveSameGroupArtifact(mavenCoordinate, reactorCoordinate)) {
                return reactorCoordinate;
            }

        }

        return null;
    }

    private boolean haveSameGroupArtifact(MavenCoordinate c1, MavenCoordinate c2) {
        if (c1 == null && c2 == null) {
            return true;
        }
        if (c1 == null) {
            return false;
        }
        if (c2 == null) {
            return false;
        }

        if (ComparatorUtils.compare(c1.getGroupId(), c2.getGroupId()) != 0) {
            return false;
        }

        if (ComparatorUtils.compare(c1.getArtefactId(), c2.getArtefactId()) != 0) {
            return false;
        }

        return true;
    }
}
