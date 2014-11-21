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
package org.eclipse.skalli.core.role;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.services.configuration.ConfigurationService;
import org.eclipse.skalli.services.configuration.EventConfigUpdate;
import org.eclipse.skalli.services.event.EventListener;
import org.eclipse.skalli.services.event.EventService;
import org.eclipse.skalli.services.permit.PermitService;
import org.eclipse.skalli.services.role.RoleService;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalRoleComponent implements RoleService, EventListener<EventConfigUpdate> {

    private static final Logger LOG = LoggerFactory.getLogger(LocalRoleComponent.class);

    private ComponentContext context;
    private ConfigurationService configService;

    protected void activate(ComponentContext context) {
        this.context = context;
        invalidatePermits();
        LOG.info(MessageFormat.format("[RoleService][local] {0} : activated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void deactivate(ComponentContext context) {
        invalidatePermits();
        this.context = null;
        LOG.info(MessageFormat.format("[RoleService][local] {0} : deactivated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void bindConfigurationService(ConfigurationService configService) {
        LOG.info(MessageFormat.format("bindConfigurationService({0})", configService)); //$NON-NLS-1$
        this.configService = configService;
        invalidatePermits();
    }

    protected void unbindConfigurationService(ConfigurationService configService) {
        LOG.info(MessageFormat.format("unbindConfigurationService({0})", configService)); //$NON-NLS-1$
        this.configService = null;
        invalidatePermits();
    }

    protected void bindEventService(EventService eventService) {
        LOG.info(MessageFormat.format("bindEventService({0})", eventService)); //$NON-NLS-1$
        eventService.registerListener(EventConfigUpdate.class, this);
    }

    protected void unbindEventService(EventService eventService) {
        LOG.info(MessageFormat.format("unbindEventService({0})", eventService)); //$NON-NLS-1$
    }

    private PermitService getPermitService() {
        if (context != null) {
            return (PermitService)context.locateService("PermitService"); //$NON-NLS-1$
        }
        return null;
    }

    @Override
    public List<String> getRoles(String userId) {
        if (configService == null || StringUtils.isBlank(userId)) {
            return Collections.emptyList();
        }
        RolesConfig rolesConfig = configService.readConfiguration(RolesConfig.class);
        if (rolesConfig == null) {
            return Collections.emptyList();
        }
        List<String> roles = new ArrayList<String>();
        for (RoleConfig roleConfig: rolesConfig.getRoles()) {
            if (roleConfig.getUsers().contains(userId)) {
                roles.add(roleConfig.getRoleId());
            }
        }
        return roles;
    }

    @Override
    public List<String> getRoles(String... groups) {
        if (configService == null || groups == null || groups.length == 0) {
            return Collections.emptyList();
        }
        RolesConfig rolesConfig = configService.readConfiguration(RolesConfig.class);
        if (rolesConfig == null) {
            return Collections.emptyList();
        }
        List<String> roles = new ArrayList<String>();
        for (String groupName: groups) {
            collectRoles(groupName, rolesConfig.getRoles(), roles);
        }
        return roles;
    }

    private void collectRoles(String groupName, List<RoleConfig> roleConfigs, List<String> roles) {
        for (RoleConfig roleConfig: roleConfigs) {
            if (roleConfig.getGroups().contains(groupName)) {
                roles.add(roleConfig.getRoleId());
            }
        }
    }

    @Override
    public void onEvent(EventConfigUpdate event) {
        if (RolesConfig.class.equals(event.getConfigClass())) {
            invalidatePermits();
        }
    }

    private void invalidatePermits() {
        PermitService permitService = getPermitService();
        if (permitService != null) {
            permitService.logoutAll();
        }
    }
}
