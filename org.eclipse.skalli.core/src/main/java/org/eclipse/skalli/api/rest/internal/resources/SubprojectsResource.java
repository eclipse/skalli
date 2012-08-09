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

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.UUID;

import org.apache.commons.lang.math.NumberUtils;
import org.eclipse.skalli.commons.UUIDUtils;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.extension.rest.ResourceBase;
import org.eclipse.skalli.services.extension.rest.ResourceRepresentation;
import org.eclipse.skalli.services.extension.rest.RestUtils;
import org.eclipse.skalli.services.permit.Permit;
import org.eclipse.skalli.services.project.ProjectService;
import org.eclipse.skalli.services.search.SearchQuery;
import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

public class SubprojectsResource extends ResourceBase {

    private static final String ID_PREFIX = "rest:api/projects/{0}/subprojects:"; //$NON-NLS-1$
    private static final String ERROR_ID_INVALID_QUERY = ID_PREFIX + "20"; //$NON-NLS-1$

    @Get
    public Representation retrieve() {
        String path = getReference().getPath();
        Representation result = checkAuthorization(Permit.ACTION_GET, path);
        if (result != null) {
            return result;
        }

        Reference resourceRef = getRequest().getResourceRef();
        Form form = resourceRef.getQueryAsForm();

        String id = (String) getRequestAttributes().get(RestUtils.PARAM_ID);
        int depth = NumberUtils.toInt(form.getFirstValue(RestUtils.PARAM_DEPTH), Integer.MAX_VALUE);
        if (depth <= 0) {
            depth = 1;
        }
        String extensionParam = form.getFirstValue(SearchQuery.PARAM_EXTENSIONS);
        String[] extensions = new String[] {};
        if (extensionParam != null) {
            extensions = extensionParam.split(SearchQuery.PARAM_LIST_SEPARATOR);
        }

        ProjectService projectService = Services.getRequiredService(ProjectService.class);
        Project project = null;
        if (UUIDUtils.isUUID(id)) {
            UUID uuid = UUID.fromString(id);
            project = projectService.getByUUID(uuid);
        } else {
            project = projectService.getProjectByProjectId(id);
        }
        if (project == null) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND, MessageFormat.format("Project {0} not found", id));
            return null;
        }

        Comparator<Project> comparator = new Comparator<Project>() {
            @Override
            public int compare(Project p1, Project p2) {
                // reverse ordering by project id!
                return p2.getProjectId().compareTo(p1.getProjectId());
            }
        };
        Subprojects subprojects = new Subprojects(projectService.getSubProjects(project.getUuid(), comparator, depth));
        return new ResourceRepresentation<Subprojects>(subprojects,
               new SubprojectsConverter(resourceRef.getHostIdentifier(), extensions));
    }
}
