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
package org.eclipse.skalli.services.permit;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.services.group.GroupService;

/**
 * Service that manages and checks permits assigned to users.
 */
public interface PermitService {

    /**
     * Extracts the principal from the request and collects permits
     * granted to the corresponding user. The request may be
     * in the context of a certain project, which may influence
     * the permits granted to the user.
     *
     * @param request  the request, from which to extract the principal.
     * @param project  the project the user wants to access.
     *
     * @return the unique identifier of the logged in user, or
     * <code>null</code> if it is an anonymous user.
     */
    public String login(HttpServletRequest request, Project project);

    /**
     * Switches the project context of the currently logged in user
     * and recollects the permits.
     *
     * @param project  the project the user wants to access.
     */
    public void switchProject(Project project);

    /**
     * Extracts the principal from the request and revokes all permits
     * granted to the corresponding user.
     *
     * @param the request, from which to extract the principal.
     */
    public void logout(HttpServletRequest request);

    /**
     * Revokes all previous issued permits for all logged in users
     * and causes the permit service to recollect the permits of a
     * user upon next request.
     * <p>
     * This method should be called by other services (like the {@link GroupService}
     * whenever a change happens that might cause permits to change (e.g. groups
     * are added or removed, group service is exchanged during runtime etc.).
     */
    public void logoutAll();

    /**
     * Returns the currently logged in user.
     *
     * @return  the currently logged in user, or <code>null</code> if the
     * user is anonymous.
     */
    public String getLoggedInUser();

    /**
     * Checks whether the currently logged in user, i.e. the user that is
     * attached to the current thread, has the given permit.
     *
     * Usage:
     * <pre>
     * if (Permits.hasPermit(Permit.valueOf("FORBID GET /projects/foobar")) {
     *   ...
     * }
     * </pre>
     * @param permit  the requested permit.
     *
     * @return <code>true</code>, if the currently logged in user has the
     * given permit.
     */
    public boolean hasPermit(Permit permit);

    /**
     * Checks whether the currently logged in user, i.e. the user that is
     * attached to the current thread, has the given permit.
     *
     * @param level  the requested permit level, e.g. 1 for <tt>"ALLOW"</tt>.
     * @param action  the requested action
     * @param segments  the requested resource. Either a  complete resource
     * path (with forward slashes (/) as separators), or a list of path
     * segments (without slashes).
     *
     * @return <code>true</code>, if the currently logged in user has the
     * given permit.
     */
    public boolean hasPermit(int level, String action, String... segments);

    /**
     * Checks whether the currently logged in user, i.e. the user that is
     * attached to the current thread, has the given permit on a certain project.
     * This method composes a path of the form <tt>/projects/&lt;project&gt;</tt>,
     * where &lt;project&gt; is first replaced by the project's symbolic, and
     * then by the project's UUID. If either path matches, the method returns
     * <code>true</code>.
     * <p>
     * @param level  the requested permit level, e.g. 1 for <tt>"ALLOW"</tt>.
     * @param action  the requested action.
     * @param project  the project to of concern.
     *
     * @return <code>true</code>, if the currently logged in user has the
     * given permit.
     */
    public boolean hasPermit(int level, String action, Project project);

    /**
     * Checks whether the currently logged in user, i.e. the user that is
     * attached to the current thread, has the given permit on a certain project
     * (or any given resource within a project, e.g. a certain property of a project).
     * <p>
     * This method composes a path of the form
     * <tt>/projects/&lt;projectId or uuid&gt;/&lt;segment&gt;/.../&lt;segment&gt;</tt>,
     * where &lt;project&gt; is first replaced by the project's symbolic, and
     * then by the project's UUID. If either path matches, the method returns
     * <code>true</code>.
     *
     * @param level  the requested permit level, e.g. 1 for <tt>"ALLOW"</tt>.
     * @param action  the requested action.
     * @param project  the project to of concern.
     * @param segments
     *
     * @return <code>true</code>, if the currently logged in user has the
     * given permit.
     */
    public boolean hasPermit(int level, String action, Project project, String... segments);
}
