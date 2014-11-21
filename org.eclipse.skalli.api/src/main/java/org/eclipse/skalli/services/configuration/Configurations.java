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
package org.eclipse.skalli.services.configuration;

import java.text.MessageFormat;

import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Convenience class for accessing configuration settings.
 *
 * This helper class should be used in code that is not an OSGI service, or where a direct binding
 * to the configuration service may not be possible (e.g. because of cyclic dependencies) or not
 * desirable. For example, infoboxes, validators and REST converters should prefer
 * <pre>
 *   ConfigClass config = Configurations.getConfiguration(ConfigClass.class);
 * </pre>
 * rather than the sequence
 * <pre>
 *   ConfigurationService configService = Services.getService(ConfigurationService.class);
 *   if (configService != null) {
 *       ConfigClass config = configService.readConfiguration(ConfigClass.class);
 *       ...
 *   }
 * </pre>
 * because the service lookup in the OSGI service registry might be quite expensive.
 */
public class Configurations {

    private static final Logger LOG = LoggerFactory.getLogger(Configurations.class);

    private static volatile ConfigurationService configService;

    protected void activate(ComponentContext context) {
        LOG.info(MessageFormat.format("[Configurations] {0} : activated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void deactivate(ComponentContext context) {
        LOG.info(MessageFormat.format("[Configurations] {0} : deactivated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void bindConfigurationService(ConfigurationService configService) {
        LOG.info(MessageFormat.format("bindConfigurationService({0})", configService)); //$NON-NLS-1$
        Configurations.configService = configService;
    }

    protected void unbindConfigurationService(ConfigurationService configService) {
        LOG.info(MessageFormat.format("unbindConfigurationService({0})", configService)); //$NON-NLS-1$
        Configurations.configService = null;
    }

    /**
     * Returns the configuration for the given configuration class.
     *
     * @param configurationClass  the class of the configuration to retrieve.
     * @return  the configuration instance, or <code>null</code> if no
     * such configuration exists, or no configuration service is currently active.
     */
    public static <T> T getConfiguration(Class<T> configurationClass) {
        return configurationClass != null & configService != null?
                configService.readConfiguration(configurationClass) : null;
    }
}
