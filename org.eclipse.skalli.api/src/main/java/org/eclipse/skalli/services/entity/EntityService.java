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
package org.eclipse.skalli.services.entity;

import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;

import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.model.ExtensibleEntityBase;
import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.model.ValidationException;
import org.eclipse.skalli.services.extension.DataMigration;

/**
 * Interface for a service that manages and provides {@link EntityBase entities},
 * e.g. {@link Project projects}.
 * <p>
 * Note that entities provided by an entity service in general are shared objects and
 * should be treated in a read-only fashion. Changing properties of such entities may have
 * undesirable side effects. In order to change an entity, an exlusive
 * instance must be obtained with {@link #loadEntity(Class, UUID)}.
 */
public interface EntityService<T extends EntityBase> {

    /**
     * Returns the class of entities supported by this service.
     */
    public Class<T> getEntityClass();

    /**
     * Returns the current model version of entities supported by
     * this service. If the entity class is {@link ExtensibleEntityBase extensible}
     * the returned model version applies only to the entity, but not
     * necessarily to the extensions of the entity. The latter may have
     * an own versioning scheme.
     * <p>
     * Note, when incrasing the model version, an entity service should
     * provide a {@link #getMigrations() migration} from older model versions
     * to the new model version.
     *
     * @return the current model version starting with 0.
     */
    public int getModelVersion();

    /**
     * Returns the entity with the given unique identifier.
     *
     * @param uuid  the unique identifier of the entity.
     *
     * @return  the entity instance for the given unique identifier, or <code>null</code>
     * if no such entity exists.
     */
    public T getByUUID(UUID uuid);

    /**
     * Returns all existing entities.
     * <p>
     * Note that the list of entities returned by this method can change without notice,
     * e.g. when properties of an entry are changed or whole entities are deleted.
     * This may lead to surprising effects especially when iterating through the list
     * (see {@link ConcurrentModificationException}).
     *
     * @returns a list of entities, or an empty list.
     */
    public List<T> getAll();

    /**
     * Returns the overall number of entities.
     * @return the number of entities {@link #getAll()} would return.
     */
    public int size();

    /**
     * Returns the unique identifiers of all existing entities.
     * @return a set of unique identifiers, or an empty set.
     */
    public Set<UUID> keySet();

    /**
     * Persists the given entity.
     *
     * @param entity
     *          the entity to persist.
     * @param userId
     *          unique identifier of the user performing the modification
     *          (relevant for the audit trail).
     * @throws ValidationException
     *           if the entity could not be persisted because of {@link Severity#FATAL}
     *           validation issues.
     */
    public void persist(T entity, String userId) throws ValidationException;

    /**
     * Loads the entity with the given unique identifier from storage.
     * <p>
     * The chain of {@link EntityBase#getParentEntity() parent entities} of the entity is resolved and
     * loaded from storage if necessary, but neither the {@link EntityBase#getFirstChild() children}
     * nor {@link EntityBase#getNextSibling() siblings} of the entity are resolved.
     * <p>
     * This method returns a fresh instance of the entity, which excusively belongs to the caller.
     * It can safely be changed and persisted afterwards.
     * <p>
     * Note that the entities in the parent hierarchy are in general shared objects and should be
     * treated in a read-only fashion. Changing properties of parent entities may have undesirable
     * side effects.
     *
     * @param entityClass  the class the entity belongs to.
     * @param uuid  the unique identifier of the entity.
     *
     * @return  the entity, or <code>null</code> if there is no persisted entity
     * with the given unique identifier.
     */
    public T loadEntity(Class<T> entityClass, UUID uuid);

    /**
     * Returns aliases for <b>additional</b> classes used to defined the data model
     * of the entities. Defining aliases makes marshaling/unmarshaling of the entities
     * independent of the concrete class names.
     *
     * @return a map which maps aliases to classes, or an empty map.
     */
    public Map<String, Class<?>> getAliases();

    /**
     * Returns classloaders for <b>additional</b> classes used to defined the data model
     * of the entities. The persistence service adds the classloader of the entity
     * service by default, so classes contained in the same bundle as the entity service
     * do not need a custom classloader.
     *
     * @return a set of classloaders, or an empty set.
     */
    public Set<ClassLoader> getClassLoaders();

    /**
     * Returns a set of data migrators used to migrate persisted
     * instances of the model entities.
     *
     * @return a set of migrations, or an empty set.
     */
    public Set<DataMigration> getMigrations();

    /**
     * Validates the given entity.
     * <p>
     * Checks whether the entity has validation issues equal to or more serious
     * than the given severity. The result set is sorted according to {@link Issue#compareTo(Issue)}.
     *
     * @param entity  the entity to validate.
     * @param minSeverity  the minimal severity of issues to report.
     *
     * @return a set of issues, or an empty set.
     */
    public SortedSet<Issue> validate(T entity, Severity minSeverity);

    /**
     * Validates all entities.
     * <p>
     * Checks whether the entity has validation issues equal to or more serious
     * than the given severity. The result set is sorted according to {@link Issue#compareTo(Issue)}.
     *
     * @param minSeverity the minimal severity of issues to report.
     * @return a set of issues, or an empty set.
     */
    public SortedSet<Issue> validateAll(Severity minSeverity);
}
