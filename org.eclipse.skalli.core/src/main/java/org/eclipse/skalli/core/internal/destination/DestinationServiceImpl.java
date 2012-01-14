package org.eclipse.skalli.core.internal.destination;

import java.net.URL;
import java.text.MessageFormat;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.services.configuration.ConfigurationService;
import org.eclipse.skalli.services.destination.ConfigKeyProxy;
import org.eclipse.skalli.services.destination.DestinationService;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DestinationServiceImpl implements DestinationService {

    private static final Logger LOG = LoggerFactory.getLogger(DestinationServiceImpl.class);

    // HTTP protocol stuff
    private static final String PROTOCOL_SEPARATOR = "://"; //$NON-NLS-1$
    private static final String PROTOCOL_HTTPS = HTTPS + PROTOCOL_SEPARATOR;
    private static final String PROTOCOL_HTTP = HTTP + PROTOCOL_SEPARATOR;

    // RegExp for non proxy hosts
    private static final String[] RE_SEARCH = new String[] { ";", "*", "." }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    private static final String[] RE_REPLACE = new String[] { "|", "(\\w|\\.|\\-)*", "\\." }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    // general timeout for connection requests
    private static final int CONNECT_TIMEOUT = 10000;
    private static final int READ_TIMEOUT = 300000;

    private ConfigurationService configService;

    protected void activate(ComponentContext context) {
        LOG.info(MessageFormat.format("[DestinationService] {0} : activated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
        Protocol.registerProtocol(HTTPS, new Protocol(HTTPS, new AlwaysTrustSSLProtocolSocketFactory(), 443));
    }

    protected void deactivate(ComponentContext context) {
        LOG.info(MessageFormat.format("[DestinationService] {0} : deactivated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
        Protocol.unregisterProtocol(HTTPS);
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

    private void setProxy(HttpClient client, URL url) {
        String proxyHost = null;
        int proxyPort = -1;
        String nonProxyHostsPattern = StringUtils.EMPTY;
        if (configService != null) {
            String host = configService.readString(ConfigKeyProxy.HOST);
            String port = configService.readString(ConfigKeyProxy.PORT);
            String nonProxyHosts = configService.readString(ConfigKeyProxy.NONPROXYHOSTS);
            if (StringUtils.isNotBlank(host) && StringUtils.isNotBlank(port) && StringUtils.isNumeric(port)) {
                proxyHost = host;
                proxyPort = Integer.valueOf(port);
                if (StringUtils.isNotBlank(nonProxyHosts)) {
                    nonProxyHostsPattern = StringUtils.replaceEach(StringUtils.deleteWhitespace(nonProxyHosts),
                            RE_SEARCH, RE_REPLACE);
                } else {
                    nonProxyHostsPattern = StringUtils.EMPTY;
                }
            }
        }
        if (StringUtils.isNotBlank(proxyHost)
                && proxyPort >= 0
                && !Pattern.matches(nonProxyHostsPattern, url.getHost())) {
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
