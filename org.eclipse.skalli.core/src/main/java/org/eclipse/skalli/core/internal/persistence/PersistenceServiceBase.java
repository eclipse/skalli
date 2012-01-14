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
package org.eclipse.skalli.core.internal.persistence;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.core.internal.persistence.xstream.ExtensionsMapConverter;
import org.eclipse.skalli.core.internal.persistence.xstream.NoopConverter;
import org.eclipse.skalli.core.internal.persistence.xstream.UUIDListConverter;
import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.services.entity.EntityService;
import org.eclipse.skalli.services.extension.DataMigration;
import org.eclipse.skalli.services.extension.ExtensionService;
import org.eclipse.skalli.services.persistence.PersistenceService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.converters.Converter;

public abstract class PersistenceServiceBase implements PersistenceService {

    private static final Logger LOG = LoggerFactory.getLogger(PersistenceServiceBase.class);

    private static final String ENTITY_PREFIX = "entity-"; //$NON-NLS-1$

    private ComponentContext context;
    private Map<Class<?>, EntityService<?>> entityServices = new HashMap<Class<?>, EntityService<?>>();
    private Set<ExtensionService<?>> extensionServices = new HashSet<ExtensionService<?>>();
    private Set<String> shortNames = new HashSet<String>();

    protected void activate(ComponentContext context) {
        this.context = context;
    }

    protected void deactivate(ComponentContext context) {
        this.context = null;
    }

    protected void bindExtensionService(ExtensionService<?> extensionService) {
        String shortName = extensionService.getShortName();
        if (shortNames.contains(shortName)) {
            throw new RuntimeException(MessageFormat.format(
                    "There is already an extension registered with the short name ''{0}''", shortName)); //$NON-NLS-1$
        }
        shortNames.add(shortName);
        extensionServices.add(extensionService);
        LOG.debug(MessageFormat.format("Registered model extension {0}", //$NON-NLS-1$
                extensionService.getExtensionClass().getName()));
    }

    protected void unbindExtensionService(ExtensionService<?> extensionService) {
        shortNames.remove(extensionService.getShortName());
        extensionServices.remove(extensionService);
        LOG.debug(MessageFormat.format("Unregistered model extension class {0}", //$NON-NLS-1$
                extensionService.getExtensionClass().getName()));
    }

    protected void bindEntityService(EntityService<?> entityService) {
        entityServices.put(entityService.getEntityClass(), entityService);
        LOG.debug(MessageFormat.format("Registered entity service {0} for entities of type {1}", //$NON-NLS-1$
                entityService, entityService.getEntityClass().getSimpleName()));
    }

    protected void unbindEntityService(EntityService<?> entityService) {
        entityServices.remove(entityService.getEntityClass());
        LOG.debug(MessageFormat.format("Unregistered entity service {0} for entities of type {1}", //$NON-NLS-1$
                entityService, entityService.getEntityClass().getSimpleName()));
    }

    protected Set<ExtensionService<?>> getExtensionServices() {
        return extensionServices;
    }

    @SuppressWarnings("unchecked")
    protected <T extends EntityBase> EntityService<T> getEntityService(Class<T> entityClass) {
        EntityService<?> entityService = entityServices.get(entityClass);
        if (entityService == null) {
            initializeEntityServices();
            entityService = entityServices.get(entityClass);
        }
        return (EntityService<T>) entityService;
    }

    private synchronized void initializeEntityServices() {
        Map<Class<?>, EntityService<?>> availableEntityServices = locateEntityServices();
        for (Class<?> entityClass: availableEntityServices.keySet()) {
            EntityService<?> entityService = availableEntityServices.get(entityClass);
            if (entityServices.containsKey(entityClass) &&
                    !entityServices.get(entityClass).equals(entityService)) {
                unbindEntityService(entityServices.get(entityClass));
            }
            if (!entityServices.containsKey(entityClass)) {
                bindEntityService(entityService);
            }
        }
    }

