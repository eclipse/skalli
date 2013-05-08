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
package org.eclipse.skalli.services.configuration;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Helper class to obfuscate fields of a given object that are annotated with {@link Protect}.
 */
public class Protector {

    public static final String PROTECTION_VALUE_STRING = "******"; //$NON-NLS-1$
    public static final char PROTECTION_VALUE_CHAR = '*';

    /**
     * Obfuscates all fields of the given object that are annotated with {@link Protect}.
     * If a field is {@link Iterable iterable}, all entries of that field are obfuscated.
     * Fields inherited from superclasses are obfuscated recursively, too.
     *
     * Obfuscation for string-like and character field types means that the field
     * value is replaced with stars (either <tt>"********"</tt> or <tt>'*'</tt>,
     * respectively).
     * For primitive types (except <code>char</code) the field value is reset to the
     * default value defined by the Java language (zero for numeric types,
     * <code>false</code> for boolean types etc.).
     * For all other types (including the wrapper types for the primitives like
     * <code>Boolean</code> and <code>Integer</code>) the field value is replaced
     * with <code>null</code>.
     *
     * @param obj  the object to obfuscate.
     * @param relevantClasses  all classes referenced by or inherited from of the given object
     * that should be included into the obfuscation. Classes not contained in the list will be
     * ignored (e.g. JDK classes).
     *
     * @throws IllegalAccessException  if any of the {@ Project protected}
     * fields of the given object is inaccessible, e.g. because that field is either static or final (or both),
     * or access control is enforced for that field.
     */
    public static void protect(Object obj, Collection<Class<?>> relevantClasses) throws IllegalAccessException {
        if (obj == null) {
            return;
        }
        List<Class<?>> list = new ArrayList<Class<?>>();
        if (relevantClasses != null) {
            list.addAll(relevantClasses);
        }
        Class<?> clazz = obj.getClass();
        list.add(clazz);
        protect(obj, clazz, list);
    }

    private static void protect(Object obj, Class<? extends Object> clazz, Collection<Class<?>> relevantClasses)
            throws IllegalAccessException {
        if (obj instanceof Iterable) {
            for (Object element : (Iterable<?>) obj) {
                if (element != null) {
                    protect(element, element.getClass(), relevantClasses);
                }
            }
        } else if (isAssignableFromAny(clazz, relevantClasses)) {
            Class<?> superclass = clazz.getSuperclass();
            if (superclass != null && superclass != Object.class) {
                protect(obj, superclass, relevantClasses);
            }
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                protectField(field, obj, relevantClasses);
            }
        }
    }

    private static boolean isAssignableFromAny(Class<? extends Object> clazz, Collection<Class<?>> relevantClasses) {
        for (Class<?> relevatClass : relevantClasses) {
            if (relevatClass.isAssignableFrom(clazz)) {
                return true;
            }
        }
        return false;
    }

    private static void protectField(Field field, Object obj, Collection<Class<?>> relevantClasses)
            throws IllegalAccessException {
        if (field.isAnnotationPresent(Protect.class)) {
            protectValue(field, obj);
        } else if (!field.getType().isPrimitive()) {
            Object fieldValue = getFieldValue(field, obj);
            if (fieldValue != null) {
                protect(fieldValue, fieldValue.getClass(), relevantClasses);
            }
        }
    }

    private static Object getFieldValue(Field field, Object obj) throws IllegalAccessException {
        field.setAccessible(true);
        return field.get(obj);
    }

    private static void protectValue(Field field, Object obj)
            throws IllegalArgumentException, IllegalAccessException {
        field.setAccessible(true);
        Class<?> fieldType = field.getType();
        if (fieldType.equals(String.class)) {
            field.set(obj, PROTECTION_VALUE_STRING);
        } else if (fieldType.equals(char.class) || fieldType.equals(Character.class)) {
            field.set(obj, PROTECTION_VALUE_CHAR);
        } else if (fieldType.equals(boolean.class)) {
            field.set(obj, false);
        } else if (fieldType.equals(int.class)
                || fieldType.equals(long.class)
                || fieldType.equals(byte.class)
                || fieldType.equals(short.class)
                || fieldType.equals(float.class)
                || fieldType.equals(double.class)) {
            field.set(obj, 0);
        } else {
            field.set(obj, null);
        }
    }

}
