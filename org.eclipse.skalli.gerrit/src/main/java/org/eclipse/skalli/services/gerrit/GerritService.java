/*******************************************************************************
 * Copyright (c) 2010-2015 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.services.gerrit;

public interface GerritService {

    /**
     * Returns a client for communication with a Gerrit server.
     *
     * @param id  the unique identifier of the Gerrit server.
     * @param onBehalfOf  the unique identifier of the user to act on behalf of.
     * Note, this user is usually the currently logged in user and not the user
     * with which the returned client communicates with Gerrit.
     *
     * @return a preconfigured Gerrit client.
     */
    public GerritClient getClient(String id, String onBehalfOf);

}
