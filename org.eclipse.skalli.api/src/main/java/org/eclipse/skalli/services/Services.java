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
package org.eclipse.skalli.services;

import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.skalli.commons.URLUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * Utiliy class that provides various methods to retrieve,
 * filter and iterate OSGi service instances.
 */
public class Services {

    /** Regular expression to search for Skalli bundles, see {@link #getBundlesMatching(String)}. */
    public static final String SKALLI_BUNDLE_PATTERN = "org\\.eclipse\\.skalli\\..*";  //$NON-NLS-1$

    // no instances, please!
    private Services() {
    }

    /**
     * Returns the currently registered instance of the given OSGi
     * <code>service</code> interface, if any.
     *
     * @param <T>
     *          type parameter representing an OSGi service interface.
     * @param service
     *          the OSGi service interface for which an instance is to be
     *          returned.
     * @return the registered instance of <code>service</code>, or
     *         <code>null</code>.
     * @throws IllegalStateException
     *           if there is more than one instance of the service registered.
     */
    public static <T> T getService(Class<T> service) {
        return getService(service, false, null);
    }

    /**
     * Returns the currently registered instance of the given OSGi
     * <code>service</code> interface, if any.
     *
     * @param <T>
     *          type parameter representing an OSGi service interface.
     * @param service
     *          the OSGi service interface for which an instance is to be
     *          returned.
     * @param filter
     *          the OSGi {@link org.osgi.framework.Filter filter} to use when
     *          looking up the service. It must contain the <code>objectClass</code> as well.
     * @return the registered instance of <code>service</code>, or
     *         <code>null</code>.
     * @throws IllegalStateException
     *           if there is more than one instance of the service registered.
     */
    public static <T> T getService(Class<T> service, String filter) {
        return getService(service, false, filter);
    }

    /**
     * Returns the currently registered instance of the given OSGi
     * <code>service</code> interface.
     *
     * @param <T>
     *          type parameter representing an OSGi service interface.
     * @param service
     *          the OSGi service interface for which an instance is to be
     *          returned.
     * @return the registered instance of <code>service</code>.
     * @throws IllegalStateException
     *           if there is none or more than one instance of the service
     *           registered.
     */
    public static <T> T getRequiredService(Class<T> service) {
        return getService(service, true, null);
    }

    /**
     * Returns all instances of a given OSGi <code>service</code>.
     *
     * @param <T>
     *          type parameter representing an OSGi service interface.
     * @param service
     *          the OSGi service interface for which instances are to be returned.
     * @return a set of service instances.
     */
    public static <T> Set<T> getServices(Class<T> service) {
        return getServices(service, null, null);
    }

    /**
     * Returns all instances of a given OSGi <code>service</code>.
     *
     * @param <T>
     *          type parameter representing an OSGi service interface.
     * @param service
     *          the OSGi service interface for which instances are to be returned.
     * @param comparator the comparator that will be used to order this set.
     *        If {@code null}, the {@linkplain Comparable natural
     *        ordering} of the elements will be used.
     * @return a set of service instances.
     */
    public static <T> Set<T> getServices(Class<T> service, Comparator<? super T> comparator) {
        return getServices(service, null, comparator);
    }

    /**
     * Returns instances of a given OSGi <code>service</code> matching
     * a certain filter condition.
     *
     * @param <T>
     *          type parameter representing an OSGi service interface.
     * @param service
     *          the OSGi service interface for which instances are to be returned.
     * @param filter
     *          a filter that determines which instances of the service to
     *          accept, or <code>null</code>. In no filter is specified, all
     *          instances of the service are returned.
     * @return a set of matching service instances.
     */
    public static <T> Set<T> getServices(Class<T> service, ServiceFilter<T> filter) {
        return getServices(service, filter, null);
    }

    /**
     * Returns instances of a given OSGi <code>service</code> matching
     * a certain filter condition.
     *
     * @param <T>
     *          type parameter representing an OSGi service interface.
     * @param service
     *          the OSGi service interface for which instances are to be returned.
     * @param filter
     *          a filter that determines which instances of the service to
     *          accept, or <code>null</code>. In no filter is specified, all
     *          instances of the service are returned.
     * @param comparator the comparator that will be used to order this set.
     *        If {@code null}, the {@linkplain Comparable natural
     *        ordering} of the elements will be used.
     * @return a set of matching service instances.
     */
    public static <T> Set<T> getServices(Class<T> service, ServiceFilter<T> filter,
            Comparator<? super T> comparator) {
        if (comparator == null) {
            comparator = new Comparator<T>() {
                @Override
                public int compare(T o1, T o2) {
                    return o1.getClass().getName().compareTo(o2.getClass().getName());
                }
            };
        }
        BundleContext context = getBundleContext();
        Set<T> serviceInstances = new TreeSet<T>(comparator);
        List<ServiceReference<?>> serviceRefs = getServiceReferences(service);
        if (serviceRefs != null) {
            for (ServiceReference<?> serviceRef : serviceRefs) {
                T serviceInstance = service.cast(context.getService(serviceRef));
                if (serviceInstance != null && (filter == null || filter.accept(serviceInstance))) {
                    serviceInstances.add(serviceInstance);
                }
            }
        }
        return serviceInstances;
    }

