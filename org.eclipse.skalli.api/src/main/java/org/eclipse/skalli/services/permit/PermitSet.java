package org.eclipse.skalli.services.permit;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;

/**
 * Sorted set-like collection of {@link Permit permits}.
 *
 * This collection behaves similar to a {@link SortedSet}, i.e. it sorts entries
 * according to {@link Permit#compareTo(Permit)} and it contains no duplicate entries,
 * but its {@link #add(Permit)} and {@link #addAll(Collection)} methods do not
 * fulfill the usual contract for a set:  A {@link Permit permit} that equals an
 * already existing permit <b>replaces</b> the existing permit. In contrast, ordinary
 * sets simply ignore the new entry and keep the existing.
 * <p>
 * Note, all methods of this set use {@link Permit#equals(Object)} for comparing arguments
 * with set entries. Two permits are equal when they have the same
 * {@link Permit#getAction() action} and {@link Permit#getPath() path}
 * regardless of their {@link Permit#getLevel() levels}.
 */
public class PermitSet implements Collection<Permit>, Iterable<Permit> {

    private TreeMap<Permit,Permit> permits = new TreeMap<Permit,Permit>();

    /**
     * Creates an empty permit collection.
     */
    public PermitSet() {
    }

    /**
     * Creates a permit set and adds the given permits to it.
     *
     * @param c  a collection of permits.
     */
    public PermitSet(Collection<? extends Permit> c) {
        addAll(c);
    }

    /**
     * Creates a permit set and adds the given permits to it.
     *
     * @param permits  a list of permits.
     */
    public PermitSet(Permit... permits) {
        if (permits != null) {
            for (Permit permit: permits) {
                add(permit);
            }
        }
    }

    /**
     * Adds the given permit to this set replacing an already present permit
     * with the same {@link Permit#getAction() action} and  {@link Permit#getPath() path}.
     *
     * @param permit  the permit to add. Note, this method accepts <code>null</code> arguments,
     * but simply ignores them.
     *
     * @return <code>true</code> if the given permit actually replaced a previously
     * added permit, or is an entirely new permit.
     */
    @Override
    public boolean add(Permit permit) {
        if (permit == null) {
            return false;
        }
        Permit oldPermit = permits.remove(permit);
        permits.put(permit, permit);
        return oldPermit == null || !isSame(oldPermit, permit);
    }

    private boolean isSame(Permit oldPermit, Permit permit) {
        return oldPermit.getLevel() == permit.getLevel()
                && oldPermit.getPath().equals(permit.getPath())
                && oldPermit.getAction().equals(permit.getAction());
    }

    /**
     * Adds the given permits to this set replacing already present permits
     * with same {@link Permit#getAction() action} and  {@link Permit#getPath() path}
     * attributes.
     *
     * @param c  the permits to add to this set. Note, this method accepts <code>null</code>
     * arguments, but simply ignores them.
     *
     * @return  <tt>true</tt> if permits actually have been added to this set.
     */
    @Override
    public boolean addAll(Collection<? extends Permit> c) {
        if (c == null || c.isEmpty()) {
            return false;
        }
        boolean modified = false;
        for (Permit permit: c) {
            if (add(permit)) {
                modified = true;
            }
        }
        return modified;
    }

    /**
     * Adds the given permits to this set replacing already
     * present permits with same {@link Permit#getAction() action} and
     * {@link Permit#getPath() path} attributes.
     *
     * @param permitSet  the permits to add to this set. Note, this method
     * accepts <code>null</code> arguments, but simply ignores them.
     *
     * @return  <tt>true</tt> if permits actually have been added to this set.
     */
    public boolean addAll(PermitSet permitSet) {
        if (permitSet == null || permitSet.isEmpty()) {
            return false;
        }
        return addAll(permitSet.permits());
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
    public Iterator<Permit> iterator() {
        return permits.keySet().iterator();
    }

    /**
     * Returns the permits stored in this set as unmodifiable set of permits.
     */
    public Set<Permit> permits() {
        return Collections.unmodifiableSet(permits.keySet());
    }

    @Override
    public Object[] toArray() {
        return permits.keySet().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return permits.keySet().toArray(a);
    }

    /**
     * Removes a permit with the same {@link Permit#getAction() action} and
     * {@link Permit#getPath() path} as the given permit from this set.
     *
     * @param o  the permit to remove. Note, this method accepts <code>null</code> arguments,
     * but simply ignores them.
     *
     *  @return <code>true</code> if any permit actually has been removed
     *  from this set.
     */
    @Override
    public boolean remove(Object o) {
        boolean modified = false;
        if (o instanceof Permit) {
            Permit permit = (Permit)o;
            modified = permits.remove(permit) != null;
        }
        return modified;
    }

    /**
     * Removes all permits with same {@link Permit#getAction() actions} and
     * {@link Permit#getPath() paths} as the given permits from this set.
     *
     * @param c  the collection of permits to remove. Note, this method accepts
     * <code>null</code> arguments, but simply ignores them.
     *
     *  @return <code>true</code> if any permit actually has been removed
     *  from this set.
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        if (c == null || c.isEmpty()) {
            return false;
        }
        boolean modified = false;
        for (Object o : c) {
            modified |= remove(o);
        }
        return modified;
    }

    /**
     * Checks if this set contains a permit with the same {@link Permit#getAction() action}
     * and {@link Permit#getPath() path} as the given permit.
     *
     * @param o  the permit to check.
     *
     *  @return <code>true</code> if this set contains a matching permit.
     */
    @Override
    public boolean contains(Object o) {
        return o != null ? permits.containsKey(o) : false;
    }

    /**
     * Checks if all permits in the given collection are contained in this set
     * by comparing corresponding {@link Permit#getAction() actions} and {@link Permit#getPath() paths}.
     *
     * @param c  a collection of permits.
     *
     *  @return <code>true</code> if all permits of the given collection are contained
     *  in this set.
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        return c != null ? permits.keySet().containsAll(c) : false;
    }

    /**
     * This method always throws {@link UnsupportedOperationException}.
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        permits.clear();
    }
}
