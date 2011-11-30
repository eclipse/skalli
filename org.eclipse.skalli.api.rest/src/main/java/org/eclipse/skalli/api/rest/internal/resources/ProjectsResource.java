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

import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.eclipse.skalli.api.java.PagingInfo;
import org.eclipse.skalli.api.java.ProjectService;
import org.eclipse.skalli.api.java.QueryParseException;
import org.eclipse.skalli.api.java.SearchService;
import org.eclipse.skalli.api.rest.internal.util.ResourceRepresentation;
import org.eclipse.skalli.common.Consts;
import org.eclipse.skalli.common.Services;
import org.eclipse.skalli.common.util.Statistics;
import org.eclipse.skalli.model.core.Project;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class ProjectsResource extends ServerResource {

    @Get
    public Representation retrieve() {
        try {
            Statistics.getDefault().trackUsage("api.rest.projects.get"); //$NON-NLS-1$
            Projects projects = new Projects();
            projects.setProjects(new HashSet<Project>());

            Form form = getRequest().getResourceRef().getQueryAsForm();
            String query = form.getFirstValue(Consts.PARAM_QUERY);
            String tag = form.getFirstValue(Consts.PARAM_TAG);

            int start = NumberUtils.toInt(form.getFirstValue(Consts.PARAM_START), 0);
            if (start < 0) {
                start = 0;
            }
            int count = NumberUtils.toInt(form.getFirstValue(Consts.PARAM_COUNT), Integer.MAX_VALUE);
            if (count < 0) {
                count = Integer.MAX_VALUE;
            }

            List<Project> projectList = null;
            if (query != null) {
                SearchService searchService = Services.getRequiredService(SearchService.class);
                projectList = searchService.findProjectsByQuery(query, new PagingInfo(start, count)).getEntities();
            } else if (tag != null) {
                SearchService searchService = Services.getRequiredService(SearchService.class);
                projectList = searchService.findProjectsByTag(tag, new PagingInfo(start, count)).getEntities();
            } else {
                ProjectService projectService = Services.getRequiredService(ProjectService.class);
                projectList = projectService.getAll();
            }
            for (Project project : projectList) {
                projects.getProjects().add(project);
            }

            String extensionParam = getQuery().getValues(Consts.PARAM_EXTENSIONS);
            String[] extensions = new String[] {};
            if (extensionParam != null) {
                extensions = extensionParam.split(Consts.PARAM_LIST_SEPARATOR);
            }
            return new ResourceRepresentation<Projects>(projects,
                   new ProjectsConverter(getRequest().getResourceRef().getHostIdentifier(), extensions, start));
        } catch (QueryParseException e) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return new StringRepresentation("Error parsing query: " + e.getMessage()); //$NON-NLS-1$
        }
    }
}
