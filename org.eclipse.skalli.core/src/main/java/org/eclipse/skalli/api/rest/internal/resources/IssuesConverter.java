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
package org.eclipse.skalli.api.rest.internal.resources;

import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.services.extension.rest.RestConverterBase;
import org.eclipse.skalli.services.issues.Issues;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

class IssuesConverter extends RestConverterBase<Issues> {

    public static final String API_VERSION = "1.0"; //$NON-NLS-1$
    public static final String NAMESPACE = "http://www.eclipse.org/skalli/2010/API"; //$NON-NLS-1$

    public IssuesConverter(String host) {
        super(Issues.class, "issues", host); //$NON-NLS-1$
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        Issues issues = (Issues) source;
        marshalNSAttributes(writer);
        marshalCommonAttributes(issues, writer);
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
        return "issues.xsd";
    }
}
