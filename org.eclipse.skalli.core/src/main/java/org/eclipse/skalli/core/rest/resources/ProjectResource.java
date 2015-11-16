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
package org.eclipse.skalli.core.rest.resources;

import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.ValidationException;
import org.eclipse.skalli.services.entity.EntityServices;
import org.eclipse.skalli.services.extension.rest.ResourceBase;
import org.eclipse.skalli.services.extension.rest.ResourceRepresentation;
import org.eclipse.skalli.services.extension.rest.RestException;
import org.eclipse.skalli.services.extension.rest.RestUtils;
import org.eclipse.skalli.services.permit.Permits;
import org.eclipse.skalli.services.project.ProjectService;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Put;

public class ProjectResource extends ResourceBase {

    private static final String ID_PREFIX = "rest:api/projects/{0}";  //$NON-NLS-1$
    private static final String ERROR_ID_IO_ERROR = ID_PREFIX +":00"; //$NON-NLS-1$
    private static final String ERROR_ID_PARSING_ERROR = ID_PREFIX + ":10"; //$NON-NLS-1$
    private static final String ERROR_ID_VALIDATION_FAILED = ID_PREFIX + ":20"; //$NON-NLS-1$

    @Get
    public Representation retrieve() {
        if (!Permits.isAllowed(getAction(), getPath())) {
            return createUnauthorizedRepresentation();
        }
        if (!isSupportedMediaType()) {
            setStatus(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE);
            return null;
        }

        String id = (String) getRequestAttributes().get(RestUtils.PARAM_ID);
        ProjectService projectService = ((ProjectService)EntityServices.getByEntityClass(Project.class));
        Project project = projectService.getProject(id);
        if (project == null) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND, MessageFormat.format("Project {0} not found", id)); //$NON-NLS-1$
            return null;
        }

        String[] extensions = ProjectConverter.ALL_EXTENSIONS;
        String extensionParam = getQueryAttribute("extensions"); //$NON-NLS-1$
        if (extensionParam != null) {
            extensions = StringUtils.split(extensionParam, ',');
        }
        if (enforceOldStyleConverters()) {
            return new ResourceRepresentation<Project>(project, new ProjectConverter(getHost(), extensions));
        }
        return new ResourceRepresentation<Project>(getResourceContext(), project, new ProjectConverter(extensions));
    }

    @Put
    public Representation update(Representation entity) {
        if (!Permits.isAllowed(getAction(), getPath())) {
            return createUnauthorizedRepresentation();
        }

        String id = (String) getRequestAttributes().get(RestUtils.PARAM_ID);
        ProjectService projectService = ((ProjectService)EntityServices.getByEntityClass(Project.class));
        Project project = projectService.loadProject(id);
        if (project == null) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND, MessageFormat.format("Project {0} not found", id));
            return null;
        }

        if (entity == null || !entity.isAvailable()) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Request entity required");
            return null;
        }
        if (!isSupportedMediaType()) {
            setStatus(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE);
            return null;
        }

        try {
            Reader entityReader = entity.getReader();
            if (entityReader == null) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Request entity required");
                return null;
            }
            Project uploaded = new ResourceRepresentation<Project>(getResourceContext(),
                    new ProjectConverter()).read(entityReader);
            updateProject(project, uploaded);

        } catch (RestException e) {
            String errorId = MessageFormat.format(ERROR_ID_PARSING_ERROR, id);
            return createParseErrorRepresentation(errorId, e);
        } catch (IOException e) {
            String errorId = MessageFormat.format(ERROR_ID_IO_ERROR, id);
            return createIOErrorRepresentation(errorId, e);
        }

        try {
            projectService.persist(project, Permits.getLoggedInUser());
        } catch (ValidationException e) {
            String errorId = MessageFormat.format(ERROR_ID_VALIDATION_FAILED, id);
            return createValidationFailedRepresentation(errorId, id, e);
        }

        setStatus(Status.SUCCESS_NO_CONTENT);
        return null;
    }

    /**
     * Merges the source project into the target project.
     * <p>
     * The {@link Project#getProjectId() symbolic name} of the target project
     * will only be overwritten, if it is not blank in the source project
     * (projects always must have a non-blank symbolic name!).
     * The UUID of the target project is never changed. All other properties
     * of the target project will be overwritten.
     * <p>
     * The extensions of the target project will only be overwritten selectively,
     * i.e. if the source project provides a certain extension then the corresponding
     * extensions of the target project will be replaced. If the target project does
     * not have theextension yet, it will be added. Extensions that are present
     * in the target project but not in the source project will not be removed.
     *
     * @param target  the project retrieved from the project service
     * @param source  the project read from the request entity
     */
    private void updateProject(Project target, Project source) {
        if (StringUtils.isNotBlank(source.getProjectId())) {
            target.setProjectId(source.getProjectId());
        }
        target.setName(source.getName());
        target.setShortName(source.getShortName());
        target.setDescriptionFormat(source.getDescriptionFormat());
        target.setDescription(source.getDescription());
        target.setProjectTemplateId(source.getProjectTemplateId());
        target.setPhase(source.getPhase());
        for (ExtensionEntityBase extension : source.getAllExtensions()) {
            Class<? extends ExtensionEntityBase> extensionClass = extension.getClass();
            if (source.isInherited(extensionClass)) {
                target.setInherited(extensionClass, true);
            } else {
                target.removeExtension(extensionClass);
                target.addExtension(extension);
            }
        }
    }
}