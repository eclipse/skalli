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
package org.eclipse.skalli.model;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.FormatUtils;
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
     * Cache for property accessor methods associated with the defining entity classes.
     * The inner maps are immutable, but lazily initialized when {@link #getProperty(String)}
     * is called first for a given entity class. The outter concurrent hash map guarantees
     * non-blocking read operations for maximum performance.
     */
    private transient static ConcurrentHashMap<Class<?>, Map<String,Method>> accessorsByEntityType =
            new ConcurrentHashMap<Class<?>, Map<String,Method>>();

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
     * processing. The result is <code>null</code> if the date/time of
     * the last modification is unknown.
     */
    public String getLastModified() {
        return lastModified;
    }

    /**
     * Returns the date/time of the last modification of this entity
     * measured in milliseconds since midnight, January 1, 1970 UTC,
     * or -1 if the date/time of the last modification is unknown.
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
     * or <code>null</code> to indicate that the date/time of the last modification
     * is unknown.
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
     * Sets the date/time of the last modification.
     *
     * Note, this method should not be called directly except for
     * testing purposes. The date/time of the last modification
     * is determined when the entity is persisted.
     *
     * @param lastModifiedMillis  the time of the last modification
     * in milliseconds since midnight, January 1, 1970 UTC, or any negative
     * value to indicate that the date/time of the last modification is unknown.
     */
    public void setLastModified(long lastModifiedMillis) {
        if (lastModifiedMillis < 0) {
            this.lastModifiedMillis = -1L;
            this.lastModified = null;
        } else {
            this.lastModifiedMillis = lastModifiedMillis;
            this.lastModified = FormatUtils.formatUTCWithMillis(lastModifiedMillis);
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
     * Returns the names of the properties of this entity.
     * <p>
     * A property is declared by defining a string constant with the
     * identifier of the property as value, annotating this constant
     * with {@link PropertyName} and defining a corresponding
     * getter method.
     *
     * @return an immutable set of property names, or an empty set,
     * if the entity has no properties.
     */
    public Set<String> getPropertyNames() {
        return getReadAccessors().keySet();
    }

    /**
     * Returns the names of the properties of a given entity class.
     * <p>
     * This method scans the given class for string constants annotated
     * with {@link PropertyName}, which have a corresponding getter
     * method. Example:
     * <pre>
     *   &#064;PropertyName public static final String PROPERTY_PROJECTID = "projectId";
     *   public String getProjectId() { ... }
     * <pre>
     * The capitalized value of the string constant is prefixed with either <tt>"get"</tt>,
     * or with <tt>"is"</tt> in case of a boolean property. If no getter method
     * is provided, the property is ignored.
     *
     * @return an immutable set of property names, or an empty set, if the entity
     * has no properties or <code>entityClass</code> was <code>null</code>.
     */
    public static Set<String> getPropertyNames(Class<? extends EntityBase> entityClass) {
        return getReadAccessors(entityClass).keySet();
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
        return getReadAccessors().containsKey(propertyName);
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
        Map<String,Method> accessors = getReadAccessors();
        if (!accessors.containsKey(propertyName)) {
            throw new NoSuchPropertyException(this, propertyName);
        }
        Method accessor = accessors.get(propertyName);
        try {
            return accessor.invoke(this, new Object[] {});
        } catch (Exception e) {
            throw new NoSuchPropertyException(this, propertyName, e);
        }
    }

    /**
     * Returns the value of a property specified by a series of {@link Expression expressions}
     * corresponding to property accessor methods, i.e. methods annotated with {@link Property},
     * or simple properties defined with {@link PropertyName}.
     *
     * @param expressions  the list of expressions specifying the property to return.
     *
     * @return the return value of the property accessor method or the value of the simple
     * property corresponding to the last of the given expressions, which may be <code>null</code>.
     * If any of the intermediate property accessors or properties returns/is <code>null</code>,
     * the result will be <code>null</code>, too.
     *
     * @throws NoSuchPropertyException  if no property matches the given series of expressions,
     * or invoking any of the property accessors failed. In the latter case, the cause
     * of the failure can be retrieved with {@link NoSuchPropertyException#getCause()}.
     */
    public Object getProperty(Expression...expressions) {
        if (expressions == null || expressions.length == 0) {
            return null;
        }
        Object o = this;
        for (int i = 0; i < expressions.length; ++i) {
            Expression expression = expressions[i];
            String[] args = expression.getArguments();
            Class<?>[] argTypes = new Class<?>[args.length];
            Arrays.fill(argTypes, String.class);
            String propertyName = expression.getName();
            try {
                Method accessor = getReadAccessor(o.getClass(), propertyName, argTypes);
                if (accessor.getAnnotation(Property.class) != null || hasProperty(propertyName)) {
                    o = accessor.invoke(o, (Object[])args);
                    if (o == null) {
                        return null;
                    }
                } else {
                    throw new NoSuchPropertyException(this, expression);
                }
            } catch (Exception e) {
                throw new NoSuchPropertyException(this, expression, e);
            }
        }
        return o;
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
        Method accessor = getWriteAccessor(propertyName, propertyType); //$NON-NLS-1$
        if (accessor == null) {
            throw new NoSuchPropertyException(this, propertyName);
        }
        try {
            accessor.invoke(this, propertyValue);
        } catch (Exception e) {
           throw new PropertyUpdateException(MessageFormat.format(
                   "Property \"{0}\" could not be updated", propertyName), e);
        }
    }

    private Map<String,Method> getReadAccessors() {
        Class<? extends EntityBase> entityClass = getClass();
        Map<String,Method> accessors = accessorsByEntityType.get(entityClass);
        if (accessors == null) {
            accessors = getReadAccessors(entityClass);
            Map<String,Method> knownAccessors = accessorsByEntityType.putIfAbsent(entityClass, accessors);
            if (knownAccessors != null) {
                accessors = knownAccessors;
            }
        }
        return accessors;
    }

    private static Map<String,Method> getReadAccessors(Class<? extends EntityBase> entityClass) {
        Map<String,Method> accessors = new HashMap<String,Method>();
        if (entityClass != null) {
            for (Field field : entityClass.getFields()) {
                if (field.getAnnotation(PropertyName.class) != null) {
                    try {
                        String propertyName = (String)field.get(null);
                        if (StringUtils.isBlank(propertyName)) {
                            throw new IllegalArgumentException(MessageFormat.format(
                                    "@PropertyName {0} defines a blank property name", field));
                        }
                        Method accessor = getReadAccessor(entityClass, propertyName, null);
                        if (accessor != null) {
                            accessors.put(propertyName, accessor);
                        }
                    } catch (Exception e) {
                        throw new IllegalArgumentException(MessageFormat.format(
                                "Invalid @PropertyName declaration: {0}", field), e);
                    }
                }
            }
        }
        return Collections.unmodifiableMap(accessors);
    }

    private static Method getReadAccessor(Class<?> c, String propertyName, Class<?>[] argTypes) {
        Method accessor = getReadAccessor(c, "get", propertyName, argTypes); //$NON-NLS-1$
        if (accessor == null) {
            accessor = getReadAccessor(c, "is", propertyName, argTypes); //$NON-NLS-1$
        }
        return accessor;
    }

    private static Method getReadAccessor(Class<?> c, String prefix, String propertyName, Class<?>[] argTypes) {
        String name = prefix + StringUtils.capitalize(propertyName);
        try {
            return c.getMethod(name, argTypes != null? argTypes : new Class[0]);
        } catch (NoSuchMethodException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Entity of type \"{0}\" has no accessor method for property \"{1}\"",
                        c.getName(), propertyName));
            }
        }
        return null;
    }

    private Method getWriteAccessor(String propertyName, Class<?> propertyType) {
        Method accessor = null;
        Class<? extends EntityBase> entityClass = getClass();
        String name = "set" + StringUtils.capitalize(propertyName); //$NON-NLS-1$
        if (propertyType != null && PRIMITIVES_MAP.containsKey(propertyType)) {
            propertyType = PRIMITIVES_MAP.get(propertyType);
        }
        Class<?>[] argumentTypes = new Class<?>[] { propertyType };
        try {
            accessor = entityClass.getMethod(name, argumentTypes);
        } catch (NoSuchMethodException e) {
            accessor = getWriteAccessor(entityClass, name, argumentTypes, propertyName);
        }
        return accessor;
    }

    private Method getWriteAccessor(Class<?> c, String name, Class<?>[] argumentTypes, String propertyName)
            throws PropertyUpdateException {
        Method accessor = null;
        try {
            for (Method candidate: c.getMethods()) {
                if (name.equals(candidate.getName())) {
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
                    accessor = c.getMethod(name, argumentTypes);
                    break;
                }
            }
        } catch (NoSuchMethodException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Entity of type \"{0}\" has no accessor method matching the signature \"{1}({2})",
                        c.getName(), name, argumentTypes[0].getName()));
            }
        }
        return accessor;
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
