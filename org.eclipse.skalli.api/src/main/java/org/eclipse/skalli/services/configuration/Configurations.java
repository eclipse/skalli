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
package org.eclipse.skalli.services.configuration;

import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.skalli.services.event.EventListener;
import org.eclipse.skalli.services.event.EventService;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Convenience class for accessing configuration settings. For performance
 * reasons configurations should always be retrieved with the help of this class,
 * since {@link ConfigurationService} is supposed to read configurations
 * directly from storage.
 * <br>
 * This class tracks all configuration changes, i.e. events of the type
 * {@link EventConfigUpdate} and caches the current configuration settings.
 */
public class Configurations implements EventListener<EventConfigUpdate> {

    private static final Logger LOG = LoggerFactory.getLogger(Configurations.class);

    private static Map<Class<?>, Object> configCache = new ConcurrentHashMap<Class<?>, Object>();

    private static volatile ConfigurationService configService;

    protected void activate(ComponentContext context) {
        LOG.info(MessageFormat.format("[Configurations] {0} : activated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void deactivate(ComponentContext context) {
        LOG.info(MessageFormat.format("[Configurations] {0} : deactivated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void bindEventService(EventService eventService) {
        LOG.info(MessageFormat.format("bindEventService({0})", eventService)); //$NON-NLS-1$
        configCache.clear();
        eventService.registerListener(EventConfigUpdate.class, this);
    }

    protected void unbindEventService(EventService eventService) {
        LOG.info(MessageFormat.format("unbindEventService({0})", eventService)); //$NON-NLS-1$
        configCache.clear();
    }

    protected void bindConfigurationService(ConfigurationService configService) {
        LOG.info(MessageFormat.format("bindConfigurationService({0})", configService)); //$NON-NLS-1$
        configCache.clear();
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
     * such configuration exists.
     */
    public static <T> T getConfiguration(Class<T> configurationClass) {
        Object config = configCache.get(configurationClass);
        if (config == null && configService != null) {
            config = configService.readConfiguration(configurationClass);
        }
        return configurationClass.cast(config);
    }

    @Override
    public void onEvent(EventConfigUpdate event) {
        Class<?> configClass = event.getConfigClass();
        Object config = event.getConfig();
        if (config == null) {
            configCache.remove(configClass);
        } else {
            configCache.put(configClass, config);
        }
        LOG.info(MessageFormat.format("[Configurations] Event received: Configuration ''{0}'' has changed",
                configClass.getSimpleName()));
    }

}
