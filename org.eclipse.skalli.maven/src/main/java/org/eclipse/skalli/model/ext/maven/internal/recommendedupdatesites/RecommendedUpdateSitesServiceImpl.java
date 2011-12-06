/*******************************************************************************
 * Copyright (c) 2010 - 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.model.ext.maven.internal.recommendedupdatesites;

import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.skalli.api.java.EntityServiceImpl;
import org.eclipse.skalli.model.ext.Issue;
import org.eclipse.skalli.model.ext.Severity;
import org.eclipse.skalli.model.ext.ValidationException;
import org.eclipse.skalli.model.ext.maven.recommendedupdatesites.RecommendedUpdateSites;
import org.eclipse.skalli.model.ext.maven.recommendedupdatesites.RecommendedUpdateSitesService;

public class RecommendedUpdateSitesServiceImpl extends EntityServiceImpl<RecommendedUpdateSites> implements
        RecommendedUpdateSitesService {

    /* (non-Javadoc)
     * @see org.eclipse.skalli.api.java.EntityService#getEntityClass()
     */
    @Override
    public Class<RecommendedUpdateSites> getEntityClass() {
        return RecommendedUpdateSites.class;
    }

    /* (non-Javadoc)
     * @see org.eclipse.skalli.api.java.EntityServiceImpl#validateEntity(org.eclipse.skalli.model.ext.EntityBase)
     */
    @Override
    protected void validateEntity(RecommendedUpdateSites entity) throws ValidationException {
        //nothing to validate
    }

    /* (non-Javadoc)
     * @see org.eclipse.skalli.api.java.EntityServiceImpl#validateEntity(org.eclipse.skalli.model.ext.EntityBase, org.eclipse.skalli.model.ext.Severity)
     */
    @Override
    protected SortedSet<Issue> validateEntity(RecommendedUpdateSites entity, Severity minSeverity) {
        return new TreeSet<Issue>();
    }

}
