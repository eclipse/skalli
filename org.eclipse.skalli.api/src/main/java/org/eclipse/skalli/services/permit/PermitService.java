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

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

public interface PermitService {

    public String login(HttpServletRequest request, UUID projectId);
    public void logout(HttpServletRequest request);

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
}
