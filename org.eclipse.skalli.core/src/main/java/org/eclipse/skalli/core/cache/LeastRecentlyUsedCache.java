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

import java.util.Map;
import java.util.Map.Entry;

/**
 * Implementation of the Least Recently Used (LRU) cache strategy.
 * <p>
 * This cache remembers the point in time when each entry was read for the last time.
 * The entry which was not accessed for the longest period of time will be discarded,
 * if there is a need to do so.
 * </p>
 *
 * @param <K>  type of the key.
 * @param <V>  type of the value.
 */
public class LeastRecentlyUsedCache<K, V> extends CacheBase<K, V, Long> {

    public LeastRecentlyUsedCache(int cacheSize) {
        super(cacheSize);
    }

    @Override
    protected Long createMetaInfo(K key) {
        return System.currentTimeMillis();
    }

    @Override
    protected Long onAccess(K key, Long metaInfo) {
        return System.currentTimeMillis();
    }

    @Override
    protected K calcEntryToDiscard(Map<K, Long> metaInfos) {
        long oldest = System.currentTimeMillis();
        K oldestEntryKey = null;
        for (Entry<K, Long> entry : metaInfos.entrySet()) {
            if (entry.getValue() < oldest) {
                oldestEntryKey = entry.getKey();
                oldest = entry.getValue();
            }
        }
        return oldestEntryKey;
    }
}
