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
package org.eclipse.skalli.core.internal.tagging;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.model.ExtensibleEntityBase;
import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.Taggable;
import org.eclipse.skalli.services.persistence.PersistenceService;
import org.eclipse.skalli.services.tagging.TaggingService;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaggingServiceImpl implements TaggingService {

    private static final Logger LOG = LoggerFactory.getLogger(TaggingServiceImpl.class);

    private PersistenceService persistenceService;

    protected void bindPersistenceService(PersistenceService srvc) {
        this.persistenceService = srvc;
    }

    protected void unbindPersistenceService(PersistenceService srvc) {
        this.persistenceService = null;
    }

    protected void activate(ComponentContext context) {
        LOG.info(MessageFormat.format("[TaggingService] {0} : activated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void deactivate(ComponentContext context) {
        LOG.info(MessageFormat.format("[TaggingService] {0} : deactivated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    @Override
    public <T extends EntityBase> Set<String> getAllTags(Class<T> entityClass) {
        Set<String> result = new HashSet<String>();
        List<T> entities = persistenceService.getEntities(entityClass);
        for (T entity : entities) {
            if (entity instanceof Taggable) {
                appendTags((Taggable)entity, result);
            } else if (entity instanceof ExtensibleEntityBase) {
                appendTagsFromExtensions((ExtensibleEntityBase)entity, result);
            }
        }
        return result;
    }

    private void appendTags(Taggable taggable, Set<String> result) {
        Set<String> tags = taggable.getTags();
        if (tags != null) {
            for (String tag : tags) {
                if (!result.contains(tag)) {
                    result.add(tag);
                }
            }
        }
    }

    private void appendTagsFromExtensions(ExtensibleEntityBase entity, Set<String> result) {
        for (ExtensionEntityBase ext: entity.getAllExtensions()) {
            if (ext instanceof Taggable) {
                appendTags((Taggable)ext, result);
            }
        }
    }

    @Override
    public <T extends EntityBase> Set<T> getTaggables(Class<T> entityClass, String tag) {
        Set<T> result = new HashSet<T>();
        List<T> entities = persistenceService.getEntities(entityClass);
        for (T entity : entities) {
            if (entity instanceof Taggable) {
                if (((Taggable)entity).hasTag(tag)) {
                    result.add(entity);
                }
            } else if (entity instanceof ExtensibleEntityBase) {
                for (ExtensionEntityBase ext: ((ExtensibleEntityBase)entity).getAllExtensions()) {
                    if (ext instanceof Taggable) {
                        if (((Taggable)ext).hasTag(tag)) {
                            result.add(entity);
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public <T extends EntityBase> Map<String, Set<T>> getTaggables(Class<T> entityClass) {
        Map<String, Set<T>> result = new HashMap<String, Set<T>>();
        for (String tag : getAllTags(entityClass)) {
            Set<T> taggables = getTaggables(entityClass, tag);
            result.put(tag, taggables);
        }
        return result;
    }
}
