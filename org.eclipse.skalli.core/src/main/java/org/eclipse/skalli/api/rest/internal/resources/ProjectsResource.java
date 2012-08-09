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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrLookup;
import org.apache.commons.lang.text.StrSubstitutor;
import org.eclipse.skalli.commons.Statistics;
import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.model.ValidationException;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.extension.ExtensionService;
import org.eclipse.skalli.services.extension.ExtensionServices;
import org.eclipse.skalli.services.extension.PropertyLookup;
import org.eclipse.skalli.services.extension.rest.ResourceBase;
import org.eclipse.skalli.services.extension.rest.ResourceRepresentation;
import org.eclipse.skalli.services.permit.Permit;
import org.eclipse.skalli.services.permit.Permits;
import org.eclipse.skalli.services.project.ProjectService;
import org.eclipse.skalli.services.search.QueryParseException;
import org.eclipse.skalli.services.search.SearchQuery;
import org.eclipse.skalli.services.search.SearchResult;
import org.eclipse.skalli.services.search.SearchUtils;
import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectsResource extends ResourceBase {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectsResource.class);

    private static final String ID_PREFIX = "rest:api/projects:"; //$NON-NLS-1$
    private static final String ERROR_ID_UNEXPECTED = ID_PREFIX + "00"; //$NON-NLS-1$
    private static final String ERROR_ID_IO_ERROR = ID_PREFIX + "10"; //$NON-NLS-1$
    private static final String ERROR_ID_INVALID_QUERY = ID_PREFIX + "20"; //$NON-NLS-1$
    private static final String ERROR_ID_VALIDATION_FAILED = ID_PREFIX + "/{0}:30"; //$NON-NLS-1$
    private static final String ERROR_ID_UNSUPPORTED_PROPERTY_TYPE = ID_PREFIX + ":40"; //$NON-NLS-1$

    /**Get might be used for 2 different cases:
     * 1) to list the details of the projects. Project set might be limited using Lucene search query or tag.
     * 2) to dry-run mass data change. The project list shows the projects, affected by the change.
     *    Affected extension is included into the project details.
     * If actual persist is wanted, a persist flag should be set as a REST API URL argument (persist=true)
     */
    @Get
    public Representation retrieve() {
        String path = getReference().getPath();
        Representation result = checkAuthorization(Permit.ACTION_GET, path);
        if (result != null) {
            return result;
        }

        Reference resourceRef = getRequest().getResourceRef();
        Form form = resourceRef.getQueryAsForm();
        try {
            RestSearchQuery queryParams = new RestSearchQuery(form);
            Projects projects = getProjects(queryParams);

            return new ResourceRepresentation<Projects>(projects,
                    new ProjectsConverter(resourceRef.getHostIdentifier(),
                            queryParams.getExtensions(), queryParams.getStart()));

        } catch (QueryParseException e) {
            return createErrorRepresentation(Status.CLIENT_ERROR_BAD_REQUEST, ERROR_ID_INVALID_QUERY,
                    "Invalid query \"?{0}\": {1}", form.getQueryString(), e.getMessage()); //$NON-NLS-1$
        }
    }

    private Projects getProjects(SearchQuery queryParams) throws QueryParseException {
        List<Project> projects = null;
        if (StringUtils.isBlank(queryParams.getQuery()) || "*".equals(queryParams.getQuery())) { //$NON-NLS-1$
            projects = Services.getRequiredService(ProjectService.class).getAll();
        } else {
            SearchResult<Project> searchResult = SearchUtils.searchProjects(queryParams);
            Statistics.getDefault().trackSearch(Permits.getLoggedInUser(), searchResult.getQueryString(),
                    searchResult.getResultCount(), searchResult.getDuration());
            projects = searchResult.getEntities();
        }

        Projects result = new Projects();
        if (StringUtils.isBlank(queryParams.getPropertyName())) {
            // if there is no property filter, add all projects to the result
            result.addProjects(projects);
        } else {
            // if there is a property filter, add only those projects
            // to the result that match this filter
            Class<? extends ExtensionEntityBase> extClass = null;
            if (queryParams.isExtension()) {
                String shortName = queryParams.getShortName();
                ExtensionService<?> extService = ExtensionServices.getByShortName(shortName);
                extClass = extService != null? extService.getExtensionClass() : null;
                // always render extensions that are referenced in the property query
                queryParams.addExtension(shortName);
            }
            Set<String> propertyNames = EntityBase.getPropertyNames(extClass != null? extClass : Project.class);
            if (!propertyNames.contains(queryParams.getPropertyName())) {
                throw new QueryParseException(MessageFormat.format("Unknown property \"{0}\"", queryParams.getProperty()));
            }

            for (Project project : projects) {
                if (matchesPropertyQuery(project, extClass, queryParams)) {
                    result.addProject(project);
                }
            }
        }
        return result;
    }

    boolean matchesPropertyQuery(Project project, Class<? extends ExtensionEntityBase> extClass, SearchQuery queryParams) {
        String propertyName = queryParams.getPropertyName();
        ExtensionEntityBase ext = extClass != null ? project.getExtension(extClass) : project;
        Object propertyValue = ext != null ? ext.getProperty(propertyName) : null;
        if (queryParams.isNegate()) {
            return isBlank(propertyValue);
        }
        return matches(propertyValue, queryParams.getPattern());
    }

    static boolean isBlank(Object o) {
        if (o == null) {
            return true;
        }
        if (o instanceof Iterable) {
            return !((Iterable<?>)o).iterator().hasNext();
        }
        return StringUtils.isBlank(o.toString());
    }

    static boolean matches(Object propertyValue, Pattern pattern) {
        if (propertyValue == null) {
            return false;
        }
        if (propertyValue instanceof Iterable) {
            Iterator<?> it = ((Iterable<?>)propertyValue).iterator();
            while (it.hasNext()) {
                Matcher matcher = pattern.matcher(it.next().toString());
                if (matcher.matches()) {
                    return true;
                }
            }
        } else {
            Matcher matcher = pattern.matcher(propertyValue.toString());
            if (matcher.matches()) {
                return true;
            }
        }
        return false;
    }

    @Put
    public Representation update(Representation entity) throws ResourceException {
        String path = getReference().getPath();
        Representation result = checkAuthorization(Permit.ACTION_PUT, path);
        if (result != null) {
            return result;
        }

        Reference resourceRef = getRequest().getResourceRef();
        Form form = resourceRef.getQueryAsForm();
        RestSearchQuery queryParams = null;
        try {
            queryParams = new RestSearchQuery(form, entity);
        } catch (QueryParseException e) {
            return createErrorRepresentation(Status.CLIENT_ERROR_BAD_REQUEST, ERROR_ID_INVALID_QUERY,
                    "Invalid query \"?{0}\": {1}", form.getQueryString(), e.getMessage()); //$NON-NLS-1$
        } catch (IOException e) {
            return createIOErrorRepresentation(ERROR_ID_IO_ERROR, e);
        }

        Projects projects = null;
        try {
            projects = getProjects(queryParams);
        } catch (QueryParseException e) {
            return createErrorRepresentation(Status.CLIENT_ERROR_BAD_REQUEST, ERROR_ID_INVALID_QUERY,
                    "Invalid query \"?{0}\": {1}", form.getQueryString(), e.getMessage()); //$NON-NLS-1$
        }

        ProjectService projectService = Services.getRequiredService(ProjectService.class);
        Projects updatedProjects = new Projects();

        result = updateProperties(projects, updatedProjects, queryParams, projectService);
        if (result != null) {
            return result;
        }

        if (queryParams.doPersist()) {
            result = persistUpdatedProjects(updatedProjects, projectService);
            if (result != null) {
                return result;
            }
        }

        return new ResourceRepresentation<Projects>(updatedProjects,
                new ProjectsConverter(resourceRef.getHostIdentifier(),
                        queryParams.getExtensions(), queryParams.getStart()));
    }

    private Representation persistUpdatedProjects(Projects projects, ProjectService projectService) {
        String loggedInUser = Permits.getLoggedInUser();
        try {
            for (Project project : projects.getProjects()) {
                projectService.persist(project, loggedInUser);
            }
        } catch (ValidationException e) {
            // should never happen since we validated all projects beforehand, but you never know
            return createUnexpectedErrorRepresentation(ERROR_ID_UNEXPECTED, e);
        }
        return null;
    }

    private Representation updateProperties(Projects projects, Projects updatedProjects,
            RestSearchQuery queryParams, ProjectService projectService) {

        Class<? extends ExtensionEntityBase> extensionClass = null;
        if (queryParams.isExtension()) {
            String shortName = queryParams.getShortName();
            ExtensionService<?> extService = ExtensionServices.getByShortName(shortName);
            extensionClass = extService != null? extService.getExtensionClass() : null;
        }
        try {
            for (Project project : projects.getProjects()) {
                Project loadedProject = projectService.loadEntity(Project.class, project.getUuid());
                if (loadedProject == null) {
                    //should never happen, just a precaution
                    continue;
                }
                updateProperty(loadedProject, extensionClass, queryParams);

                SortedSet<Issue> issues = projectService.validate(loadedProject, Severity.FATAL);
                if (!issues.isEmpty()) {
                    String errorId = MessageFormat.format(ERROR_ID_VALIDATION_FAILED, loadedProject.getProjectId());
                    return createValidationFailedRepresentation(errorId, loadedProject.getProjectId(), issues);
                }

                updatedProjects.addProject(loadedProject);
            }
        } catch (UnsupportedOperationException e) {
            String message = MessageFormat.format("Failed to update property \"{0}\": unsupported type",
                    queryParams.getProperty());
            LOG.warn(MessageFormat.format("{0} ({1})", message, ERROR_ID_UNSUPPORTED_PROPERTY_TYPE));
            return createErrorRepresentation(Status.CLIENT_ERROR_BAD_REQUEST,
                    ERROR_ID_UNSUPPORTED_PROPERTY_TYPE, message);
        } catch (RuntimeException e) {
            // NoSuchPropertyException, PropertyUpdateExeption: should not happen for correctly
            // implemented extensions, but you never know
            return createUnexpectedErrorRepresentation(ERROR_ID_UNEXPECTED, e);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void updateProperty(Project project, Class<? extends ExtensionEntityBase> extensionClass,
            RestSearchQuery queryParams) {
        String propertyName = queryParams.getPropertyName();
        ExtensionEntityBase extension = extensionClass != null ? project.getExtension(extensionClass) : project;
        Object propertyValue = extension != null ? extension.getProperty(propertyName) : null;
        if (propertyValue instanceof Collection) {
            try {
                updateCollectionProperty(project, extension, (Collection<String>) propertyValue, queryParams);
            } catch (ClassCastException e) {
                throw new UnsupportedOperationException(MessageFormat.format(
                        "\"{0}\" is not a collection of strings", propertyName));
            }
        } else {
            updateStringProperty(project, propertyValue, extension, queryParams);
        }
    }

    private void updateStringProperty(Project project, Object propertyValue,
            ExtensionEntityBase extension,  RestSearchQuery queryParams) {
        String newValue = convert(propertyValue.toString(),
                queryParams.getPattern(), queryParams.getTemplate(), project);
        extension.setProperty(queryParams.getPropertyName(), newValue);
    }

    private void updateCollectionProperty(Project project,
            ExtensionEntityBase extension, Collection<String> propertyValues, RestSearchQuery queryParams) {
        Collection<String> newValues = new ArrayList<String>(propertyValues.size());
        for (String propertyValue : propertyValues) {
            Matcher matcher = queryParams.getPattern().matcher(propertyValue);
            if (matcher.matches()) {
                propertyValue = convert(propertyValue, queryParams.getPattern(), queryParams.getTemplate(), project);
            }
            newValues.add(propertyValue);
        }
        extension.setProperty(queryParams.getPropertyName(), newValues);
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
