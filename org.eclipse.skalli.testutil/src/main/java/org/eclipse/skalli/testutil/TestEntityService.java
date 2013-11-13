package org.eclipse.skalli.testutil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.model.EntityFilter;
import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Issuer;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.model.ValidationException;
import org.eclipse.skalli.services.entity.EntityService;
import org.eclipse.skalli.services.extension.DataMigration;

/**
 * A mapped-based {@link EntityService} implementation for test.
 * This helper class is a stub that implements the accessor methods
 * and the valiadtion methods, but not {@link #persist(EntityBase, String)}.
 * {@link #loadEntity(Class, UUID)} always returns a new entity instance
 * and initialized with the given UUID.
 *
 * @param <T> the type of the entity
 */
public class TestEntityService<T extends EntityBase> implements EntityService<T>, Issuer {

    private Class<T> entityClass;
    private Map<UUID,T> entities;
    private Map<UUID, SortedSet<Issue>> issues;

    public TestEntityService(Class<T> entityClass, Collection<T> c) {
        this(entityClass, c, null);
    }

    public TestEntityService(Class<T> entityClass, Collection<T> c, Map<UUID, SortedSet<Issue>> issues) {
        entities = new HashMap<UUID,T>();
        for (T entity: c) {
            entities.put(entity.getUuid(), entity);
        }
        this.entityClass = entityClass;
        this.issues = issues;
    }

    @Override
    public Class<T> getEntityClass() {
        return entityClass;
    }

    @Override
    public int getModelVersion() {
        return 1;
    }

    @Override
    public T getByUUID(UUID uuid) {
        return entities.get(uuid);
    }

    @Override
    public List<T> getAll() {
        return getAll(null);
    }

    @Override
    public List<T> getAll(EntityFilter<T> filter) {
        ArrayList<T> all = new ArrayList<T>();
        for (T next: entities.values()) {
            if (filter == null || filter.accept(entityClass, next)) {
                all.add(next);
            }
        }
        return all;
    }

    @Override
    public int size() {
        return entities.size();
    }

    @Override
    public Set<UUID> keySet() {
        return entities.keySet();
    }

    @Override
    public void persist(T entity, String userId) throws ValidationException {
    }

    @Override
    public T loadEntity(Class<T> entityClass, UUID uuid) {
        T entity;
        try {
            entity = entityClass.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        entity.setUuid(uuid);
        return entity;
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
        SortedSet<Issue> result = new TreeSet<Issue>();
        if (issues != null && issues.containsKey(entity.getUuid())) {
            for (Issue issue: issues.get(entity.getUuid())) {
                if (minSeverity.compareTo(issue.getSeverity()) >= 0) {
                    result.add(issue);
                }
            }
        }
        return result;
    }

    @Override
    public SortedSet<Issue> validateAll(Severity minSeverity) {
        SortedSet<Issue> result = new TreeSet<Issue>();
        if (issues != null) {
            for (SortedSet<Issue> next: issues.values()) {
                for (Issue issue: next) {
                    if (minSeverity.compareTo(issue.getSeverity()) >= 0) {
                        result.add(issue);
                    }
                }
            }
        }
        return result;
    }
}
