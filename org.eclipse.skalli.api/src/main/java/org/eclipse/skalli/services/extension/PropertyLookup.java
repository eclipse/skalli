package org.eclipse.skalli.services.extension;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrLookup;
import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.model.ExtensibleEntityBase;
import org.eclipse.skalli.model.ExtensionEntityBase;

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
            ExtensionService<?> extensionService = getExtensionService(extension);
            if (extensionService != null) {
                prefix = concat(prefix, extensionService.getShortName());
                putAllProperties(extension, prefix);
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

    protected ExtensionService<?> getExtensionService(ExtensionEntityBase extension) {
        return ExtensionServices.getExtensionService(extension.getClass());
    }
}
