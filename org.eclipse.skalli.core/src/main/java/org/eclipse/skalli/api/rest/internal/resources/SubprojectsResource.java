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

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.Statistics;
import org.eclipse.skalli.commons.UUIDUtils;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.extension.rest.ResourceBase;
import org.eclipse.skalli.services.extension.rest.ResourceRepresentation;
import org.eclipse.skalli.services.extension.rest.RestUtils;
import org.eclipse.skalli.services.project.ProjectService;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

public class SubprojectsResource extends ResourceBase {

    @Get
    public Representation retrieve() {
        Statistics.getDefault().trackUsage("api.rest.subprojects.get"); //$NON-NLS-1$
        Subprojects subprojects = new Subprojects();
        subprojects.setSubprojects(new LinkedHashSet<Project>());

        String id = (String) getRequestAttributes().get(RestUtils.PARAM_ID);
        String depthArg = getQuery().getValues(RestUtils.PARAM_DEPTH);
        int depth;
        try {
            depth = (StringUtils.isBlank(depthArg)) ? Integer.MAX_VALUE : new Integer(depthArg).intValue();
        } catch (NumberFormatException e) {
            return createStatusMessage(Status.CLIENT_ERROR_BAD_REQUEST, "Depth value \"{0}\" should be a digit", depthArg);
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
            return createStatusMessage(Status.CLIENT_ERROR_NOT_FOUND, "Project \"{0}\" not found.", id);
        }
        Comparator<Project> comparator = new Comparator<Project>() {
            @Override
            public int compare(Project p1, Project p2) {
                // reverse ordering by project id!
                return p2.getProjectId().compareTo(p1.getProjectId());
            }
        };
        List<Project> subprojectList = projectService.getSubProjects(project.getUuid(), comparator, depth);
        subprojects.addAll(subprojectList);

        String extensionParam = getQuery().getValues(RestUtils.PARAM_EXTENSIONS);
        String[] extensions = new String[] {};
        if (extensionParam != null) {
            extensions = extensionParam.split(RestUtils.PARAM_LIST_SEPARATOR);
        }

        return new ResourceRepresentation<Subprojects>(subprojects,
               new SubprojectsConverter(getRequest().getResourceRef().getHostIdentifier(), extensions));
    }
}
