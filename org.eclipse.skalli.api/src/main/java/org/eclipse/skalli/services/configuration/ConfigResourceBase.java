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
package org.eclipse.skalli.services.configuration;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.services.Services;
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

/**
 * Base class for REST resources representing configurations. Derived classes must
 * implement {@link #getConfigClass()} providing the configuration class this REST resource
 * is associated with.
 * <br>
 * Note, the class that represents the configuration in the underlying storage may be different
 * from that representing the REST resource. In that case, derived classes should overwrite the
 * {@link #storeConfig(ConfigurationService, Object, Map)} and {@link #readConfig(ConfigurationService, Map)}
 * methods to provide the necessary mapping.
 *
 * @param <T>  the configuration class associated with this REST resource.
 */
public abstract class ConfigResourceBase<T> extends ResourceBase {

    protected static final String ID_PREFIX = "rest:api/config/{0}:"; //$NON-NLS-1$
    protected static final String ERROR_ID_UNEXPECTED = ID_PREFIX + "00"; //$NON-NLS-1$
    protected static final String ERROR_ID_IO_ERROR = ID_PREFIX + "10"; //$NON-NLS-1$
    protected static final String ERROR_ID_NO_CONFIGURATION_SERVICE_AVAILABLE = ID_PREFIX + "20"; //$NON-NLS-1$
    protected static final String ERROR_VALIDATION_FAILED = ID_PREFIX + "30"; //$NON-NLS-1$
    protected static final String WARN_ISSUES = ID_PREFIX + "40"; //$NON-NLS-1$
    protected static final String ERROR_INVALID_SYNTAX = ID_PREFIX + "50"; //$NON-NLS-1$
    protected static final String ERROR_ID_PROTECT_FAILED = ID_PREFIX + "60"; //$NON-NLS-1$

    /**
     * Returns the configuration class represented by this REST resource.
     */
    protected abstract Class<T> getConfigClass();

    /**
     * Returns additional classes (e.g. classes referenced in fields of the configuration class)
     * that should be parsed for {@link com.thoughtworks.xstream.annotations XStream annotations}
     * during marshaling/unmarshaling of this REST resource.
     * <br>
     * This implementation always returns an empty list.
     *
     * @return a list of additional classes, or an empty list.
     */
    protected List<Class<?>> getAdditionalConfigClasses() {
        return Collections.emptyList();
    };

    /**
     * Returns additional {@link #Converter XStream converters} to apply during marshaling/unmarshaling
     * of this REST resource.
     * <br>
     * This implementation always returns an empty list.
     *
     * @return a list of converters, or an empty list.
     */
    protected List<Converter> getAdditionalConverters() {
        return Collections.emptyList();
    }

    /**
     * Reads the configuration associated with this REST resource from the underlying storage.
     * This implementation assumes that the class of the configuration in the storage
     * is the same that represents this REST resource (i.e. {@link #getConfigClass()}).
     *
     * @param configService  the configuration service to read from.
     * @param requestAttributes  optional attributes extracted from the REST request.
     *
     * @return  the configuration stored in the underlying storage service, or <code>null</code>
     * if no such configuration exists.
     */
    protected T readConfig(ConfigurationService configService, Map<String, Object> requestAttributes) {
        return configService.readConfiguration(getConfigClass());
    }

    /**
     * Stores the configuration associated with this REST resource in the underlying storage.
     * This implementation assumes that the class of the configuration in the storage
     * is the same that represents this REST resource (i.e. {@link #getConfigClass()}).
     *
     * @param configService  the configuration service to write to.
     * @param config  the configuration to store.
     * @param requestAttributes  optional attributes extracted from the REST request.
     */
    protected void storeConfig(ConfigurationService configService, T config, Map<String, Object> requestAttributes) {
        configService.writeConfiguration(config);
    }

    /**
     * Returns a preconfigured {@link XStream} instance suitable for marshaling/unmarshaling of
     * the configuration represented by this REST resource to/from the underlying storage.
     * <br>
     * Configures necessary class loaders and registers additional {@link #getAdditionalConverters()
     * XStream converters}. Registers additional classes that should be parsed for
     * {@link com.thoughtworks.xstream.annotations XStream annotations}.
     *
     * @return a preconfigured XStream instance, never <code>null</code>.
     */
    protected XStream getXStream() {
        XStream xstream = new XStream();
        xstream.setClassLoader(getClass().getClassLoader());
        xstream.processAnnotations(getConfigClass());
        for (Class<?> additionalClass : getAdditionalConfigClasses()) {
            xstream.processAnnotations(additionalClass);
        }
        for (Converter converter : getAdditionalConverters()) {
            xstream.registerConverter(converter);
        }
        return xstream;
    }

    /**
     * Validates the given configuration prior to storage.
     * <br>
     * This implementation always returns an empty set of issues.
     *
     * @param configuration  the configuration to validate.
     * @param loggedInUser  the unique identifier of the currently logged in user,
     * or <code>null</code> if the user is anonymous.
     *
     * @return  the issues found during the validation, or an empty set.
     */
    protected SortedSet<Issue> validate(T configuration, String loggedInUser) {
        return CollectionUtils.emptySortedSet();
    }

