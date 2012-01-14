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

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.skalli.commons.XMLUtils;
import org.eclipse.skalli.nexus.NexusArtifact;
import org.eclipse.skalli.nexus.NexusClientException;
import org.junit.Test;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

@SuppressWarnings("nls")
public class NexusSearchResponseImplTest {

    final static String artifactStr1 = //
            "              <artifact>" //
                    + "      <resourceURI>http://mynexus:8081/nexus/service/local/repositories/build.milestones/content/org/example/helloworld/org.example.helloworld.updatesite/0.1.0/org.example.helloworld.updatesite-0.1.0.eclipse-update-site</resourceURI>" //
                    + "      <groupId>org.example.helloworld</groupId>" //
                    + "      <artifactId>org.example.helloworld.updatesite</artifactId>" //
                    + "      <version>0.1.0</version>" //
                    + "      <packaging>eclipse-update-site</packaging>" //
                    + "      <extension>zip</extension>" //
                    + "      <repoId>build.milestones</repoId>" //
                    + "      <contextId>build.milestones</contextId>" //
                    + "      <pomLink>http://mynexus:8081/nexus/service/local/artifact/maven/redirect?r=build.milestones&amp;g=org.example.helloworld&amp;a=org.example.helloworld.updatesite&amp;v=0.1.0&amp;e=pom</pomLink>" //
                    + "      <artifactLink>http://mynexus:8081/nexus/service/local/artifact/maven/redirect?r=build.milestones&amp;g=org.example.helloworld&amp;a=org.example.helloworld.updatesite&amp;v=0.1.0&amp;e=zip</artifactLink>" //
                    + "    </artifact>";
    final static String artifactStr2 = //
            "              <artifact>" //
                    + "      <resourceURI>http://mynexus:8081/nexus/service/local/repositories/build.milestones/content/org/example/helloworld/org.example.helloworld.updatesite/0.1.0/org.example.helloworld.updatesite-0.1.0.eclipse-update-site</resourceURI>" //
                    + "      <groupId>org.example.helloworld</groupId>" //
                    + "      <artifactId>org.example.helloworld.updatesite</artifactId>" //
                    + "      <version>0.1.0</version>" //
                    + "      <packaging>eclipse-update-site</packaging>" //
                    + "      <extension>zip</extension>" //
                    + "      <repoId>build.milestones</repoId>" //
                    + "      <contextId>build.milestones</contextId>" //
                    + "    </artifact>";

    final static String root1 = //
    "          <search-results>" //
            + "  <totalCount>1</totalCount>" //
            + "  <from>0</from>" //
            + "  <count>10000</count>" //
            + "  <tooManyResults>false</tooManyResults>" //
            + "  <data>" //
            + artifactStr1 //
            + "  </data>" //
            + "</search-results>";

    final static String artifactStr2_1 =
            "    <artifact> " //
                    + "      <resourceURI>http://mynexus:8081/nexus/service/local/repositories/build.snapshots/content/org/eclipse/skalli/org.eclipse.skalli.core/0.1.0-SNAPSHOT/org.eclipse.skalli.core-0.1.0-SNAPSHOT.jar</resourceURI> " //
                    + "      <groupId>org.eclipse.skalli</groupId> " //
                    + "      <artifactId>org.eclipse.skalli.core</artifactId> " //
                    + "      <version>0.1.0-SNAPSHOT</version> " //
                    + "      <packaging>eclipse-plugin</packaging> " //
                    + "      <extension>jar</extension> " //
                    + "      <repoId>build.snapshots</repoId> " //
                    + "      <contextId>build.snapshots</contextId> " //
                    + "      <pomLink>http://mynexus:8081/nexus/service/local/artifact/maven/redirect?r=build.snapshots&amp;g=org.eclipse.skalli&amp;a=org.eclipse.skalli.core&amp;v=0.1.0-SNAPSHOT&amp;e=pom</pomLink> " //
                    + "      <artifactLink>http://mynexus:8081/nexus/service/local/artifact/maven/redirect?r=build.snapshots&amp;g=org.eclipse.skalli&amp;a=org.eclipse.skalli.core&amp;v=0.1.0-SNAPSHOT&amp;e=jar</artifactLink> " //
                    + "    </artifact> ";

    final static String artifactStr2_2 =
            "    <artifact> " //
                    + "      <resourceURI>http://mynexus:8081/nexus/service/local/repositories/build.snapshots/content/org/eclipse/skalli/org.eclipse.skalli.core/0.1.0-SNAPSHOT/org.eclipse.skalli.core-0.1.0-SNAPSHOT-sources.jar</resourceURI> " //
                    + "      <groupId>org.eclipse.skalli</groupId> " //
                    + "      <artifactId>org.eclipse.skalli.core</artifactId> " //
                    + "      <version>0.1.0-SNAPSHOT</version> " //
                    + "      <classifier>sources</classifier> " //
                    + "      <packaging>jar</packaging> " //
                    + "      <extension>jar</extension> " //
                    + "      <repoId>build.snapshots</repoId> " //
                    + "      <contextId>build.snapshots</contextId> " //
                    + "      <pomLink></pomLink> " //
                    + "      <artifactLink>http://mynexus:8081/nexus/service/local/artifact/maven/redirect?r=build.snapshots&amp;g=org.eclipse.skalli&amp;a=org.eclipse.skalli.core&amp;v=0.1.0-SNAPSHOT&amp;e=jar&amp;c=sources</artifactLink> " //
                    + "    </artifact> ";
    final static String artifactStr2_3 =

