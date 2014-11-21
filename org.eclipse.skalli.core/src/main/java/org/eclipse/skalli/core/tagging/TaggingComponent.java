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
package org.eclipse.skalli.core.tagging;

import java.text.MessageFormat;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.commons.ThreadPool;
import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.services.entity.EntityService;
import org.eclipse.skalli.services.entity.EventEntityUpdate;
import org.eclipse.skalli.services.event.EventListener;
import org.eclipse.skalli.services.event.EventService;
import org.eclipse.skalli.services.tagging.TagCount;
import org.eclipse.skalli.services.tagging.TaggingService;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaggingComponent implements TaggingService, EventListener<EventEntityUpdate> {

    private static final Logger LOG = LoggerFactory.getLogger(TaggingComponent.class);

    private ConcurrentHashMap<Class<?>, TagCache> caches = new ConcurrentHashMap<Class<?>, TagCache>();

    protected void activate(ComponentContext context) {
        LOG.info(MessageFormat.format("[TaggingService] {0} : activated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void deactivate(ComponentContext context) {
        LOG.info(MessageFormat.format("[TaggingService] {0} : deactivated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void bindEntityService(final EntityService<?> entityService) {
        LOG.info(MessageFormat.format("bindEntityService({0})", entityService.getEntityClass().getSimpleName())); //$NON-NLS-1$
        // initialize tags asynchronously to not block the binding
        ThreadPool.submit(new Runnable() {
            @Override
            public void run() {
                initialize(entityService);
            }
        });
    }

    protected void unbindEntityService(EntityService<?> entityService) {
        LOG.info(MessageFormat.format("unbindEntityService({0})", entityService.getEntityClass().getSimpleName())); //$NON-NLS-1$
        caches.remove(entityService.getEntityClass());
    }

    protected void bindEventService(EventService eventService) {
        LOG.info(MessageFormat.format("bindEventService({0})", eventService)); //$NON-NLS-1$
        eventService.registerListener(EventEntityUpdate.class, this);
    }

    protected void unbindEventService(EventService eventService) {
        LOG.info(MessageFormat.format("unbindEventService({0})", eventService)); //$NON-NLS-1$
    }

    @Override
    public <T extends EntityBase> SortedMap<String, Integer> getTags(Class<T> entityClass) {
        TagCache tagCache = caches.get(entityClass);
        if (tagCache == null) {
            return CollectionUtils.emptySortedMap();
        }
        return tagCache.getByName();
    }

    @Override
    public <T extends EntityBase> SortedSet<TagCount> getMostPopular(Class<T> entityClass) {
        TagCache tagCache = caches.get(entityClass);
        if (tagCache == null) {
            return CollectionUtils.emptySortedSet();
        }
        return tagCache.getByCount();
    }

    @Override
    public <T extends EntityBase> SortedSet<TagCount> getMostPopular(Class<T> entityClass, int count) {
        TagCache tagCache = caches.get(entityClass);
        if (tagCache == null) {
            return CollectionUtils.emptySortedSet();
        }
        return tagCache.getByCount(count);
    }

    @Override
    public void onEvent(EventEntityUpdate event) {
        Class<?> entityClass = event.getEntityClass();
        TagCache tagCache = caches.get(entityClass);
        if (tagCache == null) {
            caches.putIfAbsent(entityClass, new TagCache());
            tagCache = caches.get(entityClass);
        }
        tagCache.update(event.getEntity());
    }

    void initialize(EntityService<?> entityService) {
        Class<?> entityClass = entityService.getEntityClass();
        TagCache tagCache = caches.get(entityClass);
        if (tagCache == null) {
            caches.putIfAbsent(entityClass, new TagCache());
            tagCache = caches.get(entityClass);
        }
        tagCache.initialize(entityService);
    }
}
