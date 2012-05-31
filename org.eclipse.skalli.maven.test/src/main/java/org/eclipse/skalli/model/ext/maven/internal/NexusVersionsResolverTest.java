/*******************************************************************************
 * Copyright (c) 2010 - 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.model.ext.maven.internal;

import static org.easymock.EasyMock.*;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.skalli.model.ext.maven.MavenCoordinate;
import org.eclipse.skalli.model.ext.maven.MavenModule;
import org.eclipse.skalli.model.ext.maven.MavenReactor;
import org.eclipse.skalli.nexus.NexusArtifact;
import org.eclipse.skalli.nexus.NexusClient;
import org.eclipse.skalli.nexus.NexusClientException;
import org.eclipse.skalli.nexus.NexusSearchResult;
import org.junit.Test;

public class NexusVersionsResolverTest {

    private NexusSearchResult createNexusSearchResultStub(String groupId, String artifactId, String versions[])
    {
        NexusSearchResult mock = createMock(NexusSearchResult.class);

        List<NexusArtifact> artifacts = new ArrayList<NexusArtifact>();
        for (int i = 0; i < versions.length; i++) {
            NexusArtifact createNexusArtifactSub = createNexusArtifactSub(groupId, artifactId, versions[i]);
            artifacts.add(createNexusArtifactSub);
            replay(createNexusArtifactSub);
        }

        expect(mock.getArtifacts()).andReturn(artifacts).anyTimes();
        return mock;
    }

    private NexusArtifact createNexusArtifactSub(String groupId, String artifactId, String version) {
        NexusArtifact naMock = createMock(NexusArtifact.class);
        expect(naMock.getGroupId()).andReturn(groupId);
        expect(naMock.getArtifactId()).andReturn(artifactId);
        expect(naMock.getVersion()).andReturn(version);
        return naMock;
    }

    private void expectSearchArtifactVersions(NexusClient nexusClientMock, MavenCoordinate coordinate,
            String[] versions) throws NexusClientException, IOException {
        NexusSearchResult createNexusSearchResultStub = createNexusSearchResultStub(coordinate.getGroupId(),
                coordinate.getArtefactId(),
                versions);
        expect(
                nexusClientMock.searchArtifactVersions(coordinate.getGroupId(),
                        coordinate.getArtefactId())).andReturn(
                createNexusSearchResultStub);
        replay(createNexusSearchResultStub);
    }

    @Test
    public void testSetNexusVersionsMavenReactor() throws NexusClientException, IOException {

        MavenReactor mavenReactor = new MavenReactor();
        MavenModule mavenCoordinateParent = new MavenModule("org.example.helloworld",
                "org.example.helloworld", null);
        MavenModule moduleA = new MavenModule("org.example.helloworld",
                "org.example.helloworld.modulA", null);
        MavenModule moduleB = new MavenModule("org.example.helloworld",
                "org.example.helloworld.modulB", null);

        mavenReactor.setCoordinate(mavenCoordinateParent);
        mavenReactor.addModule(moduleA);
        mavenReactor.addModule(moduleB);

        NexusClient nexusClientMock = createMock(NexusClient.class);

        expectSearchArtifactVersions(nexusClientMock, mavenCoordinateParent, new String[] { "0.0.1", "0.0.2" });
        expectSearchArtifactVersions(nexusClientMock, moduleA, new String[] { "0.0.4", "0.0.5" });
        expectSearchArtifactVersions(nexusClientMock, moduleB, new String[] { "0.0.1", "0.0.2", "0.0.3" });
        replay(nexusClientMock);

        NexusVersionsResolver resolver = new NexusVersionsResolver(nexusClientMock);
        resolver.setNexusVersions(mavenReactor);

        assertThat(mavenReactor.getCoordinate().getVersions().size(), is(2));
        assertThat(mavenReactor.getCoordinate().getVersions(), hasItem("0.0.1"));
        assertThat(mavenReactor.getCoordinate().getVersions(), hasItem("0.0.2"));

        assertThat(moduleA.getVersions().size(), is(2));
        assertThat(moduleA.getVersions(), hasItem("0.0.4"));
        assertThat(moduleA.getVersions(), hasItem("0.0.5"));

        assertThat(moduleB.getVersions().size(), is(3));
        assertThat(moduleB.getVersions(), hasItem("0.0.1"));
        assertThat(moduleB.getVersions(), hasItem("0.0.2"));
        assertThat(moduleB.getVersions(), hasItem("0.0.3"));
    }

    @Test
    public void testSetNexusVersionsMavenCoordinate() throws NexusClientException, IOException {
        MavenModule mavenCoordinate = new MavenModule("org.example.helloworld",
                "org.example.helloworld.updatesite", "zip");
        NexusClient nexusClientMock = createMock(NexusClient.class);
        expectSearchArtifactVersions(nexusClientMock, mavenCoordinate, new String[] { "0.0.1", "0.0.2" });
        replay(nexusClientMock);

        NexusVersionsResolver resolver = new NexusVersionsResolver(nexusClientMock);

        resolver.setNexusVersion(mavenCoordinate);

        assertThat(mavenCoordinate.getVersions().size(), is(2));
        assertThat(mavenCoordinate.getVersions(), hasItem("0.0.1"));
        assertThat(mavenCoordinate.getVersions(), hasItem("0.0.2"));
    }

    @Test
    public void testSetNexusVersionsMavenCoordinate_withNexusClientException() throws NexusClientException, IOException {
        MavenModule mavenCoordinate = new MavenModule("org.example.helloworld",
                "org.example.helloworld.updatesite", "zip");
        mavenCoordinate.addVersion("0.0.1");
        mavenCoordinate.addVersion("0.0.2");

        NexusClient nexusClientMock = createMock(NexusClient.class);
        expect(nexusClientMock.searchArtifactVersions(mavenCoordinate.getGroupId(),
                mavenCoordinate.getArtefactId())).andThrow(new NexusClientException("nexus not availaible"));
        replay(nexusClientMock);

        NexusVersionsResolver resolver = new NexusVersionsResolver(nexusClientMock);

        resolver.setNexusVersion(mavenCoordinate);

        assertThat(mavenCoordinate.getVersions().size(), is(2));
        assertThat(mavenCoordinate.getVersions(), hasItem("0.0.1"));
        assertThat(mavenCoordinate.getVersions(), hasItem("0.0.2"));
    }

    @Test
    public void testAddVersions() {
        MavenReactor oldMavenReactor = new MavenReactor();

        MavenModule oldMavenCoordinateParent = new MavenModule("org.example.helloworld",
                "org.example.helloworld", null);
        oldMavenCoordinateParent.addVersion("0.9.19");

        MavenModule oldModuleA = new MavenModule("org.example.helloworld",
                "org.example.helloworld.modulA", null);
        oldModuleA.addVersion("0.0.1");

        MavenModule oldModuleB = new MavenModule("org.example.helloworld",
                "org.example.helloworld.modulB", null);
        oldModuleB.addVersion("0.0.2");
        oldModuleB.addVersion("0.0.3");

        oldMavenReactor.setCoordinate(oldMavenCoordinateParent);
        oldMavenReactor.addModule(oldModuleA);
        oldMavenReactor.addModule(oldModuleB);

        NexusVersionsResolver resolver = new NexusVersionsResolver(null);

        MavenReactor newReactor = new MavenReactor();
        MavenModule newMavenCoordinateParent = new MavenModule(oldMavenCoordinateParent);
        newMavenCoordinateParent.getVersions().clear();

        newReactor.setCoordinate(newMavenCoordinateParent);

        MavenModule newModuleA = new MavenModule(oldModuleA);
        newModuleA.getVersions().clear();
        newReactor.addModule(newModuleA);

        MavenModule newModuleC = new MavenModule("org.example.helloworld",
                "org.example.helloworld.modulC", null);
        newReactor.addModule(newModuleC);

        resolver.addVersions(newReactor, oldMavenReactor);

        assertThat(newReactor.getCoordinate().getVersions().size(), is(1));
        assertThat(newReactor.getCoordinate().getVersions(), hasItem("0.9.19"));

        assertThat(newReactor.getModules().size(), is(2));
        for (MavenModule coordinate : newReactor.getModules()) {
            if ("org.example.helloworld.modulA".equals(coordinate.getArtefactId())) {
                assertThat(coordinate.getVersions().size(), is(1));
                assertThat(coordinate.getVersions(), hasItem("0.0.1"));
            } else if ("org.example.helloworld.modulC".equals(coordinate.getArtefactId())) {
                assertThat(coordinate.getVersions().size(), is(0));
            } else {
                fail("found an unexpected coordinate");
            }
        }
    }
}
