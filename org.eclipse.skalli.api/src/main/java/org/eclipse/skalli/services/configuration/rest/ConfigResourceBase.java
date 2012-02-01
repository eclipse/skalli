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
package org.eclipse.skalli.services.configuration.rest;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.configuration.ConfigurationService;
import org.eclipse.skalli.services.extension.rest.ResourceRepresentation;
import org.eclipse.skalli.services.group.GroupUtils;
import org.eclipse.skalli.services.user.LoginUtils;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.servlet.ServletUtils;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

public abstract class ConfigResourceBase<T> extends ServerResource {

    static class ProtectionException extends Exception {

        private static final long serialVersionUID = 6330696492753725531L;

        public ProtectionException(String message, Throwable cause) {
            super(message, cause);
        }

        public ProtectionException(String message) {
            super(message);
        }

    }

    static class ProtectionHelper {

        public static final String PROTECTION_VALUE_STRING = null;
        public static final Character PROTECTION_VALUE_CHAR = ' ';
        public static final Character PROTECTION_VALUE_CHARACTER = null;
        public static final boolean PROTECTION_VALUE_boolean = false;
        public static final Boolean PROTECTION_VALUE_BOOLEAN = null;

        public static void protect(Object obj, List<Class<?>> relevantClasses) throws ProtectionException {
            if (obj == null) {
                return;
            }
            List<Class<?>> list = new ArrayList<Class<?>>();
            if (relevantClasses != null) {
                list.addAll(relevantClasses);
            }
            list.add(obj.getClass());
            protect(obj, obj.getClass(), list);
        }

        /**
         * Obfuscates all primitive, string-like and iterable fields of the given object (and its superclasses) that are annotated with {@link Protect}.
         * If a field is {@link Iterable iterable}, all entries are obfuscated, too.
         *
         * Fields of the superclass are protected too.
         *
         * @param obj the object to protected.
         * @param relevantClasses all classes
         *
         * @throws ProtectionException is thrown when a {@link Protect} annotation is found on a field that cannot be obfuscated, either
         * because its type does not allow obfuscation, or there is no suitable setter for that field
         */
        private static void protect_(Object obj, List<Class<?>> relevantClasses) throws ProtectionException {
            if (obj == null) {
                return;
            }
            protect(obj, obj.getClass(), relevantClasses);
        }

