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
package org.eclipse.skalli.services.persistence;

import javax.persistence.EntityManager;

public interface EntityManagerService {

    /**
     * Retrieves an {@link EntityManager}.
     *
     * @return an entity manager instance, never <code>null</code>.
     *
     * @throws StorageException
     *             if no entity manager could be created.
     */
    public EntityManager getEntityManager() throws StorageException;
}
