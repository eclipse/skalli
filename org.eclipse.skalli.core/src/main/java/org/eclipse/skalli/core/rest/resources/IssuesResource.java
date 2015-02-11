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

import java.text.MessageFormat;

import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.extension.rest.ResourceBase;
import org.eclipse.skalli.services.extension.rest.ResourceRepresentation;
import org.eclipse.skalli.services.extension.rest.RestUtils;
import org.eclipse.skalli.services.issues.Issues;
import org.eclipse.skalli.services.issues.IssuesService;
import org.eclipse.skalli.services.permit.Permits;
import org.eclipse.skalli.services.project.ProjectService;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

public class IssuesResource extends ResourceBase {

    // error codes for logging and error responses
    private static final String ERROR_ID_NO_ISSUES_SERVICE_AVAILABLE = "rest:api/projects/{0}/issues:10"; //$NON-NLS-1$

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

        ProjectService projectService = Services.getRequiredService(ProjectService.class);
        Project project = projectService.getProject(id);
        if (project == null) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND, MessageFormat.format("Project \"{0}\" not found", id));
            return null;
        }

        IssuesService issuesService = Services.getService(IssuesService.class);
        if (issuesService == null) {
            String errorId = MessageFormat.format(ERROR_ID_NO_ISSUES_SERVICE_AVAILABLE, project.getProjectId());
            return createServiceUnavailableRepresentation(errorId, "Issues Service");
        }

        Issues issues = issuesService.getByUUID(project.getUuid());
        if (issues == null) {
            setStatus(Status.SUCCESS_NO_CONTENT);
            return null;
        }

        if (enforceOldStyleConverters()) {
            return new ResourceRepresentation<Issues>(issues, new IssuesConverter(getHost()));
        }
        return new ResourceRepresentation<Issues>(getResourceContext(), issues, new IssuesConverter());
    }
}
