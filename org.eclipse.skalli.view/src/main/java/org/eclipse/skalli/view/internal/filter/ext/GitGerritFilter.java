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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.gerrit.client.GerritClient;
import org.eclipse.skalli.gerrit.client.GerritService;
import org.eclipse.skalli.gerrit.client.config.GerritServerConfig;
import org.eclipse.skalli.gerrit.client.config.GerritServersConfig;
import org.eclipse.skalli.gerrit.client.exception.CommandException;
import org.eclipse.skalli.gerrit.client.exception.ConnectionException;
import org.eclipse.skalli.gerrit.client.exception.GerritClientException;
import org.eclipse.skalli.model.Member;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.model.ext.commons.PeopleExtension;
import org.eclipse.skalli.model.ext.devinf.DevInfProjectExt;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.configuration.ConfigurationService;
import org.eclipse.skalli.services.extension.PropertyLookup;
import org.eclipse.skalli.services.project.ProjectService;
import org.eclipse.skalli.view.Consts;
import org.eclipse.skalli.view.internal.filter.FilterException;
import org.eclipse.skalli.view.internal.filter.FilterUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitGerritFilter implements Filter {

    private static final String URL_GITGERRITERROR = "gitgerriterror"; //$NON-NLS-1$

    public static final String ATTRIBUTE_INVALID_GROUP = "invalidGroup"; //$NON-NLS-1$
    public static final String ATTRIBUTE_INVALID_GROUP_MSG = "invalidGroupMsg"; //$NON-NLS-1$
    public static final String ATTRIBUTE_GROUP_EXISTS = "groupExists"; //$NON-NLS-1$
    public static final String ATTRIBUTE_INVALID_REPO = "invalidRepo"; //$NON-NLS-1$
    public static final String ATTRIBUTE_INVALID_REPO_MSG = "invalidRepoMsg"; //$NON-NLS-1$
    public static final String ATTRIBUTE_REPO_EXISTS = "repoExists"; //$NON-NLS-1$
    public static final String ATTRIBUTE_INVALID_PARENT = "invalidParent"; //$NON-NLS-1$
    public static final String ATTRIBUTE_KNOWN_ACCOUNTS = "knownAccounts"; //$NON-NLS-1$
    public static final String ATTRIBUTE_DATA_SAVED = "dataSaved"; //$NON-NLS-1$
    public static final String ATTRIBUTE_NO_GERRIT_CLIENT = "noGerritClient"; //$NON-NLS-1$
    public static final String ATTRIBUTE_NO_GERRIT_USER = "noGerritUser"; //$NON-NLS-1$
    public static final String ATTRIBUTE_NO_PROJECT_MEMBER = "noProjectMember"; //$NON-NLS-1$
    public static final String ATTRIBUTE_EXCEPTION = "exception"; //$NON-NLS-1$
    public static final String ATTRIBUTE_ERROR_MESSAGE = "errormessage"; //$NON-NLS-1$

    public static final String ERROR_NO_PARENT = "noParent"; //$NON-NLS-1$
    public static final String ERROR_NO_CONFIGURATION = "noConfiguration"; //$NON-NLS-1$

    private static final String GIT_PREFIX = "scm:git:"; //$NON-NLS-1$
    private static final String GIT_EXT = ".git"; //$NON-NLS-1$

    static final String DEFAULT_SCM_TEMPLATE =
            GIT_PREFIX +"${protocol}://${host}:${port}/${repository}" + GIT_EXT; //$NON-NLS-1$
    static final String DEFAULT_DESCRIPTION =
            "Created by ${user.displayName}. More details: ${link}"; //$NON-NLS-1$

    private static final Logger LOG = LoggerFactory.getLogger(GitGerritFilter.class);

    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)resp;

        // get some attributes provided by previous filters in the chain
        final String projectId = (String) request.getAttribute(Consts.ATTRIBUTE_PROJECTID);
        final Project project = (Project) request.getAttribute(Consts.ATTRIBUTE_PROJECT);
        final User user = (User) request.getAttribute(Consts.ATTRIBUTE_USER);

        FilterException fe = null;
        if (StringUtils.isBlank(projectId)) {
            fe = new FilterException("Missing required request parameter 'projectId'");
        } else if (project == null) {
            fe = new FilterException("Missing required request parameter 'project'");
        } else if (user == null) {
            fe = new FilterException("Missing required request parameter 'user'");
        }
        if (fe != null) {
            FilterUtil.handleException(request, response, fe);
            return;
        }

        // no configuration service: cancel further processing since we will not
        // be able to create a Gerrit client
        ConfigurationService configService = Services.getService(ConfigurationService.class);
        if (configService == null) {
            request.setAttribute(ATTRIBUTE_ERROR_MESSAGE, ERROR_NO_CONFIGURATION);
            FilterUtil.forward(request, response, URL_GITGERRITERROR);
            return;
        }

        // no Gerrit server configurations: cancel further processing since we will not
        // be able to create a Gerrit client
        GerritServersConfig gerritServersConfig = configService.readConfiguration(GerritServersConfig.class);
        if (gerritServersConfig == null || gerritServersConfig.isEmpty()) {
            request.setAttribute(ATTRIBUTE_ERROR_MESSAGE, ERROR_NO_CONFIGURATION);
            FilterUtil.forward(request, response, URL_GITGERRITERROR);
            return;
        }

        // no Gerrit service available: cancel further processing since we will not
        // be able to create a Gerrit client
        GerritService gerritService = Services.getService(GerritService.class);
        if (gerritService == null) {
            request.setAttribute(ATTRIBUTE_ERROR_MESSAGE, ERROR_NO_CONFIGURATION);
            FilterUtil.forward(request, response, URL_GITGERRITERROR);
            return;
        }

        // retrieve form parameters
        final String action = request.getParameter("action"); //$NON-NLS-1$
        final String gerritId = request.getParameter("gerritId"); //$NON-NLS-1$
        final String groupMode = request.getParameter("groupMode"); //$NON-NLS-1$
        final String group = request.getParameter("group"); //$NON-NLS-1$
        final String repository = request.getParameter("repository"); //$NON-NLS-1$
        final String parentMode = request.getParameter("parentMode"); //$NON-NLS-1$
        final String parent = request.getParameter("parent"); //$NON-NLS-1$
        final boolean permitsOnly = "permitsOnly".equals(request.getParameter("permitsOnly"));
        final boolean emptyCommit = request.getParameter("emptyCommit") != null?
                "emptyCommit".equals(request.getParameter("emptyCommit")) :
                StringUtils.isBlank(action);

                // Determine which Gerrit server to use: if a serverId is known, take that
        // dedicated server; otherwise search for a server with the "preferred" flag;
        // if no server is marked as preferred, take the first
        List<GerritServerConfig> gerritServers = gerritServersConfig.getServers();
        request.setAttribute("gerritServers", gerritServers); //$NON-NLS-1$

        GerritServerConfig gerritServer = gerritId != null?
                gerritServersConfig.getServer(gerritId) :
                gerritServersConfig.getPreferredServer();
        request.setAttribute("gerritServer", gerritServer); //$NON-NLS-1$

        GerritClient client = null;
        try {
            // no Gerrit client available: cancel further processing since we will not
            // be able to communicate with Gerrit
            client = gerritService.getClient(gerritServer.getId(), user.getUserId());
            if (client == null) {
                request.setAttribute(ATTRIBUTE_ERROR_MESSAGE, ERROR_NO_CONFIGURATION);
                FilterUtil.forward(request, response, URL_GITGERRITERROR);
                return;
            }

            // if "subprojectsOnly" flag is set in configuration,
            // ensure that project has at least one parent
            if (gerritServer.isSubprojectsOnly() && project.getParentEntityId() == null) {
                request.setAttribute(ATTRIBUTE_ERROR_MESSAGE, ERROR_NO_PARENT);
                FilterUtil.forward(request, response, URL_GITGERRITERROR);
                return;
            }

            // render a contact address, if available
            if (StringUtils.isNotBlank(gerritServer.getContact())) {
                request.setAttribute("gerritContact", gerritServer.getContact());
            }

            // ***** HERE starts the action evaluation/handling ****

            // action=undefined: create initial proposals for all form elements
            // action=refresh: create initial proposals except for the changed form elements
            if (StringUtils.isBlank(action) || "refresh".equals(action)) {

                request.setAttribute("proposedRepo", StringUtils.isNotBlank(repository) ?
                        repository : generateName(project, "/", StringUtils.EMPTY));
                request.setAttribute("proposedPermitsOnly", permitsOnly);
                request.setAttribute("proposedEmptyCommit", emptyCommit);

                request.setAttribute("proposedGroup", StringUtils.isNotBlank(group) ?
                        group : generateName(project, "_", "_committers"));
                if ("related".equals(groupMode)) {
                    request.setAttribute("proposedGroups", getRelatedGroups(gerritServer, client, project, user));
                } else if ("all".equals(groupMode)) {
                    request.setAttribute("proposedGroups", getAllGroups(client, project));
                }

                request.setAttribute("proposedParent", StringUtils.isNotBlank(parent) ?
                        parent : gerritServer.getParent());
                if ("related".equals(parentMode)) {
                    request.setAttribute("proposedProjects", getRelatedProjects(gerritServer, client, project, user));
                } else if ("permissions".equals(parentMode)) {
                    request.setAttribute("proposedProjects", getProjects(client, "permissions"));
                } else if ("all".equals(parentMode)) {
                    request.setAttribute("proposedProjects", getProjects(client, "all"));
                }
            }

            // action=check: validate the input
            // action=save: validate the input and do the needful on Gerrit
            else if ("check".equals(action) || "save".equals(action)) {
                client.connect();

                // is the group name valid?
                String invalidGroupMsg = client.checkGroupName(group);
                boolean invalidGroup = invalidGroupMsg != null;
                request.setAttribute(ATTRIBUTE_INVALID_GROUP, invalidGroup);
                if (invalidGroup) {
                    request.setAttribute(ATTRIBUTE_INVALID_GROUP_MSG, invalidGroupMsg);
                }

                // does the group already exist?
                boolean groupExists = !invalidGroup && client.groupExists(group);
                request.setAttribute(ATTRIBUTE_GROUP_EXISTS, groupExists);

                // is the repository name valid?
                String invalidRepoMsg = client.checkProjectName(repository);
                boolean invalidRepo = invalidRepoMsg != null;
                request.setAttribute(ATTRIBUTE_INVALID_REPO, invalidRepo);
                if (invalidRepo) {
                    request.setAttribute(ATTRIBUTE_INVALID_REPO_MSG, invalidRepoMsg);
                }

                // does the selected repository already exist?
                boolean repoExists = !invalidRepo && client.projectExists(repository);
                request.setAttribute(ATTRIBUTE_REPO_EXISTS, repoExists);

                // does the selected parent project already exist?
                if (StringUtils.isNotBlank(parent)) {
                    request.setAttribute(ATTRIBUTE_INVALID_PARENT, !client.projectExists(parent));
                }

                // proposed committers: do they have accounts? will the acting user be a committer?
                Set<String> knownAccounts = Collections.emptySet();
                boolean actingUserHasAccount = false;
                boolean actingUserIsProjectMember = false;
                boolean createGroup = !invalidGroup && !groupExists;
                if (createGroup) {
                    Set<String> projectMembers = getProposedProjectMembers(project);
                    actingUserIsProjectMember = projectMembers.contains(user.getUserId());
                    knownAccounts = client.getKnownAccounts(projectMembers);
                    request.setAttribute(ATTRIBUTE_KNOWN_ACCOUNTS, knownAccounts);
                    actingUserHasAccount = knownAccounts.contains(user.getUserId());
                }
                request.setAttribute(ATTRIBUTE_NO_PROJECT_MEMBER, !actingUserIsProjectMember);
                request.setAttribute(ATTRIBUTE_NO_GERRIT_USER, actingUserIsProjectMember && !actingUserHasAccount);

                // (3) SAVE (action: create group/repo, set SCM location)
                if ("save".equals(action)) {
                    // Only proceed if ...
                    boolean proceed = groupExists && repoExists; // ... both entities exist
                    boolean createRepo = !invalidGroup && !invalidRepo && !repoExists;
                    proceed = proceed || groupExists && createRepo; // ... or repo will be created
                    proceed = proceed || createGroup && createRepo; // ... or repo and group will be created
                    if (proceed) {
                        // perform operations on Gerrit
                        if (createGroup || createRepo) {
                            String baseUrl = (String)request.getAttribute(Consts.ATTRIBUTE_BASE_URL);
                            if (createGroup) {
                                String groupDescription = getDescription(gerritServer.getGroupDescription(),
                                        baseUrl, project, user, Collections.singletonMap("group", group)); //$NON-NLS-1$
                                client.createGroup(group, StringUtils.EMPTY, groupDescription, knownAccounts);
                            }
                            if (createRepo) {
                                String projectDescription = getDescription(gerritServer.getProjectDescription(),
                                        baseUrl, project, user, Collections.singletonMap("repository", repository)); //$NON-NLS-1$
                                client.createProject(repository,
                                        gerritServer.getBranch(),
                                        CollectionUtils.asSet(group),
                                        StringUtils.isNotBlank(parent)? parent : gerritServer.getParent(),
                                        permitsOnly,
                                        projectDescription,
                                        gerritServer.getSubmitType(),
                                        gerritServer.isUseContributorAgreement(),
                                        gerritServer.isUseSignedOffBy(),
                                        emptyCommit);
                            }
                        }

                        // persist new SCM location @ project (if it doesn't exist already)
                        DevInfProjectExt devInf = project.getExtension(DevInfProjectExt.class);
                        if (devInf == null) {
                            devInf = new DevInfProjectExt();
                            project.addExtension(devInf);
                        }
                        String scmLocation = getScmLocation(gerritServer, repository, project, user);
                        if (!devInf.hasScmLocation(scmLocation)) {
                            devInf.addScmLocation(scmLocation);
                            Services.getRequiredService(ProjectService.class).persist(project, user.getUserId());
                        }
                    }
                    request.setAttribute(ATTRIBUTE_DATA_SAVED, proceed);
                }
            }
            // (!) CANCEL (redirect to project landing page)
            else if ("cancel".equals(action)) {
                response.sendRedirect(Consts.URL_PROJECTS + "/" + projectId); //$NON-NLS-1$
            }
        } catch (GerritClientException e) {
            handleException(request, response, e);
        } catch (Exception e) {
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
    String generateName(Project project, String delimiter, String suffix) {
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

    Set<String> getProposedProjectMembers(Project project) {
        PeopleExtension peopleExt = project.getExtension(PeopleExtension.class);
        if (peopleExt == null) {
            return Collections.emptySet();
        }

        Set<Member> projectMembers = new HashSet<Member>();
        projectMembers.addAll(peopleExt.getLeads());
        projectMembers.addAll(peopleExt.getMembers());
        Set<String> userIds = new HashSet<String>();
        for (Member projectMember : projectMembers) {
            userIds.add(projectMember.getUserID());
        }
        return userIds;
    }

    Set<String> getRelatedProjects(GerritServerConfig gerritConfig, GerritClient client,
            Project project, User user) throws GerritClientException {

        // pattern to match with SCM location strings of related projects:
        // we replace ${repository} with the regular expression "(.*)", which allows us to extract
        // the repository name from a matching SCM location with Matcher.group(int).
        Pattern scmPattern = Pattern.compile(getScmLocation(gerritConfig, "(.*)", project, user)); //$NON-NLS-1$

        ProjectService projectService = Services.getRequiredService(ProjectService.class);

        // first, add all my parents: inheriting a group from a parent is
        // likely the most common use case
        List<Project> relatedProjects = projectService.getParentChain(project.getUuid());

        // second, add my siblings unless I'm a top-level project
        Project parent = (Project)project.getParentEntity();
        if (parent != null) {
            relatedProjects.addAll(parent.getSubProjects());
        }

        // finally, add my own subprojects
        relatedProjects.addAll(project.getSubProjects());

        return getRepositoryNames(relatedProjects, scmPattern);
    }


    /**
     * Returns groups that are related to the given project based on its position in
     * the project hierarchy: Groups used by parents, groups used by siblings,
     * groups used by subprojects.
     */
    List<String> getRelatedGroups(GerritServerConfig gerritConfig, GerritClient client,
            Project project, User user) throws GerritClientException {
        Set<String> repositoryNames = getRelatedProjects(gerritConfig, client, project, user);
        return client.getGroups(repositoryNames.toArray(new String[repositoryNames.size()]));
    }

    Set<String> getAllGroups(final GerritClient client, final Project project) {
        Set<String> result = new TreeSet<String>();
        try {
            result.addAll(client.getGroups());
            return result;
        } catch (ConnectionException e) {
            LOG.warn("Can't connect to Gerrit:", e);
            return Collections.emptySet();
        } catch (CommandException e) {
            LOG.warn("Can't connect to Gerrit:", e);
            return Collections.emptySet();
        }
    }

    Set<String> getProjects(final GerritClient client, String type) {
        Set<String> result = new TreeSet<String>();
        try {
            result.addAll(client.getProjects(type));
            return result;
        } catch (ConnectionException e) {
            LOG.warn("Can't connect to Gerrit:", e);
            return Collections.emptySet();
        } catch (CommandException e) {
            LOG.warn("Can't connect to Gerrit:", e);
            return Collections.emptySet();
        }
    }

    Set<String> getRepositoryNames(List<Project> projects, Pattern scmPattern) {
        LinkedHashSet<String> repositoryNames = new LinkedHashSet<String>();
        for (Project project : projects) {
            DevInfProjectExt devInf = project.getExtension(DevInfProjectExt.class);
            if (devInf == null || project.isInherited(DevInfProjectExt.class)) {
                continue;
            }
            for (String scmLocation : devInf.getScmLocations()) {
                Matcher matcher = scmPattern.matcher(scmLocation);
                if (matcher.matches() && matcher.groupCount() > 0) {
                    repositoryNames.add(matcher.group(1));
                }
            }
        }
        return repositoryNames;
    }

    @SuppressWarnings("nls")
    String getScmLocation(GerritServerConfig gerritConfig, String repository, Project project, User user) {
        Map<String, String> parameters = new HashMap<String, String>();
        String protocol = gerritConfig.getProtocol();
        if (StringUtils.isBlank(protocol)) {
            protocol = "git";
        }
        parameters.put("protocol", protocol);
        parameters.put("host", gerritConfig.getHost());
        String port = gerritConfig.getPort();
        if (StringUtils.isBlank(port)) {
            port = Integer.toString(GerritClient.DEFAULT_PORT);
        }
        parameters.put("port", port);
        parameters.put("repository", repository);
        if (StringUtils.isNotBlank(gerritConfig.getParent())) {
            parameters.put("parent", gerritConfig.getParent());
        }
        if (StringUtils.isNotBlank(gerritConfig.getBranch())) {
            parameters.put("branch", gerritConfig.getBranch());
        }
        parameters.put("user", user.getDisplayName());
        parameters.put("userId", user.getUserId());

        String scmTemplate = gerritConfig.getScmTemplate();
        if (StringUtils.isBlank(scmTemplate)) {
            scmTemplate = DEFAULT_SCM_TEMPLATE;
        }

        PropertyLookup propertyLookup = new PropertyLookup(parameters);
        propertyLookup.putAllProperties(project, "");
        propertyLookup.putAllProperties(user, "user");
        StrSubstitutor substitutor = new StrSubstitutor(propertyLookup);
        return substitutor.replace(scmTemplate);
    }

    @SuppressWarnings("nls")
    String getDescription(String descriptionTemplate, String baseUrl, Project project,
            User user, Map<String, String> properties) {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.putAll(properties);
        parameters.put("user", user.getDisplayName());
        parameters.put("userId", user.getUserId());
        parameters.put("link", baseUrl + Consts.URL_PROJECTS + "/" + project.getProjectId());
        if (StringUtils.isBlank(descriptionTemplate)) {
            descriptionTemplate = DEFAULT_DESCRIPTION;
        }
        PropertyLookup propertyLookup = new PropertyLookup(parameters);
        propertyLookup.putAllProperties(project, "");
        propertyLookup.putAllProperties(user, "user");
        StrSubstitutor substitutor = new StrSubstitutor(propertyLookup);
        return substitutor.replace(descriptionTemplate);
    }
}
