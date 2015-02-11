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
import java.util.Comparator;

import org.apache.commons.lang.math.NumberUtils;
import org.eclipse.skalli.model.ByProjectIdComparator;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.services.entity.EntityServices;
import org.eclipse.skalli.services.extension.rest.ResourceBase;
import org.eclipse.skalli.services.extension.rest.ResourceRepresentation;
import org.eclipse.skalli.services.extension.rest.RestUtils;
import org.eclipse.skalli.services.permit.Permits;
import org.eclipse.skalli.services.project.ProjectService;
import org.eclipse.skalli.services.search.SearchQuery;
import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

public class SubprojectsResource extends ResourceBase {

    private static final String PARAM_DEPTH = "depth"; //$NON-NLS-1$

    @Get
    public Representation retrieve() {
        if (!Permits.isAllowed(getAction(), getPath())) {
            return createUnauthorizedRepresentation();
        }
        if (!isSupportedMediaType()) {
            setStatus(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE);
            return null;
        }

        Reference resourceRef = getRequest().getResourceRef();
        Form form = resourceRef.getQueryAsForm();

        String id = (String) getRequestAttributes().get(RestUtils.PARAM_ID);
        int depth = NumberUtils.toInt(form.getFirstValue(PARAM_DEPTH), Integer.MAX_VALUE);
        if (depth <= 0) {
            depth = 1;
        }
        String extensionParam = form.getFirstValue(SearchQuery.PARAM_EXTENSIONS);
        String[] extensions = new String[] {};
        if (extensionParam != null) {
            extensions = extensionParam.split(SearchQuery.PARAM_LIST_SEPARATOR);
        }

        ProjectService projectService = ((ProjectService)EntityServices.getByEntityClass(Project.class));
        Project project = projectService.getProject(id);
        if (project == null) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND, MessageFormat.format("Project {0} not found", id));
            return null;
        }

        Comparator<Project> comparator = new ByProjectIdComparator();
        Subprojects subprojects = new Subprojects(projectService.getSubProjects(project.getUuid(), comparator, depth));

        if (enforceOldStyleConverters()) {
            return new ResourceRepresentation<Subprojects>(subprojects,
                    new SubprojectsConverter(getHost(), extensions));
        }
        return new ResourceRepresentation<Subprojects>(getResourceContext(), subprojects,
                new SubprojectsConverter(extensions));
    }
}
