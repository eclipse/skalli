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
package org.eclipse.skalli.core.configuration;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;
import org.eclipse.skalli.commons.ThreadPool;
import org.eclipse.skalli.core.storage.FileStorageComponent;
import org.eclipse.skalli.core.xstream.CompositeEntityClassLoader;
import org.eclipse.skalli.services.BundleProperties;
import org.eclipse.skalli.services.configuration.ConfigSection;
import org.eclipse.skalli.services.configuration.ConfigurationService;
import org.eclipse.skalli.services.configuration.EventConfigUpdate;
import org.eclipse.skalli.services.event.EventService;
import org.eclipse.skalli.services.permit.Permits;
import org.eclipse.skalli.services.persistence.StorageException;
import org.eclipse.skalli.services.persistence.StorageService;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;

public class ConfigurationComponent implements ConfigurationService {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationComponent.class);
    private static final Logger AUDIT_LOG = LoggerFactory.getLogger("audit"); //$NON-NLS-1$

    private static final String CATEGORY_CUSTOMIZATION = "customization"; //$NON-NLS-1$

    private EventService eventService;
    private StorageService storageService;
    private String storageServiceClassName;

    private Map<String, ConfigSection<?>> byStorageKey =
            new ConcurrentHashMap<String, ConfigSection<?>>();
    private Map<Class<?>, ConfigSection<?>> byConfigClass =
            new ConcurrentHashMap<Class<?>, ConfigSection<?>>();
    private static Map<Class<?>, Object> configCache =
            new ConcurrentHashMap<Class<?>, Object>();

    public ConfigurationComponent() {
        storageServiceClassName = BundleProperties.getProperty(
                BundleProperties.PROPERTY_STORAGE_SERVICE, FileStorageComponent.class.getName());
    }

     // Constructor for testing purposes.
     // Set the storage services to use with bindStorageService().
    ConfigurationComponent(String storageServiceClassName) {
        this.storageServiceClassName = storageServiceClassName;
    }

    protected void activate(ComponentContext context) {
        LOG.info(MessageFormat.format("[ConfigurationService] {0} : activated", //$NON-NLS-1$
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void deactivate(ComponentContext context) {
        LOG.info(MessageFormat.format("[ConfigurationService] {0} : deactivated", //$NON-NLS-1$
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void bindEventService(EventService eventService) {
        this.eventService = eventService;
        LOG.info(MessageFormat.format("bindEventService({0})", eventService)); //$NON-NLS-1$
    }

    protected void unbindEventService(EventService eventService) {
        LOG.info(MessageFormat.format("unbindEventService({0})", eventService)); //$NON-NLS-1$
        this.eventService = null;
    }

    protected void bindStorageService(StorageService storageService) {
        if (storageServiceClassName.equals(storageService.getClass().getName())) {
            this.storageService = storageService;
            configCache.clear();
            notifyCustomizationChanged(storageService);
            LOG.info(MessageFormat.format("bindStorageService({0})", storageService)); //$NON-NLS-1$
        }
    }

    protected void unbindStorageService(StorageService storageService) {
        if (storageServiceClassName.equals(storageService.getClass().getName())) {
            LOG.info(MessageFormat.format("unbindStorageService({0})", storageService)); //$NON-NLS-1$
            this.storageService = null;
            configCache.clear();
            notifyCustomizationChanged(storageService);
        }
    }

    protected void bindConfigSection(ConfigSection<?> configSection) {
        byConfigClass.put(configSection.getConfigClass(), configSection);
        byStorageKey.put(configSection.getStorageKey(), configSection);
        configCache.remove(configSection.getConfigClass());
        notifyCustomizationChanged(configSection);
        LOG.info(MessageFormat.format("bindConfigSection({0})", configSection)); //$NON-NLS-1$
    }

    protected void unbindConfigSection(ConfigSection<?> configSection) {
        LOG.info(MessageFormat.format("unbindConfigSection({0})", configSection)); //$NON-NLS-1$
        byConfigClass.remove(configSection.getConfigClass());
        byStorageKey.remove(configSection.getStorageKey());
        configCache.remove(configSection.getConfigClass());
        notifyCustomizationChanged(configSection);
    }

    ConfigSection<?> getConfigSection(Class<?> configClass) {
        return byConfigClass.get(configClass);
    }

    ConfigSection<?> getConfigSection(String storageKey) {
        return byStorageKey.get(storageKey);
    }

    @Override
    public synchronized <T> void writeConfiguration(T configuration) {
        if (storageService == null) {
            LOG.error("Cannot store configurations: StorageService not available");
            return;
        }

        if (configuration == null) {
            // delete configuration?
            return;
        }

        ConfigSection<?> configSection = byConfigClass.get(configuration.getClass());
        if (configSection == null) {
            LOG.error(MessageFormat.format(
                    "Cannot store configuration: No suitable configuration extension " + //$NON-NLS-1$
                    "for configurations of type ''{0}'' available", //$NON-NLS-1$
                    configuration.getClass()));
            return;
        }

        String storageKey = configSection.getStorageKey();
        Class<?> configurationClass = configuration.getClass();
        String xml = getXStream(configurationClass).toXML(configuration);
        InputStream is = null;
        try {
            is = new ByteArrayInputStream(xml.getBytes("UTF-8")); //$NON-NLS-1$
            storageService.write(CATEGORY_CUSTOMIZATION, storageKey, is);
            configCache.put(configurationClass, configuration);
            if (eventService != null) {
                fireEvent(configSection, configuration);
            }
            AUDIT_LOG.info(MessageFormat.format("Configuration ''{0}'' changed by user ''{1}''",
                    storageKey, Permits.getLoggedInUser()));
        } catch (UnsupportedEncodingException e) {
            // should never happen for UTF-8
            throw new IllegalStateException(e);
        } catch (StorageException e) {
            LOG.error(MessageFormat.format("Failed to store configuration ''{0}''", storageKey), e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    };

    @Override
    public synchronized <T> T readConfiguration(Class<T> configurationClass) {
        if (storageService == null) {
            LOG.warn("Cannot load configurations: StorageService not available");
            return null;
        }

        if (configurationClass == null) {
            return null;
        }

        Object config = configCache.get(configurationClass);
        if (config == null) {
            ConfigSection<?> configSection = byConfigClass.get(configurationClass);
            if (configSection == null) {
                LOG.error(MessageFormat.format(
                        "Cannot retrieve configuration: No suitable configuration extension " + //$NON-NLS-1$
                        "for configurations of type ''{0}'' available", //$NON-NLS-1$
                        configurationClass));
                return null;
            }
            config = readConfiguration(configSection.getStorageKey(), configurationClass);
        }
        return configurationClass.cast(config);
    }

    private Object readConfiguration(String storageKey, Class<?> configurationClass) {
        Object config = null;
        InputStream is = null;
        try {
            is = storageService.read(CATEGORY_CUSTOMIZATION, storageKey);
            if (is == null) {
                return null;
            }
            config = getXStream(configurationClass).fromXML(is);
        } catch (XStreamException e) {
            LOG.error(MessageFormat.format(
                    "Failed to unmarshal configuration ''{0}'' ",
                    storageKey), e);
        } catch (StorageException e) {
            LOG.error(MessageFormat.format(
                    "Failed to retrieve configuration ''{0}''",
                    storageKey), e);
        } finally {
            IOUtils.closeQuietly(is);
        }
        return config;
    }

    private XStream getXStream(Class<?> customizationClass) {
        XStream xstream = new XStream();
        ClassLoader classLoader = customizationClass.getClassLoader();
        if (classLoader != null) {
            xstream.setClassLoader(new CompositeEntityClassLoader(Collections.singleton(classLoader)));
        }
        return xstream;
    }

    private void notifyCustomizationChanged(final StorageService storageService) {
        ThreadPool.submit(new Runnable() {
            @Override
            public void run() {
                if (eventService != null) {
                    List<String> storageKeys = Collections.emptyList();
                    try {
                        storageKeys = storageService.keys(CATEGORY_CUSTOMIZATION);
                    } catch (StorageException e) {
                        LOG.error("Failed to retrieve configuration keys", e);
                        return;
                    }
                    for (String storageKey: storageKeys) {
                        ConfigSection<?> configSection = byStorageKey.get(storageKey);
                        if (configSection != null) {
                            fireEvent(configSection);
                        }
                    }
                }
            }
        });
    }

    private void notifyCustomizationChanged(final ConfigSection<?> configSection) {
        ThreadPool.submit(new Runnable() {
            @Override
            public void run() {
                if (eventService != null && storageService != null) {
                    fireEvent(configSection);
                }
            }
        });
    }

    private void fireEvent(ConfigSection<?> configSection) {
        Object config = readConfiguration(configSection.getStorageKey(), configSection.getConfigClass());
        fireEvent(configSection, config);
    }

    private void fireEvent(ConfigSection<?> configSection, Object config) {
        eventService.fireEvent(new EventConfigUpdate(configSection.getConfigClass(), config));
        LOG.info(MessageFormat.format("Event sent: Configuration ''{0}'' has changed",
                configSection.getConfigClass().getSimpleName()));
    }
}
