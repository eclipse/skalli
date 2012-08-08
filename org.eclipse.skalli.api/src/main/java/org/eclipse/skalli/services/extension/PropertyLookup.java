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

    public PropertyLookup(EntityBase entity) {
        this(entity, null);
    }

    public PropertyLookup(EntityBase entity, Map<String, Object> customProperties) {
        if (entity != null) {
            putAllProperties(entity, ""); //$NON-NLS-1$
        }
        if (customProperties != null) {
            properties.putAll(customProperties);
        }
    }

    private void putAllProperties(EntityBase entity, String prefix) {
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

    public int size() {
        return properties.size();
    }

    public boolean isEmpty() {
        return properties.isEmpty();
    }

    public boolean containsKey(String key) {
        return properties.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return properties.containsValue(value);
    }

    public Set<String> keySet() {
        return properties.keySet();
    }

    public Object get(String key) {
        return properties.get(key);
    }

    public Collection<Object> values() {
        return properties.values();
    }

    public Set<Map.Entry<String,Object>> entrySet() {
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
