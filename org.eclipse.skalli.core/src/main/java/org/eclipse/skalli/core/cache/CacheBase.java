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
package org.eclipse.skalli.core.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Represents a cache for arbitrary data objects.
 * <p>
 * This class needs to be extended by a caching strategy.
 * </p>
 *
 * @param <K>  type of the key.
 * @param <V>  type of the value.
 * @param <M>  type of the cache meta information.
 */
public abstract class CacheBase<K, V, M> implements Cache<K, V> {

    private Map<K, V> cache = new HashMap<K, V>();
    private Map<K, M> metaInfos = new HashMap<K, M>();

    private int cacheSize;

    protected abstract M createMetaInfo(K key);

    protected abstract K calcEntryToDiscard(Map<K, M> metaInfos);

    protected void beforeAccess(K key) {
    }

    protected abstract M onAccess(K key, M metaInfo);


    protected CacheBase(int cacheSize) {
        this(cacheSize, null);
    }

    protected CacheBase(int cacheSize, Cache<K, V> cache) {
        if (cacheSize < 1) {
            throw new IllegalArgumentException("argument 'cacheSize' must be a positive integer"); //$NON-NLS-1$
        }
        this.cacheSize = cacheSize;
        if (cache != null) {
            for (Map.Entry<K, V> entry: cache.entrySet()) {
                put(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public synchronized final void put(K key, V value) {
        if (key == null) {
            throw new IllegalArgumentException("argument 'key' must not be null"); //$NON-NLS-1$
        }
        beforeAccess(key);
        synchronized (cache) {
            if (cache.size() >= cacheSize) {
                K keyDiscard = calcEntryToDiscard(metaInfos);
                if (keyDiscard == null || !cache.containsKey(keyDiscard)) {
                    // Strategy did not determine a valid key to be removed, so just
                    // decide randomly as a fallback strategy
                    List<K> keys = new ArrayList<K>(cache.keySet());
                    int position = new Random(System.currentTimeMillis()).nextInt(keys.size());
                    keyDiscard = keys.get(position);
                }
                cache.remove(keyDiscard);
                metaInfos.remove(keyDiscard);
            }
            cache.put(key, value);
            metaInfos.put(key, createMetaInfo(key));
        }
    }

    @Override
    public synchronized final V get(K key) {
        if (key == null) {
            throw new IllegalArgumentException("argument 'key' must not be null"); //$NON-NLS-1$
        }
        beforeAccess(key);
        synchronized (cache) {
            V value = cache.get(key);
            if (value != null) {
                M newMetaInfo = onAccess(key, metaInfos.get(key));
                metaInfos.put(key, newMetaInfo);
            }
            return value;
        }
    }

    @Override
    public synchronized final void clear() {
        synchronized (cache) {
            cache.clear();
            metaInfos.clear();
        }
    }

    @Override
    public synchronized final Collection<V> values() {
        beforeAccess(null);
        synchronized (cache) {
            return cache.values();
        }
    }

    @Override
    public synchronized final Set<Map.Entry<K, V>> entrySet() {
        beforeAccess(null);
        synchronized (cache) {
            return cache.entrySet();
        }
    }
}
