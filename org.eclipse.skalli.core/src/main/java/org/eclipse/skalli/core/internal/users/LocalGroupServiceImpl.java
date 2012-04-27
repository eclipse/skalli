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
package org.eclipse.skalli.core.internal.users;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.skalli.core.internal.groups.GroupResource;
import org.eclipse.skalli.core.internal.groups.GroupsConfig;
import org.eclipse.skalli.model.Group;
import org.eclipse.skalli.services.configuration.ConfigurationService;
import org.eclipse.skalli.services.event.EventCustomizingUpdate;
import org.eclipse.skalli.services.event.EventListener;
import org.eclipse.skalli.services.event.EventService;
import org.eclipse.skalli.services.group.GroupService;
import org.eclipse.skalli.services.permit.PermitService;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link GroupService} using {@link Group} instances.
 * The service expects to find XML files named
 * <tt>&lt;groupId&gt.xml</tt> in the <tt>$workdir/storage/Group</tt>
 * directory.
 */
public class LocalGroupServiceImpl implements GroupService, EventListener<EventCustomizingUpdate> {

    private static final Logger LOG = LoggerFactory.getLogger(LocalGroupServiceImpl.class);

    private ComponentContext context;
    private ConfigurationService configService;

    protected void activate(ComponentContext context) {
        this.context = context;
        invalidatePermits();
        LOG.info(MessageFormat.format("[GroupService][type=local] {0} : activated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void deactivate(ComponentContext context) {
        invalidatePermits();
        this.context = null;
        LOG.info(MessageFormat.format("[GroupService][type=local] {0} : deactivated",
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
        eventService.registerListener(EventCustomizingUpdate.class, this);
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
    public boolean isAdministrator(String userId) {
        return isMemberOfGroup(userId, ADMIN_GROUP);
    }

    @Override
    public boolean isMemberOfGroup(final String userId, final String groupId) {
        Group group = getGroup(groupId);

        //special case for AdminGroup: if  no admin group is defined or the group is empty, all users are admin.
        if (ADMIN_GROUP.equals(groupId)) {
            if (group == null || group.getGroupMembers().size() == 0) {
                return true;
            }
        }
        return group != null ? group.hasGroupMember(userId) : false;
    }

    @Override
    public List<Group> getGroups() {
        if (configService == null) {
            return Collections.emptyList();
        }
        GroupsConfig groupsConfig = configService.readCustomization(GroupResource.MAPPINGS_KEY, GroupsConfig.class);
        if (groupsConfig == null) {
            return Collections.emptyList();
        }
        return groupsConfig.getModelGroups();
    }

    @Override
    public Group getGroup(final String groupId) {
        List<Group> groups = getGroups();
        for (Group group : groups) {
            if (group.getGroupId().equals(groupId)) {
                return group;
            }
        }
        return null;
    }

    @Override
    public List<Group> getGroups(String userId) {
        List<Group> groups = new ArrayList<Group>();
        for (Group group : getGroups()) {
            if (group.hasGroupMember(userId)) {
                groups.add(group);
            }
        }
        return groups;
    }

    @Override
    public void onEvent(EventCustomizingUpdate event) {
        invalidatePermits();
    }

    private void invalidatePermits() {
        PermitService permitService = getPermitService();
        if (permitService != null) {
            permitService.logoutAll();
        }
    }
}
