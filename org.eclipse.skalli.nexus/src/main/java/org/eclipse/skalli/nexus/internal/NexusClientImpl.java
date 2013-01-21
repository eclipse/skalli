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

import static org.apache.http.HttpStatus.SC_MOVED_PERMANENTLY;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.eclipse.skalli.commons.HttpUtils;
import org.eclipse.skalli.commons.XMLUtils;
import org.eclipse.skalli.nexus.NexusClient;
import org.eclipse.skalli.nexus.NexusClientException;
import org.eclipse.skalli.nexus.NexusSearchResult;
import org.eclipse.skalli.nexus.internal.config.NexusConfig;
import org.eclipse.skalli.nexus.internal.config.NexusResource;
import org.eclipse.skalli.services.configuration.ConfigurationService;
import org.eclipse.skalli.services.destination.Destinations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class NexusClientImpl implements NexusClient {

    private static final Logger LOG = LoggerFactory.getLogger(NexusClientImpl.class);

    private ConfigurationService configService;
    private HttpClient client;

    /* (non-Javadoc)
     * @see org.eclipse.skalli.nexus.NexusClient#searchArtifactVersions(java.lang.String, java.lang.String)
     */
    @Override
    public NexusSearchResult searchArtifactVersions(String groupId, String artifactId) throws NexusClientException,
            IOException {
        if (configService == null) {
            throw new NexusClientException("No configuration service available");
        }
        NexusConfig nexusConfig = configService.readCustomization(NexusResource.KEY, NexusConfig.class);
        if (nexusConfig == null) {
            throw new NexusClientException(MessageFormat.format("Nexus configuration not found (key={0})",
                    NexusResource.KEY));
        }

        //hint, count=Integer.MAX_VALUE, does not work. the http request comes back without an error and from the result you cannot find out
        // that something got wrong. Take a big value under the assumption that you will never have so many versions.
        return searchArtifactVersions(nexusConfig, groupId, artifactId, 10000000);
    }

    NexusSearchResult searchArtifactVersions(NexusConfig nexusConfig, String groupId, String artifactId,
            int count)
            throws NexusClientException, IOException {
        return searchArtifactVersions(new NexusUrlCalculator(nexusConfig, groupId, artifactId), count);
    }

    NexusSearchResult searchArtifactVersions(NexusUrlCalculator nexusUrlCalculator, int count)
            throws NexusClientException, IOException {
        return new NexusSearchResponseImpl(getElementFromUrlResponse(nexusUrlCalculator.getNexusUrl(0, count)));

    }

    Element getElementFromUrlResponse(URL nexusUrl) throws IOException, NexusClientException {
        HttpClient client = getClient(nexusUrl);
        HttpGet method = new HttpGet(nexusUrl.toExternalForm());
        HttpResponse response = null;
        try {
            LOG.info("GET " + nexusUrl); //$NON-NLS-1$
            response = client.execute(method);
            int status = response.getStatusLine().getStatusCode();
            LOG.info(status + " " + response.getStatusLine().getReasonPhrase()); //$NON-NLS-1$
            if (status == HttpStatus.SC_OK) {
                InputStream in = response.getEntity().getContent();
                Document document;
                try {
                    document = XMLUtils.documentFromStream(in);
                } catch (SAXException e) {
                    throw new NexusClientException(MessageFormat.format("Problems found for {0}: {1}", nexusUrl,
                            e.getMessage()), e);
                } catch (ParserConfigurationException e) {
                    throw new NexusClientException(MessageFormat.format("Problems found for {0}: {1}", nexusUrl,
                            e.getMessage()), e);
                }
                return document.getDocumentElement();
            } else {
                switch (status) {
                case SC_UNAUTHORIZED:
                    throw new IOException(MessageFormat.format("{0} found but authentication required", nexusUrl));
                case SC_MOVED_PERMANENTLY:
                    throw new IOException(
                            MessageFormat.format("{0} not found. Resource has been moved permanently to {1}",
                                    nexusUrl, response.getFirstHeader("Location")));
                default:
                    throw new IOException(MessageFormat.format(
                            "{0} not found. Host reports a temporary problem: {1} {2}",
                            nexusUrl, status, response.getStatusLine().getReasonPhrase()));
                }
            }
        } finally {
            HttpUtils.consumeQuietly(response);
        }
    }

    private HttpClient getClient(URL url) {
        if (client == null) {
            client = Destinations.getClient(url);
        }
        return client;
    }

    protected void bindConfigurationService(ConfigurationService configService) {
        LOG.info(MessageFormat.format("bindConfigurationService({0})", configService)); //$NON-NLS-1$
        this.configService = configService;

    }

    protected void unbindConfigurationService(ConfigurationService configService) {
        LOG.info(MessageFormat.format("unbindConfigurationService({0})", configService)); //$NON-NLS-1$
        this.configService = null;
    }
  }
