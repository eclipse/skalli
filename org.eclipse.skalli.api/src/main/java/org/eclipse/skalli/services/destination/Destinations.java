package org.eclipse.skalli.services.destination;

import java.net.URL;
import java.text.MessageFormat;

import org.apache.http.client.HttpClient;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Destinations {

    private static final String DEFAULT = "default"; //$NON-NLS-1$

    private static final String DESTINATION_SERVICE_TYPE = "destinationService.type"; //$NON-NLS-1$

    private static final Logger LOG = LoggerFactory.getLogger(Destinations.class);

    private static BundleContext context = FrameworkUtil.getBundle(Destinations.class).getBundleContext();

    /**
     * @return null or a HttpClient to read from the parameter <code>url</code>.
     */
    public static HttpClient getClient(URL url) {
        HttpClient client = null;
        ServiceReference<?> defaultServiceRef = null;

        ServiceReference<?>[] serviceRefs = null;
        try {
            serviceRefs = context.getAllServiceReferences(DestinationService.class.getName(), null);
        } catch (InvalidSyntaxException e) {
            LOG.error("No destination service registered", e);
        }

        if (serviceRefs != null) {
            for (int i = 0; client == null && i < serviceRefs.length; ++i) {
                ServiceReference<?> serviceRef = serviceRefs[i];

                if (!DEFAULT.equals(serviceRef.getProperty(DESTINATION_SERVICE_TYPE))) {
                    client = getClient(serviceRef, url);

                } else {
                    defaultServiceRef = serviceRef;
                }
            }
        }

        if (client == null) {
            if (defaultServiceRef != null) {
                client = getClient(defaultServiceRef, url);
                if (client == null) {
                    LOG.info(MessageFormat.format("Default destination service cannot handle {0}", url.toExternalForm()));
                }

            } else {
                LOG.error("No default destination service registered");

            }
        }
        return client;
    }

    private static HttpClient getClient(ServiceReference<?> serviceRef, URL url) {
        HttpClient result = null;
        Object serviceObj = context.getService(serviceRef);
        DestinationService destinationService = DestinationService.class.cast(serviceObj);
        if (destinationService.isSupportedProtocol(url)) {
            result = destinationService.getClient(url);
        }
        return result;
    }
}
