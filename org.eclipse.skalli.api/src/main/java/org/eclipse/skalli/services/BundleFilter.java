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
package org.eclipse.skalli.services;

import java.util.List;
import java.util.regex.Pattern;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

/**
 * Callback interface for filtering of OSGi bundles.
 *
 * @see Services#getBundles(BundleFilter...)
 */
public interface BundleFilter {

    /**
     * Tests if the specified bundle should be included in a list of
     * bundles.
     *
     * @param bundle the bundle to check.
     *
     * @return <code>true</code> if and only if the bundle should be
     *         included in a list of bundles; <code>false</code>
     *         otherwise.
     */
    public boolean accept(Bundle bundle);

    /**
     * Bundle filter that accepts any bundle.
     */
    public static class AcceptAll implements BundleFilter {
        @Override
        public boolean accept(Bundle bundle) {
            return true;
        }
    };

    /**
     * Bundle filter that accepts bundles matching a given symbolic name pattern.
     */
    public static class AcceptMatching implements BundleFilter {

        private final Pattern pattern;

        /**
         * Creates a filter for a given regular expression.
         * @param regexp  the regular expression to filter for.
         * @see Pattern
         */
        public AcceptMatching(String regexp) {
            pattern = Pattern.compile(regexp);
        }

        @Override
        public boolean accept(Bundle bundle) {
            return pattern.matcher(bundle.getSymbolicName()).matches();
        }
    }

    /**
     * Bundle filter that accepts bundles which provides at least one
     * implementation of a given OSGI service interface.
     */
    public static class AcceptService implements BundleFilter {

        private final List<ServiceReference<?>> serviceRefs;

        /**
         * Creates a filter for a given OSGi service interface.
         * @param serviceClass  the OSGi service interface to filter for.
         */
        public AcceptService(Class<?> serviceClass) {
            serviceRefs = Services.getServiceReferences(serviceClass);
        }

        @Override
        public boolean accept(Bundle bundle) {
            if (serviceRefs != null) {
                for (ServiceReference<?> serviceRef: serviceRefs) {
                    if (serviceRef.getBundle().equals(bundle)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

}
