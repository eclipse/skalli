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
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
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
import org.eclipse.skalli.services.extension.rest.RestUtils;
import org.eclipse.skalli.services.group.GroupUtils;
import org.eclipse.skalli.services.project.ProjectService;
import org.eclipse.skalli.services.search.PagingInfo;
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
    private String query;
    private String tag;
    private String persist;
    private String propertyName;
    private String shortName;
    private Pattern pattern;
    private int start;
    private int count;
    private String[] extensionsToDisplay;

    /**Get might be used for 2 different cases:
     * 1) to list the details of the projects. Project set might be limited using lucene search query or tag.
     * 2) to dry-run mass data change. The project list shows the projects, affected by the change. Affected extension is included into the project details.
     * If actual persist is wanted, a persist flag should be set as a REST API URL argument (persist=true)
     */
    @Get
    public Representation retrieve() {
        try {
            Statistics.getDefault().trackUsage("api.rest.projects.get"); //$NON-NLS-1$
            readQueryArguments();

            Projects projects = getProjectsToUpdate();

            return new ResourceRepresentation<Projects>(projects,
                    new ProjectsConverter(getRequest().getResourceRef().getHostIdentifier(),
                            extensionsToDisplay, start));
        } catch (QueryParseException e) {
            return createStatusMessage(Status.CLIENT_ERROR_BAD_REQUEST, "Error parsing query: " + e.getMessage());//$NON-NLS-1$
        } catch (Exception e) {
            return createStatusMessage(Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
        }
    }

    private Projects getProjectsToUpdate() throws QueryParseException {
        SearchResult<Project> projectList = SearchUtils
                .searchProjects(query, tag, null, new PagingInfo(start, count));
        Projects projects = new Projects();
        projects.setProjects(new HashSet<Project>());
        for (Project project : projectList.getEntities()) {
            if (shortName != null) {
                if (shortName.equals("project")) {
                    projects = matchByProperty(projects, project, project);
                } else {
                    SortedSet<ExtensionEntityBase> allExtensions = project.getAllExtensions();
                    for (ExtensionEntityBase extensionEntity : allExtensions) {
                        ExtensionService<? extends ExtensionEntityBase> extensionService = ExtensionServices
                                .getExtensionService(extensionEntity.getClass());
                        if (extensionService.getShortName().equals(shortName)) {
                            projects = matchByProperty(projects, project, extensionEntity);
                        }
                    }
                }
                if (!ArrayUtils.contains(extensionsToDisplay, shortName)) {
                    extensionsToDisplay = (String[]) ArrayUtils.add(extensionsToDisplay, shortName);
                }
            } else {
                projects.getProjects().add(project);
            }

        }
        return projects;
    }

    private Projects matchByProperty(Projects projects, Project project, ExtensionEntityBase extensionEntity) {
        PropertyLookup propertyLookup = new PropertyLookup(extensionEntity);
        String lookupResult = propertyLookup.lookup(propertyName);
        if (lookupResult != null) {
            if (extensionEntity.getProperty(propertyName) instanceof Iterable) {
                String[] lookupSet = lookupResult.split(",");
                for (String string : lookupSet) {
                    Matcher matcher = pattern.matcher(string);
                    if (matcher.matches()) {
                        projects.getProjects().add(project);
                    }
                }
            } else {
                Matcher matcher = pattern.matcher(lookupResult);
                if (matcher.matches()) {
                    projects.getProjects().add(project);
                }
            }
        }
        return projects;
    }

    @Put
    public Representation update(Representation entity) throws ResourceException {
        try {
            ResourceRepresentation<PropertyUpdate> representation = new ResourceRepresentation<PropertyUpdate>();
            try {
                readQueryArguments();
            } catch (Exception e) {
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
                Projects projects = getProjectsToUpdate();
                ProjectService projectService = Services.getRequiredService(ProjectService.class);

                Set<Project> updatedProjectsSet = new HashSet<Project>();

                for (Project project : projects.getProjects()) {
                    try {
                        Project loadedProject = projectService.loadEntity(Project.class, project.getUuid());
                        if (loadedProject == null) {
                            //should never happen, just a precaution
                            continue;
                        }
                        if ("project".equals(shortName)) {
                            updateProperty(dataChange, loadedProject, loadedProject);
                        } else {
                            SortedSet<ExtensionEntityBase> allExtensions = loadedProject.getAllExtensions();
                            for (ExtensionEntityBase extensionEntity : allExtensions) {
                                ExtensionService<? extends ExtensionEntityBase> extensionService = ExtensionServices
                                        .getExtensionService(extensionEntity.getClass());
                                if (extensionService.getShortName().equals(shortName)) {
                                    updateProperty(dataChange, loadedProject, extensionEntity);
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
                if (doPersist()) {
                    for (Project project : updatedProjectsSet) {
                        projectService.persist(project, loggedInUser);
                    }
                }
                Projects updatedProjects = new Projects();
                updatedProjects.setProjects(updatedProjectsSet);
                return new ResourceRepresentation<Projects>(updatedProjects,
                        new ProjectsConverter(getRequest().getResourceRef().getHostIdentifier(),
                                extensionsToDisplay, start));
            }
        } catch (Exception e) {
            LOG.debug(MessageFormat.format("Could not update property {0}", propertyName), e);
            return createStatusMessage(Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
        }
    }

    private void updateProperty(PropertyUpdate dataChange, Project loadedProject, ExtensionEntityBase extensionEntity)
            throws Exception {
        Object property = extensionEntity.getProperty(propertyName);
        if (property instanceof Collection) {
            try {
                updateCollectionProperty(dataChange, loadedProject, extensionEntity, (Collection<String>) property);
            } catch (ClassCastException e) {
                throw new Exception(propertyName
                        + "is not of type Collection<String>, but only this type is currently supported.");
            }
        } else {
            updateStringProperty(dataChange, loadedProject, extensionEntity, property);
        }
    }

    private void updateStringProperty(PropertyUpdate dataChange, Project project,
            ExtensionEntityBase extensionEntity, Object property) throws Exception {
        String newValue = convert(property.toString(),
                pattern, dataChange.getTemplate(), project);
        extensionEntity.setProperty(propertyName, newValue);
    }

    private void updateCollectionProperty(PropertyUpdate dataChange, Project project,
            ExtensionEntityBase extensionEntity, Collection<String> properties) throws Exception {
        Collection<String> newCollection = new ArrayList<String>(properties.size());
        for (String string : properties) {
            Matcher matcher = pattern.matcher(string);
            if (matcher.matches()) {
                string = convert(string, pattern, dataChange.getTemplate(), project);
            }
            newCollection.add(string);
        }
        extensionEntity.setProperty(propertyName, newCollection);
    }

    private boolean doPersist() {
        return StringUtils.isNotBlank(persist) && Boolean.parseBoolean(persist);
    }

    private void readQueryArguments() throws Exception {
        Form form = getRequest().getResourceRef().getQueryAsForm();
        this.query = form.getFirstValue(RestUtils.PARAM_QUERY);
        this.tag = form.getFirstValue(RestUtils.PARAM_TAG);
        this.persist = form.getFirstValue(RestUtils.PARAM_PERSIST);
        String property = form.getFirstValue(RestUtils.PARAM_PROPERTY);
        if (StringUtils.isNotBlank(property)) {

            String[] split = property.split("\\.");
            if (split.length < 1 || split.length > 2) {
                throw new Exception("Property should conform to the pattern <extension.propertyName>");
            }
            this.shortName = split.length == 1 ? "project" : split[0];
            this.propertyName = split.length == 1 ? split[0] : split[1];

            String patternArg = form.getFirstValue(RestUtils.PARAM_PATTERN);
            if (StringUtils.isBlank(patternArg)) {
                //return all projects that have the given property, as no pattern was provided
                patternArg = ".+";
            }
            try {
                this.pattern = Pattern.compile(patternArg);
            } catch (PatternSyntaxException e) {
                throw new Exception("Pattern has a syntax error.", e);
            }
        }

        this.start = NumberUtils.toInt(form.getFirstValue(RestUtils.PARAM_START), 0);
        if (start < 0) {
            start = 0;
        }
        this.count = NumberUtils.toInt(form.getFirstValue(RestUtils.PARAM_COUNT), Integer.MAX_VALUE);
        if (count < 0) {
            count = Integer.MAX_VALUE;
        }
        this.extensionsToDisplay = new String[] {};
        String extensionParam = getQuery().getValues(RestUtils.PARAM_EXTENSIONS);
        if (extensionParam != null) {
            extensionsToDisplay = extensionParam.split(RestUtils.PARAM_LIST_SEPARATOR);
        }
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
