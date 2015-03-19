/*******************************************************************************
 * Copyright (c) 2010-2015 SAP AG and others.
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

import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.ValidationException;
import org.eclipse.skalli.services.entity.EntityServices;
import org.eclipse.skalli.services.extension.ExtensionService;
import org.eclipse.skalli.services.extension.ExtensionServices;
import org.eclipse.skalli.services.extension.rest.ResourceBase;
import org.eclipse.skalli.services.extension.rest.ResourceRepresentation;
import org.eclipse.skalli.services.extension.rest.RestException;
import org.eclipse.skalli.services.extension.rest.RestUtils;
import org.eclipse.skalli.services.permit.Permits;
import org.eclipse.skalli.services.project.ProjectService;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;

public class InheritableExtensionResource extends ResourceBase {

    private static final String ID_PREFIX = "rest:api/projects/{0}/extensions/{1}";  //$NON-NLS-1$
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
            setStatus(Status.CLIENT_ERROR_NOT_FOUND, MessageFormat.format("Project {0} not found", id));
            return null;
        }

        String shortName = (String) getRequestAttributes().get("shortName"); //$NON-NLS-1$
        ExtensionService<?> extensionService = ExtensionServices.getByShortName(shortName);
        if (extensionService == null) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST, MessageFormat.format("Unknown extension {0}", shortName));
            return null;
        }

        Class<? extends ExtensionEntityBase> extensionClass = extensionService.getExtensionClass();
        ExtensionEntityBase extension = project.getExtension(extensionClass);
        if (extension == null) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND,
                    MessageFormat.format("Project {0} has no {1} extension", id, shortName));
            return null;
        }

        return new ResourceRepresentation<InheritableExtension>(getResourceContext(),
                new InheritableExtension(extension, project.isInherited(extensionClass)),
                new InheritableExtensionConverter(extensionService));
    }

    @Put
    public Representation store(Representation entity) throws IOException {
        if (!Permits.isAllowed(getAction(), getPath())) {
            return createUnauthorizedRepresentation();
        }

        String id = (String) getRequestAttributes().get(RestUtils.PARAM_ID);
        ProjectService projectService = ((ProjectService)EntityServices.getByEntityClass(Project.class));
        Project project = projectService.getProject(id);
        if (project == null) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND, MessageFormat.format("Project {0} not found", id));
            return null;
        }

        String shortName = (String) getRequestAttributes().get("shortName"); //$NON-NLS-1$
        ExtensionService<?> extensionService = ExtensionServices.getByShortName(shortName);
        if (extensionService == null) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST, MessageFormat.format("Unknown extension {0}", shortName));
            return null;
        }

        Class<? extends ExtensionEntityBase> extensionClass = extensionService.getExtensionClass();
        ExtensionEntityBase extension = project.getExtension(extensionClass);
        if (extension == null) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND,
                    MessageFormat.format("Project {0} has no {1} extension", id, shortName));
            return null;
        }

        if (!entity.isAvailable()) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Request entity required");
            return null;
        }
        if (!isSupportedMediaType()) {
            setStatus(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE);
            return null;
        }
        Reader entityReader = entity.getReader();
        if (entityReader == null) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Request entity required");
            return null;
        }

        InheritableExtension inheritable;
        try {
            inheritable = new ResourceRepresentation<InheritableExtension>(getResourceContext(),
                    new InheritableExtensionConverter(extensionService)).read(entityReader);
        } catch (RestException e) {
            String errorId = MessageFormat.format(ERROR_ID_PARSING_ERROR, id, shortName);
            return createParseErrorRepresentation(errorId, e);
        }
        if (inheritable == null) {
            setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED,
                    MessageFormat.format("Extension {0} does not support PUT operations", shortName));
            return null;
        }

        // if there was no "inherited" attribute in the request, but the
        // extension is currently inherited, we refuse to overwrite it
        Boolean inherited = inheritable.isInherited();
        if (inherited == null && project.isInherited(extensionClass)) {
            setStatus(Status.CLIENT_ERROR_FORBIDDEN,
                    MessageFormat.format("Extension {0} is inherited", shortName));
            return null;
        }
        // if there is an "inherited" flag with the value true we switch
        // the extension to inherited state and ignore all other attributes
        // that may be present in the request; otherwise we remove the
        // previously attached extension and add the new extension; this
        // also implicitly switches of inheritance
        if (inherited == Boolean.TRUE) {
            project.setInherited(extensionClass, true);
        } else {
            project.removeExtension(extensionClass);
            project.addExtension(inheritable.getExtension());
        }

        try {
            projectService.persist(project, Permits.getLoggedInUser());
        } catch (ValidationException e) {
            String errorId = MessageFormat.format(ERROR_ID_VALIDATION_FAILED, id, shortName);
            return createValidationFailedRepresentation(errorId, id, e);
        }

        setStatus(Status.SUCCESS_NO_CONTENT);
        return null;
    }

    @Delete
    public Representation remove() throws IOException {
        if (!Permits.isAllowed(getAction(), getPath())) {
            return createUnauthorizedRepresentation();
        }

        String id = (String) getRequestAttributes().get(RestUtils.PARAM_ID);
        ProjectService projectService = ((ProjectService)EntityServices.getByEntityClass(Project.class));
        Project project = projectService.getProject(id);
        if (project == null) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND, MessageFormat.format("Project {0} not found", id));
            return null;
        }

        String shortName = (String) getRequestAttributes().get("shortName"); //$NON-NLS-1$
        ExtensionService<?> extensionService = ExtensionServices.getByShortName(shortName);
        if (extensionService == null) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST, MessageFormat.format("Unknown extension {0}", shortName));
            return null;
        }

        Class<? extends ExtensionEntityBase> extensionClass = extensionService.getExtensionClass();
        ExtensionEntityBase extension = project.removeExtension(extensionClass);
        if (extension == null) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND,
                    MessageFormat.format("Project {0} has no {1} extension", id, shortName));
            return null;
        }

        try {
            projectService.persist(project, Permits.getLoggedInUser());
        } catch (ValidationException e) {
            String errorId = MessageFormat.format(ERROR_ID_VALIDATION_FAILED, id, shortName);
            return createValidationFailedRepresentation(errorId, id, e);
        }

        setStatus(Status.SUCCESS_NO_CONTENT);
        return null;
    }
}
