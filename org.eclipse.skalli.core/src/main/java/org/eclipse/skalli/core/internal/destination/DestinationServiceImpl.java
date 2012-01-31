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
package org.eclipse.skalli.core.internal.destination;

import java.net.URL;
import java.text.MessageFormat;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.eclipse.skalli.services.configuration.ConfigurationService;
import org.eclipse.skalli.services.destination.ConfigKeyProxy;
import org.eclipse.skalli.services.destination.DestinationService;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DestinationServiceImpl implements DestinationService {

    private static final Logger LOG = LoggerFactory.getLogger(DestinationServiceImpl.class);

    private static final String HTTP = "http"; //$NON-NLS-1$
    private static final String HTTPS = "https"; //$NON-NLS-1$

    private static final String HTTP_PROXY_HOST = "http.proxyHost"; //$NON-NLS-1$
    private static final String HTTP_PROXY_PORT = "http.proxyPort"; //$NON-NLS-1$
    private static final String HTTPS_PROXY_HOST = "https.proxyHost"; //$NON-NLS-1$
    private static final String HTTPS_PROXY_PORT = "https.proxyPort"; //$NON-NLS-1$
    private static final String NON_PROXY_HOSTS = "proxy.nonProxyHosts"; //$NON-NLS-1$

    // regular expression to sanitize non-proxy host parameter
    private static final String[] RE_SEARCH = new String[] { ";", "*", "." }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    private static final String[] RE_REPLACE = new String[] { "|", "(\\w|\\.|\\-)*", "\\." }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    // general timeout for connection requests
    private static final int CONNECT_TIMEOUT = 10000;
    private static final int READ_TIMEOUT = 300000;

    private ConfigurationService configService;

    protected void activate(ComponentContext context) {
        LOG.info(MessageFormat.format("[DestinationService] {0} : activated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
        SSLBypass.activate();
    }

    protected void deactivate(ComponentContext context) {
        LOG.info(MessageFormat.format("[DestinationService] {0} : deactivated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
        SSLBypass.deactivate();
    }

    protected void bindConfigurationService(ConfigurationService configService) {
        LOG.info(MessageFormat.format("bindConfigurationService({0})", configService)); //$NON-NLS-1$
        this.configService = configService;
    }

    protected void unbindConfigurationService(ConfigurationService configService) {
        LOG.info(MessageFormat.format("unbindConfigurationService({0})", configService)); //$NON-NLS-1$
        this.configService = null;
    }

    @Override
    public HttpClient getClient(URL url) {
        if (!isSupportedProtocol(url)) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Protocol ''{0}'' is not suppported by this method", url.getProtocol()));
        }

        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, CONNECT_TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, READ_TIMEOUT);
        HttpConnectionParams.setTcpNoDelay(params, true);

        HttpClient client = new DefaultHttpClient(params);

        if (!isLocalDomain(url)) {
            setProxy(client, url);
        }

        return client;
    }

    private void setProxy(HttpClient client, URL url) {
        String protocol = url.getProtocol();

        // use the system properties as default
        String defaultProxyHost = System.getProperty(HTTP_PROXY_HOST);
        String defaultProxyPort = System.getProperty(HTTP_PROXY_PORT);
        String proxyHost = HTTPS.equals(protocol) ?
                System.getProperty(HTTPS_PROXY_HOST, defaultProxyHost)
                : defaultProxyHost;
        int proxyPort = NumberUtils.toInt(HTTPS.equals(protocol) ?
                System.getProperty(HTTPS_PROXY_PORT, defaultProxyPort)
                : defaultProxyPort);
        String nonProxyHosts = System.getProperty(NON_PROXY_HOSTS, StringUtils.EMPTY);

        // allow to overwrite the system properties with configuration /api/config/proxy
        if (configService != null) {
            String defaultConfigProxyHost = configService.readString(ConfigKeyProxy.HOST);
            String defaultConfigProxyPort = configService.readString(ConfigKeyProxy.PORT);
            String configProxyHost = HTTPS.equals(protocol) ?
                    configService.readString(ConfigKeyProxy.HTTPS_HOST)
                    : defaultConfigProxyHost;
            int configProxyPort = NumberUtils.toInt(HTTPS.equals(protocol) ?
                    configService.readString(ConfigKeyProxy.HTTPS_PORT)
                    : defaultConfigProxyPort);
            if (StringUtils.isNotBlank(configProxyHost) && configProxyPort > 0) {
                proxyHost = configProxyHost;
                proxyPort = configProxyPort;
            }
            String configNonProxyHosts = configService.readString(ConfigKeyProxy.NONPROXYHOSTS);
            if (StringUtils.isNotBlank(configNonProxyHosts)) {
                nonProxyHosts = configNonProxyHosts;
            }
        }

        // sanitize the nonProxyHost pattern (remove whitespace etc.)
        if (StringUtils.isNotBlank(nonProxyHosts)) {
            nonProxyHosts = StringUtils.replaceEach(StringUtils.deleteWhitespace(nonProxyHosts),
                    RE_SEARCH, RE_REPLACE);
        }

        if (StringUtils.isNotBlank(proxyHost)
                && proxyPort > 0
                && !Pattern.matches(nonProxyHosts, url.getHost())) {
            HttpHost proxy = new HttpHost(proxyHost, proxyPort, HTTP);
            client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        }
    }

    /**
     * Returns <code>true</code>, if the given URL starts with a known
     * protocol specifier, i.e. <tt>http://</tt> or <tt>https://</tt>.
     *
     * @param url  the URL to check.
     */
    @Override
    public boolean isSupportedProtocol(URL url) {
        String protocol = url.getProtocol();
        return protocol.equals(HTTP) || protocol.equals(HTTPS);
    }

    /**
     * Returns <code>true</code> if the given URL belongs to the local domain,
     * i.e. only a host name like <tt>"myhost"</tt> instead of <tt>"myhost.example,org"</tt>
     * is specified.
     *
     * @param url  the URL to check.
     */
    private boolean isLocalDomain(URL url) {
        return url.getHost().indexOf('.') < 0;
    }
}
