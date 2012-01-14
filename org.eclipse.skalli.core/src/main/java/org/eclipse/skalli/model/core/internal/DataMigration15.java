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
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class DataMigration15 extends ProjectDataMigration {

    public DataMigration15() {
        super(Project.class, 15);
    }

    /**
     * Changes from model version 15 -> 16:
     * <ol>
     *   <li>Link was refactored and hence the fully qualified name changed.</li>
     * </ol>
     */
    @Override
    public void migrate(Document doc) {
        NodeList nodes = doc.getElementsByTagName("org.eclipse.skalli.common.Link"); //$NON-NLS-1$
        for (int i = 0; i < nodes.getLength(); i++) {
            doc.renameNode(nodes.item(i), null, "org.eclipse.skalli.model.ext.Link"); //$NON-NLS-1$
        }
    }
}
