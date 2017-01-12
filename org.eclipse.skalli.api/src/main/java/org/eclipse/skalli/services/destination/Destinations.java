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
package org.eclipse.skalli.services.destination;

import java.net.URL;
import java.text.MessageFormat;

import org.apache.http.client.HttpClient;
import org.eclipse.skalli.services.BundleProperties;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for creating instances of {@link HttpClient} for given URLs.
 */
public class Destinations {

    private static final String DEFAULT = "default"; //$NON-NLS-1$

    private static final String DESTINATION_SERVICE_TYPE = "destinationService.type"; //$NON-NLS-1$

    private static final Logger LOG = LoggerFactory.getLogger(Destinations.class);

    private static BundleContext context = FrameworkUtil.getBundle(Destinations.class).getBundleContext();

    // if set as bundle or system property, selects the active destination service; otherwise all registered
    // destination services will be tried; if no registered destination service can handle the given URL,
    // a client will be created with the default destination service
    private static String destinationService = BundleProperties.getProperty(BundleProperties.PROPERTY_DESTINATION_SERVICE);

    /**
     * Returns an HTTP client for the given URL.
     * <p>
     * The active {@link DestinationService} can be selected with the system or bundle property
     * <tt>"skalli.destinationService"</tt>. The value must match the <tt>"destinationService.type"</tt>
     * property of a registered destination service. If the specified destination service is available,
     * it is called to create a suitable client.
     * <p>
     * If this property is not set, this method asks all registered destination services for a client.
     * The first client returned will be the result of this method. If none of the registered destination
     * services can deliver a client, a default client will be returned.
     *
     * @return an Http client for the given URL, or <code>null</code> if no registered
     * destination service was capable or willing to handle that URL.
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
                String serviceType = (String)serviceRef.getProperty(DESTINATION_SERVICE_TYPE);
                if (DEFAULT.equals(serviceType)) {
                    defaultServiceRef = serviceRef;
                } else if (destinationService == null || destinationService.equalsIgnoreCase(serviceType)) {
                    client = getClient(serviceRef, url);
                }
            }
        }

        if (client == null && destinationService == null) {
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