        private static void protect(Object obj, Class<? extends Object> clazz, List<Class<?>> relevantClasses)
                throws ProtectionException {
            if (obj instanceof Iterable) {
                for (Object element : (Iterable) obj) {
                    protect_(element, relevantClasses);
                }
            }
            else if (!isRelevantClass(clazz, relevantClasses)) {
                // we can stop to search for Protected annotations
                return;
            }
            else { // if you would like to expand the protection to more sophisticated field types - here would be the right place
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

        private static boolean isRelevantClass(Class<? extends Object> clazz, List<Class<?>> relevantClasses) {
            for (Class<?> relevatClass : relevantClasses) {
                if (relevatClass.isAssignableFrom(clazz)) {
                    return true;
                }
            }
            return false;
        }

        private static void protectField(Field field, Object obj, List<Class<?>> relevantClasses)
                throws ProtectionException {
            if (field.isAnnotationPresent(Protect.class)) {
                updateProtecionValue(field, obj);
            } else if (!field.getType().isPrimitive()) {
                protect_(getFieldValue(field, obj), relevantClasses);
            }
        }

        private static Object getFieldValue(Field field, Object obj) throws ProtectionException {
            field.setAccessible(true);
            try {
                return field.get(obj);
            } catch (Exception e) {
                throw new ProtectionException("Can't get " + field.getName() + " from Object of type " + obj.getClass());
            }
        }

        private static void updateProtecionValue(Field field, Object obj) throws ProtectionException {
            field.setAccessible(true);
            try {
                field.set(obj, getProtectionValue(field));
            } catch (Exception e) {
                throw new ProtectionException("Annotation " + Protect.class.getName() + " set to " + field.getName()
                        + ", but " + field.getType() + " cant be updated.", e);
            }
        }

        private static Object getProtectionValue(Field field) throws ProtectionException {
            Object protectionValue = null;
            if (field.getType().isAssignableFrom(String.class)) {
                protectionValue = PROTECTION_VALUE_STRING;
            } else if (field.getType().isAssignableFrom(Boolean.class)) {
                protectionValue = PROTECTION_VALUE_BOOLEAN;
            } else if (field.getType().isAssignableFrom(boolean.class)) {
                protectionValue = PROTECTION_VALUE_boolean;
            } else if (field.getType().isAssignableFrom(Character.class)) {
                protectionValue = PROTECTION_VALUE_CHARACTER;
            } else if (field.getType().isAssignableFrom(byte.class) || field.getType().isAssignableFrom(char.class)) {
                protectionValue = PROTECTION_VALUE_CHAR;
            } else {
                protectionValue = null;
            }
            return protectionValue;
        }
    }

    private final static Logger LOG = LoggerFactory.getLogger(ConfigResourceBase.class);

    /**
     * Defines the class that contains all configuration parameters and will be represented in the REST API.
     * @return
     */
    protected abstract Class<T> getConfigClass();

    /**
     * Defines classes (in addition to  {@link #getConfigClass()}) that should be known to (and parsed for annotations by) the serializer.
     * @return
     */
    protected List<Class<?>> getAdditionalConfigClasses() {
        return Collections.emptyList();
    };

    protected abstract T readConfig(ConfigurationService configService);

    protected abstract void storeConfig(ConfigurationService configService, T configObject);

    protected XStream getXStream() {
        XStream xstream = new XStream();
        xstream.setClassLoader(this.getClass().getClassLoader());
        xstream.processAnnotations(getConfigClass());
        return xstream;
    }

    protected ConfigurationService getConfigService() {
        ConfigurationService configService = Services.getService(ConfigurationService.class);
        return configService;
    }

    private final Representation checkAdminAuthorization() {
        LoginUtils loginUtils = new LoginUtils(ServletUtils.getRequest(getRequest()));
        String loggedInUser = loginUtils.getLoggedInUserId();
        if (!GroupUtils.isAdministrator(loggedInUser)) {
            String msg = "Access denied for user " + loggedInUser;
            Representation result = new StringRepresentation(msg, MediaType.TEXT_PLAIN);
            setStatus(Status.CLIENT_ERROR_FORBIDDEN, msg);
            return result;
        }
        return null;
    }

    @Get
    public final Representation retrieve() {
        Representation ret = checkAdminAuthorization();
        if (ret != null) {
            return ret;
        }

        ConfigurationService configService = getConfigService();
        if (configService != null) {
            T config = readConfig(configService);
            try {
                ProtectionHelper.protect(config, getAdditionalConfigClasses());
            } catch (ProtectionException e) {
                LOG.error("Problems protecting config:", e);
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
                return new StringRepresentation("Failed to retrieve configuration", MediaType.TEXT_PLAIN);
            }
            ResourceRepresentation<T> representation = new ResourceRepresentation<T>(config);
            representation.setXStream(getXStream());
            return representation;
        } else {
            String message = "Failed to read configuration (" + getConfigClass().getSimpleName()
                    + ") - no instance of " + ConfigurationService.class.getName() + "available";
            LOG.warn(message);
            return new StringRepresentation(message, MediaType.TEXT_PLAIN);
        }
    }

    @Put
    public final Representation store(Representation entity) {
        Representation result = checkAdminAuthorization();
        if (result != null) {
            return result;
        }

        try {
            ConfigurationService configService = getConfigService();
            if (configService != null) {
                XStream xstream = getXStream();
                T configObject = (T) xstream.fromXML(entity.getText());
                storeConfig(configService, configObject);
                result = new StringRepresentation("Configuration successfully stored", MediaType.TEXT_PLAIN);
            } else {
                LOG.warn("Failed to store configuration - no instance of " + ConfigurationService.class.getName() + "available"); //$NON-NLS-1$
                result = new StringRepresentation("Failed to store configuration", MediaType.TEXT_PLAIN);
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

}
