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

import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.ValidationException;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.extension.rest.ResourceBase;
import org.eclipse.skalli.services.extension.rest.ResourceRepresentation;
import org.eclipse.skalli.services.extension.rest.RestUtils;
import org.eclipse.skalli.services.group.GroupUtils;
import org.eclipse.skalli.services.permit.Permit;
import org.eclipse.skalli.services.project.ProjectService;
import org.eclipse.skalli.services.user.LoginUtils;
import org.restlet.data.Status;
import org.restlet.ext.servlet.ServletUtils;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Put;

public class ProjectResource extends ResourceBase {

    @Get
    public Representation retrieve() {
        String path = getReference().getPath();
        Representation result = checkAuthorization(Permit.ACTION_GET, path);
        if (result != null) {
            return result;
        }

        String id = (String) getRequestAttributes().get(RestUtils.PARAM_ID);
        ProjectService projectService = Services.getRequiredService(ProjectService.class);
        Project project = null;
        try {
            UUID uuid = UUID.fromString(id);
            project = projectService.getByUUID(uuid);
        } catch (IllegalArgumentException e) {
            project = projectService.getProjectByProjectId(id);
        }
        if (project == null) {
            return createStatusMessage(Status.CLIENT_ERROR_NOT_FOUND, "Project \"{0}\" not found.", id); //$NON-NLS-1$
        }

        ResourceRepresentation<Project> representation = new ResourceRepresentation<Project>(
                project, new ProjectConverter(getRequest().getResourceRef().getHostIdentifier(), false));
        return representation;
    }

    @Put
    public Representation store(Representation entity) {
        String path = getReference().getPath();
        Representation result = checkAuthorization(Permit.ACTION_PUT, path);
        if (result != null) {
            return result;
        }

        ResourceRepresentation<Project> representation = new ResourceRepresentation<Project>();
        representation.setConverters(new ProjectConverter(getRequest().getResourceRef().getHostIdentifier(), false));
        representation.setAnnotatedClasses(Project.class);
        Project project = null;
        try {
            project = representation.read(entity, Project.class);
        } catch (IOException e) {
            createStatusMessage(Status.SERVER_ERROR_INTERNAL,
                    "Failed to read project entity: " + e.getMessage());
        }
        try {
            LoginUtils loginUtils = new LoginUtils(ServletUtils.getRequest(getRequest()));
            String loggedInUser = loginUtils.getLoggedInUserId();
            if (GroupUtils.isAdministrator(loggedInUser)) {
                ProjectService projectService = Services.getRequiredService(ProjectService.class);
                projectService.persist(project, loggedInUser);
            } else {
                return createStatusMessage(Status.CLIENT_ERROR_FORBIDDEN, "Access denied.", new Object[] {});
            }
        } catch (ValidationException e) {
            createStatusMessage(Status.CLIENT_ERROR_BAD_REQUEST,
                    "Validating resource with id \"{0}\" failed: " + e.getMessage(), project.getProjectId());
        }
        return null;
    }

}
