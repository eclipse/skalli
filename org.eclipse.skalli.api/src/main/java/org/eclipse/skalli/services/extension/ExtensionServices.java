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
package org.eclipse.skalli.services.extension;

import java.net.URL;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.services.BundleFilter;
import org.eclipse.skalli.services.FilterMode;
import org.eclipse.skalli.services.Services;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convenience class for accessing {@link ExtensionService extension services} currently
 * registered in the OSGi runtime. Extension services can be retrieved by their short names,
 * or by the {@link ExtensionEntityBase extension classes} they represent.
 * <p>
 * Note, this class tracks extension service by means of OSGi's declarative service
 * mechanisms and caches the currently registered services in internal maps.
 * Except {@link #findExtensionResources(String, String, boolean)} all methods
 * take their result from the internal caches.
 */
public class ExtensionServices {

    private static final Logger LOG = LoggerFactory.getLogger(ExtensionServices.class);

    private static Map<String, ExtensionService<?>> byShortName =
            new ConcurrentHashMap<String, ExtensionService<?>>();
    private static Map<String, ExtensionService<?>> byExtensionClassName =
            new ConcurrentHashMap<String, ExtensionService<?>>();

    protected void activate(ComponentContext context) {
        LOG.info(MessageFormat.format("[ExtensionServices] {0} : activated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void deactivate(ComponentContext context) {
        LOG.info(MessageFormat.format("[ExtensionServices] {0} : deactivated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void bindExtensionService(ExtensionService<?> extensionService) {
        String shortName = extensionService.getShortName();
        if (byShortName.containsKey(shortName)) {
            throw new RuntimeException(MessageFormat.format(
                    "There is already an extension registered with the short name ''{0}''", shortName));
        }
        byShortName.put(extensionService.getShortName(), extensionService);
        String extensionClassName = extensionService.getExtensionClass().getName();
        byExtensionClassName.put(extensionClassName, extensionService);
        LOG.info(MessageFormat.format("[ExtensionServices][registered {0}]", extensionClassName));

    }

    protected void unbindExtensionService(ExtensionService<?> extensionService) {
        String extensionClassName = extensionService.getExtensionClass().getName();
        byExtensionClassName.remove(extensionClassName);
        byShortName.remove(extensionService.getShortName());
        LOG.info(MessageFormat.format("[ExtensionServices][unregistered {0}]", extensionClassName));
    }

    /**
     * Returns all currently registered extension services.
     *
     * @return a collection of extension services, or an empty collection.
     */
    public static Collection<ExtensionService<?>> getAll() {
        return Collections.unmodifiableCollection(byShortName.values());
    }

    /**
     * Returns all currently registered extension services by their short names.
     *
     * @return a map of extension services with short names as keys,
     * or an empty map.
     */
    public static Map<String, ExtensionService<?>> getByShortNames() {
        return Collections.unmodifiableMap(byShortName);
    }

    /**
     * Returns the {@link ExtensionService} instance matching a given short name.
     *
     * @param shortName  the {@link {@link ExtensionService#getShortName() short name}.
     * @return  the extension service instance, or <code>null</code> if there is no instance
     * for the given short name available.
     */
    public static  ExtensionService<?> getByShortName(String shortName) {
        ExtensionService<?> extensionService = byShortName.get(shortName);
        return extensionService != null? extensionService : null;
    }

    /**
     * Returns all currently registered extension services by the names
     * of the {@link ExtensionEntityBase extension classes} with which they
     * are associated.
     *
     * @return a map of extension services with (fully qualified) names
     * of extension classes as keys, or an empty map.
     */
    public static Map<String, ExtensionService<?>> getByExtensionClassNames() {
        return Collections.unmodifiableMap(byExtensionClassName);
    }

    /**
     * Returns the {@link ExtensionService} instance matching a given
     * {@link ExtensionEntityBase extension class} name.
     *
     * @param extensionClass  the extension class.
     * @return  the extension service instance, or <code>null</code> if there is no instance
     * for the given extension class available.
     */
    @SuppressWarnings("unchecked")
    public static <T extends ExtensionEntityBase> ExtensionService<T> getByExtensionClassName(String extensionClassName) {
        ExtensionService<?> extensionService = byExtensionClassName.get(extensionClassName);
        return extensionService != null? (ExtensionService<T>)extensionService : null;
    }

    /**
     * Returns the {@link ExtensionService} instance matching a given
     * {@link ExtensionEntityBase extension class}.
     *
     * @param extensionClass  the extension class.
     * @return  the extension service instance, or <code>null</code> if there is no instance
     * for the given extension class available.
     */
    public static <T extends ExtensionEntityBase> ExtensionService<T> getByExtensionClass(Class<T> extensionClass) {
        return getByExtensionClassName(extensionClass.getName());
    }

    /**
     * Scans all bundles providing an {@link ExtensionService} for resources
     * matching the given <code>path</code> and <code>pattern</code>.
     *
     * @param path  The path name in which to look. The path is always relative
     *        to the root of this bundle and may begin with &quot;/&quot;. A
     *        path value of &quot;/&quot; indicates the root of this bundle.
     * @param pattern  The file name pattern for selecting entries in the
     *        specified path. The pattern is only matched against the last
     *        element of the entry path. If the entry is a directory then the
     *        trailing &quot;/&quot; is not used for pattern matching. Substring
     *        matching is supported, as specified in the Filter specification,
     *        using the wildcard character (&quot;*&quot;). If null is
     *        specified, this is equivalent to &quot;*&quot; and matches all
     *        files.
     * @param recursive  If {@code true}, recurse into subdirectories. Otherwise
     *        only return entries from the specified path.
     * @return a list of matching resources (which may be empty).
     *
     * @see Services#findResources(String, String, boolean, FilterMode, BundleFilter...)
     * @see FilterMode#SHORT_CIRCUIT
     */
    public static List<URL> findExtensionResources(String path, String pattern, boolean recursive) {
        return Services.findResources(path, pattern, recursive, FilterMode.ALL, new ExtensionBundleFilter());
    }

    /**
     * Filter that accepts all bundles providing {@link ExtensionService extension services}.
     * @see Services#getBundles(BundleFilter...)
     */
    public static class ExtensionBundleFilter extends BundleFilter.AcceptService {
        public ExtensionBundleFilter() {
            super(ExtensionService.class);
        }
    }
}
