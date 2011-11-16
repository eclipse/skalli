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
package org.eclipse.skalli.view.internal.filter.ext;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.api.java.ProjectService;
import org.eclipse.skalli.common.Consts;
import org.eclipse.skalli.common.Services;
import org.eclipse.skalli.common.User;
import org.eclipse.skalli.common.configuration.ConfigurationService;
import org.eclipse.skalli.common.util.CollectionUtils;
import org.eclipse.skalli.gerrit.client.GerritClient;
import org.eclipse.skalli.gerrit.client.GerritService;
import org.eclipse.skalli.gerrit.client.config.ConfigKeyGerrit;
import org.eclipse.skalli.gerrit.client.exception.GerritClientException;
import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.model.core.ProjectMember;
import org.eclipse.skalli.model.ext.devinf.DevInfProjectExt;
import org.eclipse.skalli.model.ext.people.PeopleProjectExt;
import org.eclipse.skalli.view.internal.filter.FilterException;
import org.eclipse.skalli.view.internal.filter.FilterUtil;

public class GitGerritFilter implements Filter {

    private static final String URL_GITGERRITERROR = "gitgerriterror"; //$NON-NLS-1$

    public static final String PARAMETER_ACTION = "action"; //$NON-NLS-1$
    public static final String PARAMETER_GROUP = "group"; //$NON-NLS-1$
    public static final String PARAMETER_REPO = "repo"; //$NON-NLS-1$
    public static final String PARAMETER_PROPOSE_EXISTING_GROUPS = "proposeExistingGroups"; //$NON-NLS-1$

    public static final String ACTION_CHECK = "check"; //$NON-NLS-1$
    public static final String ACTION_SAVE = "save"; //$NON-NLS-1$
    public static final String ACTION_CANCEL = "cancel"; //$NON-NLS-1$

    public static final String ATTRIBUTE_GERRITHOST = "gerritHost"; //$NON-NLS-1$
    public static final String ATTRIBUTE_GERRITCONTACT = "gerritContact"; //$NON-NLS-1$
    public static final String ATTRIBUTE_PROPOSED_GROUP = "proposedGroup"; //$NON-NLS-1$
    public static final String ATTRIBUTE_PROPOSED_REPO = "proposedRepo"; //$NON-NLS-1$
    public static final String ATTRIBUTE_PROPOSED_EXISTING_GROUPS = "proposedExistingGroups"; //$NON-NLS-1$
    public static final String ATTRIBUTE_INVALID_GROUP = "invalidGroup"; //$NON-NLS-1$
    public static final String ATTRIBUTE_GROUP_EXISTS = "groupExists"; //$NON-NLS-1$
    public static final String ATTRIBUTE_INVALID_REPO = "invalidRepo"; //$NON-NLS-1$
    public static final String ATTRIBUTE_REPO_EXISTS = "repoExists"; //$NON-NLS-1$
    public static final String ATTRIBUTE_KNOWN_ACCOUNTS = "knownAccounts"; //$NON-NLS-1$

    public static final String ATTRIBUTE_DATA_SAVED = "dataSaved"; //$NON-NLS-1$

    public static final String ATTRIBUTE_NO_GERRIT_CLIENT = "noGerritClient"; //$NON-NLS-1$
    public static final String ATTRIBUTE_NO_GERRIT_USER = "noGerritUser"; //$NON-NLS-1$
    public static final String ATTRIBUTE_EXCEPTION = "exception"; //$NON-NLS-1$
    public static final String ATTRIBUTE_ERROR_MESSAGE = "errormessage"; //$NON-NLS-1$

    public static final String ERROR_NO_PARENT = "noParent"; //$NON-NLS-1$
    public static final String ERROR_NO_CONFIGURATION = "noConfiguration"; //$NON-NLS-1$

    public static final String GIT_PREFIX = "scm:git:"; //$NON-NLS-1$
    public static final String GIT_EXT = ".git"; //$NON-NLS-1$