    /**
     * Returns the currently active configuration service. This method must
     * not be called directly and not be overwritten except for testing purposes.
     */
    protected ConfigurationService getConfigService() {
        return Services.getService(ConfigurationService.class);
    }

    /**
     * Handler for GET requests routed to this REST resource.
     * <br>
     * Possible response codes:
     * <ul>
     * <li>200 OK &mdash; requested configuration found and returned in response body.</li>
     * <li>403 FORBIDDEN &mdash; the logged in user has not the necessary permits to access this resource.</li>
     * <li>404 NOT FOUND &mdash; requested configuration does not exist or could not be read from storage.</li>
     * <li>500 SERVER ERROR with error id <tt>"rest:api/config/&lt;id&gt;:20"</tt> &mdash; no
     * configuration service available.</li>
     * <li>500 SERVER ERROR with error id <tt>"rest:api/config/&lt;id&gt;:60"</tt> &mdash; obfuscating
     * of protected fields in the response failed.</li>
     * </ul>
     *
     * @return  the REST representation of the requested configuration, or an error representation
     * if the configuration could not be read, or <code>null</code> if the configuration does not exist.
     */
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
            String errorId = MessageFormat.format(ERROR_ID_PROTECT_FAILED, getPath());
            createUnexpectedErrorRepresentation(errorId, e);
        }

        ResourceRepresentation<T> representation = new ResourceRepresentation<T>(config);
        representation.setXStream(getXStream());
        return representation;
    }

    /**
     * Handler for PUT requests routed to this REST resource.
     * <br>
     * Possible response codes:
     * <ul>
     * <li>200 OK &mdash; configuration has been stored successfully, but some issues have been found
     * during validation. Response body contains an error representation with id
     * <tt>"rest:api/config/&lt;id&gt;:40"</tt>. The error detail message lists the found issues.</li>
     * <li>204 NO CONTENT &mdash; configuration is valid and has been stored successfully.</li>
     * <li>400 BAD REQUEST with error id <tt>"rest:api/config/&lt;id&gt;:30"</tt> &mdash;
     * configuration is not valid. The error detail message lists the found fatal issues.</li>
     * <li>400 BAD REQUEST with error id <tt>"rest:api/config/&lt;id&gt;:50"</tt> &mdash;
     * request body is not parsable due to malformed syntax.</li>
     * <li>403 FORBIDDEN &mdash; the logged in user has not the necessary permits to access this resource.</li>
     * <li>500 SERVER ERROR with error id <tt>"rest:api/config/&lt;id&gt;:00"</tt> &mdash; a severe
     * error occured during request processing. See server log for details.</li>
     * <li>500 SERVER ERROR with error id <tt>"rest:api/config/&lt;id&gt;:10"</tt> &mdash; an i/o error
     * occured during request processing. See server log for details.</li>
     * <li>500 SERVER ERROR with error id <tt>"rest:api/config/&lt;id&gt;:20"</tt> &mdash; no
     * configuration service available.</li>
     * </ul>
     * @param entity  the request entity to be interpreted as configuration.
     * @return  an error representation, if the configuration could not be stored or issues have
     * been found, <code>null</code> otherwise.
     */
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
            T config = getConfigClass().cast(xstream.fromXML(entity.getText()));
            SortedSet<Issue> issues = validate(config, Permits.getLoggedInUser());
            if (Issue.hasFatalIssues(issues)) {
                String errorId = MessageFormat.format(ERROR_VALIDATION_FAILED, getPath());
                return createErrorRepresentation(Status.CLIENT_ERROR_BAD_REQUEST,
                        errorId, Issue.getMessage("Invalid configuration", issues));
            } else {
                storeConfig(configService, config, getRequestAttributes());
                if (issues.size() > 0) {
                    String errorId = MessageFormat.format(WARN_ISSUES, getPath());
                    return createErrorRepresentation(Status.SUCCESS_OK, errorId,
                            Issue.getMessage("Configuration stored but has the following issues: ", issues));
                }
                setStatus(Status.SUCCESS_NO_CONTENT, "Configuration successfully stored");
                return null;
            }
        } catch (XStreamException e) {
            String errorId = MessageFormat.format(ERROR_INVALID_SYNTAX, getPath());
            return createErrorRepresentation(Status.CLIENT_ERROR_BAD_REQUEST, errorId, MessageFormat.format(
                    "Request could not be understood due to malformed syntax: {0}", e.getMessage()));
        } catch (IOException e) {
            String errorId = MessageFormat.format(ERROR_ID_IO_ERROR, getPath());
            return createIOErrorRepresentation(errorId, e);
        } catch (Exception e) {
            String errorId = MessageFormat.format(ERROR_ID_UNEXPECTED, getPath());
            return createUnexpectedErrorRepresentation(errorId, e);
        }
    }
}
