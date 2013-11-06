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
package org.eclipse.skalli.core.persistence;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.core.storage.FileStorageComponent;
import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.services.BundleProperties;
import org.eclipse.skalli.services.entity.EntityService;
import org.eclipse.skalli.services.entity.EntityServices;
import org.eclipse.skalli.services.extension.MigrationException;
import org.eclipse.skalli.services.persistence.EntityFilter;
import org.eclipse.skalli.services.persistence.PersistenceService;
import org.eclipse.skalli.services.persistence.StorageException;
import org.eclipse.skalli.services.persistence.StorageService;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link PersistenceService} based on XStream.
 */
public class XStreamPersistenceComponent extends PersistenceServiceBase implements PersistenceService {

    private static final Logger LOG = LoggerFactory.getLogger(XStreamPersistenceComponent.class);
    private static final Logger AUDIT_LOG = LoggerFactory.getLogger("audit"); //$NON-NLS-1$

    private final EntityCache cache = new EntityCache();
    private final EntityCache deleted = new EntityCache();

    private XStreamPersistence xstreamPersistence;
    private String storageServiceClassName;

    protected void activate(ComponentContext context) {
        LOG.info(MessageFormat.format("[PersistenceService][xstream] {0} : activated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void deactivate(ComponentContext context) {
        xstreamPersistence = null;
        cache.clearAll();
        deleted.clearAll();
        LOG.info(MessageFormat.format("[PersistenceService][xstream] {0} : deactivated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
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
     * Creates a <code>XStreamPersistenceComponent</code> for the storage service specified
     * with the {@link BundleProperties#PROPERTY_STORAGE_SERVICE} bundle property.
     */
    public XStreamPersistenceComponent() {
        storageServiceClassName = BundleProperties.getProperty(BundleProperties.PROPERTY_STORAGE_SERVICE,
                FileStorageComponent.class.getName());
    }

    /**
     * Creates a <code>XStreamPersistenceComponent</code> for a dedicated storage service.
     * <p>
     * This constructor is package protected for testing purposes.
     */
    XStreamPersistenceComponent(StorageService storageService) {
        xstreamPersistence = new XStreamPersistence(storageService);
    }

    @Override
    public synchronized <T extends EntityBase> void persist(Class<T> entityClass, EntityBase entity, String userId) {
        if (entity == null) {
            throw new IllegalArgumentException("argument 'entity' must not be null");
        }
        if (StringUtils.isBlank(userId)) {
            throw new IllegalArgumentException("argument 'userId' must not be null or an empty string");
        }

        if (xstreamPersistence == null) {
            LOG.warn(MessageFormat.format("Cannot persist entity {0}: StorageService not available", entity));
            return;
        }

        // load all project models
        loadModel(entityClass);

        // generate unique id
        if (entity.getUuid() == null) {
            entity.setUuid(UUID.randomUUID());
        }

        // verify parent is known
        // TODO should be in EntitySeriviceImpl#validate
        if (entity.getParentEntityId() != null) {
            UUID parentUUID = entity.getParentEntityId();
            EntityBase parent = getParentEntity(entityClass, entity);
            if (parent == null) {
                throw new RuntimeException(MessageFormat.format("Parent entity {0} does not exist", parentUUID));
            }
        }

        EntityService<?> entityService = EntityServices.getByEntityClass(entityClass);
        if (entityService == null) {
            LOG.warn(MessageFormat.format(
                    "Cannot persist entity {0}:  No entity service registered for entities of type {1}",
                    entity.getUuid(), entityClass.getName()));
            return;
        }
        try {
            xstreamPersistence.saveEntity(entityService, entity, userId,
                    getAliases(entityClass), getConverters(entityClass));
        } catch (StorageException e) {
            throw new RuntimeException(e);
        } catch (MigrationException e) {
            throw new RuntimeException(e);
        }

        // reload the entity to proof that is has been persisted successfully;
        // if so, update the caches
        T savedEntity = loadEntity(entityClass, entity.getUuid());
        if (savedEntity != null) {
            updateCache(savedEntity);
            if (entity.isDeleted()) {
                AUDIT_LOG.info(MessageFormat.format("Entity {0} of type ''{1}'' has been deleted by user ''{2}''",
                        entity.getUuid(), entityClass.getSimpleName(), userId));
            } else {
                AUDIT_LOG.info(MessageFormat.format("Entity {0} of type ''{1}'' has been changed by user ''{2}''",
                        entity.getUuid(), entityClass.getSimpleName(), userId));
            }
        } else {
            throw new RuntimeException(MessageFormat.format("Failed to save entity {0} of type {1}",
                    entity, entityClass.getName()));
        }
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
            LOG.warn(MessageFormat.format("Cannot load entity {0}/{1}: StorageService not available", entityClass, uuid));
            return null;
        }
        EntityService<T> entityService = EntityServices.getByEntityClass(entityClass);
        if (entityService == null) {
            LOG.warn(MessageFormat.format("No entity service registered for entities of type {0}", entityClass.getName()));
            return null;
        }
        T entity = null;
        try {
            entity = xstreamPersistence.loadEntity(entityService, uuid.toString(), getClassLoaders(entityClass),
                    getMigrations(entityClass), getAliases(entityClass), getConverters(entityClass));
        } catch (StorageException e) {
            LOG.warn(MessageFormat.format("Cannot load entity {0}/{1} of type {2}):",
                    entityClass, uuid, entityClass.getName()), e);
        } catch (MigrationException e) {
            LOG.warn(MessageFormat.format("Cannot load entity {0}/{1} of type {2}):",
                    entityClass, uuid, entityClass.getName()), e);
        }

        if (entity == null) {
            return null;
        }
        registerEntityClass(entityClass);
        resolveParentEntity(entityClass, entity);
        updateParentEntityInCache(entityClass, uuid, entity);

        return entity;
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
    public <T extends EntityBase> Set<UUID> keySet(Class<T> entityClass) {
        loadModel(entityClass);
        return cache.keySet(entityClass);
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
    public <T extends EntityBase> Set<UUID> deletedSet(Class<T> entityClass) {
        loadModel(entityClass);
        return deleted.keySet(entityClass);
    }

    @Override
    public <T extends EntityBase> void refresh(Class<T> entityClass) {
        cache.clearAll(entityClass);
        deleted.clearAll(entityClass);
        loadModel(entityClass);
    }

    @Override
    public void refreshAll() {
        Set<Class<? extends EntityBase>> entityClasses = new HashSet<Class<? extends EntityBase>>();
        entityClasses.addAll(cache.getEntityTypes());
        entityClasses.addAll(deleted.getEntityTypes());
        cache.clearAll();
        deleted.clearAll();
        for (Class<? extends EntityBase> entityClass : entityClasses) {
            loadModel(entityClass);
        }
    }

    /**
     * Loads all entities of the given class.
     *
     * Resolves the parent hierarchy of the loaded entities and stores the
     * result in the model caches (deleted entities in {@link #deleted},
     * all others in {@link #cache}).
     * <p>
     * This method is package protected for testing purposes.
     *
     * @param entityClass   the class of the entities to load.
     */
    synchronized <T extends EntityBase> void loadModel(Class<T> entityClass) {
        if (cache.size(entityClass) > 0) {
            //nothing to do, all entities are already loaded in the cache :-)
            return;
        }
        if (xstreamPersistence == null) {
            LOG.warn(MessageFormat.format("Cannot load entities of type {0}: StorageService not available", entityClass));
            return;
        }
        EntityService<T> entityService = EntityServices.getByEntityClass(entityClass);
        if (entityService == null) {
            LOG.warn(MessageFormat.format("No entity service registered for entities of type {0}", entityClass.getName()));
            return;
        }
        registerEntityClass(entityClass);

        List<T> loadedEntities;
        try {
            loadedEntities = xstreamPersistence.loadEntities(entityService,
                    getClassLoaders(entityClass), getMigrations(entityClass),
                    getAliases(entityClass), getConverters(entityClass));
        } catch (StorageException e) {
            throw new RuntimeException(e);
        } catch (MigrationException e) {
            throw new RuntimeException(e);
        }
        for (EntityBase entityBase : loadedEntities) {
            updateCache(entityBase);
        }
        resolveParentEntities(entityClass);
    }

    /**
     * Registers the given entity class with the caches. An entity class must be
     * registered prior to adding an instance of the entity class to the caches.
     * <p>
     * This method is package protected for testing purposes.
     *
     * @param entityClass  the entity class to register.
     */
    <T extends EntityBase> void registerEntityClass(Class<T> entityClass) {
        if (!cache.isRegistered(entityClass)) {
            cache.registerEntityClass(entityClass);
        }
        if (!deleted.isRegistered(entityClass)) {
            deleted.registerEntityClass(entityClass);
        }
    }

    /**
     * Adds the given entity to the cache (deleted entities in {@link #deleted},
     * all other in {@link #cache}).
     * <p>
     * This method is package protected for testing purposes.
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
     * <p>
     * This method is package protected for testing purposes.
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
                    LOG.warn(MessageFormat.format(
                            "Entity {0} references entity {1} as parent entity - but there is no such entity",
                            entity.getUuid(), parentId));
                    continue;
                }
                if (parentEntity.isDeleted() && !entity.isDeleted()) {
                    LOG.warn(MessageFormat.format(
                            "Entity {0} cannot reference deleted entity {1} as parent entity",
                            entity.getUuid(), parentId));
                    continue;
                }
                entity.setParentEntity(parentEntity);
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

    private <T extends EntityBase> T getParentEntity(Class<T> entityClass, EntityBase entity) {
        T parentEntity = null;
        UUID parentId = entity.getParentEntityId();
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

    private <T extends EntityBase> void updateParentEntityInCache(Class<T> entityClass, UUID uuid, T entity) {
        for (T childEntity : cache.getEntities(entityClass)) {
            if (uuid.equals(childEntity.getParentEntityId())) {
                childEntity.setParentEntity(entity);
            }
        }
    }
}
