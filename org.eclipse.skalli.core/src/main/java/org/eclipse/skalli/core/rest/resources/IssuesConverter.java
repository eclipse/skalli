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
package org.eclipse.skalli.core.rest.resources;

import java.io.IOException;
import java.util.UUID;

import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.services.extension.rest.RestConverterBase;
import org.eclipse.skalli.services.extension.rest.RestUtils;
import org.eclipse.skalli.services.issues.Issues;
import org.restlet.data.MediaType;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

class IssuesConverter extends RestConverterBase<Issues> {

    public static final String API_VERSION = "1.0"; //$NON-NLS-1$
    public static final String NAMESPACE = "http://www.eclipse.org/skalli/2010/API"; //$NON-NLS-1$

    public IssuesConverter() {
        super(Issues.class);
    }

    @SuppressWarnings("nls")
    @Override
    protected void marshal(Issues issues) throws IOException {
        writer.object("issues");
            namespaces();
            commonAttributes(issues);
            UUID uuid = issues.getUuid();
            writer.links()
              .link(SELF_RELATION, RestUtils.URL_PROJECTS, uuid, RestUtils.URL_ISSUES)
              .link(PROJECT_RELATION, RestUtils.URL_PROJECTS, uuid)
            .end()
            .pair("uuid", uuid)
            .pair("isStale", issues.isStale());
            if (writer.isMediaType(MediaType.APPLICATION_JSON)) {
                writer.key("items");
            }
            writer.array("issue");
            for (Issue issue : issues.getIssues()) {
                writer.object();
                    writer.pair("timestamp", issue.getTimestamp());
                    writer.pair("severity", issue.getSeverity().name());
                    if (issue.getExtension() != null) {
                        writer.pair("extension", issue.getExtension().getName());
                    }
                    if (issue.getPropertyId() != null) {
                        writer.pair("propertyId", issue.getPropertyId().toString());
                    }
                    writer.pair("issuer", issue.getIssuer().getName());
                    writer.pair("item", issue.getItem());
                    writer.pair("message", issue.getMessage());
                    writer.pair("description", issue.getDescription());
                writer.end();
            }
            writer.end();
        writer.end();
    }

    @Deprecated
    public IssuesConverter(String host) {
        super(Issues.class, "issues", host); //$NON-NLS-1$
    }

    @Deprecated
    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        Issues issues = (Issues) source;
        marshalNSAttributes(writer);
        marshalCommonAttributes(writer, issues);
        writeNode(writer, "isStale", Boolean.toString(issues.isStale())); //$NON-NLS-1$
        for (Issue issue : issues.getIssues()) {
            writer.startNode("issue"); //$NON-NLS-1$
            writeNode(writer, "timestamp", issue.getTimestamp()); //$NON-NLS-1$
            writeNode(writer, "severity", issue.getSeverity().name()); //$NON-NLS-1$
            if (issue.getExtension() != null) {
                writeNode(writer, "extension", issue.getExtension().getName()); //$NON-NLS-1$
            }
            if (issue.getPropertyId() != null) {
                writeNode(writer, "propertyId", issue.getPropertyId().toString()); //$NON-NLS-1$
            }
            writeNode(writer, "issuer", issue.getIssuer().getName()); //$NON-NLS-1$
            writeNode(writer, "item", issue.getItem()); //$NON-NLS-1$
            writeNode(writer, "message", issue.getMessage()); //$NON-NLS-1$
            writeNode(writer, "description", issue.getDescription()); //$NON-NLS-1$
            writer.endNode();
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader arg0, UnmarshallingContext arg1) {
        // don't support that yet
        return null;
    }

    @Override
    public String getApiVersion() {
        return API_VERSION;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public String getXsdFileName() {
        return "issues.xsd"; //$NON-NLS-1$
    }
}
