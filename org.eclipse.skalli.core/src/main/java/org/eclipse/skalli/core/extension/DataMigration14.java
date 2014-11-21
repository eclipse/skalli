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
package org.eclipse.skalli.core.extension;

import org.eclipse.skalli.model.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class DataMigration14 extends ProjectDataMigration {

    private static final Logger LOG = LoggerFactory.getLogger(DataMigration14.class);

    public DataMigration14() {
        super(Project.class, 14);
    }

    /**
     * Changes from model version 14 -> 15:
     * <ol>
     *   <li>Fixes malformed project IDs</li>
     * </ol>
     */
    @Override
    public void migrate(Document doc) {
        Node projectIdNode = doc.getElementsByTagName("projectId").item(0); //$NON-NLS-1$
        String projectId = projectIdNode.getTextContent();
        String projectIdTrimmed = projectId.trim();
        if (projectId.length() != projectIdTrimmed.length()) {
            projectIdNode.setTextContent(projectIdTrimmed);
            LOG.info(String.format("Trimmed project ID '%s'.", projectIdTrimmed)); //$NON-NLS-1$
        }
    }
}
