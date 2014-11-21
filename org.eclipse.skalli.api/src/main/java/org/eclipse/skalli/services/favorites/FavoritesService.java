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
package org.eclipse.skalli.services.favorites;

import java.util.UUID;

import org.eclipse.skalli.model.ValidationException;
import org.eclipse.skalli.services.entity.EntityService;

public interface FavoritesService extends EntityService<Favorites> {

    /**
     * Returns the favorites entity for the given user, or
     * creates a new <code>Favorites</code> entity.
     *
     * @param userId
     *          the unique identifier of a user.
     * @return the favorites entity assigned to the user.
     */
    public Favorites getFavorites(String userId);

    /**
     * Adds a project to the favorites list of the given user.
     * @param userId
     *          the unique identifier of a user.
     * @param project
     *          the project to add to the
     *          favorites list of the user
     * @throws ValidationException
     *           if the <code>Favorites</code> instance of the user is invalid.
     */
    public void addFavorite(String userId, UUID project) throws ValidationException;

    /**
     * Removes a project from the favorites list of the given user.
     * @param userId
     *          the unique identifier of a user.
     * @param project
     *          the project to remove from the
     *          favorites list of the user
     * @throws ValidationException
     *           if the <code>Favorites</code> instance of the user is invalid.
     * @throws DuplicateEntityException
     *           if there exists already another model entity with the same UUID
     *           (see {@link org.eclipse.skalli.services.projects.EntityBase#getUuid()}).
     */
    public void removeFavorite(String userId, UUID project) throws ValidationException;

}
