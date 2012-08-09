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
import java.text.MessageFormat;
import java.util.UUID;

import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.ValidationException;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.extension.rest.ResourceBase;
import org.eclipse.skalli.services.extension.rest.ResourceRepresentation;
import org.eclipse.skalli.services.extension.rest.RestUtils;
import org.eclipse.skalli.services.permit.Permit;
import org.eclipse.skalli.services.permit.Permits;
import org.eclipse.skalli.services.project.ProjectService;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectResource extends ResourceBase {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectResource.class);

    // error codes for logging and error responses
    private static final String ERROR_ID_IO_ERROR = "rest:api/projects/{0}:00"; //$NON-NLS-1$
    private static final String ERROR_VALIDATION_FAILED = "rest:api/projects/{0}:10"; //$NON-NLS-1$

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
            setStatus(Status.CLIENT_ERROR_NOT_FOUND, MessageFormat.format("Project {0} not found", id)); //$NON-NLS-1$
            return null;
        }

        return new ResourceRepresentation<Project>(project,
                new ProjectConverter(getRequest().getResourceRef().getHostIdentifier(), false));
    }

    @Put
    public Representation store(Representation entity) {
        String path = getReference().getPath();
        Representation result = checkAuthorization(Permit.ACTION_PUT, path);
        if (result != null) {
            return result;
        }

        String id = (String) getRequestAttributes().get(RestUtils.PARAM_ID);
        ResourceRepresentation<Project> representation = new ResourceRepresentation<Project>();
        representation.setConverters(new ProjectConverter());
        representation.setAnnotatedClasses(Project.class);
        Project project = null;
        try {
            project = representation.read(entity, Project.class);
        } catch (IOException e) {
            String errorId = MessageFormat.format(ERROR_ID_IO_ERROR, id);
            String message = MessageFormat.format("Failed to read project {0}", id);
            LOG.error(MessageFormat.format("{0} ({1})", message, errorId), e);
            return createErrorRepresentation(Status.SERVER_ERROR_INTERNAL, errorId, message);
        }

        try {
            ProjectService projectService = Services.getRequiredService(ProjectService.class);
            projectService.persist(project, Permits.getLoggedInUser());
        } catch (ValidationException e) {
            String errorId = MessageFormat.format(ERROR_VALIDATION_FAILED, project.getProjectId());
            String message = MessageFormat.format("Validating project {0} failed: {1}", project.getProjectId(), e.getMessage());
            LOG.warn(MessageFormat.format("{0} ({1})", message, errorId));
            return createErrorRepresentation(Status.CLIENT_ERROR_BAD_REQUEST,  errorId, message);
        }
        setStatus(Status.SUCCESS_NO_CONTENT);
        return null;
    }

}