    /**
     * Returns references to all implementations of a given OSGi <code>service</code>.
     *
     * @param <T>
     *          type parameter representing an OSGi service interface.
     * @param service
     *          the OSGi service interface for which instances are to be returned.
     * @return a list of service references, or an empty list.
     */
    public static <T> List<ServiceReference<?>> getServiceReferences(Class<T> service) {
        return getServiceReferences(service, null);
    }

    /**
     * Returns references to all implementations of a given OSGi <code>service</code> matching
     * the given filter expression;
     *
     * @param <T>
     *          type parameter representing an OSGi service interface.
     * @param service
     *          the OSGi service interface for which instances are to be returned.
     * @param filter
     *          the OSGi {@link org.osgi.framework.Filter filter} to use when
     *          looking up the service. It must contain the <code>objectClass</code> as well.
     * @return a list of service references, or an empty list.
     */
    public static <T> List<ServiceReference<?>> getServiceReferences(Class<T> service, String filter) {
        List<ServiceReference<?>> ret = new ArrayList<ServiceReference<?>>();
        try {
            BundleContext context = getBundleContext();
            ServiceReference<?>[] serviceRefs = context.getAllServiceReferences(service.getName(), filter);
            if (serviceRefs != null) {
                for (ServiceReference<?> serviceRef: serviceRefs) {
                    ret.add(serviceRef);
                }
            }
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException(e);
        }
        return ret;
    }

    /**
     * Returns an iterator for all implementations of the given OSGi <code>service</code>.
     *
     * @param <T> type parameter representing an OSGi service interface.
     * @param service  the OSGi service interface for which implementations are
     * to be returned.
     */
    public static <T> Iterator<T> getServiceIterator(Class<T> service) {
        BundleContext context = getBundleContext();
        try {
            ServiceReference<?>[] serviceRefs = context.getAllServiceReferences(service.getName(), null);
            if (serviceRefs != null) {
                return new ServiceIterator<T>(service, serviceRefs, context);
            }
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException(e);
        }
        return new ServiceIterator<T>(service, null, context);
    }

    /**
     * Returns all registered bundles.
     *
     * @return a set of bundles, or an empty set.  The bundles in the result
     * are sorted according to their symbolic names.
     */
    public static SortedSet<Bundle> getBundles() {
        SortedSet<Bundle> bundles = createSortedBundleSet();
        for (Bundle bundle: getBundleContext().getBundles()) {
            bundles.add(bundle);
        }
        return bundles;
    }

    /**
     * Returns all registered bundles matching given filters.
     *
     * @param mode  determines whether the method should collect all bundles
     *              matching any of the given filters ({@link FilterMode#ALL}), or whether it should stop
     *              when the first filter found matching bundles ({@link FilterMode#SHORT_CIRCUIT})
     *              or when any matching bundle is found ({@link FilterMode#FIRST_MATCHING}).
     * @param filters  a collection of bundle filters. If no filters are specified
     *        {@link BundleFilter#AcceptAll() all available bundles} are returned.
     */
    public static SortedSet<Bundle> getBundles(FilterMode mode, BundleFilter... filters) {
        SortedSet<Bundle> allBundles = getBundles();
        if (filters == null || filters.length == 0) {
            return allBundles;
        }
        SortedSet<Bundle> bundles = createSortedBundleSet();
        for (BundleFilter filter: filters) {
            if (FilterMode.SHORT_CIRCUIT.equals(mode) && bundles.size() > 0) {
                return bundles;
            }
            for (Bundle bundle: allBundles) {
                if (filter.accept(bundle)) {
                    bundles.add(bundle);
                    if (FilterMode.FIRST_MATCHING.equals(mode)) {
                        return bundles;
                    }
                }
            }
        }
        return bundles;
    }

