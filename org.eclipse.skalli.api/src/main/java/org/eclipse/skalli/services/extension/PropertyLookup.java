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
package org.eclipse.skalli.services.extension;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrLookup;
import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.model.ExtensibleEntityBase;
import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.PropertyName;

/**
 * A {@link StrLookup} implementation that reflectively scans a given
 * {@link EntityBase entity}, e.g. a project, for properties.
 * <p>
 * Maps property values to the respective property names declared with
 * {@link PropertyName} annotations in the entity.
 * For {@link ExtensibleEntityBase extensible entities}, properties of
 * extensions are mapped to keys of the form <tt>extensionName.propertyName</tt>,
 * where <tt>extensionName</tt> is the {@link ExtensionService#getShortName() short name}
 * of the extension. Properties of the extensible entity itself are mapped to
 * simple <tt>propertyName</tt>.
 */
public class PropertyLookup extends StrLookup {

    private final HashMap<String, Object> properties = new HashMap<String, Object>();

    /**
     * Creates a property lookup instance from a given property map.
     * The given properties are mapped to placeholders by their respective keys.
     *
     * @param @param customProperties  optional custom properties, or <code>null</code>.
     */
    public PropertyLookup(Map<String, ?> customProperties) {
        this(null, customProperties);
    }

    /**
     * Creates a property lookup instance for a given entity, e.g. a {@link Project}.
     * Properties of the entity are scanned and provided as placeholders for the lookup.
     *
     * @param entity  the entity, for which to create a property lookup.
     */
    public PropertyLookup(EntityBase entity) {
        this(entity, null);
    }

    /**
     * Creates a property lookup instance for a given entity, e.g. a {@link Project}.
     * Properties of the entity are scanned and provided as placeholders for the lookup.
     * If the entity is {@link ExtensibleEntityBase extensible}, extensions are scanned,
     * too, and added as placeholders of the form <tt>${extensionName.propertyName}</tt>.
     * The given custom properties are mapped to placeholders by their respective keys
     * and provided for the lookup as well.
     *
     * @param entity  the entity, for which to create a property lookup.
     * @param customProperties  optional custom properties, or <code>null</code>.
     */
    public PropertyLookup(EntityBase entity, Map<String, ?> customProperties) {
        if (entity != null) {
            putAllProperties(entity, ""); //$NON-NLS-1$
        }
        if (customProperties != null) {
            properties.putAll(customProperties);
        }
    }

    /**
     * Adds all properties of the given entity to this property lookup.
     * Property names of the entity are scanned and provided as placeholders of the form
     * <tt>${prefix.propertyName}</tt> for the lookup. If the prefix is blank, placeholders
     * of the form <tt>${propertyName}</tt> are used.
     * Extensions of the entity are scanned, too, and added as placeholders of the form
     * <tt>${prefix.extensionName.propertyName}</tt>. If the prefix is blank, placeholders are
     * named <tt>${extensionName.propertyName}</tt>, respectively.
     *
     * @param entity  the entity to add to this property lookup.
     * @param prefix  the prefix for building placeholders.
     */
    public void putAllProperties(EntityBase entity, String prefix) {
        prefix = StringUtils.removeEnd(prefix, "."); //$NON-NLS-1$
        if (entity instanceof ExtensibleEntityBase) {
            putAllProperties((ExtensibleEntityBase) entity, prefix);
        }
        for (String propertyName : entity.getPropertyNames()) {
            properties.put(concat(prefix, propertyName), entity.getProperty(propertyName));
        }
    }

    private void putAllProperties(ExtensibleEntityBase extensible, String prefix) {
        for (ExtensionEntityBase extension : extensible.getAllExtensions()) {
            ExtensionService<?> extensionService = ExtensionServices.getByExtensionClass(extension.getClass());
            if (extensionService != null) {
                putAllProperties(extension, concat(prefix, extensionService.getShortName()));
            }
        }
    }

    private String concat(String prefix, String s) {
        StringBuilder sb = new StringBuilder(prefix);
        if (sb.length() > 0) {
            sb.append('.');
        }
        sb.append(s);
        return sb.toString();
    }

    /**
     * Returns the properties available for lookup as map.
     */
    public Map<String, Object> asMap() {
        return properties;
    }

    /**
     * Returns the number of properties available for the lookup.
     */
    public int size() {
        return properties.size();
    }

    /**
     * Returns <tt>true</tt> if this lookup contains no properties at all.
     */
    public boolean isEmpty() {
        return properties.isEmpty();
    }

    /**
     * Returns <tt>true</tt> if this lookup contains a property with the given key.
     *
     * @param key  the key of the property (without the surrounding
     * <tt>${...}</tt> of the placeholder).
     */
    public boolean containsKey(String key) {
        return properties.containsKey(key);
    }

    /**
     * Returns <tt>true</tt> if this lookup contains a given property value.
     *
     * @param value  the property value to search for.
     */
    public boolean containsValue(Object value) {
        return properties.containsValue(value);
    }

    /**
     * Returns all known property keys
     *
     * @return a set of keys (equivalent to the placeholders, but without the surrounding
     * <tt>${...}</tt>), or an empty set.
     */
    public Set<String> keySet() {
        return properties.keySet();
    }

    /**
     * Returns the property for the given key.
     *
     * @param key the key of the property to return (equivalent to the placeholder,
     * but without the surrounding <tt>${...}</tt>).
     *
     * @return  the value of the property, or <code>nully</code> if there is no property
     * with the given key.
     */
    public Object get(String key) {
        return properties.get(key);
    }

    /**
     * Returns a collection of all property values.
     * @return the collection of property values, or an empty collection.
     */
    public Collection<Object> values() {
        return properties.values();
    }

    /**
     * Returns an entry set of all properties.
     * @return an entry set of properties, or an empty set.
     */
    public Set<Map.Entry<String, Object>> entrySet() {
        return properties.entrySet();
    }

    @Override
    public String lookup(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        Object o = properties.get(key);
        if (o == null) {
            return null;
        }
        if (o instanceof Collection) {
            return CollectionUtils.toString((Collection<?>) o, ',');
        }
        return o.toString();
    }
}
