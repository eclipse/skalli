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
package org.eclipse.skalli.gerrit.client;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.skalli.gerrit.client.exception.CommandException;
import org.eclipse.skalli.gerrit.client.exception.ConnectionException;
import org.eclipse.skalli.gerrit.client.exception.GerritClientException;

/**
 * SSH Gerrit client w/ some basic commands. <strong>Requires Gerrit-2.1.6-rc1 or higher.</strong>
 *
 * Problems when establishing a connection will result in a <code>ConnectionException</code>.
 * Problems through command execution will result in a <code>CommandException</code>.
 *
 * Both are <code>GerritClientExceptions</code>s in case the caller does not intend to handle this differently.
 *
 * @see GerritClientException
 * @see ConnectionException
 * @see CommandException
 */
public interface GerritClient {

    /**
     * The Gerrit default port (@value).
     */
    public static final int DEFAULT_PORT = 29418;

    /**
     * Connects the SSH client
     *
     * @throws ConnectionException in case of connection / communication problems
     */
    public void connect() throws ConnectionException;

    /**
     * Disconnects the SSH client
     */
    public void disconnect();

    /**
     * Returns the version identifier of the Gerrit server.
     *
     * @return the version of the Gerrit server, or {@link GerritVersion#GERRIT_UNKNOWN_VERSION}
     * if the version could not be retrieved or identified.
     *
     * @throws ConnectionException      in case of connection / communication problems
     * @throws CommandException         in case of unsuccessful commands
     */
    public GerritVersion getVersion() throws ConnectionException, CommandException;

    /**
     * Returns the plugins installed in the Gerrit server.
     *
     * Gerrit before version 2.5 did not support plugins. In that case always an
     * empty result will be returned.
     *
     * @return  a map of plugins associated with their names, or an empty map.
     *
     * @throws ConnectionException      in case of connection / communication problems
     * @throws CommandException         in case of unsuccessful commands
     *
     * @since Gerrit 2.5
     */
    public Map<String,GerritPlugin> getPlugins() throws ConnectionException, CommandException;

    /**
     * Creates a project with a given name and assigns an initial set of members to it.
     *
     * @param name
     *            required, no whitespaces.
     * @param branch
     *            optional, defaults to <code>master</code>.
     * @param ownerList
     *            optional. group must exist. defaults to
     *            <code>repository.ownerGroup</code>,
     *            <code>repository.createGroup</code> or finally
     *            <code>Administrators</code>
     * @param parent
     *            optional, defaults to <code>-- All Projects --</code>
     * @param permissionsOnly
     *            optional, defaults to <code>false</code>
     * @param description
     *            optional
     * @param submitType
     *            optional, defaults to <code>MERGE_IF_NECESSARY</code>
     * @param useContributorAgreements
     *            optional, defaults to <code>false</code>
     * @param useSignedOffBy
     *            optional, defaults to <code>false</code>
     * @param emptyCommit
     *            optional, defaults to <code>false</code>
     *
     * @throws ConnectionException      in case of connection / communication problems
     * @throws CommandException         in case of unsuccessful commands
     * @throws IllegalArgumentException in case an invalid name is passed
     *
     * @see <tt>gerrit create-project</tt>
     */
    @Deprecated
    public void createProject(String name, String branch, Set<String> ownerList, String parent,
            boolean permissionsOnly, String description, SubmitType submitType,
            boolean useContributorAgreements, boolean useSignedOffBy, boolean emptyCommit)
                    throws ConnectionException, CommandException;

    /**
     * Creates a Gerrit project with a given set of parameters.
     *
     * @param options the parameters of the Gerrit project to be created.
     *
     * @throws ConnectionException      in case of connection / communication problems
     * @throws CommandException         in case of unsuccessful commands
     * @throws IllegalArgumentException in case an invalid name is passed
     */
    public void createProject(ProjectOptions options)
                    throws ConnectionException, CommandException;

    /**
     * Returns the list of all globally visible projects.
     *
     * @return a list of all project names, or an empty list.
     *
     * @throws ConnectionException in case of connection / communication problems
     * @throws CommandException    in case of unsuccessful commands
     */
    public List<String> getProjects() throws ConnectionException, CommandException;

