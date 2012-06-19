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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.model.ValidationException;
import org.eclipse.skalli.services.extension.DataMigration;
import org.eclipse.skalli.services.persistence.PersistenceService;
import org.eclipse.skalli.services.validation.Validation;
import org.eclipse.skalli.services.validation.ValidationService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base implementation of an entity service.
 *
 * Derived classes must implement the {@link #getEntityClass()} method returning the class
 * of the supported entity type and should provide a validation mechanism by overwriting
 * {@link #validateEntity(EntityBase, Severity)}.
 * <p>
 * <b>IMPORTANT NOTE:</b><br>
 * Derived classes must declare the following &lt;reference&gts; in their service component descriptors:
 * <pre>
 *    &lt;reference
 *        name="PersistenceService"
 *        cardinality="1..1"
 *        interface="org.eclipse.skalli.services.persistence.PersistenceService"
 *        policy="dynamic"
 *        bind="bindPersistenceService"
 *        unbind="unbindPersistenceService" /&gt
 *    &lt;<reference
 *        name="ValidationService"
 *        interface="org.eclipse.skalli.services.validation.ValidationService"
 *        cardinality="0..1"
 *        policy="dynamic" /&gt;
 * </pre>
 */
public abstract class EntityServiceBase<T extends EntityBase> implements EntityService<T> {

    private static final Logger LOG = LoggerFactory.getLogger(EntityServiceBase.class);

    private ComponentContext context;
    private PersistenceService persistenceService;

    protected void activate(ComponentContext context) {
        this.context = context;
    }

    protected void deactivate(ComponentContext context) {
        this.context = null;
    }

    /**
     * Locates and binds the given service to the entity service.
     *
     * @param service  the service class to locate and bind.
     * @param name  the name of the service as defined in the component descriptor.
     *
     * @return  an instance of the rquested service, or <code>null</code>.
     */
    protected <S> S getService(Class<S> service, String name) {
        return context != null? service.cast(context.locateService(name)) : null;
    }

    /**
     * Updates the search index.
     *
     * @param entity  the entity to pass to the search service.
     */
    protected void updateSearchIndex(T entity) {
    };

    /**
     * Binds the required persistence service.
     */
    public void bindPersistenceService(PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    /** Unbinds the required persistence service */
    public void unbindPersistenceService(PersistenceService persistenceService) {
        this.persistenceService = null;
    }

    /**
     * Returns the (required) persistence service the entity service should use
     * to retrieve or store entities.
     *
     * @throws {@link java.lang.IllegalStateException} if no persistence
     * service is registered.
     */
    protected PersistenceService getPersistenceService() {
        if (persistenceService == null) {
            throw new IllegalStateException(MessageFormat.format(
                    "ERROR: No implementation for required service {0} registered", PersistenceService.class.getName()));
        }
        return persistenceService;
    }

    /**
     * Ensures that an entity can be persisted. Throws <code>ValidationException</code>
     * if the entity has {@link Severity#FATAL} issues.
     *
     * @param entity  the entity to validate.
     *
     * @throws ValidationException  if the entity has fatal issues that prevents
     * the entity from being persisted.
     */
    protected abstract void validateEntity(T entity) throws ValidationException;

    /**
     * Validates an entity with its default validators.
     *
     * @param entity  the entity to validate.
     * @param minSeverity  the minimal severity of issues to report.
     * @return a set of issues, or an empty set.
     */
    protected abstract SortedSet<Issue> validateEntity(T entity, Severity minSeverity);

    @Override
    public Set<UUID> keySet() {
        return getPersistenceService().keySet(getEntityClass());
    }

    @Override
    public T getByUUID(UUID uuid) {
        return getPersistenceService().getEntity(getEntityClass(), uuid);
    }

    @Override
    public List<T> getAll() {
        return getPersistenceService().getEntities(getEntityClass());
    }

    @Override
    public int size() {
        return getPersistenceService().size(getEntityClass());
    }

    @Override
    public synchronized void persist(T entity, String userId) throws ValidationException {
        if (entity.getUuid() == null) {
            entity.setUuid(UUID.randomUUID());
        }
        validateEntity(entity);
        getPersistenceService().persist(entity, userId);
        updateSearchIndex(entity);

        ValidationService validationService = getService(ValidationService.class, "ValidationService"); //$NON-NLS-1$
        if (validationService != null) {
            validationService.queue(new Validation<T>(getEntityClass(), entity.getUuid(),
                    Severity.INFO, userId, Validation.IMMEDIATE));
        }
    }

    @Override
    public T loadEntity(Class<T> entityClass, UUID uuid) {
        return getPersistenceService().loadEntity(entityClass, uuid);
    }

    @Override
    public Map<String, Class<?>> getAliases() {
        return new HashMap<String, Class<?>>();
    }

    @Override
    public Set<ClassLoader> getClassLoaders() {
        return new HashSet<ClassLoader>();
    }

    @Override
    public Set<DataMigration> getMigrations() {
        return  new HashSet<DataMigration>();
    }

    @Override
    public SortedSet<Issue> validate(T entity, Severity minSeverity) {
        SortedSet<Issue> issues = new TreeSet<Issue>();
        issues.addAll(validateEntity(entity, minSeverity));
        validateIssues(entity.getUuid(), minSeverity, issues);
        return issues;
    }

    @Override
    public SortedSet<Issue> validateAll(Severity minSeverity) {
        SortedSet<Issue> issues = new TreeSet<Issue>();
        for (T entity : getAll()) {
            issues.addAll(validateEntity(entity, minSeverity));
            validateIssues(entity.getUuid(), minSeverity, issues);
        }
        return issues;
    }

    /**
     * Validates the given set of issues. Ensures that all entries apply to the given
     * <code>entityId</code> and have a severity equal or greater than the given
     * <code>minSeverity</code>. Removes invalid entries from the issue set and logs
     * the cause. Sets the timestamp of the issues to the current system time unless
     * already defined.
     *
     * @param entityId  the unique identifier of the entity that caused the given issues.
     * @param minSeverity  the minimal severity of issues to report.
     * @param issues  set of issues to validate.
     */
    protected void validateIssues(UUID entityId, Severity minSeverity, SortedSet<Issue> issues) {
        ArrayList<Issue> invalidIssues = new ArrayList<Issue>();
        for (Issue issue : issues) {
            // don't accept issues for other entities
            if (!issue.getEntityId().equals(entityId)) {
                invalidIssues.add(issue);
                LOG.warn(MessageFormat.format("Invalid issue detected (requested entity={0} but found entity={1})",
                        entityId, issue.getEntityId()));
            }
            // we cannot guarantee that 3rd-party validators honor minSeverity, so
            // to be sure we filter out issues with a severity less than minSeverity
            if (minSeverity.compareTo(issue.getSeverity()) < 0) {
                invalidIssues.add(issue);
                LOG.warn(MessageFormat.format(
                        "Invalid issue detected (requested minSeverity={0} but found severity={1})", minSeverity,
                        issue.getSeverity()));
            }
            // if the issue has no timestamp, set the current system time
            long timestamp = issue.getTimestamp();
            if (timestamp <= 0) {
                issue.setTimestamp(System.currentTimeMillis());
            }
        }
        // remove invalid issues from the result and log them
        for (Issue invalidIssue : invalidIssues) {
            issues.remove(invalidIssue);
        }
    }
}
