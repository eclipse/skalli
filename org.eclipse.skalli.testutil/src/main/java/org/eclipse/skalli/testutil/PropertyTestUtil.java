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
package org.eclipse.skalli.testutil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.Derived;
import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.model.PropertyName;
import org.junit.Assert;

@SuppressWarnings("nls")
public class PropertyTestUtil {

    public static Map<String, Object> getValues() {
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put(EntityBase.PROPERTY_UUID, TestUUIDs.TEST_UUIDS[0]);
        values.put(EntityBase.PROPERTY_DELETED, Boolean.FALSE);
        TestExtensibleEntityBase parent = new TestExtensibleEntityBase(TestUUIDs.TEST_UUIDS[1]);
        values.put(EntityBase.PROPERTY_PARENT_ENTITY, parent);
        values.put(EntityBase.PROPERTY_PARENT_ENTITY_ID, TestUUIDs.TEST_UUIDS[1]);
        TestExtensibleEntityBase firstChild = new TestExtensibleEntityBase(TestUUIDs.TEST_UUIDS[2]);
        values.put(EntityBase.PROPERTY_FIRST_CHILD, firstChild);
        TestExtensibleEntityBase nextSibling = new TestExtensibleEntityBase(TestUUIDs.TEST_UUIDS[3]);
        values.put(EntityBase.PROPERTY_NEXT_SIBLING, nextSibling);
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.ENGLISH); //$NON-NLS-1$
        String lastModified = DatatypeConverter.printDateTime(now);
        values.put(EntityBase.PROPERTY_LAST_MODIFIED, lastModified);
        values.put(EntityBase.PROPERTY_LAST_MODIFIED_BY, "homer"); //$NON-NLS-1$
        return values;
    }

    public static Map<Class<?>, String[]> getRequiredProperties() {
        return new HashMap<Class<?>, String[]>();
    }

    public static final void checkPropertyDefinitions(Class<?> classToCheck,
            Map<Class<?>, String[]> requiredProperties, Map<String, Object> values)
            throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException,
            InstantiationException, InvocationTargetException
    {
        Class<?> clazz = classToCheck;
        while (clazz != null) {

            // assert that the model class under test has a suitable constructor (either
            // a default constructor if requiredProperties is empty, or a constructor with the
            // correct number and type of parameters), and can be instantiated
            Object instance = assertExistsConstructor(clazz, requiredProperties, values);

            for (Field field : clazz.getDeclaredFields()) {
                if (hasAnnotation(field, PropertyName.class)) {

                    // assert that the field is public static final
                    Assert.assertTrue(clazz.getName() + ": constant " + field.getName() + " is not declared STATIC",
                            (field.getModifiers() & Modifier.STATIC) == Modifier.STATIC); //$NON-NLS-1$
                    Assert.assertTrue(clazz.getName() + ": constant " + field.getName() + " is not declared PUBLIC",
                            (field.getModifiers() & Modifier.PUBLIC) == Modifier.PUBLIC); //$NON-NLS-1$
                    Assert.assertTrue(clazz.getName() + ": constant " + field.getName() + " is not declared FINAL",
                            (field.getModifiers() & Modifier.FINAL) == Modifier.FINAL); //$NON-NLS-1$

                    // assert that the constant if of type String and has a value assigned.
                    Object object = field.get(null);
                    Assert.assertNotNull(clazz.getName() + ": constant " + field.getName() + " is NULL", object); //$NON-NLS-1$
                    Assert.assertTrue(clazz.getName() + ": constants " + field.getName() + " is not of type STRING",
                            object instanceof String); //$NON-NLS-1$

                    // assert that there is a private non-static field with a name matching the
                    // value of the constant unless the property is marked as derived
                    String fieldName = (String) object;
                    if (!hasAnnotation(field, Derived.class)) {
                        Assert.assertTrue(clazz.getName() + ": must have a private field named " + fieldName,
                                hasPrivateField(clazz, fieldName));
                    }

                    // assert that the values argument contains a test value for this field
                    Assert.assertTrue(clazz.getName() + ": no test value for field " + fieldName,
                            values.containsKey(fieldName));

                    Methods methods = new Methods();

                    // assert that the class has a getter for this property
                    methods.getMethod = assertExistsGetMethod(clazz, fieldName);

                    // assert that the class has a setter for this property if it is an optional property;
                    // required properties must be set in the constructor;
                    // skip properties that are annotated as @Derived
                    if (isOptionalProperty(clazz, fieldName, requiredProperties)
                            && !hasAnnotation(field, Derived.class)) {
                        Class<?> returnType = methods.getMethod.getReturnType();
                        if (!Collection.class.isAssignableFrom(returnType)) {
                            methods.setMethod = assertExistsSetMethod(clazz, fieldName, returnType);
                        } else {
                            Class<?> entryType = ((Collection<?>) values.get(fieldName)).iterator().next().getClass();
                            methods.addMethod = assertExistsCollectionMethod(clazz, fieldName, entryType, "add");
                            methods.removeMethod = assertExistsCollectionMethod(clazz, fieldName, entryType, "remove");
                            methods.hasMethod = assertExistsCollectionMethod(clazz, fieldName, entryType, "has");
                        }
                        // call the setter/adder and getter methods with the given test value
                        if (instance != null) {
                            assertChangeReadCycle(clazz, fieldName, methods, instance, values.get(fieldName));
                        }
                    } else {
                        if (instance != null) {
                            assertReadCycle(clazz, methods, instance, values.get(fieldName));
                        }
                    }
                }
            }

            // check the properties of the parent class (EntityBase!)
            clazz = clazz.getSuperclass();
        }
    }

    private static final class Methods {
        public Method getMethod;
        public Method setMethod;
        public Method addMethod;
        public Method removeMethod;
        public Method hasMethod;
    }

    private static final String getGetMethodName(String fieldName) {
        return "get" + StringUtils.capitalize(fieldName);
    }

    private static final String getSetMethodName(String fieldName) {
        return "set" + StringUtils.capitalize(fieldName);
    }

    private static final String getCollectionMethodName(String fieldName, String prefix) {
        return prefix + singular(fieldName);
    }

    private static final String singular(String fieldName) {
        String name = StringUtils.capitalize(fieldName);
        if (name.endsWith("ies")) {
            name = name.substring(0, name.length() - 3) + "y";
        } else if (name.endsWith("s")) {
            name = name.substring(0, name.length() - 1);
        } else {
            Assert.fail(fieldName + ": is not a valid field name for a collection-like " +
                    "property (must end with 's' or 'ies'");
        }
        return name;
    }

    private static final String getBooleanGetMethodName(String fieldName) {
        return "is" + StringUtils.capitalize(fieldName); //$NON-NLS-1$
    }

    private static boolean hasPrivateField(Class<?> clazz, String fieldName) {
        for (Field field : clazz.getDeclaredFields()) {
            if (fieldName.equals(field.getName())
                    && ((field.getModifiers() & Modifier.PRIVATE) == Modifier.PRIVATE)
                    && !((field.getModifiers() & Modifier.STATIC) == Modifier.STATIC))
            {
                return true;
            }
        }
        return false;
    }

    private static <T extends Annotation> boolean hasAnnotation(Field field, Class<T> annotationClass) {
        return field.getAnnotation(annotationClass) != null;
    }

    private static boolean isOptionalProperty(Class<?> clazz, String fieldName,
            Map<Class<?>, String[]> requiredProperties) {
        String[] props = requiredProperties.get(clazz);
        if (props == null) {
            return true;
        }
        for (String prop : props) {
            if (fieldName.equals(prop)) {
                return false;
            }
        }
        return true;
    }

    private static Object assertExistsConstructor(Class<?> clazz, Map<Class<?>, String[]> requiredProperties,
            Map<String, Object> values)
            throws IllegalAccessException, IllegalArgumentException, InstantiationException, InvocationTargetException
    {
        Object instance = null;
        String[] params = requiredProperties.get(clazz);
        if (params == null) {
            try {
                instance = clazz.newInstance();
            } catch (InstantiationException ex) {
                Assert.assertTrue(clazz.getName() + ": class without constructor must be abstract",
                        (clazz.getModifiers() & Modifier.ABSTRACT) == Modifier.ABSTRACT);
            }
        }
        else {
            Class<?>[] paramTypes = new Class<?>[params.length];
            Object[] args = new Object[params.length];
            for (int i = 0; i < params.length; ++i) {
                Object arg = values.get(params[i]);
                Assert.assertNotNull(arg);
                args[i] = arg;
                paramTypes[i] = arg.getClass();
            }
            Constructor<?> c;
            try {
                c = clazz.getConstructor(paramTypes);
                instance = c.newInstance(args);
            } catch (NoSuchMethodException e) {
                Assert.fail(clazz.getName() + ": must have constructor " + clazz.getName() + "("
                        + Arrays.toString(paramTypes) + ")");
            } catch (InstantiationException e) {
                Assert.assertTrue(clazz.getName() + ": class is not instantiable",
                        (clazz.getModifiers() & Modifier.ABSTRACT) == Modifier.ABSTRACT);
            }
        }
        return instance;
    }

    /**
     *
     * @param clazz
     * @param fieldName
     * @return
     */
    private static Method assertExistsGetMethod(Class<?> clazz, String fieldName) {
        boolean found = false;
        Method getter = null;
        String methodName = null;
        try {
            methodName = getGetMethodName(fieldName);
            getter = clazz.getMethod(methodName, new Class[] {});
            found = true;
        } catch (NoSuchMethodException e) {
            found = false;
        }
        if (!found) {
            try {
                methodName = getBooleanGetMethodName(fieldName);
                getter = clazz.getMethod(methodName, new Class[] {});
                if (getter.getReturnType().getName().equals("boolean")) { //$NON-NLS-1$
                    found = true;
                }
            } catch (NoSuchMethodException e) {
                found = false;
            }
        }
        Assert.assertTrue(clazz.getName() + ": must hava a getter for '" + fieldName + "'", found); //$NON-NLS-1$ //$NON-NLS-2$
        Class<?>[] params = getter.getParameterTypes();
        Assert.assertEquals(clazz.getName() + ": getter " + methodName + " must not have parameters", 0, params.length);
        return getter;
    }

    /**
     * Ensure that a setter method with a single parameter exists for the given field name,
     * and that the given field type can be assigned to the parameter of the setter.
     */
    private static Method assertExistsSetMethod(Class<?> clazz, String fieldName, Class<?> fieldType) {
        boolean found = false;
        Method setter = null;
        String methodName = null;
        try {
            methodName = getSetMethodName(fieldName);
            setter = getMethod(clazz, methodName, fieldType);
            found = true;
        } catch (NoSuchMethodException e) {
            found = false;
        }

        Assert.assertTrue(clazz.getName() + ": must have a set method for " + fieldName, found);

        Class<?>[] params = setter.getParameterTypes();
        Assert.assertEquals(clazz.getName() + ": " + methodName + " must have a single parameter", 1, params.length);

        Assert.assertTrue(clazz.getName() + ": value of type " + fieldType.getName() + " is not assignable to " +
                "parameter of type " + params[0].getName(), fieldType.isAssignableFrom(params[0]));
        return setter;
    }

    private static Method assertExistsCollectionMethod(Class<?> clazz, String fieldName, Class<?> entryType,
            String prefix) {
        boolean found = false;
        Method adder = null;
        String methodName = null;
        while (!found && entryType != null) {
            try {
                methodName = getCollectionMethodName(fieldName, prefix);
                adder = getMethod(clazz, methodName, entryType);
                found = true;
            } catch (NoSuchMethodException e) {
                // try to find a method that matches the superclass
                // of the given entry type
                entryType = entryType.getSuperclass();
                found = false;
            }
        }
        Assert.assertTrue(clazz.getName() + ": must have a " + prefix + " method for " + fieldName, found);
        Class<?>[] params = adder.getParameterTypes();
        Assert.assertEquals(clazz.getName() + ": " + methodName + " must have a single parameter", 1, params.length);
        Assert.assertTrue(clazz.getName() + ": value of type " + entryType.getName() + " is not assignable to " +
                " parameter of type " + params[0].getName(), entryType.isAssignableFrom(params[0]));
        return adder;
    }

    private static Method getMethod(Class<?> clazz, String methodName, Class<?> fieldType)
            throws NoSuchMethodException {
        String name = fieldType.getSimpleName();
        if ("Boolean".equals(name)) {
            return clazz.getMethod(methodName, boolean.class);
        }
        if ("Integer".equals(name)) {
            return clazz.getMethod(methodName, int.class);
        }
        if ("Long".equals(name)) {
            return clazz.getMethod(methodName, long.class);
        }
        if ("Float".equals(name)) {
            return clazz.getMethod(methodName, float.class);
        }
        if ("Double".equals(name)) {
            return clazz.getMethod(methodName, double.class);
        }
        if ("Character".equals(name)) {
            return clazz.getMethod(methodName, char.class);
        }
        return clazz.getMethod(methodName, fieldType);
    }

    private static void assertChangeReadCycle(Class<?> clazz, String fieldName, Methods methods, Object instance,
            Object valueSet)
            throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        if (methods.setMethod != null) {
            methods.setMethod.invoke(instance, valueSet);
            Object valueGet = methods.getMethod.invoke(instance);
            Assert.assertEquals(clazz.getName() + "#" + methods.getMethod.getName() + ":", valueSet, valueGet);
        }
        else if (methods.addMethod != null) {
            Object first = null;
            Collection<?> collectionSet = (Collection<?>) valueSet;
            for (Object o : collectionSet) {
                methods.addMethod.invoke(instance, o);
                if (first == null) {
                    first = o;
                }
            }
            Collection<?> valueGet = (Collection<?>) methods.getMethod.invoke(instance);
            assertEqualsAnyOrder(clazz.getName() + "#" + methods.getMethod.getName(), collectionSet, valueGet);

            // has-remove-has-add-has cycle
            Assert.assertTrue(clazz.getName() + "#" + methods.hasMethod.getName() + " before remove",
                    (Boolean) methods.hasMethod.invoke(instance, first));
            methods.removeMethod.invoke(instance, first);
            Assert.assertFalse(clazz.getName() + "#" + methods.hasMethod.getName() + " after remove",
                    (Boolean) methods.hasMethod.invoke(instance, first));
            methods.addMethod.invoke(instance, first);
            Assert.assertTrue(clazz.getName() + "#" + methods.hasMethod.getName() + " after add",
                    (Boolean) methods.hasMethod.invoke(instance, first));
        }
        else {
            Assert.fail(clazz.getName() + ": neither a setter nor an adder available for property " + fieldName);
        }
    }

    private static void assertReadCycle(Class<?> clazz, Methods methods, Object instance, Object value)
            throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        String msg = clazz.getName() + "#" + methods.getMethod.getName() + ": ";
        Object valueGet = methods.getMethod.invoke(instance);
        if (value != null) {
            if (Collection.class.isAssignableFrom(clazz)) {
                assertEqualsAnyOrder(msg, (Collection<?>) value, (Collection<?>) valueGet);
            } else {
                Assert.assertEquals(msg, value, valueGet);
            }
        } else if (Collection.class.isAssignableFrom(clazz)) {
            Assert.assertTrue(msg + " - expected empty collection", ((Collection<?>) valueGet).isEmpty());
        } else if (valueGet instanceof String) {
            Assert.assertEquals(msg, "", valueGet);
        } else if (valueGet instanceof Boolean) {
            Assert.assertEquals(msg, Boolean.FALSE, valueGet);
        }
        else {
            Assert.assertNull(msg, valueGet);
        }
    }

    private static void assertEqualsAnyOrder(String message, Collection<?> collection1, Collection<?> collection2) {
        Assert.assertEquals(message + "[size]", collection1.size(), collection2.size());
        Iterator<?> it1 = collection1.iterator();
        while (it1.hasNext()) {
            Object next1 = it1.next();
            Assert.assertTrue(message + "[" + next1 + " found]", collection2.contains(next1));
        }
    }
}
