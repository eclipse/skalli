/*******************************************************************************
 * Copyright (c) 2010-2014 SAP AG and others.
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

/**
 * Utilities and constants for REST extensions.
 */
public class RestUtils {

    private RestUtils() {
    }

    /**
     * Default namespace (<tt>{@value}</tt>) reserved for the Skalli core model. Should be used as prefix
     * for the namespaces of REST extensions, e.g. <tt>"http://www.eclipse.org/skalli/2010/API/Extension-DevInf"</tt>.
     */
    public static final String API_NAMESPACE = "http://www.eclipse.org/skalli/2010/API"; //$NON-NLS-1$

    /**
     * Path prefix (<tt>{@value}</tt>) used by {@link #findSchemaResource(String)} to search for
     * schema definitions (<tt>*.xsd</tt> files) for REST API extensions in
     * registered bundles.
     */
    public static final String SCHEMA_RESOURCE_PATH = "/schemas"; //$NON-NLS-1$

    /** URL prefix for schema access (<tt>{@value}</tt>) */
    public static final String URL_SCHEMAS = "/schemas/"; //$NON-NLS-1$

    /** URL prefix of the REST API (<tt>{@value}</tt>) */
    public static final String URL_API = "/api/"; //$NON-NLS-1$

    /** URL prefix for the collection of projects (<tt>{@value}</tt>) */
    public static final String URL_PROJECTS = URL_API + "projects/"; //$NON-NLS-1$

    /** URL prefix for the collection of users (<tt>{@value}</tt>) */
    public static final String URL_USER = URL_API + "user/"; //$NON-NLS-1$
    public static final String URL_USERS = URL_API + "users/"; //$NON-NLS-1$

    /** URL suffix for the collection of issues of a project (<tt>{@value}</tt>) */
    public final static String URL_ISSUES = "/issues"; //$NON-NLS-1$

    /** URL prefix for the browseable collection of projects (<tt>{@value}</tt>) */
    public final static String URL_BROWSE = "/projects/"; //$NON-NLS-1$

    /** URL prefix for the browseable collection of subprojects (<tt>{@value}</tt>) */
    public final static String URL_SUBPROJECTS = "/subprojects"; //$NON-NLS-1$

    /** Query parameter  <tt>{@value}</tt>) */
    public static final String PARAM_ID = "id"; //$NON-NLS-1$

    /**
     * Searches a schema file with the given name, e.g. <tt>project.xsd</tt>,
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
