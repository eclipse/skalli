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
package org.eclipse.skalli.commons;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class CollectionUtils {

    public static <T> Set<T> asSet(T... args) {
        Set<T> result = new HashSet<T>();
        if (args != null) {
            for (T arg : args) {
                result.add(arg);
            }
        }
        return result;
    }

    public static <T> SortedSet<T> asSortedSet(T... args) {
        SortedSet<T> result = new TreeSet<T>();
        if (args != null) {
            for (T arg : args) {
                result.add(arg);
            }
        }
        return result;
    }

    public static Map<String, String> asMap(String[][] args) {
        HashMap<String, String> result = new HashMap<String, String>();
        if (args != null) {
            for (int i = 0; i < args.length; ++i) {
                String key = args[i][0];
                String value = args[i][1];
                result.put(key, value);
            }
        }
        return result;
    }

    public static <K, V> Map<K, V> asMap(K key, V value) {
        HashMap<K, V> result = new HashMap<K, V>(1);
        result.put(key, value);
        return result;
    }

    /**
     * Similar to Collection.add() but only allows non-null elements.
     *
     * @return <code>true</code> if this collection changed as a result of the call
     */
    public static <T> boolean addSafe(final Collection<T> collection, T element) {
        if (element != null) {
            return collection.add(element);
        } else {
            return false;
        }
    }

    /**
    * Returns a new <code>Map</code> instance which contains the given <code>sourceMap<code>
    * and <code>members<code> values.
    *
    * @param sourceMap  map from which to copy entries into the result.
    * @param members  additional entries to add.
    */
    public static Map<String, String> addAll(Map<String, String> sourceMap, String[][] members) {
        Map<String, String> result = new HashMap<String, String>(sourceMap);
        result.putAll(asMap(members));
        return result;
    }

    public static Map<String, List<String>> asMap(String key, String... args) {
        HashMap<String, List<String>> result = new HashMap<String, List<String>>();
        if (args != null) {
            result.put(key, Arrays.asList(args));
        }
        return result;
    }

    public static class MapBuilder<T> {
        private Map<String, T> map;

        public MapBuilder() {
            map = new HashMap<String, T>();
        }

        public MapBuilder(Map<String, T> map) {
            this.map = map;
        }

        public MapBuilder<T> put(String key, T value) {
            map.put(key, value);
            return this;
        }

        public Map<String, T> toMap() {
            return map;
        }
    }

    public static boolean isNotBlank(Collection<?> c) {
        return c != null && !c.isEmpty();
    }

    public static String toString(Collection<?> c, char separator) {
        if (c == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Object o: c) {
            if (o != null) {
                if (sb.length() > 0) {
                    sb.append(separator);
                }
                sb.append(o.toString());
            }
        }
        return sb.toString();
    }

    public static int[] asSortedArray(int[] a) {
        Arrays.sort(a);
        return a;
    }

    public static final  SortedSet<Object> EMPTY_SORTED_SET = new EmptySortedSet<Object>();

    @SuppressWarnings("unchecked")
    public static <T> SortedSet<T> emptySortedSet() {
        return (SortedSet<T>)EMPTY_SORTED_SET;
    }

    private static class EmptySortedSet<T> extends AbstractSet<T> implements SortedSet<T>, Serializable {

        private static final long serialVersionUID = -293982024607513646L;

        @Override
        public Iterator<T> iterator() {
            return new Iterator<T>() {
                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public T next() {
                    throw new NoSuchElementException();
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public Comparator<? super T> comparator() {
            return null;
        }

        @Override
        public SortedSet<T> subSet(T fromElement, T toElement) {
            return this;
        }

        @Override
        public SortedSet<T> headSet(T toElement) {
            return this;
        }

        @Override
        public SortedSet<T> tailSet(T fromElement) {
            return this;
        }

        @Override
        public T first() {
            throw new NoSuchElementException();
        }

        @Override
        public T last() {
            throw new NoSuchElementException();
        }
    }
}
