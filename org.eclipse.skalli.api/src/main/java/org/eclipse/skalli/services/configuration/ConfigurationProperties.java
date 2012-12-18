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

    private static Properties propertyCache = getBundleProperties();

    public static String getProperty(String propertyName) {
        return getProperty(propertyName, null);
    }

    public static String getProperty(String propertyName, String defaultValue) {
        String propertyValue = System.getProperty(propertyName);
        if (propertyValue == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format("System property ''{0}'' is undefined", propertyName)); //$NON-NLS-1$
            }
            propertyValue = propertyCache.getProperty(propertyName);
        }
        if (propertyValue == null && StringUtils.isNotBlank(defaultValue)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format("Using default value ''{0}'' for property ''{0}''", //$NON-NLS-1$
                        defaultValue, propertyName));
            }
            propertyValue = defaultValue;
        }
        return propertyValue;
    }

    private static Properties getBundleProperties() {
        Properties bundleProperties = new Properties();
        Iterator<URL> resources = Services.findResources("/", PROPERTIES_RESOURCE, false, FilterMode.ALL, //$NON-NLS-1$
                new BundleFilter.AcceptAll()).iterator();
        while (resources.hasNext()) {
            addBundleProperties(resources.next(), bundleProperties);
        }
        return bundleProperties;
    }

    private static void addBundleProperties(URL resource, Properties bundleProperties) {
        InputStream in = null;
        try {
            in = resource.openStream();
            if (in != null) {
                Properties properties = new Properties();
                properties.load(in);
                bundleProperties.putAll(properties);
            }
        } catch (Exception e) {
            LOG.info(MessageFormat.format("Failed to retrieve properties from resource ''{1}''", //$NON-NLS-1$
                    resource));
        } finally {
            IOUtils.closeQuietly(in);
        }
    }
}
