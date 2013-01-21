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
package org.eclipse.skalli.core.permit;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.skalli.services.configuration.ConfigurationService;
import org.eclipse.skalli.services.configuration.rest.CustomizingResource;
import org.eclipse.skalli.services.permit.Permits;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.converters.Converter;


public class PermitsResource extends CustomizingResource<PermitsConfig> {

    public static final String MAPPINGS_KEY = "permissions"; //$NON-NLS-1$

    @Override
    protected String getKey() {
        return MAPPINGS_KEY;
    }

    @Override
    protected Class<PermitsConfig> getConfigClass() {
        return PermitsConfig.class;
    }

    @Override
    protected List<Class<?>> getAdditionalConfigClasses() {
        List<Class<?>> ret = new ArrayList<Class<?>>();
        ret.add(PermitConfig.class);
        return ret;
    }

    @Override
    protected List<Converter> getAdditionalConverters() {
        List<Converter> ret = new ArrayList<Converter>();
        ret.add(new PermitConfigConverter(getHost()));
        ret.add(new PermitsConfigConverter());
        return ret;
    }

    @Post
    public final Representation add(Representation entity) {
        if (!Permits.isAllowed(getAction(), getPath())) {
            return createUnauthorizedRepresentation();
        }

        ConfigurationService configService = getConfigService();
        if (configService == null) {
            String errorId = MessageFormat.format(ERROR_ID_NO_CONFIGURATION_SERVICE_AVAILABLE, getPath());
            return createServiceUnavailableRepresentation(errorId, "Configuration Service");
        }

        PermitsConfig permitsConfig = readConfig(configService, getRequestAttributes());
        if (permitsConfig == null) {
            permitsConfig = new PermitsConfig();
        }

        try {
            XStream xstream = getXStream();
            PermitConfig configObject = (PermitConfig) xstream.fromXML(entity.getText());

            UUID uuid = configObject.getUuid();
            if (uuid != null && permitsConfig.get(uuid) != null) {
                setStatus(Status.CLIENT_ERROR_CONFLICT, MessageFormat.format("Permit with id ''{0}'' already exists", uuid));
                return null;
            }
            permitsConfig.add(configObject);
            storeConfig(configService, permitsConfig, getRequestAttributes());
            setStatus(Status.SUCCESS_CREATED);
            return null;

        } catch (XStreamException e) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST, MessageFormat.format("Invalid permit configuration: {0}", e.getMessage()));
            return null;
        } catch (IOException e) {
            String errorId = MessageFormat.format(ERROR_ID_IO_ERROR, getPath());
            return createIOErrorRepresentation(errorId, e);
        } catch (Exception e) {
            String errorId = MessageFormat.format(ERROR_ID_UNEXPECTED, getPath());
            return createUnexpectedErrorRepresentation(errorId, e);
        }
    }
}
