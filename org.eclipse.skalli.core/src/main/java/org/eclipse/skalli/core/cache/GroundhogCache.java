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

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Implementation of the "Groundhog" cache strategy.
 * <p>
 * This cache remembers the point in time when each entry was read for the last time.
 * The entry which was not accessed for the longest period of time will be discarded,
 * if there is a need to do so.
 * <p>
 * Furthermore, this cache remembers its data only for the current day.
 * The next day it starts over empty again.
 * <p>
 * For more details ask Phil Connors (a.k.a. Bill Murray).
 *
 * @param <K>  type of the key.
 * @param <V>  type of the value.
 */
public class GroundhogCache<K, V> extends CacheBase<K, V, Long> {

    private int activeDayOfYear;
    private int activeYear;

    public GroundhogCache(int cacheSize) {
        super(cacheSize);
        initializeIfNeeded();
    }

    public GroundhogCache(int cacheSize, Cache<K, V> cache) {
        super(cacheSize, cache);
        initializeIfNeeded();
    }

    Calendar getCalendar() {
        return new GregorianCalendar();
    }

    private void initializeIfNeeded() {
        Calendar cal = getCalendar();
        int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
        int year = cal.get(Calendar.YEAR);
        if (dayOfYear != activeDayOfYear || year != activeYear) {
            activeDayOfYear = dayOfYear;
            activeYear = year;
            clear();
        }
    }

    @Override
    protected Long createMetaInfo(K key) {
        return System.nanoTime();
    }

    @Override
    protected void beforeAccess(K key) {
        initializeIfNeeded();
    };

    @Override
    protected Long onAccess(K key, Long metaInfo) {
        return System.nanoTime();
    }

    @Override
    protected K calcEntryToDiscard(Map<K, Long> metaInfos) {
        long oldest = System.nanoTime();
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
