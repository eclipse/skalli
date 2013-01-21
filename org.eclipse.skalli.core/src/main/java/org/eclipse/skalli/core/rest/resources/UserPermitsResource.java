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
package org.eclipse.skalli.core.rest.resources;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.extension.rest.ResourceBase;
import org.eclipse.skalli.services.extension.rest.ResourceRepresentation;
import org.eclipse.skalli.services.permit.PermitService;
import org.eclipse.skalli.services.permit.PermitSet;
import org.eclipse.skalli.services.permit.Permits;
import org.eclipse.skalli.services.project.ProjectService;
import org.eclipse.skalli.services.search.QueryParseException;
import org.eclipse.skalli.services.search.SearchService;
import org.eclipse.skalli.services.user.UserUtils;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

public class UserPermitsResource extends ResourceBase {

    private static final String ID_PREFIX = "rest:api/users/{0}/permits:"; //$NON-NLS-1$
    private static final String ERROR_ID_SERVIVE_UNAVAILABLE = ID_PREFIX + "10";  //$NON-NLS-1$
    private static final String ERROR_ID_INVALID_QUERY = ID_PREFIX + "20"; //$NON-NLS-1$

    private static final String PARAM_USERID = "userId"; //$NON-NLS-1$
    private static final String PARAM_PROJECTID = "projectId"; //$NON-NLS-1$

    @Get
    public Representation retrieve() {
        if (!Permits.isAllowed(getAction(), getPath())) {
            return createUnauthorizedRepresentation();
        }

        String userId = (String) getRequestAttributes().get(PARAM_USERID);
        User user = UserUtils.getUser(userId);
        if (user.isUnknown()) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND, MessageFormat.format("User \"{0}\" not found", userId)); //$NON-NLS-1$
            return null;
        }
        PermitService permitService = Services.getService(PermitService.class);
        if (permitService == null) {
            String errorId = MessageFormat.format(ERROR_ID_SERVIVE_UNAVAILABLE, userId);
            return createServiceUnavailableRepresentation(errorId, "Permit Service"); //$NON-NLS-1$
        }

        String projectId = (String) getRequestAttributes().get(PARAM_PROJECTID);
        List<Project> projects = new ArrayList<Project>();
        if (projectId != null) {
            Project project = null;
            ProjectService projectService = Services.getRequiredService(ProjectService.class);
            try {
                UUID uuid = UUID.fromString(projectId);
                project = projectService.getByUUID(uuid);
            } catch (IllegalArgumentException e) {
                project = projectService.getProjectByProjectId(projectId);
            }
            if (project == null) {
                setStatus(Status.CLIENT_ERROR_NOT_FOUND, MessageFormat.format("Project \"{0}\" not found", projectId)); //$NON-NLS-1$
                return null;
            }
            projects.add(project);
        } else {
            SearchService searchService = Services.getService(SearchService.class);
            if (searchService != null) {
                try {
                    projects.addAll(searchService.findProjectsByUser(userId, null).getEntities());
                } catch (QueryParseException e) {
                    return createErrorRepresentation(Status.CLIENT_ERROR_BAD_REQUEST, ERROR_ID_INVALID_QUERY,
                            "Invalid query \"?{0}\": {1}", getQueryString(), e.getMessage()); //$NON-NLS-1$
                }
            }
        }

        PermitSet permits = new PermitSet();
        for (Project project: projects) {
            permits.addAll(permitService.getPermits(userId, project));
        }
        return new ResourceRepresentation<PermitSet>(permits, new PermitSetConverter(userId, getHost()));
    }

}
