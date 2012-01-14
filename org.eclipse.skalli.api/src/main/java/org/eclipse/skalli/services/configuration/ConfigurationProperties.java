package org.eclipse.skalli.services.configuration;

import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.services.BundleFilter;
import org.eclipse.skalli.services.FilterMode;
import org.eclipse.skalli.services.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationProperties {

    public static final String PROPERTIES_RESOURCE = "skalli.properties";//$NON-NLS-1$

    public static final String PROPERTY_STORAGE_SERVICE = "skalli.storageService"; //$NON-NLS-1$
    public static final String PROPERTY_WORKDIR = "workdir"; //$NON-NLS-1$

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationProperties.class);

    public static String getProperty(String propertyName) {
        return getProperty(propertyName, null);
    }

    public static String getProperty(String propertyName, String defaultValue) {
        String propertyValue = System.getProperty(propertyName);
        if (propertyValue == null) {
            LOG.debug(MessageFormat.format("System property ''{0}'' is undefined", propertyName)); //$NON-NLS-1$
            propertyValue = getPropertyFromBundles(propertyName);
        }
        if (propertyValue == null && StringUtils.isNotBlank(defaultValue)) {
            LOG.debug(MessageFormat.format("Using default value ''{0}'' for property ''{0}''", defaultValue, propertyName)); //$NON-NLS-1$
            propertyValue = defaultValue;
        }
        return propertyValue;
    }

    private static String getPropertyFromBundles(String propertyName) {
        String propertyValue = null;
        Iterator<URL> resources = Services.findResources("/", PROPERTIES_RESOURCE, false, FilterMode.ALL, //$NON-NLS-1$
                new BundleFilter.AcceptAll()).iterator();
        while (propertyValue == null && resources.hasNext()) {
            propertyValue = getPropertyFromURL(propertyName, resources.next());
        }
        return propertyValue;
    }

    private static String getPropertyFromURL(String propertyName, URL resource) {
        String propertValue = null;
        InputStream in = null;
        try {
            in = resource.openStream();
            if (in != null) {
                Properties properties = new Properties();
                properties.load(in);
                propertValue = (String) properties.get(propertyName);
            }
        } catch (Exception e) {
            LOG.info(MessageFormat.format("Failed to retrieve property ''{0}'' from resource ''{1}''", //$NON-NLS-1$
                    propertyName, resource));
        } finally {
            IOUtils.closeQuietly(in);
        }
        return propertValue;
    }
}
