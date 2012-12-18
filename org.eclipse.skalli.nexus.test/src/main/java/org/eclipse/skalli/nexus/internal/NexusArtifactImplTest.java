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
package org.eclipse.skalli.nexus.internal;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URI;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.skalli.commons.XMLUtils;
import org.eclipse.skalli.nexus.NexusClientException;
import org.junit.Test;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

@SuppressWarnings("nls")
public class NexusArtifactImplTest {

    private Element getElement(String xml) throws Exception {
        return XMLUtils.documentFromString(xml).getDocumentElement();
    }

    @Test
    public void testNexusArtifactImpl() throws Exception {
        NexusArtifactImpl nexusArtifact = new NexusArtifactImpl(getElement(//
                "          <artifact>" //
                        + "  <resourceURI>http://mynexus:8081/nexus/service/local/repositories/build.milestones/" //
                        + "content/org/example/helloworld/org.example.helloworld.updatesite/0.1.0/org.example." //
                        + "helloworld.updatesite-0.1.0.eclipse-update-site</resourceURI>" //
                        + "  <groupId>org.example.helloworld</groupId>" //
                        + "  <artifactId>org.example.helloworld.updatesite</artifactId>" //
                        + "  <version>0.1.0</version>" //
                        + "  <packaging>eclipse-update-site</packaging>" //
                        + "  <extension>zip</extension>" //
                        + "  <repoId>build.milestones</repoId>" //
                        + "  <contextId>context.build.milestones</contextId>" //
                        + "  <pomLink>http://mynexus:8081/nexus/service/local/artifact/maven/redirect?r=build." //
                        + "milestones&amp;g=org.example.helloworld&amp;a=org.example.helloworld.updatesite&amp;" //
                        + "v=0.1.0&amp;e=pom</pomLink>" //
                        + "  <artifactLink>http://mynexus:8081/nexus/service/local/artifact/maven/redirect?r=" //
                        + "build.milestones&amp;g=org.example.helloworld&amp;a=org.example.helloworld.updatesite&" //
                        + "amp;v=0.1.0&amp;e=zip</artifactLink>" //
                        + "</artifact>"));

        assertEquals("org.example.helloworld", nexusArtifact.getGroupId());
        assertEquals("org.example.helloworld.updatesite", nexusArtifact.getArtifactId());
        assertEquals("0.1.0", nexusArtifact.getVersion());
        assertEquals(null, nexusArtifact.getClassifier());
        assertEquals("eclipse-update-site", nexusArtifact.getPackaging());
        assertEquals("zip", nexusArtifact.getExtension());
        assertEquals("build.milestones", nexusArtifact.getRepoId());
        assertEquals("context.build.milestones", nexusArtifact.getContextId());

        assertEquals(
                new URI("http://mynexus:8081/nexus/service/local/repositories/build.milestones/content/org/" //
                        + "example/helloworld/org.example.helloworld.updatesite/0.1.0/org.example.helloworld." //
                        + "updatesite-0.1.0.eclipse-update-site"),
                nexusArtifact.getResourceURI());
        assertEquals(
                new URI("http://mynexus:8081/nexus/service/local/artifact/maven/redirect?r=build.milestones" //
                        + "&g=org.example.helloworld&a=org.example.helloworld.updatesite&v=0.1.0&e=pom"),
                nexusArtifact.getPomLink());
        assertEquals(
                new URI("http://mynexus:8081/nexus/service/local/artifact/maven/redirect?r=build.milestones" //
                        + "&g=org.example.helloworld&a=org.example.helloworld.updatesite&v=0.1.0&e=zip"),
                nexusArtifact.getArtifactLink());
    }

    @Test
    public void testNexusArtifactImpl_StrangeOrderAndIncomplete() throws Exception {
        NexusArtifactImpl nexusArtifact = new NexusArtifactImpl(getElement(//
                "          <artifact>" //
                        + "  <version>2.1.1</version>" //
                        + "  <packaging>eclipse-update-site</packaging>" //
                        + "  <extension>zip</extension>" //
                        + "  <repoId>build.milestones</repoId>" //
                        + "  <contextId>context.build.milestones</contextId>" //
                        + "  <groupId>org.example.helloworld</groupId>" //
                        + "  <artifactId>org.example.helloworld.updatesite</artifactId>" //
                        + "</artifact>"));

        assertEquals("org.example.helloworld", nexusArtifact.getGroupId());
        assertEquals("org.example.helloworld.updatesite", nexusArtifact.getArtifactId());
        assertEquals("2.1.1", nexusArtifact.getVersion());
        assertEquals(null, nexusArtifact.getClassifier());
        assertEquals("eclipse-update-site", nexusArtifact.getPackaging());
        assertEquals("zip", nexusArtifact.getExtension());
        assertEquals("build.milestones", nexusArtifact.getRepoId());
        assertEquals("context.build.milestones", nexusArtifact.getContextId());
    }

    @Test
    public void testNexusArtifactImpl_EmptyArtifact() throws Exception {
        NexusArtifactImpl nexusArtifact = new NexusArtifactImpl(getElement("<artifact/>"));

        assertEquals(null, nexusArtifact.getGroupId());
        assertEquals(null, nexusArtifact.getArtifactId());
        assertEquals(null, nexusArtifact.getVersion());
        assertEquals(null, nexusArtifact.getClassifier());
        assertEquals(null, nexusArtifact.getPackaging());
        assertEquals(null, nexusArtifact.getExtension());
        assertEquals(null, nexusArtifact.getRepoId());
        assertEquals(null, nexusArtifact.getContextId());

        assertEquals(null, nexusArtifact.getResourceURI());
        assertEquals(null, nexusArtifact.getPomLink());
        assertEquals(null, nexusArtifact.getArtifactLink());
    }

    @Test
    public void testNexusArtifactImpl_NoArtifact() throws Exception {
        try {
            new NexusArtifactImpl(getElement("<dummy/>"));
            fail("IllegalArgumentException expected, but not thrown.");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("'artifact'"));
        }
    }

    @Test
    public void testNexusArtifactImpl_rootElementIsNull() throws SAXException, IOException,
            ParserConfigurationException,
            NexusClientException {
        try {
            new NexusArtifactImpl(null);
            fail("IllegalArgumentException expected, but not thrown.");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("null"));
        }
    }

    @Test
    public void testNexusArtifactImpl_DuplicatedVersion() throws Exception {
        try {
            new NexusArtifactImpl(getElement(//
                    "          <artifact>" //
                            + "  <version>2.1.0</version>" //
                            + "  <groupId>org.example.helloworld</groupId>" //
                            + "  <artifactId>org.example.helloworld.updatesite</artifactId>" //
                            + "  <version>0.1.0</version>" //
                            + "</artifact>"));
            fail("NexusClientException expected, but not thrown.");
        } catch (NexusClientException e) {
            assertTrue(e.getMessage().contains("version"));
        }
    }

}
