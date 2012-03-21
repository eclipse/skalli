package org.eclipse.skalli.services.permit;

import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Sorted set-like collection of {@link Permit permits}.
 *
 * This collection behaves similar to a {@link SortedSet}, i.e. it sorts entries
 * according to {@link Permit#compareTo(Permit)} and it contains no duplicate entries,
 * but its {@link #add(Permit)} and {@link #addAll(Collection)} methods do not
 * fulfill the usual contract for a set:  A {@link Permit permit} that equals an
 * already existing permit <b>replaces</b> the existing permit. In contrast, ordinary
 * sets simply ignore the new entry and keep the existing.
 */
public class PermitSet implements Collection<Permit>, Iterable<Permit> {

    private TreeSet<Permit> permits = new TreeSet<Permit>();

    /**
     * Creates an empty permit collection.
     */
    public PermitSet() {
    }

    /**
     * Creates a permit collection and add the given permits to it.
     * @param permits  a collection of permits.
     */
    public PermitSet(Collection<? extends Permit> permits) {
        addAll(permits);
    }

    /**
     * Creates a permit collection and add the given permits to it.
     * @param permits  a list of permits.
     */
    public PermitSet(Permit...permits) {
        if (permits != null) {
            for (Permit permit: permits) {
                add(permit);
            }
        }
    }

    /**
     * Adds the given permit to this collection replacing an already present permit
     * with the same {@link Permit#getAction() action} and  {@link Permit#getPath() path}.
     */
    @Override
    public boolean add(Permit permit) {
        boolean modified = permits.remove(permit);
        modified |= permits.add(permit);
        return modified;
    }

    /**
     * Adds the given permits to this collection replacing already present permits
     * with same {@link Permit#getAction() action} and  {@link Permit#getPath() path}
     * attributes.
     */
    @Override
    public boolean addAll(Collection<? extends Permit> permits) {
        boolean modified = false;
        if (permits != null) {
            for (Permit permit: permits) {
                if (add(permit)) {
                    modified = true;
                }
            }
        }
        return modified;
    }

    @Override
    public int size() {
        return permits.size();
    }

    @Override
    public boolean isEmpty() {
        return permits.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return permits.contains(0);
    }

    @Override
    public Iterator<Permit> iterator() {
        return permits.iterator();
    }

    @Override
    public Object[] toArray() {
        return permits.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return permits.toArray(a);
    }

    @Override
    public boolean remove(Object o) {
        return permits.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return permits.containsAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return permits.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return permits.retainAll(c);
    }

    @Override
    public void clear() {
        permits.clear();
    }
}