    /**
     * Returns the list of projects of a given type.
     * Currently supported types are <tt>"all"</tt>, <tt>"permissions"</tt> and <tt>"code"</tt>.
     *
     * @param type  the type of projects to return.
     * @return a list of project names, or an empty list.
     *
     * @throws ConnectionException in case of connection / communication problems
     * @throws CommandException    in case of unsuccessful commands
     */
    public List<String> getProjects(String type) throws ConnectionException, CommandException;

    /**
     * Checks if a given project exists.
     *
     * @param name
     *            name of the project to look up.
     *
     * @return <code>true</code> if the project exists, otherwise
     *         <code>false</code>
     *
     * @throws ConnectionException in case of connection / communication problems
     * @throws CommandException    in case of unsuccessful commands
     *
     * @see <tt>gerrit ls-projects</tt>
     */
    public boolean projectExists(final String name) throws ConnectionException, CommandException;

    /**
     * Creates a group with a given name, owner, description and initial set of members.
     * <p>
     * Note: Since Gerrit release 2.1.7 an account check before creating a group is not necessary
     * anymore, when Gerrit is connected to LDAP for user authentication (see <tt>--member</tt> option
     * of the <tt>gerrit create-group</tt> command.
     *
     * @param name
     *            required, no whitespaces
     * @param owner
     *            optional
     * @param description
     *            optional
     * @param members
     *            optional, in Gerrit versions before 2.1.7  members must already have
     *            an account on Gerrit
     *
     * @throws ConnectionException      in case of connection / communication problems
     * @throws CommandException         in case of unsuccessful commands
     * @throws IllegalArgumentException in case an invalid name is passed
     *
     * @see <tt>gerrit create-group</tt>
     */
    public void createGroup(final String name, final String owner, final String description,
            final Set<String> members)
            throws ConnectionException, CommandException;

    /**
     * Returns a list of all globally visible projects.
     *
     * @return a list of all group names, or an empty list.
     *
     * @throws ConnectionException in case of connection / communication problems
     * @throws CommandException    in case of unsuccessful commands
     *
     * @see <tt>gerrit ls-groups</tt>
     */
    public List<String> getGroups() throws ConnectionException, CommandException;

    /**
     * Retrieves the groups associated with the given projects.
     *
     * @param projectName
     *            the project name to look for
     *
     * @return a list of all groups related to the project, or an empty list.
     *
     * @throws ConnectionException in case of connection / communication problems
     * @throws CommandException    in case of unsuccessful commands
     */
    public List<String> getGroups(String... projectNames) throws ConnectionException, CommandException;

    /**
     * Checks if a group with the given name exists.
     * @param name
     *            name of the group to look up.
     *
     * @return <code>true</code> if the group exists, otherwise
     *         <code>false</code>.
     *
     * @throws ConnectionException in case of connection / communication problems
     * @throws CommandException    in case of unsuccessful commands
     */
    public boolean groupExists(final String name) throws ConnectionException, CommandException;

    /**
     * Filters the given collection of user identifiers and sorts out those that have
     * no valid account on Gerrit. This method requires administrators privileges on Gerrit.
     * <p>
     * Note: Since Gerrit release 2.1.7 an account check before creating a group is not necessary
     * anymore, when Gerrit is connected to LDAP for user authentication (see <tt>--member</tt> option
     * of the <tt>gerrit create-group</tt> command.
     *
     * @param variousAccounts
     *          the accounts to check.
     *
     * @return a subset of the passed in accounts that are known to Gerrit, or just
     * the collection passed in for Gerrit releases beyond 2.1.7.
     *
     * @throws ConnectionException in case of connection / communication problems
     * @throws CommandException    in case of unsuccessful commands
     */
    public Set<String> getKnownAccounts(Set<String> variousAccounts) throws ConnectionException, CommandException;

    /**
     * Checks that the given name is a valid Gerrit group name.
     *
     * @param name  the name to check.
     *
     * @return <code>null</code>, if the name is valid, otherwise an error message indicating
     * the cause of the invalidity.
     */
    public String checkGroupName(String name);

    /**
     * Checks that the given name is a valid Gerrit project name.
     *
     * @param name  the name to check.
     *
     * @return <code>null</code>, if the name is valid, otherwise an error message indicating
     * the cause of the invalidity.
     */
    public String checkProjectName(String name);
}