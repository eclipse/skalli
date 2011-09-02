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

import static org.apache.commons.httpclient.HttpStatus.SC_MOVED_PERMANENTLY;
import static org.apache.commons.httpclient.HttpStatus.SC_UNAUTHORIZED;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.eclipse.skalli.common.configuration.ConfigurationService;
import org.eclipse.skalli.common.util.HttpUtils;
import org.eclipse.skalli.common.util.XMLUtils;
import org.eclipse.skalli.nexus.NexusClient;
import org.eclipse.skalli.nexus.NexusClientException;
import org.eclipse.skalli.nexus.NexusSearchResult;
import org.eclipse.skalli.nexus.internal.config.NexusConfig;
import org.eclipse.skalli.nexus.internal.config.NexusResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class NexusClientImpl implements NexusClient {

    private static final Logger LOG = LoggerFactory.getLogger(NexusClientImpl.class);

    private ConfigurationService configService;

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
            throw new NexusClientException("Nexus configuration not found (key=" + NexusResource.KEY + ")");
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

    Element getElementFromUrlResponse(URL nexusUrl) throws NexusClientException, IOException {
        String externalForm = nexusUrl.toExternalForm();
        GetMethod method = new GetMethod(externalForm);
        method.setFollowRedirects(false);
        try {
            int statusCode;
            try {
                LOG.info("GET " + nexusUrl); //$NON-NLS-1$
                statusCode = HttpUtils.getClient(nexusUrl).executeMethod(method);
                LOG.info(statusCode + " " + HttpStatus.getStatusText(statusCode)); //$NON-NLS-1$
            } catch (HttpException e) {
                throw new HttpException(MessageFormat.format("Problems found for {0}: {1}", nexusUrl,
                        e.getMessage()), e);
            } catch (IOException e) {
                throw new IOException(MessageFormat.format("Problems found for {0}: {1}", nexusUrl,
                        e.getMessage()), e);
            }
            if (statusCode == HttpStatus.SC_OK) {
                InputStream in = method.getResponseBodyAsStream();
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
                switch (statusCode) {
                case SC_UNAUTHORIZED:
                    throw new HttpException(MessageFormat.format("{0} found but authentication required", nexusUrl));
                case SC_MOVED_PERMANENTLY:
                    throw new HttpException(
                            MessageFormat.format("{0} not found. Resource has been moved permanently to {1}",
                                    nexusUrl, method.getResponseHeader("Location")));
                default:
                    throw new HttpException(MessageFormat.format(
                            "{0} not found. Host reports a temporary problem: {1} {2}",
                            nexusUrl, statusCode, method.getStatusText()));
                }
            }
        } finally {
            method.releaseConnection();
        }
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
