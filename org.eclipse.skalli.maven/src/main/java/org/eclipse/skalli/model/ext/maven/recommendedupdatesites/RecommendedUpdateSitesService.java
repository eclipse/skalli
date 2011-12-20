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
package org.eclipse.skalli.model.ext.maven.recommendedupdatesites;

import org.eclipse.skalli.model.Issuer;
import org.eclipse.skalli.services.entity.EntityService;

public interface RecommendedUpdateSitesService extends EntityService<RecommendedUpdateSites>, Issuer {

    public RecommendedUpdateSites getRecommendedUpdateSites(String shortName);

    /**
     * Returns a set of update sites for given <code>userId</code> and <code>updateSiteId</code>
     * or null if no matching record could be found.
     * @param userId the unique identifier of the user that created the recommendation.
     * @param updateSiteId the identifier of the recommendation.
     * @return the recommended update sites or <code>null</code>.
     */
    public RecommendedUpdateSites getRecommendedUpdateSites(String userId, String updateSiteId);

}
