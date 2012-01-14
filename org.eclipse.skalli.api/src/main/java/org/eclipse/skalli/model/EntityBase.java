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

import java.util.UUID;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang.StringUtils;

/**
 * Abstract base class for all uniquely indentifiable model entities.
 * This class defines one property, <code>uuid</code>, that must be a
 * globally unique identifier. Once set, the <code>uuid</code> is immutable.
 */
public abstract class EntityBase implements Comparable<Object> {

    @PropertyName
    public static final String PROPERTY_UUID = "uuid"; //$NON-NLS-1$

    @PropertyName
    public static final String PROPERTY_DELETED = "deleted"; //$NON-NLS-1$

    @PropertyName
    public static final String PROPERTY_PARENT_ENTITY = "parentEntity"; //$NON-NLS-1$

    @Derived
    @PropertyName
    public static final String PROPERTY_PARENT_ENTITY_ID = "parentEntityId"; //$NON-NLS-1$

    @Derived
    @PropertyName
    public static final String PROPERTY_LAST_MODIFIED = "lastModified"; //$NON-NLS-1$

    @Derived
    @PropertyName
    public static final String PROPERTY_LAST_MODIFIED_BY = "lastModifiedBy"; //$NON-NLS-1$

    /**
     * The unique identifier of a project - created once, never changed!
     * @see org.eclipse.skalli.core.internal.persistence.xstream.PersistenceServiceXStream#persist(EntityBase,String)
     */
    private UUID uuid;

    /**
     * Deleted entities are ignored during loading of the model.
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
     * Date of last modification.
     */
    private transient String lastModified;

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
     * method does not change the unique identifier, if it already
     * has been set.
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

    // for testing purposes, see PropertyHelperUtils#TestEntityBase...
    protected void setParentEntityId(UUID parentEntityId) {
        this.parentEntityId = parentEntityId;
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
     * Sets the date/time of the last modification. Note, this method
     * should not be called directly. The date/time of the last modification
     * is set automatically when the entity is persisted.
     *
     * @param lastModified  an ISO8061-compliant date/time string following the
     * pattern <tt>[-]CCYY-MM-DDThh:mm:ss[Z|(+|-)hh:mm]</tt> as defined for
     * type <tt>xsd:dateTime</tt>) in <tt>"XML Schema Part 2: Datatypes"</tt>,
     * or <code>null</code>.
     *
     * @throws IllegalArgumentException  if the date/time string does not conform to
     * type <tt>xsd:dateTime</tt>.
     */
    public void setLastModified(String lastModified) {
        if (StringUtils.isBlank(lastModified)) {
            this.lastModified = null;
        } else {
            DatatypeConverter.parseDateTime(lastModified);
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
     * Sets the unique identifier of the last modifier. Note, this method
     * should not be called directly. The last modifier is set automatically
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

    @Override
    public String toString() {
        if (uuid != null) {
            return uuid.toString();
        }
        return super.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
        return result;
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

    @Override
    public int compareTo(Object o) {
        if (o != null) {
            int ret = 0;
            if (ret == 0) {
                ret = this.getClass().getName().compareTo(o.getClass().getName());
            }
            return ret;
        } else {
            return -1;
        }
    }
}
