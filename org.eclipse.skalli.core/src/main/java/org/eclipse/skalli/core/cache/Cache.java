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
package org.eclipse.skalli.core.cache;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Interface representing a key-value cache.
 *
 * @param <K>  type of the key.
 * @param <V>  type of the value.
 */
public interface Cache<K, V> {

    /**
     * Stores a new entry.
     * <p>
     * If the cache is filled already, then another entry may be discarded according to the cache strategy,
     * before the new entry actually is added.
     * </p>
     * <p>
     * Please note that the key must implement equals() and hashCode() properly.
     * </p>
     * @param key  the key, not <code>null</code>.
     * @param value  the value, or <code>null</code>.
     */
    public void put(K key, V value);

    /**
     * Returns an entry from the cache.
     * <p>
     * Please be aware of the fact that according to the cache strategy there is no guarantee
     * that an entry which was stored once in the cache still is in there at a later point in time.
     * </p>
     * @param key  the key of the entry to retrieve, not <code>null</code>.
     * @return  the value of the entry, or <code>null</code>, if the entry is not contained
     * in the cache (anymore).
     */
    public V get(K key);

    /**
     * Returns all currently stored cache values.
     * @return  a collection of cache values, or an empty collection.
     */
    public Collection<V> values();

    /**
     * Returns all currently stored cache entries.
     * @return a set of cache entries, or an empty set.
     */
    public Set<Map.Entry<K, V>> entrySet();

    /**
     * Discards all cache entries.
     */
    public void clear();

}
