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
package org.eclipse.skalli.testutil;

import java.util.Dictionary;
import java.util.Set;
import java.util.SortedSet;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.services.BundleFilter;
import org.eclipse.skalli.services.FilterMode;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.extension.ExtensionService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;

/**
 * Utility class to start bundles for plugin tests.
 *
 */
public class BundleManager {

    /**
     * Starts bundles with a symbolic name matching <tt>org.eclipse.skalli.*</tt>
     * and all bundles providing an {@link ExtensionService}.
     *
     * @param c  the class for which to start a bundle
     * @throws BundleException  if starting the bundles failed.
     */
    public static void startBundles() throws BundleException {
        SortedSet<Bundle> bundles = Services.getBundles(FilterMode.ALL,
                new BundleFilter.AcceptService(ExtensionService.class),
                new BundleFilter.AcceptMatching(Services.SKALLI_BUNDLE_PATTERN));
        startBundles(bundles);
    }

    /**
     * Starts the bundle containing the given class, as well as all
     * bundles with a symbolic name matching <tt>org.eclipse.skalli.*</tt>
     * and all bundles providing an {@link ExtensionService}.
     *
     * @param c  the class for which to start a bundle
     * @throws BundleException  if starting the bundles failed.
     */
    public static void startBundles(Class<?> c) throws BundleException {
        Bundle bundle = FrameworkUtil.getBundle(c);
        startBundle(bundle);
        startBundles();
    }

    /**
     * Starts a given set of bundles with {@link #startBundle(Bundle)}.
     *
     * @param bundles  the bundles to start.
     * @throws BundleException  if starting the bundles failed.
     */
    public static void startBundles(SortedSet<Bundle> bundles) throws BundleException {
        for (Bundle bundle : bundles) {
            startBundle(bundle);
        }
    }

    /**
     * Starts the given bundle unless its symbolic name ends with <tt>.test</tt>,
     * it is a fragment or it already is started.
     *
     * @param bundle  the bundle to start.
     * @throws BundleException  if the bundle could not be started.
     */
    public static void startBundle(Bundle bundle) throws BundleException {
        if (!bundle.getSymbolicName().endsWith(".test") //$NON-NLS-1$
                 && !isFragment(bundle)
                 && bundle.getState() != Bundle.ACTIVE) {
             bundle.start();
         }
    }

    /**
     * Checks if the given bundle is a fragment.
     * @param bundle  the bundle to check.
     * @return <code>true</code> if the bundle has a <tt>Fragment-Host</tt> header
     * in its manifest, <code>false</code> otherwise.
     */
    public static boolean isFragment(Bundle bundle) {
        Dictionary<String,String> headers = bundle.getHeaders();
        return StringUtils.isNotBlank(headers.get("Fragment-Host")); //$NON-NLS-1$
    }

    /**
     * Returns an instance of a given service class. Starts all bundles providing
     * the given service interface, as well as all bundles with a symbolic name
     * matching <tt>org.eclipse.skalli.*</tt> and all bundles providing
     * an {@link ExtensionService}.
     *
     * @param serviceClass  the service to retrieve.
     * @throws BundleException  if starting the bundles failed.
     * @throws IllegalStateException
     *           if there is none or more than one instance of the service
     *           registered.
     */
    public static <T> T getRequiredService(Class<T> serviceClass) throws BundleException {
        startBundles(serviceClass);
        return Services.getRequiredService(serviceClass);
    }

    /**
     * Waits for a dedicated service implementation to appear.
     * @param serviceClass  the service to wait for.
     * @param implementationClass  the expected service implementation.
     * @param timeout  the maximum time to wait in milliseconds.
     * @return the service instance, or <code>null</code>.
     *
     * @throws BundleException if starting the bundles failed.
     * @throws InterruptedException  if the waiting has been interrupted.
     */
    public static <S,T> S waitService(Class<S> serviceClass, Class<T> implementationClass, long timeout)
            throws BundleException, InterruptedException {
        startBundles(serviceClass);
        int waited = 0;
        S result = null;
        while (result == null && waited < timeout) {
            Set<S> services = Services.getServices(serviceClass);
            for (S service : services) {
                if (implementationClass == null || implementationClass.getName().equals(service.getClass().getName())) {
                    return service;
                }
            }
            Thread.sleep(100);
            waited += 100;
        }
        return result;
    }

    /**
     * Registers a service for testing purposes.
     *
     * @param <S> type of the service.
     * @param serviceClass  the service interface class under which the service instance should be registered.
     * @param serviceInstance  the actual service instance.
     * @param properties optional properties for this service, or <code>null</code>.
     *
     * @param the <code>ServiceRegistration</code> which should be used to unregister the test service
     * after the test finished. Make sure to unregister the service properly (finally block!), otherwise
     * other tests might get affected by a messy service registry.
     *
     * @throws BundleException  if starting the bundles failed.
     */
    public static <S> ServiceRegistration<S> registerService(Class<S> serviceClass, S serviceInstance,
            Dictionary<String,?> properties) throws BundleException {
        startBundles(serviceClass);
        Bundle bundle = FrameworkUtil.getBundle(serviceClass);
        return bundle.getBundleContext().registerService(serviceClass, serviceInstance, properties);
    }
}