    private static final String SPACE = " "; //$NON-NLS-1$

    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {

        // get some attributes from request provided by other filters
        final String projectId = (String) request.getAttribute(Consts.ATTRIBUTE_PROJECTID);
        final Project project = (Project) request.getAttribute(Consts.ATTRIBUTE_PROJECT);
        final User user = (User) request.getAttribute(Consts.ATTRIBUTE_USER);

        FilterException fe = null;
        if (StringUtils.isBlank(projectId)) {
            fe = new FilterException("projectId is blank.");
        } else if (project == null) {
            fe = new FilterException("project is null.");
        } else if (user == null) {
            fe = new FilterException("user is null.");
        }
        if (fe != null) {
            FilterUtil.handleException(request, response, fe);
            // abort this filter, forward to error page is triggered
            return;
        }

        // Retrieve request parameters
        final String action = request.getParameter(PARAMETER_ACTION);
        final String group = request.getParameter(PARAMETER_GROUP);
        final String repo = request.getParameter(PARAMETER_REPO);

        GerritClient client = null;
        try {
            client = getClient(user.getUserId());

            // special case. no client configured. cancel further processing.
            if (client == null) {
                request.setAttribute(ATTRIBUTE_ERROR_MESSAGE, ERROR_NO_CONFIGURATION);
                FilterUtil.forward(request, response, URL_GITGERRITERROR);
                return;
            }

            // (0) Check if project has at least one parent.
            if (project.getParentEntityId() == null) {
                request.setAttribute(ATTRIBUTE_ERROR_MESSAGE, ERROR_NO_PARENT);
                FilterUtil.forward(request, response, URL_GITGERRITERROR);
                return;
            }

            request.setAttribute(ATTRIBUTE_GERRITCONTACT, fromConfig(ConfigKeyGerrit.CONTACT));

            // (1) INITIAL (input proposals for group and repo)
            if (StringUtils.isBlank(action)) {
                request.setAttribute(ATTRIBUTE_GERRITHOST, fromConfig(ConfigKeyGerrit.HOST));
                request.setAttribute(ATTRIBUTE_PROPOSED_GROUP,
                        StringUtils.isNotBlank(group) ? group : generateName(project, "_", "_committers")); //$NON-NLS-1$ //$NON-NLS-2$
                request.setAttribute(ATTRIBUTE_PROPOSED_REPO,
                        StringUtils.isNotBlank(repo) ? repo : generateName(project, "/", StringUtils.EMPTY)); //$NON-NLS-1$
                if (BooleanUtils.toBoolean(request.getParameter(PARAMETER_PROPOSE_EXISTING_GROUPS))) {
                    request.setAttribute(ATTRIBUTE_PROPOSED_EXISTING_GROUPS, getGroupsFromHierarchy(client, project));
                }
            }
            // (2) CHECK (validate input against Gerrit)
            else if (ACTION_CHECK.equals(action) || ACTION_SAVE.equals(action)) {
                client.connect();

                // general group checks
                final boolean invalidGroup = StringUtils.isBlank(group) || StringUtils.contains(group, SPACE);
                request.setAttribute(ATTRIBUTE_INVALID_GROUP, invalidGroup);
                final boolean groupExists = !invalidGroup && client.groupExists(group);
                request.setAttribute(ATTRIBUTE_GROUP_EXISTS, groupExists);
                final boolean createGroup = !invalidGroup && !groupExists;

                // general repo checks
                final boolean invalidRepo = StringUtils.isBlank(repo) || StringUtils.contains(repo, SPACE);
                request.setAttribute(ATTRIBUTE_INVALID_REPO, invalidRepo);
                final boolean repoExists = !invalidRepo && client.projectExists(repo);
                request.setAttribute(ATTRIBUTE_REPO_EXISTS, repoExists);
                final boolean createRepo = !invalidGroup && !invalidRepo && !repoExists;

                // checks only relevant for group creation
                Set<String> knownAccounts = Collections.emptySet();
                boolean actingUserHasAccount = false;
                if (createGroup) {
                    knownAccounts = getKnownGerritAccountsForProject(client, project);
                    request.setAttribute(ATTRIBUTE_KNOWN_ACCOUNTS, knownAccounts);
                    actingUserHasAccount = knownAccounts.contains(user.getUserId());
                }
                request.setAttribute(ATTRIBUTE_NO_GERRIT_USER, !actingUserHasAccount);

                // (3) SAVE (if validation is OK)
                if (ACTION_SAVE.equals(action)) {
                    // Only proceed if ...
                    boolean persist = groupExists && repoExists; // ... both entities exist
                    persist = persist || groupExists && createRepo; // ... or repo will be created
                    persist = persist || createGroup && createRepo; // ... or repo and group will be created

                    if (persist) {
                        // perform operations on Gerrit
                        if (createGroup || createRepo) {
                            HttpServletRequest httpRequest = (HttpServletRequest) request;
                            final String description = "Created by " + user.getDisplayName() + ".\nMore details: " + httpRequest.getRequestURL().toString().replaceFirst(httpRequest.getServletPath(), "") + Consts.URL_PROJECTS + "/" + projectId; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

                            if (createGroup) {
                                client.createGroup(group, StringUtils.EMPTY, description, knownAccounts);
                            }
                            if (createRepo) {
                                client.createProject(repo, null, CollectionUtils.asSet(group), null, false,
                                        description, null, false, false);
                            }
                        }

                        // persist new SCM location @ project (if it doesn't exist already)
                        DevInfProjectExt devInf = project.getExtension(DevInfProjectExt.class);
                        if (devInf == null) {
                            devInf = new DevInfProjectExt();
                            project.addExtension(devInf);
                        }
                        String newScmLocation = getScmLocationStaticPart() + repo + GIT_EXT;
                        if (!devInf.hasScmLocation(newScmLocation)) {
                            devInf.addScmLocation(newScmLocation);
                            Services.getRequiredService(ProjectService.class).persist(project, user.getUserId());
                        }
                    }
                    request.setAttribute(ATTRIBUTE_DATA_SAVED, persist);
                }
            }
            // (!) CANCEL (redirect to project details)
            else if (ACTION_CANCEL.equals(action)) {
                ((HttpServletResponse) response).sendRedirect(Consts.URL_PROJECTS + "/" + projectId); //$NON-NLS-1$
            }

        } catch (final GerritClientException e) {
            handleException(request, response, e);
        } catch (final Exception e) {
            handleException(request, response, e);
        } finally {
            if (client != null) {
                client.disconnect();
            }
        }

        // proceed along the chain
        chain.doFilter(request, response);
    }

