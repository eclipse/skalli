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
package org.eclipse.skalli.core.persistence;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.commons.UUIDListConverter;
import org.eclipse.skalli.core.xstream.ExtensionsMapConverter;
import org.eclipse.skalli.core.xstream.NoopConverter;
import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.services.entity.EntityService;
import org.eclipse.skalli.services.entity.EntityServices;
import org.eclipse.skalli.services.extension.DataMigration;
import org.eclipse.skalli.services.extension.ExtensionService;
import org.eclipse.skalli.services.extension.ExtensionServices;
import org.eclipse.skalli.services.persistence.PersistenceService;

import com.thoughtworks.xstream.converters.Converter;

public abstract class PersistenceServiceBase implements PersistenceService {

    private static final String ENTITY_PREFIX = "entity-"; //$NON-NLS-1$

    protected <T extends EntityBase> Set<DataMigration> getMigrations(Class<T> entityClass) {
        Set<DataMigration> migrations = new HashSet<DataMigration>();
        EntityService<?> entityService = EntityServices.getByEntityClass(entityClass);
        if (entityService != null) {
            migrations.addAll(entityService.getMigrations());
        }
        for (ExtensionService<?> extensionService : ExtensionServices.getAll()) {
            if (extensionService.getMigrations() != null) {
                migrations.addAll(extensionService.getMigrations());
            }
        }
        return migrations;
    }

    protected <T extends EntityBase> Map<String, Class<?>> getAliases(Class<T> entityClass) {
        Map<String, Class<?>> aliases = new HashMap<String, Class<?>>();
        EntityService<?> entityService = EntityServices.getByEntityClass(entityClass);
        if (entityService != null) {
            aliases.putAll(entityService.getAliases());
        }
        addExtensionAliases(aliases);
        return aliases;
    }

    private void addExtensionAliases(Map<String, Class<?>> aliases) {
        for (ExtensionService<?> extensionService : ExtensionServices.getAll()) {
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
        EntityService<?> entityService = EntityServices.getByEntityClass(entityClass);
        if (entityService != null) {
            classLoaders.add(entityService.getClass().getClassLoader());
            Set<ClassLoader> additionalClassLoaders = entityService.getClassLoaders();
            if (additionalClassLoaders != null) {
                classLoaders.addAll(additionalClassLoaders);
            }
        }

        // add the classloaders of all extension services and the additional
        // classloaders provided by the extension services
        for (ExtensionService<?> extensionService : ExtensionServices.getAll()) {
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
