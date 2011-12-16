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
package org.eclipse.skalli.common.util;

import java.net.URL;
import java.text.MessageFormat;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.eclipse.skalli.common.Services;
import org.eclipse.skalli.common.configuration.ConfigurationService;
import org.eclipse.skalli.common.configuration.proxy.ConfigKeyProxy;

public class HttpUtils {

    // HTTP protocol stuff
    private static final String PROTOCOL_SEPARATOR = "://"; //$NON-NLS-1$

    public static final String HTTPS = "https"; //$NON-NLS-1$
    public static final String PROTOCOL_HTTPS = HTTPS + PROTOCOL_SEPARATOR;

    public static final String HTTP = "http"; //$NON-NLS-1$
    public static final String PROTOCOL_HTTP = HTTP + PROTOCOL_SEPARATOR;

    private static final String HTTP_PROXY_HOST = "http.proxyHost"; //$NON-NLS-1$
    private static final String HTTP_PROXY_PORT = "http.proxyPort"; //$NON-NLS-1$
    private static final String HTTPS_PROXY_HOST = "https.proxyHost"; //$NON-NLS-1$
    private static final String HTTPS_PROXY_PORT = "https.proxyPort"; //$NON-NLS-1$
    private static final String NON_PROXY_HOSTS = "proxy.nonProxyHosts"; //$NON-NLS-1$

    // RegExp for non proxy hosts
    private static final String[] RE_SEARCH = new String[] { ";", "*", "." }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    private static final String[] RE_REPLACE = new String[] { "|", "(\\w|\\.|\\-)*", "\\." }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    // general timeout for connection requests
    private static final int CONNECT_TIMEOUT = 10000;
    private static final int READ_TIMEOUT = 300000;

    // no instances
    private HttpUtils() {
    }

    static {
        Protocol.registerProtocol(HTTPS, new Protocol(HTTPS, new AlwaysTrustSSLProtocolSocketFactory(), 443));
    }

    /**
     * Returns an HTTP "user-agent" for the given URL to which HTTP methods can be applied.
     *
     * @param url  the URL to address.
     *
     * @throws IllegalArgumentException  if the protocol specified by <code>url</code> is not supported.
     */
    public static HttpClient getClient(URL url) {
        if (!isSupportedProtocol(url)) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Protocol ''{0}'' is not suppported by this method", url.getProtocol()));
        }

        HttpClient client = new HttpClient();
        HttpConnectionManagerParams params = client.getHttpConnectionManager().getParams();
        params.setConnectionTimeout(CONNECT_TIMEOUT);
        params.setSoTimeout(READ_TIMEOUT);
        params.setTcpNoDelay(true);

        if (!isLocalDomain(url)) {
            setProxy(client, url);
        }

        return client;
    }

    private static void setProxy(HttpClient client, URL url) {
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

        // allows to overwrite the system properties with configuration /api/config/proxy
        ConfigurationService configService = Services.getService(ConfigurationService.class);
        if (configService != null) {
            String defaultConfigProxyHost = configService.readString(ConfigKeyProxy.HOST);
            String defaultConfigProxyPort = configService.readString(ConfigKeyProxy.PORT);
            String configProxyHost = HTTPS.equals(protocol) ?
                    configService.readString(ConfigKeyProxy.HTTPS_HOST)
                    : defaultConfigProxyHost;
            int configProxyPort = NumberUtils.toInt(HTTPS.equals(protocol) ?
                    configService.readString(ConfigKeyProxy.HTTPS_PORT)
                    : defaultConfigProxyPort);
            String configNonProxyHosts = configService.readString(ConfigKeyProxy.NONPROXYHOSTS);
            if (StringUtils.isNotBlank(configProxyHost) && configProxyPort > 0) {
                proxyHost = configProxyHost;
                proxyPort = configProxyPort;
                if (StringUtils.isNotBlank(configNonProxyHosts)) {
                    nonProxyHosts = configNonProxyHosts;
                }
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
            HostConfiguration config = client.getHostConfiguration();
            config.setProxy(proxyHost, proxyPort);
        }
    }


    /**
     * Returns <code>true</code>, if the given URL starts with a known
     * protocol specifier, i.e. <tt>http://</tt> or <tt>https://</tt>.
     *
     * @param url  the URL to check.
     */
    public static boolean isSupportedProtocol(URL url) {
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
    public static boolean isLocalDomain(URL url) {
        return url.getHost().indexOf('.') < 0;
    }

}
