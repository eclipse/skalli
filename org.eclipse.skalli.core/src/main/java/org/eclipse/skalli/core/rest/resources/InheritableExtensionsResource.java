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

import org.apache.commons.lang.StringUtils;
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
import org.restlet.resource.Post;


public class InheritableExtensionsResource extends ResourceBase {

    private static final String ID_PREFIX = "rest:api/projects/{0}/extensions";  //$NON-NLS-1$
    private static final String ERROR_ID_PARSING_ERROR = ID_PREFIX + ":10"; //$NON-NLS-1$
    private static final String ERROR_ID_VALIDATION_FAILED = ID_PREFIX + ":20"; //$NON-NLS-1$

    @Post
    public Representation create(Representation entity) throws IOException {
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

        String shortName = getHeader("Slug", null); //$NON-NLS-1$
        if (StringUtils.isBlank(shortName)) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST, MessageFormat.format("Slug header missing", id));
            return null;
        }

        ExtensionService<?> extensionService = ExtensionServices.getByShortName(shortName);
        if (extensionService == null) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST, MessageFormat.format("Unknown extension {0}", shortName));
            return null;
        }

        Class<? extends ExtensionEntityBase> extensionClass = extensionService.getExtensionClass();
        ExtensionEntityBase extension = project.getExtension(extensionClass);
        if (extension != null) {
            setStatus(Status.CLIENT_ERROR_FORBIDDEN,
                    MessageFormat.format("Project {0} already has extension {1}", id, shortName));
            return null;
        }

        if (entity.isAvailable()) {
            if (!isSupportedMediaType()) {
                setStatus(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE);
                return null;
            }

            Reader entityReader = entity.getReader();

            InheritableExtension inheritable;
            try {
                inheritable = new ResourceRepresentation<InheritableExtension>(getResourceContext(),
                        new InheritableExtensionConverter(extensionService)).read(entityReader);
            } catch (RestException e) {
                String errorId = MessageFormat.format(ERROR_ID_PARSING_ERROR, id);
                return createParseErrorRepresentation(errorId, e);
            }
            if (inheritable == null) {
                setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED,
                        MessageFormat.format("Extension {0} does not support write", shortName));
                return null;
            }

            Boolean inherited = inheritable.isInherited();
            if (inherited == Boolean.TRUE) {
                project.setInherited(extensionClass, true);
            } else {
                project.addExtension(inheritable.getExtension());
            }
        } else {
            project.addExtension(extensionService.newExtension());
        }

        try {
            projectService.persist(project, Permits.getLoggedInUser());
        } catch (ValidationException e) {
            String errorId = MessageFormat.format(ERROR_ID_VALIDATION_FAILED, id, shortName);
            return createValidationFailedRepresentation(errorId, id, e);
        }

        getResponse().setLocationRef(MessageFormat.format(
                "{0}{1}{2}/extensions/{3}", getHost(), RestUtils.URL_PROJECTS, id, shortName));
        setStatus(Status.SUCCESS_CREATED);
        return null;
    }
}
