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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.ComparatorUtils;
import org.eclipse.skalli.core.storage.FileStorageComponent;
import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.model.EntityFilter;
import org.eclipse.skalli.services.BundleProperties;
import org.eclipse.skalli.services.entity.EntityService;
import org.eclipse.skalli.services.entity.EntityServices;
import org.eclipse.skalli.services.extension.MigrationException;
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
        UUID entityId = entity.getUuid();
        if (entityId == null) {
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

        EntityBase oldEntity = getCachedEntity(entityClass, entityId);
        try {
            xstreamPersistence.saveEntity(entityService, entity, userId,
                    getAliases(entityClass), getConverters(entityClass));
        } catch (StorageException e) {
            throw new RuntimeException(e);
        } catch (MigrationException e) {
            throw new RuntimeException(e);
        }

        // reload the entity to proof that is has been persisted successfully;
        // if so, adjust the parent/child relations of entity and put it into the cache.
        if (loadEntity(entityClass, entityId) != null) {
            adjustEntityRelations(entityClass, oldEntity, entity);
            updateCache(entity);
            if (entity.isDeleted()) {
                AUDIT_LOG.info(MessageFormat.format("Entity {0} of type ''{1}'' has been deleted by user ''{2}''",
                        entityId, entityClass.getSimpleName(), userId));
            } else {
                AUDIT_LOG.info(MessageFormat.format("Entity {0} of type ''{1}'' has been changed by user ''{2}''",
                        entityId, entityClass.getSimpleName(), userId));
            }
        } else {
            throw new RuntimeException(MessageFormat.format("Failed to save entity {0} of type {1}",
                    entity, entityClass.getName()));
        }
    }

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

        // resolve the parent chain; if necessary, load missing entities from storage
        if (entity != null) {
            T parentEntity = getParentChain(entityClass, entity);
            if (parentEntity != null) {
                entity.setParentEntity(parentEntity);
            }
        }
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
    public <T extends EntityBase> List<T> getEntities(Class<T> entityClass, EntityFilter<T> filter) {
        loadModel(entityClass);
        return cache.getEntities(entityClass, filter);
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
     * Loads all entities of a given class from storage.
     *
     * Resolves the parent/child hierarchy of the loaded entities and stores the
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
        for (EntityBase loadedEntity : loadedEntities) {
            updateCache(loadedEntity);
        }
        resolveEntityRelations(entityClass);
    }

    <T extends EntityBase> T getCachedEntity(Class<T> entityClass, UUID uuid) {
        T entity = cache.getEntity(entityClass, uuid);
        if (entity == null) {
            entity = deleted.getEntity(entityClass, uuid);
        }
        return entity;
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
     * Resolves all parent/child relations between entities of the
     * given class (separately for deleted and non-deleted entities!).
     * This method assumes that the caches have already been filled from
     * storage so that all referenced parents/children/siblings of any
     * entity in the cache can be resolved without loading additional
     * data from storage.
     * <p>
     * This method is package protected for testing purposes.
     *
     * @param entityClass  the class of entities to resolve relations for.
     */
    <T extends EntityBase> void resolveEntityRelations(Class<T> entityClass) {
        resolveEntityRelations(entityClass, cache.getEntities(entityClass));
        resolveEntityRelations(entityClass, deleted.getEntities(entityClass));
    }

    /**
     * Resolves all parent/child relations between entities in a
     * given collection. This method assumes that the collection of entities
     * is "self contained", i.e. all referenced parents/children/siblings
     * are also contained in the given list.
     * <p>
     * This method is package protected for testing purposes.
     *
     * @param entityClass  the class of entities to resolve relations for.
     * @param entities the entities to resolve relations for.
     */
    <T extends EntityBase> void resolveEntityRelations(Class<T> entityClass, Collection<T> entities) {
        for (T entity : entities) {
            resolveEntityRelations(entityClass, entity);
        }
    }

    /**
     * Determines the parent of the given entity and inserts the entity as
     * child of that parent. This method assumes that the parent is already in the cache.
     * <p>
     * This method is package protected for testing purposes.
     *
     * @param entityClass  the class of the entity.
     * @param entity  the entity to resolve.
     */
    <T extends EntityBase> void resolveEntityRelations(Class<T> entityClass, EntityBase entity) {
        T parentEntity = getParentEntity(entityClass, entity);
        entity.setParentEntity(parentEntity);
        if (parentEntity != null) {
            insertChildEntity(parentEntity, entity);
        }
    }

    /**
     * Adjusts the parent/child relations of an entity after it has been changed
     * or created. If the parent changed or the deleted flag has been switched,
     * the entity is removed from the old parent (if any) and assigned to
     * the new parent (if any). Furthermore, the children of the old entity
     * are assigned to the new entity.
     *
     * @param entityClass  the class of the entity.
     * @param oldEntity  the old entity instance, or <code>null</code> if the entity
     * did not exist before.
     * @param newEntity  the new entity instance, never <code>null</code>.
     */
    <T extends EntityBase> void adjustEntityRelations(Class<T> entityClass,
            EntityBase oldEntity, EntityBase newEntity) {
        T newParent = getParentEntity(entityClass, newEntity);
        if (oldEntity != null) {
            reassignChildren(oldEntity, newEntity);
            T oldParent = getParentEntity(entityClass, oldEntity);
            if (!ComparatorUtils.equals(oldParent, newParent)
                    || oldEntity.isDeleted() != newEntity.isDeleted()) {
                removeChildEntity(oldParent, oldEntity);
            }
        }
        insertChildEntity(newParent, newEntity);
    }

    /**
     * Re-assigns the children of the oldEntity to the newEntity, i.e.
     * iterates through the children of oldEntity and sets the parent
     * pointer to newEntity. Furthermore, the firstChild pointer of
     * newEntity is assigned from oldEntity.
     *
     * @param oldEntity  the old entity.
     * @param newEntity  the new entity.
     */
    void reassignChildren(EntityBase oldEntity, EntityBase newEntity) {
        EntityBase next = oldEntity.getFirstChild();
        newEntity.setFirstChild(next);
        while (next != null) {
            next.setParentEntity(newEntity);
            next = next.getNextSibling();
        }
    }

    /**
     * Assigns a child entity to a given parent entity.
     * <p>
     * If there was no child yet, insert the entity as first child.
     * If an entity with the same uuid was already in the list of children,
     * replace the entity with the new value. Otherwise append the entity
     * to the end of the siblings chain. If the parent entity is <code>null</code>,
     * or parent and child have different deleted flags, the method does nothing.
     * <p>
     * This method is package protected for testing purposes.
     *
     * @param parentEntity  the parent entity, or <code>null</code>.
     * @param entity the entity to replace or append.
     */
    void insertChildEntity(EntityBase parentEntity, EntityBase entity) {
        if (parentEntity == null) {
            return;
        }
        entity.setParentEntity(parentEntity);
        if (parentEntity.isDeleted() != entity.isDeleted()) {
            return;
        }
        EntityBase next = parentEntity.getFirstChild();
        if (next == null) {
            parentEntity.setFirstChild(entity);
            return;
        }
        EntityBase prev = null;
        while (next != null) {
            if (next.equals(entity)) {
                if (prev != null) {
                    prev.setNextSibling(entity);
                } else {
                    parentEntity.setFirstChild(entity);
                }
                entity.setNextSibling(next.getNextSibling());
                return;
            }
            prev = next;
            next = next.getNextSibling();
        }
        prev.setNextSibling(entity);
    }

    /**
     * Removes a child entity from a given parent entity.
     * <p>
     * If the parent entity has no children, or there is no entity
     * with the same uuid among the children, this method does nothing.
     * Otherwise the child entity matching the uuid is removed and the
     * siblings chain is adjusted. If the parent entity is <code>null</code>,
     * the method does nothing.
     * <p>
     * This method is package protected for testing purposes.
     *
     * @param parentEntity  the parent entity, or <code>null</code>.
     * @param entity  the entity to remove.
     */
    void removeChildEntity(EntityBase parentEntity, EntityBase entity) {
        if (parentEntity == null) {
            return;
        }
        EntityBase next = parentEntity.getFirstChild();
        if (next == null) {
            return;
        }
        EntityBase prev = null;
        while (next != null) {
            if (next.equals(entity)) {
                if (prev != null) {
                    prev.setNextSibling(next.getNextSibling());
                } else {
                    parentEntity.setFirstChild(next.getNextSibling());
                }
                next.setNextSibling(null);
                return;
            }
            prev = next;
            next = next.getNextSibling();
        }
    }

    /**
     * Loads the whole chain of parent entities from storage (if neccessary),
     * and returns the parent entity of the given entity.
     * <p>
     * This method is package protected for testing purposes.
     *
     * @param entityClass  the class of the entity.
     * @param entity  the entity for which to lookup the parent.
     *
     * @return  the parent entity, or <code>null</code> if the entity has no parent, the parent
     * is deleted but the entity is not, or the parent entity could not be read from storage.
     */
    <T extends EntityBase> T getParentChain(Class<T> entityClass, EntityBase entity) {
        T parentEntity = null;
        UUID parentId = entity.getParentEntityId();
        if (parentId != null) {
            parentEntity = getParentEntity(entityClass, entity);
            if (parentEntity == null) {
                parentEntity = loadEntity(entityClass, parentId);
                if (parentEntity == null) {
                    LOG.warn(MessageFormat.format(
                            "Entity {0} references entity {1} as parent entity but there is no such entity",
                            entity.getUuid(), parentId));
                    return null;
                }
            }
            if (parentEntity.isDeleted() && !entity.isDeleted()) {
                LOG.warn(MessageFormat.format(
                        "Entity {0} cannot reference deleted entity {1} as parent entity",
                        entity.getUuid(), parentId));
                return null;
            }
        }
        return parentEntity;
    }

    /**
     * Returns the parent entity of the given entity from the cache: If the given entity is
     * {@link EntityBase#isDeleted() deleted} the lookup is performed in the cache of
     * deleted entities. For all other entities the lookup is performed first in
     * the cache of non-deleted entities. If there is no match, the lookup is repeated
     * in the cache of deleted entities.
     * <p>
     * This method is package protected for testing purposes.
     *
     * @param entityClass  the class of the entity.
     * @param entity  the entity for which to lookup the parent.
     *
     * @return  the parent entity, or <code>null</code> if the parent entity is not in the caches
     * or the entity has no parent.
     */
    <T extends EntityBase> T getParentEntity(Class<T> entityClass, EntityBase entity) {
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
}
