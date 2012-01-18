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
package org.eclipse.skalli.gerrit.client;

import java.util.List;
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

    public enum SubmitType {
        FAST_FORWARD_ONLY, MERGE_IF_NECESSARY, MERGE_ALWAYS, CHERRY_PICK
    }

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
     * Creates a project according to <a href=
     * "http://gerrit.googlecode.com/svn/documentation/2.1.5/cmd-create-project.html"
     * >gerrit create-project</a> (<a href=
     * "http://gerrit.googlecode.com/svn/documentation/2.1.5/cmd-create-project.html#options"
     * >options</a>).
     *
     * @param name
     *            required, no whitespaces
     * @param branch
     *            optional. defaults to <code>master</code>
     * @param ownerList
     *            optional. group must exist. defaults to
     *            <code>repository.ownerGroup</code>,
     *            <code>repository.createGroup</code> or finally
     *            <code>Administrators</code>
     * @param parent
     *            optional. defaults to <code>-- All Projects --</code>
     * @param permissionsOnly
     *            optional. defaults to <code>false</code>
     * @param description
     *            optional
     * @param submitType
     *            optional. defaults to <code>MERGE_IF_NECESSARY</code>
     * @param useContributorAgreements
     *            optional. defaults to <code>false</code>
     * @param useSignedOffBy
     *            optional. defaults to <code>false</code>
     *
     * @throws ConnectionException      in case of connection / communication problems
     * @throws CommandException         in case of unsuccessful commands
     * @throws IllegalArgumentException in case an invalid name is passed
     */
    public void createProject(final String name, final String branch, final Set<String> ownerList, final String parent,
            final boolean permissionsOnly, final String description, final SubmitType submitType,
            final boolean useContributorAgreements, final boolean useSignedOffBy) throws ConnectionException,
            CommandException;

    /**
     * @return a list of all project names
     *
     * @throws GerritClientException in case of unforeseen communication problems
     */
    public List<String> getProjects() throws ConnectionException, CommandException;

    /**
     * @param name
     *            name of the project to lookup
     *
     * @return <code>true</code> if the project exists, otherwise
     *         <code>false</code>
     *
     * @throws ConnectionException in case of connection / communication problems
     * @throws CommandException    in case of unsuccessful commands
     */
    public boolean projectExists(final String name) throws ConnectionException, CommandException;

    /**
     * Creates a group according to <a href=
     * "http://gerrit.googlecode.com/svn/documentation/2.1.5/cmd-create-group.html"
     * >gerrit create-group</a> (<a href=
     * "http://gerrit.googlecode.com/svn/documentation/2.1.5/cmd-create-group.html#options"
     * >options</a>).
     *
     * Note that the members need to exist on the Gerrit and unknown members won't be added.
     * This needs to happen manually and individually via Gerrit's web UI.
     *
     * @param name
     *            required, no whitespaces
     * @param owner
     *            optional. defaults to self-owning
     * @param description
     *            optional
     * @param memberList
     *            optional, members need to exist on Gerrit
     *
     * @throws ConnectionException      in case of connection / communication problems
     * @throws CommandException         in case of unsuccessful commands
     * @throws IllegalArgumentException in case an invalid name is passed
     */
    public void createGroup(final String name, final String owner, final String description,
            final Set<String> memberList)
            throws ConnectionException, CommandException;

    /**
     * @return a list of all group names
     *
     * @throws ConnectionException in case of connection / communication problems
     * @throws CommandException    in case of unsuccessful commands
     */
    public List<String> getGroups() throws ConnectionException, CommandException;

    /**
     * @param projectName
     *            the project name to look for
     *
     * @return a list of all groups related to the project
     *
     * @throws ConnectionException in case of connection / communication problems
     * @throws CommandException    in case of unsuccessful commands
     */
    public List<String> getGroups(String... projectNames) throws ConnectionException, CommandException;

    /**
     * @param name
     *            name of the group to lookup
     *
     * @return <code>true</code> if the group exists, otherwise
     *         <code>false</code>
     *
     * @throws ConnectionException in case of connection / communication problems
     * @throws CommandException    in case of unsuccessful commands
     */
    public boolean groupExists(final String name) throws ConnectionException, CommandException;

    /**
     * @param variousAccounts
     *          the accounts to check
     *
     * @return a subset of the passed in accounts that are known to Gerrit
     *
     * @throws ConnectionException in case of connection / communication problems
     * @throws CommandException    in case of unsuccessful commands
     */
    public Set<String> getKnownAccounts(Set<String> variousAccounts) throws ConnectionException, CommandException;

}