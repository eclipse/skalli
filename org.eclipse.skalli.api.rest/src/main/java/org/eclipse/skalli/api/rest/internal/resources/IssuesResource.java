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

import java.util.UUID;

import org.eclipse.skalli.api.java.IssuesService;
import org.eclipse.skalli.api.java.ProjectService;
import org.eclipse.skalli.api.rest.AbstractServerResource;
import org.eclipse.skalli.api.rest.ResourceRepresentation;
import org.eclipse.skalli.common.Services;
import org.eclipse.skalli.common.util.Statistics;
import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.model.ext.Issues;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

public class IssuesResource extends AbstractServerResource {

    @Get
    public Representation retrieve() {
        Statistics.getDefault().trackUsage("api.rest.issues.get"); //$NON-NLS-1$

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
            return createError(Status.CLIENT_ERROR_NOT_FOUND, "Project \"{0}\" not found.", id);
        }

        IssuesService issuesService = Services.getService(IssuesService.class);

        if (issuesService == null) {
            return createError(Status.SERVER_ERROR_SERVICE_UNAVAILABLE,
                    "Issues service is currently unavailable. Try again later.");
        }

        Issues issues = issuesService.getByUUID(project.getUuid());
        if (issues == null) {
            return createError(Status.CLIENT_ERROR_NOT_FOUND, "Project \"{0}\" has no issues", id);
        }

        return new ResourceRepresentation<Issues>(issues,
                new IssuesConverter(getRequest().getResourceRef().getHostIdentifier()));
    }
}
