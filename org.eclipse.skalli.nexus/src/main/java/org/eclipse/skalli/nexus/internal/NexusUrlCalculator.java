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

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.skalli.nexus.NexusClientException;
import org.eclipse.skalli.nexus.internal.config.NexusConfig;

/**
 *
 */
public class NexusUrlCalculator {

    private String groupId;
    private String artifactId;
    private NexusConfig nexusConfig;

    public NexusUrlCalculator(NexusConfig nexusConfig, String groupId, String artifactId) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.nexusConfig = nexusConfig;
    }

    public URL getNexusUrl(int from, int count) throws NexusClientException
    {

        StringBuilder str = new StringBuilder(nexusConfig.getUrl());
        if (!str.toString().endsWith("/")) {
            str.append("/");
        }

        str.append("service/local/data_index/");
        str.append(nexusConfig.getDomain());
        str.append("/");
        str.append(nexusConfig.getTarget());
        str.append("/content?g=");
        str.append(groupId);
        str.append("&a=");
        str.append(artifactId);
        str.append("&from=");
        str.append(Integer.toString(from));
        str.append("&count=");
        str.append(count);

        try {
            return new URL(str.toString());
        } catch (MalformedURLException e) {
            throw new NexusClientException("Can't get a valid Url for " + nexusConfig.toString() + " and groupId = '"
                    + groupId + "' and artifactId = '" + artifactId + ". The invalid Url is:'" + str.toString() + "'",
                    e);
        }
    }

}
