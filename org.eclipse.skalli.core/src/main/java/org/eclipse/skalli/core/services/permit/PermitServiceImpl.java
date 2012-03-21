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
package org.eclipse.skalli.core.services.permit;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.Group;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.services.configuration.ConfigurationService;
import org.eclipse.skalli.services.event.EventCustomizingUpdate;
import org.eclipse.skalli.services.event.EventListener;
import org.eclipse.skalli.services.event.EventService;
import org.eclipse.skalli.services.group.GroupService;
import org.eclipse.skalli.services.permit.Permit;
import org.eclipse.skalli.services.permit.PermitService;
import org.eclipse.skalli.services.permit.PermitSet;
import org.eclipse.skalli.services.project.ProjectService;
import org.eclipse.skalli.services.user.LoginUtils;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PermitServiceImpl implements PermitService, EventListener<EventCustomizingUpdate> {

    private static final Logger LOG = LoggerFactory.getLogger(PermitServiceImpl.class);

    private static final String PERMITS_ATTRIBUTE = "PERMITS"; //$NON-NLS-1$
    private static final String PERMITS_TIMESTAMP_ATTRIBUTE = "PERMITS_TIMESTAMP"; //$NON-NLS-1$

    private static final PermitSet DEFAULT_PERMITS = new PermitSet(Permit.FORBID_ALL);

    private static ThreadLocal<String> threadUserId = new InheritableThreadLocal<String>();
    private static ThreadLocal<UUID> threadEntity = new InheritableThreadLocal<UUID>();
    private static ThreadLocal<PermitSet> threadPermits = new InheritableThreadLocal<PermitSet>();

    private ComponentContext context;
    private ConfigurationService configService;

    private long permitsConfigChangedTimestamp;

    protected void activate(ComponentContext context) {
        this.context = context;
        LOG.info(MessageFormat.format("[PermitService] {0} : activated", //$NON-NLS-1$
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void deactivate(ComponentContext context) {
        this.context = null;
        LOG.info(MessageFormat.format("[PermitService] {0} : deactivated", //$NON-NLS-1$
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void bindConfigurationService(ConfigurationService configService) {
        LOG.info(MessageFormat.format("bindConfigurationService({0})", configService)); //$NON-NLS-1$
        this.configService = configService;
        permitsConfigChangedTimestamp = System.currentTimeMillis();
    }

    protected void unbindConfigurationService(ConfigurationService configService) {
        LOG.info(MessageFormat.format("unbindConfigurationService({0})", configService)); //$NON-NLS-1$
        this.configService = null;
    }

    protected void bindEventService(EventService eventService) {
        LOG.info(MessageFormat.format("bindEventService({0})", eventService)); //$NON-NLS-1$
        eventService.registerListener(EventCustomizingUpdate.class, this);
    }

    protected void unbindEventService(EventService eventService) {
        LOG.info(MessageFormat.format("unbindEventService({0})", eventService)); //$NON-NLS-1$
    }

    @Override
    public void onEvent(EventCustomizingUpdate event) {
        if (PermitsResource.MAPPINGS_KEY.equals(event.getCustomizationName())) {
            permitsConfigChangedTimestamp = System.currentTimeMillis();
        }
    }

    private GroupService getGroupService() {
        if (context != null) {
            return (GroupService)context.locateService("GroupService"); //$NON-NLS-1$
        }
        return null;
    }

    private ProjectService getProjectService() {
        if (context != null) {
            return (ProjectService)context.locateService("ProjectService"); //$NON-NLS-1$
        }
        return null;
    }

    @Override
    public boolean hasPermit(Permit permit) {
        return hasPermit(permit.getLevel(), permit.getAction(), permit.getSegments());
    }

    @Override
    public boolean hasPermit(int level, String action, String... segments) {
        PermitSet permits = threadPermits.get();
        if (permits == null) {
            permits = DEFAULT_PERMITS;
            threadPermits.set(permits);
        }
        return Permit.match(permits, level, action, segments);
    }

    @SuppressWarnings("unchecked")
    @Override
    public String login(HttpServletRequest request, UUID projectId) {
        LoginUtils loginUtil = new LoginUtils(request);
        String userId = loginUtil.getLoggedInUserId();
        PermitSet permits = null;
        if (userId != null) {
            // ensure that session permits are still valid, i.e. that no configuration
            // change has occured in the meantime
            Long timestamp = (Long)request.getSession().getAttribute(PERMITS_TIMESTAMP_ATTRIBUTE);
            if (timestamp != null && timestamp > permitsConfigChangedTimestamp) {
                permits = (PermitSet)request.getSession().getAttribute(PERMITS_ATTRIBUTE);
            }
        }
        if (permits == null) {
            permits = getPermits(userId, projectId);
            request.getSession().setAttribute(PERMITS_TIMESTAMP_ATTRIBUTE, System.currentTimeMillis());
            request.getSession().setAttribute(PERMITS_ATTRIBUTE, permits);
        }
        threadUserId.set(userId);
        threadEntity.set(projectId);
        threadPermits.set(permits);
        return userId;
    }

    @Override
    public void logout(HttpServletRequest request) {
        request.getSession().removeAttribute(PERMITS_ATTRIBUTE);
        threadUserId.set(null);
        threadEntity.set(null);
        threadPermits.set(null);
    }

    private PermitSet getPermits(String userId, UUID projectId) {
        PermitSet permits = new PermitSet();
        if (configService != null) {
            userId = StringUtils.isNotBlank(userId)? userId : User.UNKNOWN;

            Project project = getProject(projectId);
            String templateId = project != null? project.getProjectTemplateId() : null;

            Set<String> groups = getGroupNames(userId);
            Set<String> groupRoles = getGroupRoles(groups, project);
            Set<String> roles = getRoles(userId, project);

            PermitsConfig permitsConfig = configService.readCustomization(PermitsResource.MAPPINGS_KEY, PermitsConfig.class);
            if (permitsConfig != null) {
                collectGlobalCommits(permitsConfig, permits);
                if (templateId != null) {
                    collectTemplatePermits(templateId, permitsConfig, permits);
                }
                if (groups.size() > 0) {
                    if (groupRoles.size() > 0) {
                        collectRolePermits(groupRoles, permitsConfig, permits);
                    }
                    collectGroupPermits(groups, permitsConfig, permits);
                }
                if (roles.size() > 0) {
                    collectRolePermits(roles, permitsConfig, permits);
                }
                collectUserPermits(userId, permitsConfig, permits);
            }
        }
        if (permits.isEmpty()) {
            permits.addAll(DEFAULT_PERMITS);
            LOG.debug("falling back to default permits for user " + userId);
        }
        return permits;
    }

    private void collectGlobalCommits(PermitsConfig permitsConfig, PermitSet permits) {
        List<PermitConfig> globalPermits = permitsConfig.getGlobalPermits();
        for (PermitConfig globalPermit: globalPermits) {
            permits.add(globalPermit.asPermit());
        }
    }

    private void collectUserPermits(String userId, PermitsConfig permitsConfig, PermitSet permits) {
        List<PermitConfig> userPermits = permitsConfig.getUserPermits();
        for (PermitConfig userPermit: userPermits) {
            if (userId.equals(userPermit.getOwner())) {
                permits.add(userPermit.asPermit());
            }
        }
    }

    private void collectGroupPermits(Set<String> groups, PermitsConfig permitsConfig, PermitSet permits) {
        List<PermitConfig> groupPermits = permitsConfig.getGroupPermits();
        for (PermitConfig groupPermit: groupPermits) {
            if (groups.contains(groupPermit.getOwner())) {
                permits.add(groupPermit.asPermit());
            }
        }
    }

    private void collectRolePermits(Set<String> roles, PermitsConfig permitsConfig, PermitSet permits) {
        List<PermitConfig> rolePermits = permitsConfig.getRolePermits();
        for (PermitConfig rolePermit: rolePermits) {
            if (roles.contains(rolePermit.getOwner())) {
                permits.add(rolePermit.asPermit());
            }
        }
    }

    private void collectTemplatePermits(String templateId, PermitsConfig permitsConfig, PermitSet permits) {
        List<PermitConfig> templatePermits = permitsConfig.getTemplatePermits();
        for (PermitConfig templatePermit: templatePermits) {
            if (templateId.equals(templatePermit.getOwner())) {
                permits.add(templatePermit.asPermit());
            }
        }
    }

    private Set<String> getGroupRoles(Set<String> groups, Project project) {
        // TODO
        return Collections.emptySet();
    }

    private Set<String> getRoles(String userId, Project project) {
        // TODO
        return Collections.emptySet();
    }

    private Set<String> getGroupNames(String userId) {
        Set<String> groupNames = new HashSet<String>();
        GroupService groupService = getGroupService();
        if (groupService != null) {
            for (Group group: groupService.getGroups(userId)) {
                groupNames.add(group.getGroupId());
            }
        }
        return groupNames;
    }

    private Project getProject(UUID projectId) {
        Project project = null;
        if (projectId != null) {
            ProjectService projectService = getProjectService();
            if (projectService != null) {
                project = projectService.getByUUID(projectId);
            }
        }
        return project;
    }
}
