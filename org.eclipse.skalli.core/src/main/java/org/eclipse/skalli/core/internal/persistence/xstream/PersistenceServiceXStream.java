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
package org.eclipse.skalli.core.internal.persistence.xstream;

import java.text.MessageFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.api.java.EntityFilter;
import org.eclipse.skalli.api.java.PersistenceService;
import org.eclipse.skalli.api.java.StorageException;
import org.eclipse.skalli.api.java.StorageService;
import org.eclipse.skalli.core.internal.persistence.AbstractPersistenceService;
import org.eclipse.skalli.core.utils.ConfigurationProperties;
import org.eclipse.skalli.model.ext.EntityBase;
import org.eclipse.skalli.model.ext.ExtensionService;
import org.eclipse.skalli.model.ext.ValidationException;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link PersistenceService} based on XStream.
 */
public class PersistenceServiceXStream extends AbstractPersistenceService implements PersistenceService {

    private static final Logger LOG = LoggerFactory.getLogger(PersistenceServiceXStream.class);

    private final DataModelContainer cache = new DataModelContainer();
    private final DataModelContainer deleted = new DataModelContainer();

    private XStreamPersistence xstreamPersistence;

    private String storageServiceClassName;

    /**
     * Creates a new, uninitialized <code>PersistenceServiceXStream</code>.
     */
    public PersistenceServiceXStream() {
        storageServiceClassName = ConfigurationProperties.getConfiguredStorageService();
    }

    /**
     * Creates a <code>PersistenceServiceXStream</code> based on the given <code>XStreamPersistence</code>.
     * Note, this constructor should not be used to instantiate instances of this service directly except
     * for testing purposes.
     */
    PersistenceServiceXStream(XStreamPersistence xstreamPersistence) {
        this.xstreamPersistence = xstreamPersistence;
    }

