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
package org.eclipse.skalli.model.core.internal;

import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.services.extension.MigrationException;
import org.eclipse.skalli.services.extension.MigrationUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DataMigration16 extends ProjectDataMigration {

    public DataMigration16() {
        super(Project.class, 16);
    }

    @Override
    public void migrate(Document doc) throws MigrationException {
        String extensionClassName = "entity-relatedProjects"; //$NON-NLS-1$
        Element relatedProjectsNode = MigrationUtils.getOrCreateExtensionNode(doc, extensionClassName);

        addSection(doc, relatedProjectsNode, "relatedProjects", "");//$NON-NLS-1$//$NON-NLS-2$
        addSection(doc, relatedProjectsNode, "calculated", "true");//$NON-NLS-1$//$NON-NLS-2$
    }

    private void addSection(Document doc, Node parentNode, String name, String value) {
        Element childNode = doc.createElement(name);
        childNode.setTextContent(value);
        parentNode.appendChild(childNode);
    }

}
