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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.skalli.commons.XMLUtils;
import org.eclipse.skalli.nexus.NexusClientException;
import org.eclipse.skalli.nexus.NexusSearchResult;
import org.eclipse.skalli.nexus.internal.config.NexusConfig;
import org.eclipse.skalli.nexus.internal.config.NexusResource;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.configuration.ConfigKey;
import org.eclipse.skalli.services.configuration.ConfigTransaction;
import org.eclipse.skalli.services.configuration.ConfigurationService;
import org.eclipse.skalli.services.destination.DestinationService;
import org.eclipse.skalli.testutil.BundleManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

@SuppressWarnings("nls")
public class NexusClientImplTest {

    private static final String artifactId = "org.eclipse.skalli.core";
    final static String groupId = "org.eclipse.skalli";
    final static String artifactStr =
            "    <artifact> " //
                    + "      <resourceURI>http://mynexus/nexus/service/local/repositories/build.snapshots/content/org/eclipse/skalli/org.eclipse.skalli.core/0.1.0-SNAPSHOT/org.eclipse.skalli.core-0.1.0-SNAPSHOT.jar</resourceURI> " //
                    + "      <groupId>"
                    + groupId
                    + "</groupId> " //
                    + "<artifactId>"
                    + artifactId
                    + "</artifactId> " //
                    + "      <version>0.1.0-SNAPSHOT</version> " //
                    + "      <packaging>eclipse-plugin</packaging> " //
                    + "      <extension>jar</extension> " //
                    + "      <repoId>build.snapshots</repoId> " //
                    + "      <contextId>build.snapshots</contextId> " //
                    + "      <pomLink>http://mynexus/nexus/service/local/artifact/maven/redirect?r=build.snapshots&amp;g=org.eclipse.skalli&amp;a=org.eclipse.skalli.core&amp;v=0.1.0-SNAPSHOT&amp;e=pom</pomLink> " //
                    + "      <artifactLink>http://mynexus/nexus/service/local/artifact/maven/redirect?r=build.snapshots&amp;g=org.eclipse.skalli&amp;a=org.eclipse.skalli.core&amp;v=0.1.0-SNAPSHOT&amp;e=jar</artifactLink> " //
                    + "    </artifact> ";

    final static String root = "<search-results> " //
            + "  <totalCount>1</totalCount> " //
            + "  <from>0</from> " //
            + "  <count>10</count> " //
            + "  <tooManyResults>false</tooManyResults> " //
            + "  <data> " //
            + artifactStr //
            + "  </data> " //
            + "</search-results>";

    private Element rootElement;
    private NexusConfig nexusConfig;
    private static DestinationService destinationService;

    @BeforeClass
    public static void beforeClass() throws Exception {
        BundleManager.startBundles();
        destinationService = Services.getService(DestinationService.class);
        Assert.assertNotNull(destinationService);
    }

    @Before
    public void Before() throws SAXException, IOException, ParserConfigurationException {
        rootElement = XMLUtils.documentFromString(root).getDocumentElement();
        nexusConfig = new NexusConfig();
        nexusConfig.setUrl("http://mynexus/nexus/");
        nexusConfig.setDomain("repositories");
        nexusConfig.setTarget("build.milestones");
    }

    @Test
    public void testSearchArtifactVersions_StringString() throws NexusClientException, IOException {
        NexusClientImpl nexusClientImpl = new NexusClientImpl() {
            /* (non-Javadoc)
             * @see org.eclipse.skalli.nexus.internal.NexusClientImpl#searchArtifactVersions(org.eclipse.skalli.nexus.internal.config.NexusConfig, java.lang.String, java.lang.String, int)
             */
            @Override
            NexusSearchResult searchArtifactVersions(NexusConfig nexusConfigPar, String groupIdPar,
                    String artifactIdPar,
                    int count) throws NexusClientException {
                if (!nexusConfig.equals(nexusConfigPar) || !groupId.equals(groupIdPar)
                        || !artifactId.equals(artifactIdPar) ) {
                    throw new RuntimeException("call with unexpected parameters");
                }
                return new NexusSearchResponseImpl(rootElement);
            }
        };
        ConfigurationService configService = new ConfigurationService() {

            @Override
            public void writeString(ConfigTransaction tx, ConfigKey key, String value) {
            }

            @Override
            public void writeInteger(ConfigTransaction tx, ConfigKey key, Integer value) {
            }

            @Override
            public <T> void writeCustomization(String customizationKey, T customization) {
            }

            @Override
            public void writeBoolean(ConfigTransaction tx, ConfigKey key, Boolean value) {
            }

            @Override
            public ConfigTransaction startTransaction() {
                return null;
            }

            @Override
            public void rollback(ConfigTransaction tx) {
            }

            @Override
            public String readString(ConfigKey key) {
                return null;
            }

            @Override
            public Integer readInteger(ConfigKey key) {
                return null;
            }

            @SuppressWarnings("unchecked")
            @Override
            public <T> T readCustomization(String customizationKey, Class<T> customizationClass) {
                return (T) nexusConfig;
            }

            @Override
            public Boolean readBoolean(ConfigKey key) {
                return null;
            }

            @Override
            public void commit(ConfigTransaction tx) {
            }
        };

        nexusClientImpl.bindConfigurationService(configService);
        nexusClientImpl.bindDestinationService(destinationService);

        NexusSearchResult nexusSearchResult = nexusClientImpl.searchArtifactVersions(groupId, artifactId);
        assertNexusSearchResult(nexusSearchResult);

    }

