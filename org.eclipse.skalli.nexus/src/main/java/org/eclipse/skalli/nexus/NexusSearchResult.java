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
package org.eclipse.skalli.nexus;

import java.util.List;

/**
 * Interface that present a Nexus search response.
 *  
 * @ee https://repository.sonatype.org/nexus-indexer-lucene-plugin/default/docs/data_ns0.html#element_search-results
 */
public interface NexusSearchResult {

    /**
     * Returns the total number of results found for the search.
     */
    public int getTotalCount();

    /**
     * Returns the starting index of the results.
     */
    public int getFrom();

    /**
     * Returns the number of results in this response.
     */
    public int getCount();

    /**
     * Returns <codeY>true</code> if too many results were found.
     */
    public boolean isToManyResults();

    /**
     * Returns information about the artifacts found by the search.
     * @return  a list of artifact, or an empty list if the search did
     * not match any artifact.
     */
    public List<NexusArtifact> getArtifacts();
}