    /**
     * Dispatch this request to error page
     */
    private void handleException(ServletRequest request, ServletResponse response, Exception e)
            throws ServletException, IOException {
        RequestDispatcher rd = request.getRequestDispatcher(Consts.URL_ERROR);
        request.setAttribute(ATTRIBUTE_EXCEPTION, e);
        rd.forward(request, response);
    }

    /**
     * Generates a name based on the project hierarchy (short names) and the project ID using a delimiter and a possible suffix.
     */
    private String generateName(Project project, String delimiter, String suffix) {
        StringBuffer sb = new StringBuffer();
        for (Project parent : Services.getRequiredService(ProjectService.class).getParentChain(project.getUuid())) {
            if (parent.getProjectId() == project.getProjectId()) {
                sb.insert(0, parent.getProjectId());
            } else {
                sb.insert(0, delimiter);
                sb.insert(0, parent.getOrConstructShortName());
            }
        }
        sb.append(suffix);

        return sb.toString();
    }

    /**
     * Returns all accounts known to Gerrit based on the users (IDs) of the project.
     */
    private Set<String> getKnownGerritAccountsForProject(final GerritClient client, final Project project)
            throws GerritClientException {
        PeopleProjectExt peopleExt = project.getExtension(PeopleProjectExt.class);

        if (peopleExt == null) {
            return Collections.emptySet();
        }

        Set<ProjectMember> projectMembers = new HashSet<ProjectMember>();
        projectMembers.addAll(peopleExt.getLeads());
        projectMembers.addAll(peopleExt.getMembers());

        Set<String> accountIds = new HashSet<String>();
        for (ProjectMember projectMember : projectMembers) {
            accountIds.add(projectMember.getUserID());
        }
        return client.getKnownAccounts(accountIds);
    }

