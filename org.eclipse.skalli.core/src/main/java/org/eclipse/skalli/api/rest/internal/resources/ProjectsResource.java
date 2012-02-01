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

import org.apache.commons.lang.math.NumberUtils;
import org.eclipse.skalli.commons.Statistics;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.services.extension.rest.ResourceBase;
import org.eclipse.skalli.services.extension.rest.ResourceRepresentation;
import org.eclipse.skalli.services.extension.rest.RestUtils;
import org.eclipse.skalli.services.search.PagingInfo;
import org.eclipse.skalli.services.search.QueryParseException;
import org.eclipse.skalli.services.search.SearchResult;
import org.eclipse.skalli.services.search.SearchUtils;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

public class ProjectsResource extends ResourceBase {

    @Get
    public Representation retrieve() {
        try {
            Statistics.getDefault().trackUsage("api.rest.projects.get"); //$NON-NLS-1$
            Projects projects = new Projects();
            projects.setProjects(new HashSet<Project>());

            Form form = getRequest().getResourceRef().getQueryAsForm();
            String query = form.getFirstValue(RestUtils.PARAM_QUERY);
            String tag = form.getFirstValue(RestUtils.PARAM_TAG);

            int start = NumberUtils.toInt(form.getFirstValue(RestUtils.PARAM_START), 0);
            if (start < 0) {
                start = 0;
            }
            int count = NumberUtils.toInt(form.getFirstValue(RestUtils.PARAM_COUNT), Integer.MAX_VALUE);
            if (count < 0) {
                count = Integer.MAX_VALUE;
            }

            SearchResult<Project> projectList = SearchUtils.searchProjects(query, tag, null, new PagingInfo(start, count));

            for (Project project : projectList.getEntities()) {
                projects.getProjects().add(project);
            }

            String extensionParam = getQuery().getValues(RestUtils.PARAM_EXTENSIONS);
            String[] extensions = new String[] {};
            if (extensionParam != null) {
                extensions = extensionParam.split(RestUtils.PARAM_LIST_SEPARATOR);
            }
            return new ResourceRepresentation<Projects>(projects,
                   new ProjectsConverter(getRequest().getResourceRef().getHostIdentifier(), extensions, start));
        } catch (QueryParseException e) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return new StringRepresentation("Error parsing query: " + e.getMessage()); //$NON-NLS-1$
        }
    }
}
