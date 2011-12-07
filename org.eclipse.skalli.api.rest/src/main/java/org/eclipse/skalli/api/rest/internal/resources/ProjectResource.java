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
package org.eclipse.skalli.api.rest.internal.resources;

import java.io.IOException;
import java.util.UUID;

import org.eclipse.skalli.api.java.ProjectService;
import org.eclipse.skalli.api.java.authentication.LoginUtil;
import org.eclipse.skalli.api.java.authentication.UserUtil;
import org.eclipse.skalli.api.rest.AbstractServerResource;
import org.eclipse.skalli.api.rest.ResourceRepresentation;
import org.eclipse.skalli.common.Services;
import org.eclipse.skalli.common.util.Statistics;
import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.model.ext.ValidationException;
import org.restlet.data.Status;
import org.restlet.ext.servlet.ServletUtils;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Put;

public class ProjectResource extends AbstractServerResource {

    @Get
    public Representation retrieve() {
        Statistics.getDefault().trackUsage("api.rest.project.get"); //$NON-NLS-1$

        String id = (String) getRequestAttributes().get("id"); //$NON-NLS-1$
        ProjectService projectService = Services.getRequiredService(ProjectService.class);
        Project project = null;
        try {
            UUID uuid = UUID.fromString(id);
            project = projectService.getByUUID(uuid);
        } catch (IllegalArgumentException e) {
            project = projectService.getProjectByProjectId(id);
        }
        if (project == null) {
            return createError(Status.CLIENT_ERROR_NOT_FOUND, "Project \"{0}\" not found.", id); //$NON-NLS-1$
        }

        ResourceRepresentation<Project> representation = new ResourceRepresentation<Project>(
                project, new ProjectConverter(getRequest().getResourceRef().getHostIdentifier(), false));
        return representation;
    }

    @Put
    public Representation store(Representation entity) {
        ResourceRepresentation<Project> representation = new ResourceRepresentation<Project>();
        representation.setConverters(new ProjectConverter(getRequest().getResourceRef().getHostIdentifier(), false));
        representation.setAnnotatedClasses(Project.class);
        Project project = null;
        try {
            project = representation.read(entity, Project.class);
        } catch (IOException e) {
            createError(Status.SERVER_ERROR_INTERNAL,
                    "Failed to read project entity: " + e.getMessage());
        }
        try {
            LoginUtil loginUtil = new LoginUtil(ServletUtils.getRequest(getRequest()));
            String loggedInUser = loginUtil.getLoggedInUserId();
            if (UserUtil.isAdministrator(loggedInUser)) {
                ProjectService projectService = Services.getRequiredService(ProjectService.class);
                projectService.persist(project, loggedInUser);
            } else {
                return createError(Status.CLIENT_ERROR_FORBIDDEN, "Access denied.", new Object[] {});
            }
        } catch (ValidationException e) {
            createError(Status.CLIENT_ERROR_BAD_REQUEST,
                    "Validating resource with id \"{0}\" failed: " + e.getMessage(), project.getProjectId());
        }
        return null;
    }

}
