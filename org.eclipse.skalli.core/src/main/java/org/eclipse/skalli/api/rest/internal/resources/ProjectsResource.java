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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.text.StrLookup;
import org.apache.commons.lang.text.StrSubstitutor;
import org.eclipse.skalli.commons.Statistics;
import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.extension.ExtensionService;
import org.eclipse.skalli.services.extension.ExtensionServices;
import org.eclipse.skalli.services.extension.PropertyLookup;
import org.eclipse.skalli.services.extension.rest.ResourceBase;
import org.eclipse.skalli.services.extension.rest.ResourceRepresentation;
import org.eclipse.skalli.services.group.GroupUtils;
import org.eclipse.skalli.services.permit.Permit;
import org.eclipse.skalli.services.permit.Permits;
import org.eclipse.skalli.services.project.ProjectService;
import org.eclipse.skalli.services.search.QueryParseException;
import org.eclipse.skalli.services.search.SearchResult;
import org.eclipse.skalli.services.search.SearchUtils;
import org.eclipse.skalli.services.user.LoginUtils;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.ext.servlet.ServletUtils;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectsResource extends ResourceBase {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectsResource.class);

    /**Get might be used for 2 different cases:
     * 1) to list the details of the projects. Project set might be limited using lucene search query or tag.
     * 2) to dry-run mass data change. The project list shows the projects, affected by the change. Affected extension is included into the project details.
     * If actual persist is wanted, a persist flag should be set as a REST API URL argument (persist=true)
     */
    @Get
    public Representation retrieve() {
        String path = getReference().getPath();
        Representation result = checkAuthorization(Permit.ACTION_GET, path);
        if (result != null) {
            return result;
        }

        Form form = getRequest().getResourceRef().getQueryAsForm();
        try {
            RestSearchQuery queryParams = new RestSearchQuery(form);
            Projects projects = getProjectsToUpdate(queryParams);

            return new ResourceRepresentation<Projects>(projects,
                    new ProjectsConverter(getRequest().getResourceRef().getHostIdentifier(),
                            queryParams.getExtensions(), queryParams.getStart()));
        } catch (QueryParseException e) {
            return createStatusMessage(Status.CLIENT_ERROR_BAD_REQUEST, "Error parsing query: " + e.getMessage());//$NON-NLS-1$
        } catch (Exception e) {
            return createStatusMessage(Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
        }
    }

    private Projects getProjectsToUpdate(RestSearchQuery queryParams) throws QueryParseException {
        SearchResult<Project> searchResult = SearchUtils.searchProjects(queryParams);
        Statistics.getDefault().trackSearch(Permits.getLoggedInUser(), searchResult.getQueryString(),
                searchResult.getResultCount(), searchResult.getDuration());

        Projects projects = new Projects();
        projects.setProjects(new HashSet<Project>());
        String shortName = queryParams.getShortName();
        for (Project project : searchResult.getEntities()) {
            if (shortName != null) {
                if ("project".equals(shortName)) {
                    projects = matchByProperty(projects, project, project, queryParams);
                } else {
                    SortedSet<ExtensionEntityBase> allExtensions = project.getAllExtensions();
                    for (ExtensionEntityBase extensionEntity : allExtensions) {
                        ExtensionService<? extends ExtensionEntityBase> extensionService = ExtensionServices
                                .getByExtensionClass(extensionEntity.getClass());
                        if (extensionService.getShortName().equals(shortName)) {
                            projects = matchByProperty(projects, project, extensionEntity, queryParams);
                        }
                    }
                }
                // always render extensions that are referenced in the property attribute
                if (!queryParams.hasExtension(shortName)) {
                    queryParams.addExtension(shortName);
                }
            } else {
                projects.addProject(project);
            }

        }
        return projects;
    }

    private Projects matchByProperty(Projects projects, Project project, ExtensionEntityBase extensionEntity, RestSearchQuery queryParams) {
        String propertyName = queryParams.getPropertyName();
        PropertyLookup propertyLookup = new PropertyLookup(extensionEntity);
        String lookupResult = propertyLookup.lookup(propertyName);
        if (lookupResult != null & !queryParams.isNegate()) {
            if (extensionEntity.getProperty(propertyName) instanceof Iterable) {
                String[] lookupSet = lookupResult.split(",");
                for (String string : lookupSet) {
                    Matcher matcher = queryParams.getPattern().matcher(string);
                    if (matcher.matches()) {
                        projects.addProject(project);
                    }
                }
            } else {
                Matcher matcher = queryParams.getPattern().matcher(lookupResult);
                if (matcher.matches()) {
                    projects.addProject(project);
                }
            }
        } else if (lookupResult == null && queryParams.isNegate()){
            projects.addProject(project);
        }
        return projects;
    }

    @Put
    public Representation update(Representation entity) throws ResourceException {
        String path = getReference().getPath();
        Representation result = checkAuthorization(Permit.ACTION_PUT, path);
        if (result != null) {
            return result;
        }

        Form form = getRequest().getResourceRef().getQueryAsForm();
        RestSearchQuery queryParams = null;
        try {
            ResourceRepresentation<PropertyUpdate> representation = new ResourceRepresentation<PropertyUpdate>();
            try {
                queryParams = new RestSearchQuery(form);
            } catch (QueryParseException e) {
                return createStatusMessage(Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
            }
            representation.setConverters(new PropertyUpdateConverter(getRequest().getResourceRef()
                    .getHostIdentifier()));
            representation.setAnnotatedClasses(PropertyUpdate.class);
            PropertyUpdate dataChange = representation.read(entity, PropertyUpdate.class);
            LoginUtils loginUtils = new LoginUtils(ServletUtils.getRequest(getRequest()));
            String loggedInUser = loginUtils.getLoggedInUserId();
            if (!GroupUtils.isAdministrator(loggedInUser)) {
                return createStatusMessage(Status.CLIENT_ERROR_FORBIDDEN,
                        "Admin rights are required in order to perform this operation.");
            } else {
                Projects projects = getProjectsToUpdate(queryParams);
                ProjectService projectService = Services.getRequiredService(ProjectService.class);

                Set<Project> updatedProjectsSet = new HashSet<Project>();

                String shortName = queryParams.getShortName();
                for (Project project : projects.getProjects()) {
                    try {
                        Project loadedProject = projectService.loadEntity(Project.class, project.getUuid());
                        if (loadedProject == null) {
                            //should never happen, just a precaution
                            continue;
                        }
                        if ("project".equals(shortName)) {
                            updateProperty(dataChange, loadedProject, loadedProject, queryParams);
                        } else {
                            SortedSet<ExtensionEntityBase> allExtensions = loadedProject.getAllExtensions();
                            for (ExtensionEntityBase extensionEntity : allExtensions) {
                                ExtensionService<? extends ExtensionEntityBase> extensionService = ExtensionServices
                                        .getByExtensionClass(extensionEntity.getClass());
                                if (extensionService.getShortName().equals(shortName)) {
                                    updateProperty(dataChange, loadedProject, extensionEntity, queryParams);
                                }
                            }
                        }
                        //validate project
                        SortedSet<Issue> errors = projectService.validate(loadedProject, Severity.FATAL);
                        if (!errors.isEmpty()) {
                            String message = Issue.getMessage(
                                    MessageFormat.format(
                                            "Operation was canceled as validation of project {0} failed",
                                            loadedProject.getName()),
                                    errors);
                            return createStatusMessage(Status.CLIENT_ERROR_BAD_REQUEST, message);
                        }
                        updatedProjectsSet.add(loadedProject);
                    } catch (Exception e) {
                        LOG.debug(MessageFormat.format(
                                "Unexpected error while trying to update the property of the project {0}: {1}",
                                project.getName(), e.getMessage()), e);
                        return createStatusMessage(Status.SERVER_ERROR_INTERNAL, e.getMessage());
                    }
                }
                if (queryParams.doPersist()) {
                    for (Project project : updatedProjectsSet) {
                        projectService.persist(project, loggedInUser);
                    }
                }
                Projects updatedProjects = new Projects();
                updatedProjects.setProjects(updatedProjectsSet);
                return new ResourceRepresentation<Projects>(updatedProjects,
                        new ProjectsConverter(getRequest().getResourceRef().getHostIdentifier(),
                                queryParams.getExtensions(), queryParams.getStart()));
            }
        } catch (Exception e) {
            LOG.debug(MessageFormat.format("Could not update property {0}", queryParams.getPropertyName()), e);
            return createStatusMessage(Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
        }
    }

    private void updateProperty(PropertyUpdate dataChange, Project loadedProject, ExtensionEntityBase extensionEntity, RestSearchQuery queryParams)
            throws Exception {
        String propertyName = queryParams.getPropertyName();
        Object property = extensionEntity.getProperty(propertyName);
        if (property instanceof Collection) {
            try {
                updateCollectionProperty(dataChange, loadedProject, extensionEntity, (Collection<String>) property, queryParams);
            } catch (ClassCastException e) {
                throw new Exception(propertyName
                        + "is not of type Collection<String>, but only this type is currently supported.");
            }
        } else {
            updateStringProperty(dataChange, loadedProject, extensionEntity, property, queryParams);
        }
    }

    private void updateStringProperty(PropertyUpdate dataChange, Project project,
            ExtensionEntityBase extensionEntity, Object property, RestSearchQuery queryParams) throws Exception {
        String newValue = convert(property.toString(),
                queryParams.getPattern(), dataChange.getTemplate(), project);
        extensionEntity.setProperty(queryParams.getPropertyName(), newValue);
    }

    private void updateCollectionProperty(PropertyUpdate dataChange, Project project,
            ExtensionEntityBase extensionEntity, Collection<String> properties, RestSearchQuery queryParams) throws Exception {
        Collection<String> newCollection = new ArrayList<String>(properties.size());
        for (String string : properties) {
            Matcher matcher = queryParams.getPattern().matcher(string);
            if (matcher.matches()) {
                string = convert(string, queryParams.getPattern(), dataChange.getTemplate(), project);
            }
            newCollection.add(string);
        }
        extensionEntity.setProperty(queryParams.getPropertyName(), newCollection);
    }

    /*copied from MapperUtil. Should we move this class to the api?*/
    private static String convert(String s, Pattern regex, String template, Project project) {
        Matcher matcher = regex.matcher(s);
        if (!matcher.matches()) {
            return null;
        }
        Map<String, Object> properties = new HashMap<String, Object>();

        // put the project ID as property ${0}
        if (project != null) {
            properties.put("0", project.getProjectId()); //$NON-NLS-1$
        }
        // put the groups found by the matcher as properties ${1}, ${2}, ...
        for (int i = 1; i <= matcher.groupCount(); i++) {
            properties.put(Integer.toString(i), matcher.group(i));
        }
        StrLookup propertyResolver = new PropertyLookup(project, properties);
        StrSubstitutor subst = new StrSubstitutor(propertyResolver);
        return subst.replace(template);
    }

}
