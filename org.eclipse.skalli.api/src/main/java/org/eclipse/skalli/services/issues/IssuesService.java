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
package org.eclipse.skalli.services.issues;

import java.util.Collection;
import java.util.UUID;

import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Issuer;
import org.eclipse.skalli.model.ValidationException;
import org.eclipse.skalli.services.entity.EntityService;

/**
 * Service that allows to manage (retrieve, persist, load, validate) validation
 * {@link org.eclipse.skalli.model.Issue issues}.
 */
public interface IssuesService extends EntityService<Issues>, Issuer {

    /**
     * Persists validation issues for the given entity. Previously recorded issues for this entity
     * are cleared. Sets the timestamps of the issues to the current system time, if not already defined.
     *
     * @param issues    the issues to persist.
     * @param userId    unique identifier of the user saving the given issues.
     *
     * @throws ValidationException  if the issues could not be persisted, i.e. there are
     * issue entries that belong to a different entity or are not associated with an entity at all.
     */
    @Override
    public void persist(Issues issues, String userId) throws ValidationException;

    /**
     * Persists validation issues for the given entity. Previously recorded issues for this entity
     * are cleared. Sets the timestamps of the issues to the current system time, if not already defined.
     *
     * @param entityId  the entity that causes the validation issues.
     * @param issues    the collection of issues to persist.
     * @param userId    unique identifier of the user saving the given issues.
     *
     * @throws ValidationException  if the issues could not be persisted, i.e. the
     * given collection contains entries that do not match the  given <code>entityId</code>
     * or are not assigned to an entity at all.
     */
    public void persist(UUID entityId, Collection<Issue> issues, String userId) throws ValidationException;
}
