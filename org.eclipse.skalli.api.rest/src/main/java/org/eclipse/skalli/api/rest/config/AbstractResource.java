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
package org.eclipse.skalli.api.rest.config;

import java.io.IOException;

import org.eclipse.skalli.api.java.authentication.LoginUtil;
import org.eclipse.skalli.api.java.authentication.UserUtil;
import org.eclipse.skalli.api.rest.ResourceRepresentation;
import org.eclipse.skalli.common.Services;
import org.eclipse.skalli.common.configuration.ConfigurationService;
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

public abstract class AbstractResource<T> extends ServerResource {
    private final static Logger LOG = LoggerFactory.getLogger(AbstractResource.class);

    /**
     * Defines the class that contains all configuration parameters and will be represented in the REST API.
     * @return
     */
    protected abstract Class<T> getConfigClass();

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
        LoginUtil loginUtil = new LoginUtil(ServletUtils.getRequest(getRequest()));
        String loggedInUser = loginUtil.getLoggedInUserId();
        if (!UserUtil.isAdministrator(loggedInUser)) {
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