    private Map<Class<?>, EntityService<?>> locateEntityServices() {
        Map<Class<?>, EntityService<?>> availableEntityServices = new HashMap<Class<?>, EntityService<?>>();
        Object[] services = context != null? context.locateServices("EntityService") : null; //$NON-NLS-1$
        if (services != null) {
            for (Object service: services) {
                EntityService<?> entityService = (EntityService<?>) service;
                availableEntityServices.put(entityService.getEntityClass(), entityService);
            }
        }
        return availableEntityServices;
    }

    protected <T extends EntityBase> Set<DataMigration> getMigrations(Class<T> entityClass) {
        Set<DataMigration> migrations = new HashSet<DataMigration>();
        EntityService<?> entityService = getEntityService(entityClass);
        if (entityService != null) {
            migrations.addAll(entityService.getMigrations());
        }
        for (ExtensionService<?> extensionService : extensionServices) {
            if (extensionService.getMigrations() != null) {
                migrations.addAll(extensionService.getMigrations());
            }
        }
        return migrations;
    }

    protected <T extends EntityBase> Map<String, Class<?>> getAliases(Class<T> entityClass) {
        Map<String, Class<?>> aliases = new HashMap<String, Class<?>>();
        EntityService<?> entityService = getEntityService(entityClass);
        if (entityService != null) {
            aliases.putAll(entityService.getAliases());
        }
        addExtensionAliases(aliases);
        return aliases;
    }

    private void addExtensionAliases(Map<String, Class<?>> aliases) {
        for (ExtensionService<?> extensionService : extensionServices) {
            Class<?> extensionClass = extensionService.getExtensionClass();
            String shortName = extensionService.getShortName();
            Map<String, Class<?>> extensionAliases = extensionService.getAliases();
            if (extensionAliases != null) {
                for (Entry<String, Class<?>> alias: extensionAliases.entrySet()) {
                    String key = alias.getKey();
                    Class<?> value = alias.getValue();
                    if (aliases.containsKey(key) && !aliases.get(key).equals(value)) {
                        throw new IllegalStateException(MessageFormat.format(
                                "Alias ''{0}'' is already registered for class {1}", key, value.getName()));
                    }

                    // add the alias definition, unless it is an
                    // alias for the extension class
                    if (!value.equals(extensionClass)) {
                        aliases.put(key, value);
                    }

                }
            }
            // add the alias for the extension class
            aliases.put(ENTITY_PREFIX + shortName, extensionClass);
        }
    }

    protected <T extends EntityBase> Set<ClassLoader> getClassLoaders(Class<T> entityClass) {
        Set<ClassLoader> classLoaders = new HashSet<ClassLoader>();

        // ensure that we always have a classloader for the API bundle
        classLoaders.add(EntityBase.class.getClassLoader());

        // if an EntityService is matching the given entity class,
        // add the classloader of the entity service and all additional
        // classloaders provided by the entity service
        EntityService<?> entityService = getEntityService(entityClass);
        if (entityService != null) {
            classLoaders.add(entityService.getClass().getClassLoader());
            Set<ClassLoader> additionalClassLoaders = entityService.getClassLoaders();
            if (additionalClassLoaders != null) {
                classLoaders.addAll(additionalClassLoaders);
            }
        }

        // add the classloaders of all extension services and the additional
        // classloaders provided by the extension services
        for (ExtensionService<?> extensionService : extensionServices) {
            classLoaders.add(extensionService.getClass().getClassLoader());
            Set<ClassLoader> additionalClassLoaders = extensionService.getClassLoaders();
            if (additionalClassLoaders != null) {
                classLoaders.addAll(additionalClassLoaders);
            }
        }
        return classLoaders;
    }

    protected <T extends EntityBase> Set<Converter> getConverters(Class<T> entityClass) {
        return CollectionUtils.asSet(new NoopConverter(), new UUIDListConverter(), new ExtensionsMapConverter());
    }

}
