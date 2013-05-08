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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.skalli.commons.UUIDUtils;
import org.eclipse.skalli.model.Issuer;
import org.eclipse.skalli.services.configuration.ConfigResourceBase;
import org.eclipse.skalli.services.configuration.ConfigurationService;
import org.eclipse.skalli.services.permit.Permits;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;

import com.thoughtworks.xstream.converters.Converter;
;

public class PermitResource extends ConfigResourceBase<PermitConfig> implements Issuer {

    private static final String PARAM_ID = "id"; //$NON-NLS-1$

    @Override
    protected Class<PermitConfig> getConfigClass() {
        return PermitConfig.class;
    }

    @Override
    protected PermitConfig readConfig(ConfigurationService configService, Map<String, Object> requestAttributes) {
        PermitsConfig permitsConfig = readPermitsConfig(configService);
        if (permitsConfig == null) {
            return null;
        }

        String id = (String)requestAttributes.get(PARAM_ID);
        if (UUIDUtils.isUUID(id)) {
            return permitsConfig.get(UUIDUtils.asUUID(id));
        }
        return null;
    }

    @Override
    protected void storeConfig(ConfigurationService configService, PermitConfig configObject,
            Map<String, Object> requestAttributes) {
        PermitsConfig permitsConfig = readPermitsConfig(configService);
        if (permitsConfig == null) {
            permitsConfig = new PermitsConfig();
        }
        String id = (String)requestAttributes.get(PARAM_ID);
        if (UUIDUtils.isUUID(id)) {
            configObject.setUuid(UUIDUtils.asUUID(id));
        }
        permitsConfig.add(configObject);
        storePermitsConfig(configService, permitsConfig);
    }

    @Delete
    public final Representation remove() {
        if (!Permits.isAllowed(getAction(), getPath())) {
            return createUnauthorizedRepresentation();
        }

        ConfigurationService configService = getConfigService();
        if (configService == null) {
            String errorId = MessageFormat.format(ERROR_ID_NO_CONFIGURATION_SERVICE_AVAILABLE, getPath());
            return createServiceUnavailableRepresentation(errorId, "Configuration Service");
        }

        String id = (String)getRequestAttributes().get(PARAM_ID);
        if (!UUIDUtils.isUUID(id)) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST, MessageFormat.format("''{0}'' is not a valid permit id", id));
            return null;
        }

        PermitsConfig permitsConfig = readPermitsConfig(configService);
        if (permitsConfig == null) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Permit with id ''{0}'' not found");
            return null;
        }

        PermitConfig stored = permitsConfig.remove(UUIDUtils.asUUID(id));
        if (stored == null) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Permit with id ''{0}'' not found");
            return null;
        }
        storePermitsConfig(configService, permitsConfig);

        setStatus(Status.SUCCESS_NO_CONTENT);
        return null;
    }

    private PermitsConfig readPermitsConfig(ConfigurationService configService) {
        return configService.readConfiguration(PermitsConfig.class);
    }

    private void storePermitsConfig(ConfigurationService configService, PermitsConfig configObject) {
        configService.writeConfiguration(configObject);
    }

    @Override
    protected List<Converter> getAdditionalConverters() {
        List<Converter> ret = new ArrayList<Converter>();
        ret.add(new PermitConfigConverter(getHost()));
        return ret;
    }
}
