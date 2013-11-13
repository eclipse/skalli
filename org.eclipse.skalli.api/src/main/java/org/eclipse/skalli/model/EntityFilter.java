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
package org.eclipse.skalli.model;

/**
 * Functional interface for filtering of entities.
 */
public interface EntityFilter<T extends EntityBase> {

    /**
     * Checks if the given entity matches the filter criteria.
     *
     * @param entityClass  the class of the entity.
     * @param entity  the entity to check.
     *
     * @return <code>true</code> if the entity is accecpted by the filter.
     */
    public boolean accept(Class<T> entityClass, T entity);

}