            "    <artifact> " //
                    + "      <resourceURI>http://mynexus:8081/nexus/service/local/repositories/build.snapshots/content/org/eclipse/skalli/org.eclipse.skalli.core/0.1.0-SNAPSHOT/org.eclipse.skalli.core-0.1.0-SNAPSHOT-p2metadata.xml</resourceURI> " //
                    + "      <groupId>org.eclipse.skalli</groupId> " //
                    + "      <artifactId>org.eclipse.skalli.core</artifactId> " //
                    + "      <version>0.1.0-SNAPSHOT</version> " //
                    + "      <classifier>p2metadata</classifier> " //
                    + "      <packaging>xml</packaging> " //
                    + "      <extension>xml</extension> " //
                    + "      <repoId>build.snapshots</repoId> " //
                    + "      <contextId>build.snapshots</contextId> " //
                    + "      <pomLink></pomLink> " //
                    + "      <artifactLink>http://mynexus:8081/nexus/service/local/artifact/maven/redirect?r=build.snapshots&amp;g=org.eclipse.skalli&amp;a=org.eclipse.skalli.core&amp;v=0.1.0-SNAPSHOT&amp;e=xml&amp;c=p2metadata</artifactLink> " //
                    + "    </artifact> ";

    final static String artifactStr2_4 =
            "    <artifact> " //
                    + "      <resourceURI>http://mynexus:8081/nexus/service/local/repositories/build.snapshots/content/org/eclipse/skalli/org.eclipse.skalli.core/0.1.0-SNAPSHOT/org.eclipse.skalli.core-0.1.0-SNAPSHOT-p2artifacts.xml</resourceURI> " //
                    + "      <groupId>org.eclipse.skalli</groupId> " //
                    + "      <artifactId>org.eclipse.skalli.core</artifactId> " //
                    + "      <version>0.1.0-SNAPSHOT</version> " //
                    + "      <classifier>p2artifacts</classifier> " //
                    + "      <packaging>xml</packaging> " //
                    + "      <extension>xml</extension> " //
                    + "      <repoId>build.snapshots</repoId> " //
                    + "      <contextId>build.snapshots</contextId> " //
                    + "      <pomLink></pomLink> " //
                    + "      <artifactLink>http://mynexus:8081/nexus/service/local/artifact/maven/redirect?r=build.snapshots&amp;g=org.eclipse.skalli&amp;a=org.eclipse.skalli.core&amp;v=0.1.0-SNAPSHOT&amp;e=xml&amp;c=p2artifacts</artifactLink> " //
                    + "    </artifact> ";

    final static String root2 = "<search-results> " //
            + "  <totalCount>4</totalCount> " //
            + "  <from>0</from> " //
            + "  <count>10</count> " //
            + "  <tooManyResults>false</tooManyResults> " //
            + "  <data> " //
            + artifactStr2_1 //
            + artifactStr2_2 //
            + artifactStr2_3 //
            + artifactStr2_4 //
            + "  </data> " //
            + "</search-results>";

    private NexusArtifactImpl getNexusArtifact(final String artifactStr) throws NexusClientException, SAXException,
            IOException,
            ParserConfigurationException {
        return new NexusArtifactImpl(XMLUtils.documentFromString(artifactStr)
                .getDocumentElement());
    }

    private Element getRootElement(String root) throws SAXException, IOException, ParserConfigurationException {
        return XMLUtils.documentFromString(root).getDocumentElement();
    }

    @Test
    public void testNexusSearchResponseImpl_SimpleExample() throws SAXException, IOException,
            ParserConfigurationException,
            NexusClientException {
        NexusSearchResponseImpl nexusSearchResponseImpl = new NexusSearchResponseImpl(getRootElement(root1));
        assertThat(nexusSearchResponseImpl.getTotalCount(), is(1));
        assertThat(nexusSearchResponseImpl.getFrom(), is(0));
        assertThat(nexusSearchResponseImpl.getCount(), is(10000));
        assertThat(nexusSearchResponseImpl.isToManyResults(), is(false));
        assertThat(nexusSearchResponseImpl.getArtifacts().size(), is(1));
        assertThat(nexusSearchResponseImpl.getArtifacts(), hasItem((NexusArtifact) getNexusArtifact(artifactStr1)));
    }

    @Test
    public void testNexusSearchResponseImpl_ExtendedExample() throws SAXException, IOException,
            ParserConfigurationException,
            NexusClientException {
        NexusSearchResponseImpl nexusSearchResponseImpl = new NexusSearchResponseImpl(getRootElement(root2));
        assertThat(nexusSearchResponseImpl.getTotalCount(), is(4));
        assertThat(nexusSearchResponseImpl.getFrom(), is(0));
        assertThat(nexusSearchResponseImpl.getCount(), is(10));
        assertThat(nexusSearchResponseImpl.isToManyResults(), is(false));
        assertThat(nexusSearchResponseImpl.getArtifacts().size(), is(4));
        assertThat(nexusSearchResponseImpl.getArtifacts(), hasItem((NexusArtifact) getNexusArtifact(artifactStr2_1)));
        assertThat(nexusSearchResponseImpl.getArtifacts(), hasItem((NexusArtifact) getNexusArtifact(artifactStr2_2)));
        assertThat(nexusSearchResponseImpl.getArtifacts(), hasItem((NexusArtifact) getNexusArtifact(artifactStr2_3)));
        assertThat(nexusSearchResponseImpl.getArtifacts(), hasItem((NexusArtifact) getNexusArtifact(artifactStr2_4)));
    }

}