    protected void activate(ComponentContext context) {
        LOG.info(MessageFormat.format("[PersistenceService] {0} : activated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void deactivate(ComponentContext context) {
        xstreamPersistence = null;
        cache.clearAll();
        deleted.clearAll();
        LOG.info(MessageFormat.format("[PersistenceService] {0} : deactivated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    @Override
    protected synchronized void bindExtensionService(ExtensionService<?> extensionService) {
        super.bindExtensionService(extensionService);
        cache.clearAll();
        deleted.clearAll();
    }

    @Override
    protected synchronized void unbindExtensionService(ExtensionService<?> extensionService) {
        super.unbindExtensionService(extensionService);
        cache.clearAll();
        deleted.clearAll();
    }

    protected void bindStorageService(StorageService storageService) {
        if (storageServiceClassName.equals(storageService.getClass().getName())) {
            xstreamPersistence = new XStreamPersistence(storageService);
            cache.clearAll();
            deleted.clearAll();
            LOG.info(MessageFormat.format("bindStorageService({0})", storageService)); //$NON-NLS-1$
        }
    }

    protected void unbindStorageService(StorageService storageService) {
        if (storageServiceClassName.equals(storageService.getClass().getName())) {
            LOG.info(MessageFormat.format("unbindStorageService({0})", storageService)); //$NON-NLS-1$
            xstreamPersistence = null;
            cache.clearAll();
            deleted.clearAll();
        }
    }

    /**
     * Loads all entities of the given class. Resolves
     * the parent hierarchy of the loaded entities and stores the
     * result in the model caches (deleted entities in {@link #deleted},
     * all other in {@link #cache}).
     *
     * @param entityClass   the class the entities belongs to.
     */
    private synchronized <T extends EntityBase> void loadModel(Class<T> entityClass) {
        if (cache.size(entityClass) > 0) {
            //nothing to do, all entities are already loaded in the cache :-)
            return;
        }
        if (xstreamPersistence == null) {
            LOG.warn("Cannot load entities of type " + entityClass + ": StorageService not available");
            return;
        }
        List<T> loadedEntities;
        try {
            loadedEntities = xstreamPersistence.loadEntities(entityClass,
                    getEntityClassLoaders(), getMigrations(), getAliases());
        } catch (StorageException e) {
            throw new RuntimeException(e);
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
        updateCache(loadedEntities);
        resolveParentEntities(entityClass);
    }

    private <T extends EntityBase> void updateCache(List<T> loadedEntitiys) {
        for (EntityBase entityBase : loadedEntitiys) {
            updateCache(entityBase);
        }
    }

    @Override
    public synchronized void persist(EntityBase entity, String userId) {
        if (entity == null) {
            throw new IllegalArgumentException("argument 'entity' must not be null");
        }
        if (StringUtils.isBlank(userId)) {
            throw new IllegalArgumentException("argument 'userId' must not be null or an empty string");
        }
        if (xstreamPersistence == null) {
            throw new IllegalStateException("StorageService not available");
        }

        // load all project models
        loadModel(entity.getClass());

        // generate unique id
        if (entity.getUuid() == null) {
            entity.setUuid(UUID.randomUUID());
        }

        // verify parent is known
        // TODO should be in EntitySeriviceImpl#validate
        if (entity.getParentEntityId() != null) {
            UUID parentUUID = entity.getParentEntityId();
            EntityBase parent = getParentEntity(entity.getClass(), entity);
            if (parent == null) {
                throw new RuntimeException("Parent entity " + parentUUID + " does not exist");
            }
        }

        try {
            xstreamPersistence.saveEntity(entity, userId, getAliases());
        } catch (StorageException e) {
            throw new RuntimeException(e);
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
        reloadAndUpdateCache(entity);
    }

    private void reloadAndUpdateCache(EntityBase entity) {
        EntityBase savedEntity = loadEntity(entity.getClass(), entity.getUuid());
        if (savedEntity != null) {
            updateCache(savedEntity);
            LOG.debug("entity '" + savedEntity + "' successfully saved");
        } else {
            throw new RuntimeException("Failed to save entity '" + entity + "'");
        }
    }

    /**
     * Adds the given entity to the cache (deleted entities in {@link #deleted},
     * all other in {@link #cache}).
     *
     * Note, this method should not be called directly except for testing purposes.
     *
     * @param entity  the entity to add.
     */
    void updateCache(EntityBase entity) {
        if (entity.isDeleted()) {
            cache.removeEntity(entity);
            deleted.putEntity(entity);
        } else {
            deleted.removeEntity(entity);
            cache.putEntity(entity);
        }
    }

    /**
     * Resolves the parent hierarchies for all entities of the given class.
     * This method resolves the parents both for deleted and non-deleted entities.
     * It assumes, that the entity caches for the given entity class have already
     * been initialized with {@link #loadModel(Class)}.
     *
     * Note, this method should not be called directly except for testing purposes.
     *
     * @param <T>  type of an antity derived from <code>EntityBase</code>.
     * @param entityClass  the class of entities to resolve.
     */
    <T extends EntityBase> void resolveParentEntities(Class<T> entityClass) {
        resolveParentEntities(entityClass, cache.getEntities(entityClass));
        resolveParentEntities(entityClass, deleted.getEntities(entityClass));
    }

    private <T extends EntityBase> void resolveParentEntities(Class<T> entityClass, List<T> entities) {
        for (T entity : entities) {
            UUID parentId = entity.getParentEntityId();
            if (parentId != null) {
                T parentEntity = getParentEntity(entityClass, entity);
                if (parentEntity == null) {
                    LOG.warn("Entity " + entity.getUuid() + " references entity " + parentId
                            + " as parent entity - but there is no such entity");
                    continue;
                }
                if (parentEntity.isDeleted() && !entity.isDeleted()) {
                    LOG.warn("Entity " + entity.getUuid() + " cannot reference deleted entity "
                            + parentId + " as parent entity");
                    continue;
                }
                entity.setParentEntity(parentEntity);
            }
        }
    }

    private <T extends EntityBase> T getParentEntity(Class<T> entityClass, EntityBase entity) {
        UUID parentId = entity.getParentEntityId();
        T parentEntity = null;
        if (parentId != null) {
            if (!entity.isDeleted()) {
                // undeleted entities can only reference undeleted entities
                parentEntity = cache.getEntity(entityClass, parentId);
            }
            else {
                // deleted entities can reference deleted & undeleted entities
                parentEntity = cache.getEntity(entityClass, parentId);
                if (parentEntity == null) {
                    parentEntity = deleted.getEntity(entityClass, parentId);
                }
            }
        }
        return parentEntity;
    }

    /**
     * Loads the entity with the given UUID.
     * This method loads the parent hierarchy of the entity, too, if available.
     *
     * @param entityClass  the class the entity belongs to.
     * @param uuid  the unique identifier of the entity.
     *
     * @return  the entity, or <code>null</code> if the requested EntityBase could not be found.
     */
    @Override
    public <T extends EntityBase> T loadEntity(Class<T> entityClass, UUID uuid) {
        if (xstreamPersistence == null) {
            LOG.warn("Cannot load entity " + entityClass + "/" + uuid + ": StorageService not available");
            return null;
        }
        T entity = null;
        try {
            entity = xstreamPersistence.loadEntity((Class<T>) entityClass, uuid,
                    getEntityClassLoaders(),
                    getMigrations(), getAliases());
        } catch (StorageException e) {
            new RuntimeException(e);
        } catch (ValidationException e) {
            new RuntimeException(e);
        }

        if (entity == null) {
            return null;
        }

        resolveParentEntity(entityClass, entity);
        updateParentEntityInCache(entityClass, uuid, entity);

        return entity;
    }

    private <T extends EntityBase> void updateParentEntityInCache(Class<T> entityClass, UUID uuid, T entity) {
        // update the parentEntity of all direct children
        for (T childEntity : cache.getEntities(entityClass)) {
            if (uuid.equals(childEntity.getParentEntityId())) {
                childEntity.setParentEntity(entity);
            }
        }
    }

    private <T extends EntityBase> void resolveParentEntity(Class<T> entityClass, T entity) {
        UUID parentId = entity.getParentEntityId();
        if (parentId != null) {
            EntityBase parentEntity = null;
            parentEntity = getParentEntity(entityClass, entity);
            if (parentEntity == null) {
                // Fallback: try to load it
                parentEntity = loadEntity(entityClass, parentId);
            }
            if (parentEntity == null) {
                throw new RuntimeException(MessageFormat.format("Parent entity {0} does not exist", parentId));
            }
            entity.setParentEntity(parentEntity);
        }
    }

    @Override
    public <T extends EntityBase> T getEntity(Class<T> entityClass, UUID uuid) {
        loadModel(entityClass);
        return cache.getEntity(entityClass, uuid);
    }

    @Override
    public <T extends EntityBase> List<T> getEntities(Class<T> entityClass) {
        loadModel(entityClass);
        return cache.getEntities(entityClass);
    }

    @Override
    public <T extends EntityBase> int size(Class<T> entityClass) {
        loadModel(entityClass);
        return cache.size(entityClass);
    }

    @Override
    public <T extends EntityBase> T getEntity(Class<T> entityClass, EntityFilter<T> filter) {
        loadModel(entityClass);
        return cache.getEntity(entityClass, filter);
    }

    @Override
    public <T extends EntityBase> T getDeletedEntity(Class<T> entityClass, UUID uuid) {
        loadModel(entityClass);
        return deleted.getEntity(entityClass, uuid);
    }

    @Override
    public <T extends EntityBase> List<T> getDeletedEntities(Class<T> entityClass) {
        loadModel(entityClass);
        return deleted.getEntities(entityClass);
    }

    @Override
    public <T extends EntityBase> void refresh(Class<T> entityClass) {
        cache.clearAll(entityClass);
        deleted.clearAll(entityClass);
        loadModel(entityClass);
    }

    @Override
    public void refreshAll() {
        Set<Class<? extends EntityBase>> entityClasses = cache.getEntityTypes();
        entityClasses.addAll(deleted.getEntityTypes());
        cache.clearAll();
        deleted.clearAll();
        for (Class<? extends EntityBase> entityClass : entityClasses) {
            loadModel(entityClass);
        }
    }
}
