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
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.configuration.ConfigurationService;
import org.eclipse.skalli.services.extension.rest.ResourceBase;
import org.eclipse.skalli.services.extension.rest.ResourceRepresentation;
import org.eclipse.skalli.services.permit.Permits;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Put;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.converters.Converter;

public abstract class ConfigResourceBase<T> extends ResourceBase {

    protected static final String ID_PREFIX = "rest:api/config/{0}:"; //$NON-NLS-1$
    protected static final String ERROR_ID_UNEXPECTED = ID_PREFIX + "00"; //$NON-NLS-1$
    protected static final String ERROR_ID_IO_ERROR = ID_PREFIX + "10"; //$NON-NLS-1$
    protected static final String ERROR_ID_NO_CONFIGURATION_SERVICE_AVAILABLE = ID_PREFIX + "20"; //$NON-NLS-1$
    protected static final String ERROR_VALIDATION_FAILED = ID_PREFIX + "30"; //$NON-NLS-1$
    protected static final String WARN_ISSUES = ID_PREFIX + "40"; //$NON-NLS-1$
    protected static final String ERROR_INVALID_XML = ID_PREFIX + "50"; //$NON-NLS-1$

    /**
     * Defines the class that contains all configuration parameters and will be represented in the REST API.
     * @return
     */
    protected abstract Class<T> getConfigClass();

    /**
     * Returns classes (in addition to  {@link #getConfigClass()}) that should be known to
     * (and parsed for annotations by) the serializer.
     * @return a list of classes, or an empty list.
     */
    protected List<Class<?>> getAdditionalConfigClasses() {
        return Collections.emptyList();
    };

    /**
     * Returns additional {@link #Converter converters}
     * @return a list of converters, or an empty list.
     */
    protected List<Converter> getAdditionalConverters() {
        return Collections.emptyList();
    }

    protected abstract T readConfig(ConfigurationService configService, Map<String,Object> requestAttributes);

    protected abstract void storeConfig(ConfigurationService configService, T configObject,
            Map<String,Object> requestAttributes);

    protected XStream getXStream() {
        XStream xstream = new XStream();
        xstream.setClassLoader(this.getClass().getClassLoader());
        xstream.processAnnotations(getConfigClass());
        for (Class<?> additionalClass : getAdditionalConfigClasses()) {
            xstream.processAnnotations(additionalClass);
        }
        for (Converter converter : getAdditionalConverters()) {
            xstream.registerConverter(converter);
        }
        return xstream;
    }

    protected ConfigurationService getConfigService() {
        ConfigurationService configService = Services.getService(ConfigurationService.class);
        return configService;
    }

    @Get
    public final Representation retrieve() {
        if (!Permits.isAllowed(getAction(), getPath())) {
            return createUnauthorizedRepresentation();
        }

        ConfigurationService configService = getConfigService();
        if (configService == null) {
            String errorId = MessageFormat.format(ERROR_ID_NO_CONFIGURATION_SERVICE_AVAILABLE, getPath());
            return createServiceUnavailableRepresentation(errorId, "Configuration Service");
        }

        T config = readConfig(configService, getRequestAttributes());
        if (config == null) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND, MessageFormat.format("Configuration {0} not found", getPath()));
            return null;
        }

        try {
            Protector.protect(config, getAdditionalConfigClasses());
        } catch (Exception e) {
            String errorId = MessageFormat.format(ERROR_ID_UNEXPECTED, getPath());
            createUnexpectedErrorRepresentation(errorId, e);
        }

        ResourceRepresentation<T> representation = new ResourceRepresentation<T>(config);
        representation.setXStream(getXStream());
        return representation;
    }

    @Put
    public final Representation store(Representation entity) {
        if (!Permits.isAllowed(getAction(), getPath())) {
            return createUnauthorizedRepresentation();
        }

        ConfigurationService configService = getConfigService();
        if (configService == null) {
            String errorId = MessageFormat.format(ERROR_ID_NO_CONFIGURATION_SERVICE_AVAILABLE, getPath());
            return createServiceUnavailableRepresentation(errorId, "Configuration Service");
        }

        try {
            XStream xstream = getXStream();
            @SuppressWarnings("unchecked")
            T configObject = (T) xstream.fromXML(entity.getText());
            SortedSet<Issue> issues = validate(configObject, Permits.getLoggedInUser());
            if (Issue.hasFatalIssues(issues)) {
                String errorId = MessageFormat.format(ERROR_VALIDATION_FAILED, getPath());
                return createErrorRepresentation(Status.CLIENT_ERROR_BAD_REQUEST,
                        errorId, Issue.getMessage("Invalid configuration", issues));
            } else {
                storeConfig(configService, configObject, getRequestAttributes());
                if (issues.size() > 0) {
                    String errorId = MessageFormat.format(WARN_ISSUES, getPath());
                    return createErrorRepresentation(Status.SUCCESS_OK, errorId,
                            Issue.getMessage("Configuration stored but has the following issues: ", issues));
                }
                setStatus(Status.SUCCESS_NO_CONTENT, "Configuration successfully stored");
                return null;
            }
        } catch (XStreamException e) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST, MessageFormat.format(
                    "Configuration is invalid: {0}", e.getMessage()));
            return null;
        } catch (IOException e) {
            String errorId = MessageFormat.format(ERROR_ID_IO_ERROR, getPath());
            return createIOErrorRepresentation(errorId, e);
        } catch (Exception e) {
            String errorId = MessageFormat.format(ERROR_ID_UNEXPECTED, getPath());
            return createUnexpectedErrorRepresentation(errorId, e);
        }
    }

    protected SortedSet<Issue> validate(T configObject, String loggedInUser) {
        return CollectionUtils.emptySortedSet();
    }
}
