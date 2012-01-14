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
package org.eclipse.skalli.model;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ExtensionEntityBase extends EntityBase {

    private static final Logger LOG = LoggerFactory.getLogger(ExtensionEntityBase.class);

    /** Non-persistent pointer of an extension entity to an extensible entity. */
    private transient ExtensibleEntityBase extensibleEntity;

    /**
     * Returns the extensible entity this extension entity is assigned to,
     * e.g. points to the project an extension is assigned to.
     */
    public ExtensibleEntityBase getExtensibleEntity() {
        return extensibleEntity;
    }

    /**
     * Sets the extensible entity this extension entity is assigned to.
     */
    public void setExtensibleEntity(ExtensibleEntityBase extensibleEntity) {
        this.extensibleEntity = extensibleEntity;
    }

    /**
     * Returns the identifiers of the properties this extension provides.
     * A property is declared by defining a String constant with the
     * identifier of the property as value and annotating this constant
     * with {@link PropertyName}.
     *
     * @return  a set of property identifiers, or an empty set, if the extension
     * provides no properties.
     */
    public Set<String> getPropertyNames() {
        Set<String> propertyNames = new HashSet<String>();
        for (Field field : getClass().getFields()) {
            if (field.getAnnotation(PropertyName.class) != null) {
                try {
                    propertyNames.add((String) field.get(null));
                } catch (Exception e) {
                    // should not happen, since fields annotated with @PropertyName are
                    // expected to be public static final String constants, but if this
                    // happens it is a severe issue
                    throw new IllegalStateException("Invalid @PropertyName declaration: " + field, e);
                }
            }
        }
        return propertyNames;
    }

    /**
     * Returns the value of the given property, if that property exists.
     *
     * @param propertyName  the identifier of the property.
     *
     * @throws NoSuchPropertyException  if no property with the given name
     * exists, or retrieving the value from that property failed.
     *
     * @see org.eclipse.skalli.services.projects.PropertyName
     */
    public Object getProperty(String propertyName) {
        Method method = getMethod(propertyName);
        if (method == null) {
            throw new NoSuchPropertyException(this, propertyName);
        }
        try {
            return method.invoke(this, new Object[] {});
        } catch (Exception e) {
            throw new NoSuchPropertyException(this, propertyName, e);
        }
    }

    private Method getMethod(String propertyName) {
        Method getter = getMethod("get", propertyName); //$NON-NLS-1$
        if (getter == null) {
            getter = getMethod("is", propertyName); //$NON-NLS-1$
        }
        return getter;
    }

    private Method getMethod(String prefix, String propertyName) {
        String methodName = prefix + StringUtils.capitalize(propertyName);
        try {
            return getClass().getMethod(methodName, new Class[] {});
        } catch (NoSuchMethodException e) {
            LOG.debug(MessageFormat.format("Entity of type {0} does not have a getter {1} for property {2}", getClass()
                    .getName(), methodName, propertyName));
        }
        return null;
    }
}
