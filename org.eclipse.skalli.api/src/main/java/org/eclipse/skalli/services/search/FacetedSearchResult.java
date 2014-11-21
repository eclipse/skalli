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
package org.eclipse.skalli.services.search;

import java.util.Map;

import org.eclipse.skalli.model.EntityBase;

public class FacetedSearchResult<T extends EntityBase> extends SearchResult<T> {
    private Map<String, Map<String, Integer>> facetInfo;

    public Map<String, Map<String, Integer>> getFacetInfo() {
        return facetInfo;
    }

    public void setFacetInfo(Map<String, Map<String, Integer>> facetInfo) {
        this.facetInfo = facetInfo;
    }

}
