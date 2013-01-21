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
package org.eclipse.skalli.core.persistence;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.model.ExtensibleEntityBase;
import org.eclipse.skalli.model.ExtensionEntityBase;

public final class EntityHelper {

    /**
     * Normalizes an entity and its extensions:
     * <ol>
     * <li>Replaces a <code>null</code> {@link java.lang.String} field with a blank ("") string.</li>
     * <li>Replaces a <code>null</code> {@link java.util.Collection}-like field with an empty collection
     * of matching type (i.e. an instance of {@link java.util.ArrayList} is assigned to a field of type
     * <code>ArrayList</code>, an instance of {@link java.util.HashSet} is assigned to a field of type
     * <code>HashSet</code> and so on). An <code>ArrayList</code> is assigned to a field of type
     * {@link java.util.List}, a <code>HashSet</code> is assigned to a field of type {@link java.util.Set} and
     * a {@link java.util.TreeSet} is assigned to a field of type {@link java.util.SortedSet}.</li>
     * <li>Replaces a <code>null</code> {@link java.util.Map}-like field with an empty map
     * of matching type (i.e. an instance of {@link java.util.HashMap} is assigned to a field of type
     * <code>HashMap</code>, and so on). A <code>HashMap</code> is assigned to a field type <code>Map</code>,
     * while a {@link java.util.TreeMap</code> is assigned to a field of type {@link java.utilSortedMap}.</li>
     * <li>Removes blank strings from {@link java.util.Collection}-like fields and from the values of
     * {@link java.util.Map}-like fields. Entries that are not strings are ignored.</li>
     * </ol>
     * All other kinds of fields are ignored.<br>
     * This method assumes, that the collection type to be
     * instantiated has either a constructor with a single integer argument (specifying the initial capacity
     * of the collection), or a parameterless constructor. The former is preferred.<br>
     * This method iterates the entity and, in case the entity is an instance
     * of {@link ExtensibleEntityBase}, all extensions of that entity, too.
     */
    public static void normalize(EntityBase entity) {
        doNormalize(entity);
        if (entity instanceof ExtensibleEntityBase) {
            ExtensibleEntityBase extensibleEntity = (ExtensibleEntityBase) entity;
            for (ExtensionEntityBase extension : extensibleEntity.getAllExtensions()) {
                extension.setExtensibleEntity(extensibleEntity);
                doNormalize(extension);
            }
        }
    }

    private static void doNormalize(EntityBase entity) {
        if (entity == null) {
            return;
        }
        Class<?> currentClass = entity.getClass();
        while (currentClass != null) {
            for (Field field : currentClass.getDeclaredFields()) {
                try {
                    // do not try to change constants or transient fields
                    int modifiers = field.getModifiers();
                    if (Modifier.isFinal(modifiers) || Modifier.isStatic(modifiers) ||
                            Modifier.isTransient(modifiers)) {
                        continue;
                    }
                    field.setAccessible(true);

                    // ensure, thet the value is non null
                    Object value = field.get(entity);
                    if (value == null) {
                        instantiateField(entity, field);
                    }
                    else {
                        // for non-null collections or maps, ensure that there
                        // are no null entries or empty strings
                        Class<?> type = field.getType();
                        if (Collection.class.isAssignableFrom(type)) {
                            Collection<?> collection = (Collection<?>) value;
                            ArrayList<Object> remove = new ArrayList<Object>();
                            for (Object entry : collection) {
                                if (entry instanceof String && StringUtils.isBlank((String) entry)) {
                                    remove.add(entry);
                                }
                            }
                            collection.removeAll(remove);
                            field.set(entity, collection);
                        }
                        else if (Map.class.isAssignableFrom(type)) {
                            Map<?, ?> map = (Map<?, ?>) value;
                            ArrayList<Object> remove = new ArrayList<Object>();
                            for (Entry<?, ?> entry : map.entrySet()) {
                                if (entry.getValue() instanceof String
                                        && StringUtils.isBlank((String) entry.getValue())) {
                                    remove.add(entry.getKey());
                                }
                            }
                            for (Object key : remove) {
                                map.remove(key);
                            }
                            field.set(entity, map);
                        }
                    }
                } catch (UnsupportedOperationException e) {
                    //  TODO exception handling/logging
                    // some collections/map may not support remove
                    throw new RuntimeException(e);
                } catch (IllegalArgumentException e) {
                    //TODO exception handling/logging
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    //TODO exception handling/logging
                    throw new RuntimeException(e);
                }
            }
            currentClass = currentClass.getSuperclass();
        }
    }

    private static void instantiateField(EntityBase entity, Field field)
            throws IllegalArgumentException, IllegalAccessException {
        Class<?> type = field.getType();
        if (type.equals(String.class)) {
            field.set(entity, ""); //$NON-NLS-1$
        } else if (type.equals(List.class)) {
            field.set(entity, new ArrayList<Object>(0));
        } else if (type.equals(Set.class)) {
            field.set(entity, new HashSet<Object>(0));
        } else if (type.equals(SortedSet.class)) {
            field.set(entity, new TreeSet<Object>());
        } else if (type.equals(Map.class)) {
            field.set(entity, new HashMap<Object, Object>(0));
        } else if (type.equals(SortedMap.class)) {
            field.set(entity, new TreeMap<Object, Object>());
        } else if (Collection.class.isAssignableFrom(type)
                || Map.class.isAssignableFrom(type)) {
            field.set(entity, getInstance(type));
        }
    }

    /**
     * Returns an instance of the given collection type.
     * This method assumes, that the type has either a constructor
     * with a single integer argument (specifying the initial capacity
     * of the collection), or a parameterless constructor. The former
     * is preferred.
     * @param collectionType  the collection type to instantiate.
     */
    private static Object getInstance(Class<?> collectionType) {
        Object instance = null;
        try {
            try {
                Constructor<?> constructor = collectionType.getConstructor(Integer.class);
                instance = constructor.newInstance(0);
            } catch (NoSuchMethodException e) {
                instance = collectionType.newInstance();
            }
        } catch (Exception e) {
            //TODO exception handling/logging
            throw new RuntimeException(e);
        }
        return instance;
    }
}
