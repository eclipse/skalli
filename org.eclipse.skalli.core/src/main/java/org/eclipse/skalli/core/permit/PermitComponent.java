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
package org.eclipse.skalli.core.permit;

import java.security.Principal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.Group;
import org.eclipse.skalli.model.Member;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.services.configuration.ConfigurationService;
import org.eclipse.skalli.services.configuration.EventConfigUpdate;
import org.eclipse.skalli.services.event.EventListener;
import org.eclipse.skalli.services.event.EventService;
import org.eclipse.skalli.services.group.GroupService;
import org.eclipse.skalli.services.permit.Permit;
import org.eclipse.skalli.services.permit.PermitService;
import org.eclipse.skalli.services.permit.PermitSet;
import org.eclipse.skalli.services.role.RoleProvider;
import org.eclipse.skalli.services.role.RoleService;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PermitComponent implements PermitService, EventListener<EventConfigUpdate> {

    private static final Logger LOG = LoggerFactory.getLogger(PermitComponent.class);

    private static final String PERMITS_ATTRIBUTE = "PERMITS"; //$NON-NLS-1$
    private static final String PERMITS_TIMESTAMP_ATTRIBUTE = "PERMITS_TIMESTAMP"; //$NON-NLS-1$
    private static final String PERMITS_PROJECT_ATTRIBUTE = "PERMITS_PROJECT"; //$NON-NLS-1$

    private static final String PROPERTY_PROJECTID =
            StringUtils.substringBetween(Permit.PROJECT_WILDCARD, "{", "}"); //$NON-NLS-1$ //$NON-NLS-2$
    private static final String USER_WILDCARD =
            StringUtils.substringBetween(Permit.USER_WILDCARD, "{", "}"); //$NON-NLS-1$ //$NON-NLS-2$

    private static final PermitSet DEFAULT_PERMITS = new PermitSet(Permit.FORBID_ALL);
    private static final PermitSet DEFAULT_ADMIN_PERMITS = new PermitSet(Permit.ALLOW_ALL);

    private static final String PATH_PROJECTS = "projects"; //$NON-NLS-1$


    private static ThreadLocal<String> threadUserId = new InheritableThreadLocal<String>();
    private static ThreadLocal<Project> threadProject = new InheritableThreadLocal<Project>();
    private static ThreadLocal<PermitSet> threadPermits = new InheritableThreadLocal<PermitSet>();

    private ComponentContext context;
    private ConfigurationService configService;
    private Set<RoleProvider> roleProviders = new HashSet<RoleProvider>();

    // ensure that threads are properly synchronized when accessing this instance variable
    private volatile long permitsConfigChangedTimestamp;

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
        logoutAll();
    }

    protected void unbindConfigurationService(ConfigurationService configService) {
        LOG.info(MessageFormat.format("unbindConfigurationService({0})", configService)); //$NON-NLS-1$
        this.configService = null;
        logoutAll();
    }

    protected void bindEventService(EventService eventService) {
        LOG.info(MessageFormat.format("bindEventService({0})", eventService)); //$NON-NLS-1$
        eventService.registerListener(EventConfigUpdate.class, this);
    }

    protected void unbindEventService(EventService eventService) {
        LOG.info(MessageFormat.format("unbindEventService({0})", eventService)); //$NON-NLS-1$
    }

    protected void bindRoleProvider(RoleProvider roleProvider) {
        roleProviders.add(roleProvider);
        logoutAll();
    }

    protected void unbindRoleProvider(RoleProvider roleProvider) {
        roleProviders.remove(roleProvider);
        logoutAll();
    }

    private GroupService getGroupService() {
        if (context != null) {
            return (GroupService)context.locateService("GroupService"); //$NON-NLS-1$
        }
        return null;
    }

    private RoleService getRoleService() {
        if (context != null) {
            return (RoleService)context.locateService("RoleService"); //$NON-NLS-1$
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

    @Override
    public boolean hasPermit(int level, String action, Project project) {
        String projectId = project.getProjectId();
        return StringUtils.isNotBlank(projectId) && hasPermit(level, action, PATH_PROJECTS, projectId)
                || hasPermit(level, action, PATH_PROJECTS, project.getUuid().toString());
    }

    @Override
    public boolean hasPermit(int level, String action, Project project, String... segments) {
        if (segments == null || segments.length == 0) {
            return hasPermit(level, action, project);
        }
        String[] amendedSegments = new String[segments.length + 2];
        amendedSegments[0] = PATH_PROJECTS;
        System.arraycopy(segments, 0, amendedSegments, 2, segments.length);
        String projectId = project.getProjectId();
        return StringUtils.isNotBlank(projectId)?
                hasProjectPermit(level, action, projectId, amendedSegments) :
                hasProjectPermit(level, action, project.getUuid().toString(), amendedSegments);
    }

    private boolean hasProjectPermit(int level, String action, String id, String... segments) {
        segments[1] = id;
        return StringUtils.isNotBlank(id) && hasPermit(level, action, segments);
    }

    @Override
    public String login(HttpServletRequest request, Project project) {
        String userId = null;
        Principal userPrincipal = request.getUserPrincipal();
        if (userPrincipal != null) {
            userId = userPrincipal.getName();
            if (StringUtils.isNotBlank(userId)) {
                userId = userId.toLowerCase(Locale.ENGLISH);
            }
        }
        HttpSession session = request.getSession();
        PermitSet permits = getSessionPermits(session, userId, project);
        if (permits == null) {
            permits = getPermits(userId, project);
            setSessionPermits(session, permits, project);
        }
        threadUserId.set(userId);
        threadProject.set(project);
        threadPermits.set(permits);
        return userId;
    }

    @Override
    public void switchProject(Project project) {
        String userId = threadUserId.get();
        PermitSet permits = getPermits(userId, project);
        threadProject.set(project);
        threadPermits.set(permits);
    }

    @Override
    public void logout(HttpServletRequest request) {
        removeSessionPermits(request.getSession());
        threadUserId.set(null);
        threadProject.set(null);
        threadPermits.set(null);
    }

    @Override
    public void logoutAll() {
        permitsConfigChangedTimestamp = System.currentTimeMillis();
    }

    @Override
    public String getLoggedInUser() {
        return threadUserId.get();
    }

    @Override
    public void onEvent(EventConfigUpdate event) {
        // we do not know which configurations affect permissions,
        // so throw away all login sessions just to be sure
        logoutAll();
    }

    private PermitSet getSessionPermits(HttpSession session, String userId, Project project) {
        if (session == null) {
            return null;
        }
        if (StringUtils.isBlank(userId)) {
            // anonymous users have never a session
            return null;
        }
        if (project != null) {
            // ensure that session permits apply to the same project
            String projectId = (String)session.getAttribute(PERMITS_PROJECT_ATTRIBUTE);
            if (!project.getProjectId().equals(projectId)) {
                return null;
            }
        }
        // ensure that session permits are still valid, i.e. that no configuration
        // change has occurred in the meantime
        Long timestamp = (Long)session.getAttribute(PERMITS_TIMESTAMP_ATTRIBUTE);
        if (timestamp == null || timestamp <= permitsConfigChangedTimestamp) {
            return null;
        }
        return (PermitSet)session.getAttribute(PERMITS_ATTRIBUTE);
    }

    private void setSessionPermits(HttpSession session, PermitSet permits, Project project) {
        if (session != null) {
            session.setAttribute(PERMITS_TIMESTAMP_ATTRIBUTE, System.currentTimeMillis());
            session.setAttribute(PERMITS_ATTRIBUTE, permits);
            if (project != null) {
                session.setAttribute(PERMITS_PROJECT_ATTRIBUTE, project.getProjectId());
            } else {
                session.removeAttribute(PERMITS_PROJECT_ATTRIBUTE);
            }
        }
    }

    private void removeSessionPermits(HttpSession session) {
        if (session != null) {
            session.removeAttribute(PERMITS_TIMESTAMP_ATTRIBUTE);
            session.removeAttribute(PERMITS_ATTRIBUTE);
            session.removeAttribute(PERMITS_PROJECT_ATTRIBUTE);
        }
    }

    @Override
    public PermitSet getPermits(String userId, Project project) {
        PermitSet permits = new PermitSet();
        if (configService != null) {
            userId = StringUtils.isNotBlank(userId)? userId : User.UNKNOWN;

            String templateId = project != null? project.getProjectTemplateId() : null;

            List<String> groups = getGroupNames(userId);
            List<String> groupRoles = getRoles(groups);
            List<String> roles = getRoles(userId);
            List<String> projectRoles = getProjectRoles(userId, project);

            PermitsConfig permitsConfig = configService.readConfiguration(PermitsConfig.class);
            if (permitsConfig != null) {
                Map<String,String> properties = new HashMap<String,String>();
                properties.put(USER_WILDCARD, userId);
                String projectId = "?"; //$NON-NLS-1$
                if (project != null) {
                    projectId = project.getProjectId();
                    if (StringUtils.isBlank(projectId)) {
                        projectId = project.getUuid().toString();
                    }
                }
                properties.put(PROPERTY_PROJECTID, projectId);

                Map<String, List<PermitConfig>> permitsByType = permitsConfig.getByType();
                collectGlobalCommits(properties, permitsByType, permits);
                if (templateId != null) {
                    collectTemplatePermits(templateId, properties, permitsByType, permits);
                }
                if (groupRoles.size() > 0) {
                    collectRolePermits(groupRoles, properties, permitsByType, permits);
                }
                if (roles.size() > 0) {
                    collectRolePermits(roles, properties, permitsByType, permits);
                }
                if (projectRoles.size() > 0) {
                    collectRolePermits(projectRoles, properties, permitsByType, permits);
                }
                if (groups.size() > 0) {
                    collectGroupPermits(groups, properties, permitsByType, permits);
                }
                collectUserPermits(userId, properties, permitsByType, permits);
            } else {
                // special handling for bootstrapping of a new instance or for
                // instances where authorization is not relevant
                permits.addAll(DEFAULT_ADMIN_PERMITS);
            }
        }
        if (permits.isEmpty()) {
            permits.addAll(DEFAULT_PERMITS);
        }
        return permits;
    }

    private void collectGlobalCommits(Map<String,String> properties,
            Map<String, List<PermitConfig>> permitsConfig, PermitSet permits) {
        List<PermitConfig> globalPermits = permitsConfig.get(PermitsConfig.GLOBAL_PERMIT);
        if (globalPermits != null) {
            for (PermitConfig globalPermit: globalPermits) {
                permits.add(globalPermit.asPermit(properties));
            }
        }
    }

    private void collectUserPermits(String userId,  Map<String,String> properties,
            Map<String, List<PermitConfig>> permitsConfig, PermitSet permits) {
        List<PermitConfig> userPermits = permitsConfig.get(PermitsConfig.USER_PERMIT);
        if (userPermits != null) {
            for (PermitConfig userPermit: userPermits) {
                if (userId.equals(userPermit.getOwner())) {
                    permits.add(userPermit.asPermit(properties));
                }
            }
        }
    }

    private void collectGroupPermits(List<String> groups,  Map<String,String> properties,
            Map<String, List<PermitConfig>> permitsConfig, PermitSet permits) {
        List<PermitConfig> groupPermits = permitsConfig.get(PermitsConfig.GROUP_PERMIT);
        if (groupPermits != null) {
            for (PermitConfig groupPermit: groupPermits) {
                if (groups.contains(groupPermit.getOwner())) {
                    permits.add(groupPermit.asPermit(properties));
                }
            }
        }
    }

    private void collectRolePermits(List<String> roles, Map<String,String> properties,
            Map<String, List<PermitConfig>> permitsConfig, PermitSet permits) {
        List<PermitConfig> rolePermits = permitsConfig.get(PermitsConfig.ROLE_PERMIT);
        if (rolePermits != null) {
            for (PermitConfig rolePermit: rolePermits) {
                if (roles.contains(rolePermit.getOwner())) {
                    permits.add(rolePermit.asPermit(properties));
                }
            }
        }
    }

    private void collectTemplatePermits(String templateId, Map<String,String> properties,
            Map<String, List<PermitConfig>> permitsConfig, PermitSet permits) {
        List<PermitConfig> templatePermits = permitsConfig.get(PermitsConfig.TEMPLATE_PERMIT);
        if (templatePermits != null) {
            for (PermitConfig templatePermit: templatePermits) {
                if (templateId.equals(templatePermit.getOwner())) {
                    permits.add(templatePermit.asPermit(properties));
                }
            }
        }
    }

    private List<String> getRoles(List<String> groups) {
        RoleService roleService = getRoleService();
        if (roleService == null) {
            return Collections.emptyList();
        }
        return roleService.getRoles(groups.toArray(new String[groups.size()]));
    }

    private List<String> getRoles(String userId) {
        List<String> roles = new ArrayList<String>();
        RoleService roleService = getRoleService();
        if (roleService != null) {
            roles.addAll(roleService.getRoles(userId));
        }
        return roles;
    }

    private List<String> getProjectRoles(String userId, Project project) {
        List<String> roles = new ArrayList<String>();
        if (project != null) {
            Member member = new Member(userId);
            for (RoleProvider roleProvider: roleProviders) {
                Map<String, SortedSet<Member>> membersByRole = roleProvider.getMembersByRole(project);
                for (String role: membersByRole.keySet()) {
                    if (membersByRole.get(role).contains(member)) {
                        roles.add(role);
                    }
                }
            }
        }
        return roles;
    }

    private List<String> getGroupNames(String userId) {
        List<String> groupNames = new ArrayList<String>();
        GroupService groupService = getGroupService();
        if (groupService != null) {
            for (Group group: groupService.getGroups(userId)) {
                groupNames.add(group.getGroupId());
            }
        }
        return groupNames;
    }
}
