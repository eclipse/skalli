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
import org.eclipse.skalli.services.extension.MigrationException;
import org.eclipse.skalli.services.extension.MigrationUtils;
import org.w3c.dom.Document;

public class DataMigration13 extends ProjectDataMigration {

    public DataMigration13() {
        super(Project.class, 13);
    }

    /**
     * Changes from model version 13 -> 14:
     * <ol>
     *   <li>Project members/leads now in separate extension PeopleProjectExt</li>
     * </ol>
     */
    @Override
    public void migrate(Document doc) throws MigrationException {
        String extensionClassName = "org.eclipse.skalli.model.ext.people.PeopleProjectExt"; //$NON-NLS-1$
        MigrationUtils.getOrCreateExtensionNode(doc, extensionClassName);
        MigrationUtils.moveTagToExtension(doc, extensionClassName, "members"); //$NON-NLS-1$
        MigrationUtils.moveTagToExtension(doc, extensionClassName, "leads"); //$NON-NLS-1$
    }
}
