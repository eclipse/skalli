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
package org.eclipse.skalli.services.entity;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.skalli.model.EntityBase;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convenience class for accessing {@link EntityServices entity services} currently
 * registered in the OSGi runtime. Extension services can be retrieved by the
 * {@link EntityBase entity classes} they manage.
 * <p>
 * Note, this class tracks entity service by means of OSGi's declarative service
 * mechanisms and caches the currently registered services in internal maps.
 * All accessor methods take their result from the internal caches.
 */
public class EntityServices {

    private static final Logger LOG = LoggerFactory.getLogger(EntityServices.class);

    private static Set<EntityService<?>> all =
            Collections.synchronizedSet(new HashSet<EntityService<?>>());
    private static Map<String, EntityService<?>> byEntityClassName =
            Collections.synchronizedMap(new HashMap<String, EntityService<?>>());

    protected void activate(ComponentContext context) {
        LOG.info(MessageFormat.format("[EntityServices] {0} : activated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void deactivate(ComponentContext context) {
        LOG.info(MessageFormat.format("[EntityServices] {0} : deactivated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void bindEntityService(EntityService<?> entityService) {
        String entityClassName = entityService.getEntityClass().getName();
        all.add(entityService);
        byEntityClassName.put(entityClassName, entityService);
        LOG.info(MessageFormat.format("[EntityServices][registered {0}]", entityClassName));
    }

    protected void unbindEntityService(EntityService<?> entityService) {
        String entityClassName = entityService.getEntityClass().getName();
        byEntityClassName.remove(entityClassName);
        all.remove(entityService);
        LOG.info(MessageFormat.format("[EntityServices][unregistered {0}]", entityClassName));
    }

    /**
     * Returns all currently registered entity services.
     *
     * @return a collection of entity services, or an empty collection.
     */
    public static Collection<EntityService<?>> getAll() {
        return Collections.unmodifiableSet(all);
    }

    /**
     * Returns all currently registered entity services by the names
     * of the {@link EntityBase entity classes} with which they
     * are associated.
     *
     * @return a map of entity services with (fully qualified) names
     * of entity classes as keys, or an empty map.
     */
    public static Map<String, EntityService<?>> getByEntityClassNames() {
        return Collections.unmodifiableMap(byEntityClassName);
    }

    /**
     * Returns the {@link EntityService} instance matching a given
     * {@link EntityBase entity class} name.
     *
     * @param entityClassName  the entity class name.
     * @return  the entity service instance, or <code>null</code> if there is no instance
     * for the given entity class available.
     */
    @SuppressWarnings("unchecked")
    public static <T extends EntityBase> EntityService<T> getByEntityClassName(String entityClassName) {
        EntityService<?> entityService = byEntityClassName.get(entityClassName);
        return entityService != null? (EntityService<T>)entityService : null;
    }

    /**
     * Returns the {@link EntityService} instance matching a given
     * {@link EntityBase entity class}.
     *
     * @param entityClass  the entity class.
     * @return  the entity service instance, or <code>null</code> if there is no instance
     * for the given entity class available.
     */
    public static <T extends EntityBase> EntityService<T> getByEntityClass(Class<T> entityClass) {
        return getByEntityClassName(entityClass.getName());
    }

}
