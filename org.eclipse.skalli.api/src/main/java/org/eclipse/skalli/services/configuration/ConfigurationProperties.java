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

/**
 * Utility class to access system properties and properties defined in
 * <tt>"skalli.properties"</tt> files in installed bundles.
 */
public class ConfigurationProperties {

    /** Name of the property files to search for: <tt>{@value}</tt> */
    public static final String PROPERTIES_RESOURCE = "skalli.properties";//$NON-NLS-1$

    /** Name of the property specifying the active storage service: <tt>{@value}</tt> */
    public static final String PROPERTY_STORAGE_SERVICE = "skalli.storageService"; //$NON-NLS-1$

    /** Name of the property specifying the workig directory for file storage: <tt>{@value}</tt> */
    public static final String PROPERTY_WORKDIR = "workdir"; //$NON-NLS-1$

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationProperties.class);

    private static Properties propertyCache = getBundleProperties();

    /**
     * Returns the value of the specified property. This method is a shortcut for
     * {@link #getProperty(String, String) getProperty(propertyName, null)}.
     *
     * @param propertyName the name of the property.
     * @return  the value of ths property, or <code>null</code> if the specified property does not exist.
     */
    public static String getProperty(String propertyName) {
        return getProperty(propertyName, null);
    }

    /**
     * Returns the value of the specified property or a default value if the property does not exist.
     * <br>
     * This method returns the value of a system property with the given name, if it exists.
     * Otherwise, the property is searched in all installed bundles, i.e. in property files with
     * name <tt>"skalli.properties"</tt>, which must reside in the root directories of their
     * respective bundles. The first encounter of the property wins in case the property is
     * defined in several property files. Since the search order is in general unpredictable this
     * situation should be avoided.
     * <br>
     * Note: Properties from installed bundles are cached during startup, since scanning all installed
     * bundles for certain files is a time consuming operation. Installing additional bundles at
     * a later time or uninstalling bundles will not update the property cache.
     *
     * @param propertyName  the name of the property.
     * @param defaultValue  the value to return in case the property does not exist.
     * @return  the value of the property, or the given default value if the property does not exist.
     */
    public static String getProperty(String propertyName, String defaultValue) {
        String propertyValue = System.getProperty(propertyName);
        if (propertyValue == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format("system property ''{0}'' is undefined", propertyName)); //$NON-NLS-1$
            }
            propertyValue = propertyCache.getProperty(propertyName);
            if (propertyValue == null && StringUtils.isNotBlank(defaultValue)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format(
                            "using default value ''{0}'' for property ''{1}''", //$NON-NLS-1$
                            defaultValue, propertyName));
                }
                propertyValue = defaultValue;
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format("found property ''{0}'' with value ''{1}''", //$NON-NLS-1$
                    propertyName, propertyValue));
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
            LOG.info(MessageFormat.format("Failed to retrieve properties from resource ''{0}''", //$NON-NLS-1$
                    resource));
        } finally {
            IOUtils.closeQuietly(in);
        }
    }
}
