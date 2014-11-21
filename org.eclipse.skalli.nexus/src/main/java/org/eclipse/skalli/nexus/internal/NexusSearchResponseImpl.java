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
package org.eclipse.skalli.nexus.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.skalli.nexus.NexusArtifact;
import org.eclipse.skalli.nexus.NexusClientException;
import org.eclipse.skalli.nexus.NexusSearchResult;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NexusSearchResponseImpl implements NexusSearchResult {

    private int totalCount;
    private int from;
    private int count;
    private boolean toManyResults;
    private List<NexusArtifact> artifacts;

    public NexusSearchResponseImpl(Element rootElement) throws NexusClientException {

        if (rootElement == null) {
            throw new IllegalArgumentException("Parameter rootElement must not be null.");
        }

        if (!"search-results".equals(rootElement.getNodeName())) {
            throw new IllegalArgumentException("rootElement.getNodeName() must be 'search-results'");
        }

        from = NexusResponseParser.getNodeTextContentAsInt(rootElement, "from", 0);
        totalCount = NexusResponseParser.getNodeTextContentAsInt(rootElement, "totalCount", 0);
        count = NexusResponseParser.getNodeTextContentAsInt(rootElement, "count", 0);

        toManyResults = NexusResponseParser.getNodeTextContentAsBoolean(rootElement, "toManyResults");

        artifacts = new ArrayList<NexusArtifact>();

        Node data = NexusResponseParser.findNode(rootElement, "data");
        if (data != null) {
            NodeList dataChildren = data.getChildNodes();
            for (int i = 0; i < dataChildren.getLength(); i++) {
                Node artifactNode = dataChildren.item(i);
                if ("artifact".equals(artifactNode.getNodeName())) {
                    artifacts.add(new NexusArtifactImpl((Element) artifactNode));
                }
            }
        }
    }

    @Override
    public int getTotalCount() {
        return totalCount;
    }

    @Override
    public int getFrom() {
        return from;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public boolean isToManyResults() {
        return toManyResults;
    }

    @Override
    public List<NexusArtifact> getArtifacts() {
        return artifacts;
    }
}
