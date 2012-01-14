package org.eclipse.skalli.services.extension;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.services.BundleFilter;
import org.eclipse.skalli.services.FilterMode;
import org.eclipse.skalli.services.Services;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

public class ExtensionServices {
    /**
     * Returns the {@link ExtensionService} instance matching a given model extension class.
     *
     * @param extensionClass  the model extension class.
     * @return  the extension service instance, or <code>null</code> if there is no instance
     * for the given model extension class available.
     */
    @SuppressWarnings("unchecked")
    public static <T extends ExtensionEntityBase> ExtensionService<T> getExtensionService(Class<T> extensionClass) {
        BundleContext context = FrameworkUtil.getBundle(ExtensionServices.class).getBundleContext();
        String extensionClassName = extensionClass.getName();
        List<ExtensionService<T>> extensionServices = new ArrayList<ExtensionService<T>>();
        List<ServiceReference<?>> serviceRefs = Services.getServiceReferences(ExtensionService.class);
        if (serviceRefs != null) {
            for (ServiceReference<?> serviceRef : serviceRefs) {
                ExtensionService<?> serviceInstance = (ExtensionService<?>) context.getService(serviceRef);
                if (serviceInstance != null && extensionClassName.equals(serviceInstance.getExtensionClass().getName())) {
                    extensionServices.add((ExtensionService<T>) serviceInstance);
                }
            }
        }
        if (extensionServices.size() > 1) {
            throw new IllegalStateException("More than one extension service registered for model extension "
                    + extensionClassName);
        }
        return extensionServices.isEmpty() ? null : extensionServices.get(0);
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