    /**
     * Scans bundles for resources matching the given <code>path</code>
     * and <code>pattern</code>. Searches bundles based on a given list
     * of bundle filters and in the order defined by their symbolic names.
     * <p>
     * In order to search specifically in extension bundles, i.e. bundles providing an
     * {@link org.eclipse.skalli.services.extension.ExtensionService}), you may use the convenience method
     * {@link org.eclipse.skalli.services.extension.ExtensionServices#findExtensionResources(String,String,boolean)}.
     *
     * @param path  The path name in which to look. The path is always relative
     *        to the root of a bundle and may begin with &quot;/&quot;. A
     *        path value of &quot;/&quot; indicates the root of a bundle.
     * @param filePattern  The file name pattern for selecting entries in the
     *        specified path. The pattern is only matched against the last
     *        element of the entry path. If the entry is a directory then the
     *        trailing &quot;/&quot; is not used for pattern matching. Substring
     *        matching is supported, as specified in the Filter specification,
     *        using the wildcard character (&quot;*&quot;). If null is
     *        specified, this is equivalent to &quot;*&quot; and matches all
     *        files.
     * @param recurse  If {@code true}, recurse into subdirectories. Otherwise
     *        only return entries from the specified path.
     * @param mode  determines whether the method should collect all resources from
     *              all accepted bundles ({@link FilterMode#ALL}),
     *              or whether it should stop when
     *              the first filter yielded a non-empty amount of matching
     *              resources ({@link FilterMode#SHORT_CIRCUIT}),
     *              or when the first matching resource is found
     *              ({@link FilterMode#FIRST_MATCHING}).
     * @param filters  a collection of filters determining the bundles to search
     *        for matching resources. If no filters are specified
     *        {@link BundleFilter#AcceptAll() all available bundles} are searched.
     * @return a list of matching resources, or an empty list.
     *
     * @see Bundle#findEntries(String, String, boolean)
     */
    public static List<URL> findResources(String path, String pattern, boolean recursive, FilterMode mode,
            BundleFilter... filters) {
        if (filters == null || filters.length == 0) {
            filters = new BundleFilter[]{ new BundleFilter.AcceptAll() };
        }
        List<URL> resources = new ArrayList<URL>();
        SortedSet<Bundle> bundles = getBundles();
        for (BundleFilter filter: filters) {
            if (FilterMode.SHORT_CIRCUIT.equals(mode) && resources.size() > 0) {
                return resources;
            }
            for (Bundle bundle: bundles) {
                if (filter.accept(bundle)) {
                    Enumeration<URL> urls = bundle.findEntries(path, pattern, recursive);
                    if (urls != null) {
                        resources.addAll(URLUtils.asURLs(urls));
                        if (FilterMode.FIRST_MATCHING.equals(mode)) {
                            return resources;
                        }
                    }
                }
            }
        }
        return resources;
    }

    private static final BundleContext getBundleContext() {
        return FrameworkUtil.getBundle(Services.class).getBundleContext();
    }

    private static SortedSet<Bundle> createSortedBundleSet() {
        return new TreeSet<Bundle>(new Comparator<Bundle>() {
            @Override
            public int compare(Bundle o1, Bundle o2) {
                return o1.getSymbolicName().compareTo(o2.getSymbolicName());
            }
        });
    }

    private static <T> T getService(Class<T> serviceClass, boolean required, String filter) {
        T serviceInstance = null;
        try {
            BundleContext context = getBundleContext();
            ServiceReference<?>[] serviceRefs = context.getAllServiceReferences(serviceClass.getName(), filter);
            if (serviceRefs != null) {
                if (serviceRefs.length > 1) {
                    throw new IllegalStateException(MessageFormat.format(
                            "Multiple implementations for service {0} registered",
                            serviceClass.getName()));
                }
                serviceInstance = serviceClass.cast(context.getService(serviceRefs[0]));
            }
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException(e);
        }
        if (required && serviceInstance == null) {
            throw new IllegalStateException(MessageFormat.format(
                    "No implementation for required service {0} registered",
                    serviceClass.getName()));
        }
        return serviceInstance;
    }

    private static boolean matchesAnyOf(Bundle bundle, BundleFilter... filters) {
        if (filters == null || filters.length == 0) {
            return true;
        }
        for (BundleFilter filter: filters) {
            if (filter.accept(bundle)) {
                return true;
            }
        }
        return false;
    }

    private static class ServiceIterator<T> implements Iterator<T> {
        private int i = 0;
        private final Class<T> serviceClass;
        private final BundleContext context;
        private final ServiceReference<?>[] serviceRefs;

        public ServiceIterator(Class<T> serviceClass, ServiceReference<?>[] serviceRefs, BundleContext context) {
            this.serviceClass = serviceClass;
            this.context = context;
            this.serviceRefs = serviceRefs;
        }

        @Override
        public boolean hasNext() {
            return serviceRefs != null ? i < serviceRefs.length : false;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException("Iteration has no more elements");
            }
            return serviceClass.cast(context.getService(serviceRefs[i++]));
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Remove operation is not supported by this Iterator");
        }
    }

}
