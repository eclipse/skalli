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
package org.eclipse.skalli.testutil;

import java.util.SortedSet;

import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.model.ValidationException;
import org.eclipse.skalli.services.entity.EntityServiceBase;

/**
 * A simple implementation of <code>EntityService</code> based on {@link HashMapPersistenceService}.
 * Note, derived classes must implement {@link #getEntityClass()}.
 */
public abstract class HashMapEntityService<T extends EntityBase> extends EntityServiceBase<T> {

    protected HashMapEntityService() {
        bindPersistenceService(new HashMapPersistenceService(getEntityClass()));
    }

    @Override
    public int getModelVersion() {
        return 1;
    }

    @Override
    protected void validateEntity(T entity) throws ValidationException {
        // nothing to do
    }

    @Override
    protected SortedSet<Issue> validateEntity(T entity, Severity minSeverity) {
        return CollectionUtils.emptySortedSet();
    }

}
