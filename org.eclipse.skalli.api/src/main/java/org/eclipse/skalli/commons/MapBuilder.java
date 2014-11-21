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
package org.eclipse.skalli.commons;

import java.util.HashMap;
import java.util.Map;

public class MapBuilder<K,V> {
    private Map<K, V> map;

    public MapBuilder() {
        map = new HashMap<K,V>();
    }

    public MapBuilder(Map<K,V> map) {
        this.map = map;
    }

    public MapBuilder<K,V> put(K key, V value) {
        map.put(key, value);
        return this;
    }

    public Map<K,V> toMap() {
        return map;
    }
}