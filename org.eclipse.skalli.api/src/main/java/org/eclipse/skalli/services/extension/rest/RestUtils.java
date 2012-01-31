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
package org.eclipse.skalli.services.extension.rest;

import java.net.URL;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.services.BundleFilter;
import org.eclipse.skalli.services.FilterMode;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.extension.ExtensionServices;

public class RestUtils {

    private RestUtils() {
    }

    /**
     * Path prefix used by {@link #findSchemaResource(String)} to search for
     * schema definitions (<tt>*.xsd</tt> files) for REST API extensions in
     * registered bundles.
     */
    public static final String SCHEMA_RESOURCE_PATH = "/schemas"; //$NON-NLS-1$

    /** URL prefix for schema access. */
    public static final String URL_SCHEMAS = "/schemas/"; //$NON-NLS-1$

    /** URL prefixes for REST API access. */
    public static final String URL_API = "/api/"; //$NON-NLS-1$
    public static final String URL_PROJECTS = URL_API + "projects/"; //$NON-NLS-1$
    public static final String URL_USER = URL_API + "user/"; //$NON-NLS-1$

    public final static String URL_ISSUES = "/issues"; //$NON-NLS-1$
    public final static String URL_BROWSE = "/projects/"; //$NON-NLS-1$

    public static final String PARAM_EXTENSIONS = "extensions"; //$NON-NLS-1$
    public static final String PARAM_LIST_SEPARATOR = ","; //$NON-NLS-1$

    public static final String PARAM_ID = "id"; //$NON-NLS-1$
    public static final String PARAM_DEPTH = "depth"; //$NON-NLS-1$
    public static final String PARAM_QUERY = "query"; //$NON-NLS-1$
    public static final String PARAM_START = "start"; //$NON-NLS-1$
    public static final String PARAM_COUNT = "count"; //$NON-NLS-1$
    public static final String PARAM_USER = "user"; //$NON-NLS-1$
    public static final String PARAM_TAG = "tag"; //$NON-NLS-1$
    public static final String PARAM_PERSIST = "persist"; //$NON-NLS-1$
    public static final String PARAM_PROPERTY = "property"; //$NON-NLS-1$
    public static final String PARAM_PATTERN = "pattern"; //$NON-NLS-1$


    /**
     * Search a schema file with the given name, e.g. <tt>project.xsd</tt>,
     * first in all extension bundles, then in all Skalli bundles and finally
     * in all registered bundles.
     * <p>
     * Schema resources must be stored in the <tt>/schemas</tt> directory in
     * the root directory of a bundle.
     *
     * @param schemaLocation  the schema location as URL or the file name
     * of the schema resource to retrieve. Note, the method extracts only the last
     * part (i.e. the file name) of the given schema location.
     *
     * @return the URL of the schema resource, or <code>null</code> if no
     * matching schema resource exists.
     *
     * @see ExtensionServices#findExtensionResources(String, String, boolean)
     */
    public static URL findSchemaResource(String schemaLocation) {
        if (StringUtils.isBlank(schemaLocation)) {
            return null;
        }
        String resourceName = FilenameUtils.getName(schemaLocation);
        List<URL> urls = Services.findResources(SCHEMA_RESOURCE_PATH, resourceName, false,
                FilterMode.FIRST_MATCHING,
                new ExtensionServices.ExtensionBundleFilter(),
                new BundleFilter.AcceptMatching(Services.SKALLI_BUNDLE_PATTERN),
                new BundleFilter.AcceptAll());
        return urls.size() == 1 ? urls.get(0) : null;
    }
}
