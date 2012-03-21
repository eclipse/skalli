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

import org.eclipse.skalli.services.Services;

public class Permits {

    private static volatile PermitService permitService;

    protected Permits() {
    }

    private static PermitService getPermitService() {
        if (permitService == null) {
            synchronized (Permits.class) {
                if (permitService == null) {
                    permitService = Services.getRequiredService(PermitService.class);
                }
            }
        }
        return permitService;
    }

    /**
     * Checks whether the currently logged in user, i.e. the user that is
     * attached to the current thread, is allowed to perform the given
     * action on a given resource.
     *
     * Usage:
     * <pre>
     * if (Permits.isAllowed("GET", "/projects/foobar")) {
     *   ...
     * }
     * </pre>
     * or equivalent:
     * <pre>
     * String projectId = "foobar";
     * if (Permits.isAllowed("GET", "projects", projectId)) {
     *   ...
     * }
     * </pre>
     *
     * @param action  the action to perform, e.g. <tt>"GET"</tt>.
     * @param segments  either a complete resource path (with forward slashes (/)
     * as separators), or a list of path segments (without slashes).
     *
     * @return <code>true</code>, if the currently logged in user is allowed
     * to perform that action.
     */
    public static boolean isAllowed(String action, String... segments) {
        return hasPermit(1, action, segments);
    }

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
    public static boolean hasPermit(Permit permit) {
        return hasPermit(permit.getLevel(), permit.getAction(), permit.getSegments());
    }

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
    public static boolean hasPermit(int level, String action, String... segments) {
        PermitService permitService = getPermitService();
        return permitService != null? permitService.hasPermit(level, action, segments) : false;
    }
}
