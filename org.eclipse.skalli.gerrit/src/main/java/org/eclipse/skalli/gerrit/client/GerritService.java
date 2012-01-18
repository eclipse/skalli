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

public interface GerritService {

    /**
     * Returns a client to perform operations on the configured remote Gerrit.
     *
     * @param user the user to act on behalf on (used in logging, not for authentication)
     *
     * @return the client
     */
    GerritClient getClient(String userId);

}