    @Test
    public void testSearchArtifactVersions_StringString_noConfigServie() throws IOException {
        try {
            new NexusClientImpl().searchArtifactVersions(groupId, artifactId);
            fail();
        } catch (NexusClientException e) {
            assertThat(e.getMessage(), containsString("available"));
        }
    }

    @Test
    public void testSearchArtifactVersions_StringString_noConfig() throws IOException {
        NexusClientImpl nexusClientImpl = new NexusClientImpl();
        ConfigurationService configService = new ConfigurationService() {

            @Override
            public void writeString(ConfigTransaction tx, ConfigKey key, String value) {
            }

            @Override
            public void writeInteger(ConfigTransaction tx, ConfigKey key, Integer value) {
            }

            @Override
            public <T> void writeCustomization(String customizationKey, T customization) {
            }

            @Override
            public void writeBoolean(ConfigTransaction tx, ConfigKey key, Boolean value) {
            }

            @Override
            public ConfigTransaction startTransaction() {
                return null;
            }

            @Override
            public void rollback(ConfigTransaction tx) {
            }

            @Override
            public String readString(ConfigKey key) {
                return null;
            }

            @Override
            public Integer readInteger(ConfigKey key) {
                return null;
            }

            @Override
            public <T> T readCustomization(String customizationKey, Class<T> customizationClass) {
                return (T) null;
            }

            @Override
            public Boolean readBoolean(ConfigKey key) {
                return null;
            }

            @Override
            public void commit(ConfigTransaction tx) {
            }
        };

        nexusClientImpl.bindConfigurationService(configService);
        nexusClientImpl.bindDestinationService(destinationService);

        try {
            nexusClientImpl.searchArtifactVersions(groupId, artifactId);
            fail();
        } catch (NexusClientException e) {
            assertThat(e.getMessage(), containsString(NexusResource.KEY));
        }
    }

    /**
     * Test method for {@link org.eclipse.skalli.nexus.internal.NexusClientImpl#searchArtifactVersions(org.eclipse.skalli.nexus.internal.config.NexusConfig, java.lang.String, java.lang.String, int)}.
     * @throws NexusClientException
     * @throws IOException
     */
    @Test
    public void testSearchArtifactVersions_NexusConfigStringStringInt() throws NexusClientException, IOException {
        NexusClientImpl nexusClientImpl = new NexusClientImpl() {
            @Override
            Element getElementFromUrlResponse(URL nexusUrl) throws NexusClientException {
                return rootElement;
            }
        };

        NexusSearchResult nexusSearchResult = nexusClientImpl.searchArtifactVersions(nexusConfig, groupId, artifactId,
                10);
        assertNexusSearchResult(nexusSearchResult);
    }

    /**
     * Test method for {@link org.eclipse.skalli.nexus.internal.NexusClientImpl#searchArtifactVersions(org.eclipse.skalli.nexus.internal.NexusUrlCalculator, int)}.
     * @throws NexusClientException
     * @throws IOException
     */
    @Test
    public void testSearchArtifactVersions_NexusUrlCalculatorInt() throws NexusClientException, IOException {

        NexusClientImpl nexusClientImpl = new NexusClientImpl() {
            @Override
            Element getElementFromUrlResponse(URL nexusUrl) throws NexusClientException {
                return rootElement;
            }
        };

        NexusUrlCalculator nexusUrlCalculator = new NexusUrlCalculator(nexusConfig, groupId, artifactId);
        NexusSearchResult nexusSearchResult = nexusClientImpl.searchArtifactVersions(nexusUrlCalculator, 10);
        assertNexusSearchResult(nexusSearchResult);

    }

    /**
     * Test method for {@link org.eclipse.skalli.nexus.internal.NexusClientImpl#getElementFromUrlResponse(java.net.URL)}.
     */
    @Test
    @Ignore("dont know how we can test that")
    public void testGetElementFromUrlResponse() {
        //        NexusClientImpl imp = new NexusClientImpl();
        //        imp.getElementFromUrlResponse(nexusUrl)

    }

    private void assertNexusSearchResult(NexusSearchResult nexusSearchResult) {
        assertThat(nexusSearchResult.getTotalCount(), is(1));
        assertThat(nexusSearchResult.getCount(), is(10));
        assertThat(nexusSearchResult.getFrom(), is(0));
        assertThat(nexusSearchResult.getArtifacts().size(), is(1));
        assertThat(nexusSearchResult.getArtifacts().get(0).getArtifactId(), is(artifactId));
        assertThat(nexusSearchResult.getArtifacts().get(0).getGroupId(), is(groupId));
    }
}
