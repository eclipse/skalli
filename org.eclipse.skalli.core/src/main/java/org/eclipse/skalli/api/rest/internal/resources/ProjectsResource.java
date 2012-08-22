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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
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
import org.eclipse.skalli.services.extension.PropertyMapper;
import org.eclipse.skalli.services.extension.rest.ResourceBase;
import org.eclipse.skalli.services.extension.rest.ResourceRepresentation;
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

    public static final String PARAM_COMPACT = "compact"; //$NON-NLS-1$

    private static final String ID_PREFIX = "rest:api/projects:"; //$NON-NLS-1$
    private static final String ERROR_ID_UNEXPECTED = ID_PREFIX + "00"; //$NON-NLS-1$
    private static final String ERROR_ID_IO_ERROR = ID_PREFIX + "10"; //$NON-NLS-1$
    private static final String ERROR_ID_INVALID_QUERY = ID_PREFIX + "20"; //$NON-NLS-1$
    private static final String ERROR_ID_VALIDATION_FAILED = ID_PREFIX + "/{0}:30"; //$NON-NLS-1$
    private static final String ERROR_ID_UNSUPPORTED_PROPERTY_TYPE = ID_PREFIX + ":40"; //$NON-NLS-1$

    @Get
    public Representation retrieve() {
        if (!Permits.isAllowed(getAction(), getPath())) {
            return createUnauthorizedRepresentation();
        }

        try {
            RestSearchQuery queryParams = new RestSearchQuery(getQueryAsForm());
            Projects projects = getProjects(queryParams);
            setStatus(Status.SUCCESS_OK);
            return createProjectsResourceRepresentation(projects, queryParams);

        } catch (QueryParseException e) {
            return createErrorRepresentation(Status.CLIENT_ERROR_BAD_REQUEST, ERROR_ID_INVALID_QUERY,
                    "Invalid query \"?{0}\": {1}", getQueryString(), e.getMessage()); //$NON-NLS-1$
        }
    }

    private Representation createProjectsResourceRepresentation(Projects projects, RestSearchQuery queryParams) {
        ResourceRepresentation<Projects> representation = new ResourceRepresentation<Projects>(projects,
                new ProjectsConverter(getHost(), queryParams.getExtensions(), queryParams.getStart()));
        representation.setCompact(hasQueryAttribute(PARAM_COMPACT));
        return representation;
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

        int size = projects.size();
        int fromIndex = Math.min(queryParams.getStart(), size) ;
        int toIndex = Math.min(fromIndex + queryParams.getCount(), size);
        Projects result = new Projects();

        if (StringUtils.isBlank(queryParams.getPropertyName())) {
            // if there is no property filter, just add the requested subset
            // of projects to the result and quit
            result.addProjects(projects.subList(fromIndex, toIndex));
        } else {
            // if there is a property filter, add only those projects
            // to the result that match the filter
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

            int index = 0;
            for (Project project : projects) {
                if (index >= toIndex) {
                    break;
                }
                if (matchesPropertyQuery(project, extClass, queryParams)) {
                    if (index >= fromIndex) {
                        result.addProject(project);
                    }
                    ++index;
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
        if (!Permits.isAllowed(getAction(), getPath())) {
            return createUnauthorizedRepresentation();
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

        Representation representation = updateProperties(projects, updatedProjects, queryParams, projectService);
        if (representation != null) {
            return representation;
        }

        if (queryParams.doPersist()) {
            representation = persistUpdatedProjects(updatedProjects, projectService);
            if (representation != null) {
                return representation;
            }
        }

        setStatus(Status.SUCCESS_OK);
        return createProjectsResourceRepresentation(projects, queryParams);
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
        String newValue = PropertyMapper.convert(propertyValue.toString(),
                queryParams.getPattern(), queryParams.getTemplate(), project);
        extension.setProperty(queryParams.getPropertyName(), newValue);
    }

    private void updateCollectionProperty(Project project,
            ExtensionEntityBase extension, Collection<String> propertyValues, RestSearchQuery queryParams) {
        Collection<String> newValues = new ArrayList<String>(propertyValues.size());
        for (String propertyValue : propertyValues) {
            Matcher matcher = queryParams.getPattern().matcher(propertyValue);
            if (matcher.matches()) {
                propertyValue = PropertyMapper.convert(propertyValue, queryParams.getPattern(), queryParams.getTemplate(), project);
            }
            newValues.add(propertyValue);
        }
        extension.setProperty(queryParams.getPropertyName(), newValues);
    }
}
