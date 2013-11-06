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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.MapBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for all uniquely identifiable model entities.
 * The identifier of an entity is a {@link UUID universally unique identifier (UUID)}.
 * Once set, the <code>uuid</code> of an entity  is immutable. An entity can be assigned
 * to one (and only one) parent entity, so that hierarchies of entities can be built.
 * Furthermore, entities can be marked as {@link #isDeleted() deleted} to hide them
 * from common operations.
 */
public abstract class EntityBase {

    private static final Logger LOG = LoggerFactory.getLogger(EntityBase.class);

    @PropertyName
    public static final String PROPERTY_UUID = "uuid"; //$NON-NLS-1$

    @PropertyName
    public static final String PROPERTY_DELETED = "deleted"; //$NON-NLS-1$

    @PropertyName
    public static final String PROPERTY_PARENT_ENTITY_ID = "parentEntityId"; //$NON-NLS-1$

    @Derived
    @PropertyName
    public static final String PROPERTY_PARENT_ENTITY = "parentEntity"; //$NON-NLS-1$

    @Derived
    @PropertyName
    public static final String PROPERTY_FIRST_CHILD = "firstChild"; //$NON-NLS-1$

    @Derived
    @PropertyName
    public static final String PROPERTY_NEXT_SIBLING = "nextSibling"; //$NON-NLS-1$

    @Derived
    @PropertyName
    public static final String PROPERTY_LAST_MODIFIED = "lastModified"; //$NON-NLS-1$

    @Derived
    @PropertyName
    public static final String PROPERTY_LAST_MODIFIED_BY = "lastModifiedBy"; //$NON-NLS-1$


    private static final Map<Class<?>,Class<?>> PRIMITIVES_MAP =
            new MapBuilder<Class<?>,Class<?>>()
                .put(Boolean.class, boolean.class)
                .put(Character.class, char.class)
                .put(Byte.class, byte.class)
                .put(Short.class, short.class)
                .put(Integer.class, int.class)
                .put(Long.class, long.class)
                .put(Float.class, float.class)
                .put(Double.class, double.class)
                .toMap();

    /**
     * The unique identifier of a project - set once, never changed.
     */
    private UUID uuid;

    /**
     * Deleted entities are loaded and cached separately.
     */
    private boolean deleted = false;

    /**
     * Persistent UUID of the parent entity,
     * or <code>null</code> if this entity has no parent.
     */
    private UUID parentEntityId;

    /**
     * Non-persistent pointer to the parent entity of this entity,
     * or <code>null</code> if this entity has no parent.
     */
    private transient EntityBase parentEntity;

    /**
     * Non-persistent pointer to the next sibling of this entity,
     * or <code>null</code> if this is the last sibling in the chain
     * or the entity is {@link #isDeleted() deleted}.
     */
    private transient EntityBase nextSibling;

    /**
     * Non-persistent pointer to the first child of this entity,
     * or <code>null</code> if this entity has no children
     * or all children are {@link #isDeleted() deleted}.
     */
    private transient EntityBase firstChild;

    /**
     * Date of last modification in ISO 8601 format
     * and in milliseconds since midnight, January 1, 1970 UTC.
     */
    private transient String lastModified;
    private transient long lastModifiedMillis;

    /**
     * Unique identifier of the last modifier.
     */
    private transient String lastModifiedBy;

    /**
     * Returns the unique identifier of the entity.
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Sets the unique identifier of the entity. Note, this
     * method does not change the unique identifier, if it has
     * been set before.
     */
    public void setUuid(UUID uuid) {
        if (this.uuid == null) {
            this.uuid = uuid;
        }
    }

    /**
     * Returns <code>true</code> if the entity is marked as deleted.
     */
    public boolean isDeleted() {
        return deleted;
    }

    /**
     * Marks an entity as deleted or removes such a mark.
     */
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * Returns the parent entity of this entity,
     * or <code>null</code> if this entity has no parent.
     */
    public EntityBase getParentEntity() {
        return parentEntity;
    }

    /**
     * Sets the parent entity of this entity.
     * @param parentEntity  the parent entity to set, or <code>null</code>
     * to reset the parent entity.
     */
    public void setParentEntity(EntityBase parentEntity) {
        this.parentEntity = parentEntity;
        if (parentEntity != null) {
            UUID parentUuid = parentEntity.getUuid();
            if (parentUuid == null) {
                throw new IllegalArgumentException("parentUUID is null, which makes it hard to remember");
            } else {
                this.parentEntityId = parentUuid;
            }
        } else {
            this.parentEntityId = null;
        }
    }

    /**
     * Returns the unique identifier of the parent entity,
     * or <code>null</code> if this entity has no parent.
     */
    public UUID getParentEntityId() {
        return parentEntityId;
    }

    /**
     * Sets the unique identifier of the parent entity.
     *
     * Note, this method should not be called directly except for
     * testing purposes. The parent entity is determined when
     * the entity is loaded.
     */
    public void setParentEntityId(UUID parentEntityId) {
        this.parentEntityId = parentEntityId;
    }

    /**
     * Returns the next sibling of this entity,
     * or <code>null</code> if this entity has no next sibling
     * or the entity is {@link #isDeleted() deleted}.
     *
     * Note that deleted entities never are reported as
     * children or siblings of another entity, but they nevertheless
     * may have a {@link #getParentEntity() parent entity}.
     */
    public EntityBase getNextSibling() {
        return nextSibling;
    }

    /**
     * Sets the next sibling of this entity.
     *
     * Note, this method should not be called directly except for
     * testing purposes. The siblings of an entity are determined
     * when the entity is loaded.
     *
     * @param nextSibling  the next sibling of this entity,
     * or <code>null</code> if this entity has no further siblings.
     */
    public void setNextSibling(EntityBase nextSibling) {
        this.nextSibling = nextSibling;
    }

    /**
     * Returns the first child of this entity,
     * or <code>null</code> if this entity has no children
     * or all children are {@link #isDeleted() deleted}.
     *
     * Note that deleted entities never are reported as
     * children or siblings of another entity, but they nevertheless
     * may have a {@link #getParentEntity() parent entity}.
     */
    public EntityBase getFirstChild() {
        return firstChild;
    }

    /**
     * Sets the first child of this entity.
     * Note, this method should not be called directly except for
     * testing purposes. The children of an entity are determined
     * when the entity is loaded.
     *
     * @param firstChild  the first child of this entity,
     * or <code>null</code> if this entity has no children.
     */
    public void setFirstChild(EntityBase firstChild) {
        this.firstChild = firstChild;
    }

    /**
     * Returns the children of this entity as list. This method follows
     * the {@link #getNextSibling() siblings chain} starting with the
     * {@link #getFirstChild() first child}.
     *
     * @return  the list of children, or an empty list if this entity
     * has no children.
     */
    public List<EntityBase> getChildren() {
        ArrayList<EntityBase> subprojects = new ArrayList<EntityBase>();
        EntityBase next = getFirstChild();
        while (next != null) {
            subprojects.add(next);
            next = next.getNextSibling();
        }
        return subprojects;
    }

    /**
     * Returns the date/time of the last modification of this entity.
     *
     * @return an ISO8061-compliant date/time string following the pattern
     * <tt>[-]CCYY-MM-DDThh:mm:ss[Z|(+|-)hh:mm]</tt> (equivalent to the
     * definition of the XML schema type <tt>xsd:dateTime</tt>),
     * or <code>null</code> if the entity has not yet been persisted.
     * Use for example {@link DatatypeConverter#parseDateTime(String)} to
     * convert the result into a {@link java.util.Calendar} for further
     * processing.
     */
    public String getLastModified() {
        return lastModified;
    }

    /**
     * Returns the date/time of the last modification of this entity
     * measured in milliseconds since midnight, January 1, 1970 UTC,
     * or -1 if the entity has not yet been persisted.
     */
    public long getLastModifiedMillis() {
        return lastModifiedMillis;
    }

    /**
     * Sets the date/time of the last modification.
     *
     * Note, this method should not be called directly except for
     * testing purposes. The date/time of the last modification
     * is determined when the entity is persisted.
     *
     * @param lastModified  an ISO8061-compliant date/time string following the
     * pattern <tt>[-]CCYY-MM-DDThh:mm:ss[Z|(+|-)hh:mm]</tt> as defined for
     * type <tt>xsd:dateTime</tt>) in <tt>"XML Schema Part 2: Datatypes"</tt>,
     * or <code>null</code> to indicate that the entity has not yet been
     * persisted or the date/time of last modification is unknown.
     *
     * @throws IllegalArgumentException  if the date/time string does not conform to
     * type <tt>xsd:dateTime</tt>.
     */
    public void setLastModified(String lastModified) {
        if (StringUtils.isBlank(lastModified)) {
            this.lastModifiedMillis = -1L;
            this.lastModified = null;
        } else {
            this.lastModifiedMillis = DatatypeConverter.parseDateTime(lastModified).getTimeInMillis();
            this.lastModified = lastModified;
        }
    }

    /**
     * Returns the unique identifier of the last modifier,
     * or <code>null</code> if the entity has not yet been persisted.
     */
    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    /**
     * Sets the unique identifier of the last modifier.
     *
     * Note, this method should not be called directly except for
     * testing purposes. The last modifier is determined
     * when the entity is persisted.
     *
     * @param lastModifiedBy  the unique identifier of the last modifier, or
     * <code>null</code>.
     */
    public void setLastModifiedBy(String lastModifiedBy) {
        if (StringUtils.isBlank(lastModifiedBy)) {
            this.lastModifiedBy = null;
        } else {
            this.lastModifiedBy = lastModifiedBy;
        }
    }

    /**
     * Returns the identifiers of the properties this entity provides.
     * A property is declared by defining a String constant with the
     * identifier of the property as value and annotating this constant
     * with {@link PropertyName}.
     *
     * @return  a set of property identifiers, or an empty set, if the entity
     * provides no properties.
     */
    public Set<String> getPropertyNames() {
        return getPropertyNames(getClass());
    }

    /**
     * Returns the identifiers of the properties of a given entity class.
     * A property is declared by defining a String constant with the
     * identifier of the property as value and annotating this constant
     * with {@link PropertyName}.
     *
     * @return  a set of property identifiers, or an empty set, if the entity
     * provides no properties or <code>entityClass</code> was <code>null</code>.
     */
    public static Set<String> getPropertyNames(Class<? extends EntityBase> entityClass) {
        Set<String> propertyNames = new HashSet<String>();
        if (entityClass == null) {
            return propertyNames;
        }
        for (Field field : entityClass.getFields()) {
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
     * Returns <code>true</code> if this entity has a property
     * with the given name.
     *
     * @param propertyName  the identifier of the property.
     * @return <code>true</code> if this entity has the requested property,
     * <code>false</code> otherwise.
     *
     * @see org.eclipse.skalli.services.projects.PropertyName
     */
    public boolean hasProperty(String propertyName) {
        return getMethod(propertyName) != null;
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

    private Method getMethod(String methodPrefix, String propertyName) {
        String methodName = methodPrefix + StringUtils.capitalize(propertyName);
        try {
            return getClass().getMethod(methodName, new Class[] {});
        } catch (NoSuchMethodException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Entity of type {0} has no getter method for property \"{1}\"",
                        getClass(), propertyName));
            }
        }
        return null;
    }

    /**
     * Sets the value for the given property, if that property exists.
     *
     * @param propertyName  the identifier of the property.
     * @param propertyValue  the new value of the property.

     * @throws NoSuchPropertyException  if no property with the given name exists.
     * @throws PropertyUpdateException  if the property value could not be changed.
     *
     * @see org.eclipse.skalli.services.projects.PropertyName
     */
    public void setProperty(String propertyName, Object propertyValue) {
        Class<?> propertyType = propertyValue != null? propertyValue.getClass() : null;
        Method method = getMethod("set", propertyName, propertyType); //$NON-NLS-1$
        if (method == null) {
            throw new NoSuchPropertyException(this, propertyName);
        }
        try {
            method.invoke(this, propertyValue);
        } catch (Exception e) {
           throw new PropertyUpdateException(MessageFormat.format(
                   "Property \"{0}\" could not be updated", propertyName), e);
        }
    }

    private Method getMethod(String methodPrefix, String propertyName, Class<?> propertyType) {
        Class<? extends EntityBase> entityClass = getClass();
        String methodName = methodPrefix + StringUtils.capitalize(propertyName);
        if (propertyType != null && PRIMITIVES_MAP.containsKey(propertyType)) {
            propertyType = PRIMITIVES_MAP.get(propertyType);
        }
        Class<?>[] argumentTypes = new Class<?>[] { propertyType };
        Method method = null;
        try {
            method = entityClass.getMethod(methodName, argumentTypes);
        } catch (NoSuchMethodException e) {
            method = findSetterMethod(entityClass, propertyName, methodName, argumentTypes);
        }
        return method;
    }

    private Method findSetterMethod(Class<? extends EntityBase> entityClass, String propertyName,
            String methodName,  Class<?>[] argumentTypes) throws PropertyUpdateException {
        Method method = null;
        try {
            for (Method candidate: entityClass.getMethods()) {
                if (methodName.equals(candidate.getName())) {
                    Class<?>[] parameterTypes = candidate.getParameterTypes();
                    if (parameterTypes.length == 1) {
                        if (argumentTypes[0] == null || parameterTypes[0].isAssignableFrom(argumentTypes[0])) {
                            argumentTypes[0] = parameterTypes[0];
                        } else {
                            throw new PropertyUpdateException(MessageFormat.format(
                                    "Argument of type \"{0}\" cannot be assigned to property \"{1}\" of type \"{2}\"",
                                    argumentTypes[0], propertyName, parameterTypes[0]));
                        }
                    }
                    method = entityClass.getMethod(methodName, argumentTypes);
                    break;
                }
            }
        } catch (NoSuchMethodException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Entity of type {0} has no setter method matching the signature \"{1}({2})",
                        entityClass.getName(), methodName, argumentTypes[0].getName()));
            }
        }
        return method;
    }

    @Override
    public String toString() {
        if (uuid != null) {
            return uuid.toString();
        }
        return super.toString();
    }

    @Override
    public int hashCode() {
        return (uuid == null) ? super.hashCode() : uuid.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (getClass() != o.getClass()) {
            return false;
        }
        EntityBase entityBase = (EntityBase) o;
        if (uuid == null) {
            if (entityBase.uuid != null) {
                return false;
            }
        } else if (!uuid.equals(entityBase.uuid)) {
            return false;
        }
        return true;
    }
}
