package org.eclipse.skalli.services.persistence.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

public class ReflectionUtil {

    static public Set<String> getPublicStaticFinalFieldValues(Class<?> clazz) {
        Set<String> properties = new HashSet<String>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.getType().isAssignableFrom(String.class)) {
                int mod = field.getModifiers();
                if (Modifier.isPublic(mod) && Modifier.isStatic(mod) && Modifier.isFinal(mod)) {
                    try {
                        String propertieValue = field.get(null).toString();
                        if (StringUtils.isNotBlank(propertieValue)) {
                            properties.add(propertieValue);
                        }
                    } catch (Exception e) {
                        // should not happen, nothing to do
                    }
                }
            }
        }
        return properties;
    }
}