    /**
     * Retrieves a list of all groups known to Gerrit based on the project hierarchy.
     */
    private Set<String> getGroupsFromHierarchy(final GerritClient client, final Project project)
            throws GerritClientException {

        URI newScmUri = null;
        try {
            newScmUri = new URI(StringUtils.removeStart(getScmLocationStaticPart(), GIT_PREFIX));
        } catch (URISyntaxException e) {
            return Collections.emptySet();
        }

        ProjectService projectService = Services.getRequiredService(ProjectService.class);
        List<Project> relevantSkalliProjects = projectService.getParentChain(project.getUuid());
        relevantSkalliProjects.addAll(getSiblingsAndAllDescendants(projectService, project));

        Set<String> proposedGroups = new TreeSet<String>(new Comparator<String>() {
            @Override
            public int compare(String g1, String g2) {
                return g1.compareTo(g2);
            }
        });
        proposedGroups.addAll(client.getGroups(getRelevantGerritProjects(relevantSkalliProjects, newScmUri).toArray(
                new String[0])));

        return proposedGroups;
    }

    private List<Project> getSiblingsAndAllDescendants(ProjectService projectService, final Project project) {
        List<Project> result = new ArrayList<Project>();

        UUID parent = project.getParentProject();
        List<Project> siblings = projectService.getSubProjects(parent);
        result.addAll(siblings);

        //now add all Descendants of all the siblings
        for (Project sibling : siblings) {
            Comparator<Project> comparator = new Comparator<Project>() {
                @Override
                public int compare(Project p1, Project p2) {
                    return p1.getProjectId().compareTo(p2.getProjectId());
                }
            };

            result.addAll(projectService.getSubProjects(sibling.getUuid(), comparator, Integer.MAX_VALUE));
        }
        return result;
    }

    private Set<String> getRelevantGerritProjects(List<Project> skalliProjects, final URI newScmUri) {
        Set<String> relevantParentProjects = new HashSet<String>();
        for (Project parent : skalliProjects) {
            DevInfProjectExt devInf = parent.getExtension(DevInfProjectExt.class);
            if (devInf == null || parent.isInherited(DevInfProjectExt.class)) {
                continue;
            }

            for (String scmLocation : devInf.getScmLocations()) {
                URI existingScmUri = null;
                try {
                    existingScmUri = new URI(StringUtils.removeStart(scmLocation, GIT_PREFIX));
                } catch (URISyntaxException e) {
                    continue;
                }

                if (!StringUtils.equals(newScmUri.getScheme(), existingScmUri.getScheme())
                        || !StringUtils.equals(newScmUri.getHost(), existingScmUri.getHost())) {
                    continue;
                }

                String relevantProjectName = StringUtils.removeStart(existingScmUri.getPath(), "/"); //$NON-NLS-1$
                if (relevantProjectName.endsWith(GIT_EXT)) {
                    relevantProjectName = StringUtils.substring(relevantProjectName, 0, -GIT_EXT.length());
                }
                relevantParentProjects.add(relevantProjectName);
            }
        }
        return relevantParentProjects;
    }

    /**
     * Retrieves the client from the service, might be null
     */
    @SuppressWarnings("nls")
    private GerritClient getClient(final String userId) throws Exception {
        GerritService service = Services.getService(GerritService.class);
        if (service == null) {
            throw new Exception("Gerrit Service not available.");
        }
        return service.getClient(userId);
    }

    /**
     * Constructs the static part of a SCM location for the configured Gerrit
     */
    @SuppressWarnings("nls")
    private String getScmLocationStaticPart() {
        return String.format("%sgit://%s/", GIT_PREFIX, fromConfig(ConfigKeyGerrit.HOST));
    }

    /**
     * Utility to get GerritConfig entries
     */
    private String fromConfig(final ConfigKeyGerrit key) {
        ConfigurationService configService = Services.getService(ConfigurationService.class);
        return configService.readString(key);
    }

}
