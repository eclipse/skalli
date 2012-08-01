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
package org.eclipse.skalli.view.internal;

import java.net.URL;
import java.util.List;

import org.eclipse.skalli.services.BundleFilter;
import org.eclipse.skalli.services.FilterMode;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.extension.ExtensionService;
import org.eclipse.skalli.view.ext.InfoBox;
import org.osgi.framework.FrameworkUtil;

public class ViewBundleUtil {

    private static final String BUNDLE_VAADIN = "com.vaadin"; //$NON-NLS-1$

    private ViewBundleUtil() {
    }

    /**
     * Scans the Vaadin bundle, the o.e.s.view bundle (including its fragments)
     * all extensions providing a {@link InfoBox}, all extensions providing
     * a {@link ExtensionServic} and finally all bundles for theme resources
     * matching the given <code>path</code> and <code>pattern</code>.
     *
     * @param path  the path name in which to look.
     * @param pattern the  pattern for selecting entries in the
     *        specified path.
     * @param recursive  if <code>true</code>, recurse into subdirectories.
     * @param mode  determines whether the method should collect all resources from
     *              all bundles, or whether it should stop when at least one matching
     *              resource has been found.
     * @see Services#findResources(String, String, boolean, FilterMode, BundleFilter...)
     */
    public static List<URL> findThemeResources(String path, String pattern, boolean recursive, FilterMode mode) {
        if (FilterMode.ALL.equals(mode)) {
            return Services.findResources(path, pattern, recursive, mode,  new BundleFilter.AcceptAll());
        }
        return Services.findResources(path, pattern, recursive, mode,
                // try Vaadin bundle
                new BundleFilter.AcceptMatching(BUNDLE_VAADIN),
                // try the o.e.s.view bundle
                new BundleFilter.AcceptMatching(FrameworkUtil.getBundle(ViewBundleUtil.class).getLocation()),
                // try view extension bundle
                new BundleFilter.AcceptService(InfoBox.class),
                // try extension bundles
                new BundleFilter.AcceptService(ExtensionService.class),
                // and finally all bundles
                new BundleFilter.AcceptAll()
         );
    }
}
