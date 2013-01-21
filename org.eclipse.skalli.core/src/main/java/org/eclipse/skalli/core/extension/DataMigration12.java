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
package org.eclipse.skalli.core.extension;

import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.services.extension.MigrationException;
import org.eclipse.skalli.services.extension.MigrationUtils;
import org.w3c.dom.Document;

public class DataMigration12 extends ProjectDataMigration {

    public DataMigration12() {
        super(Project.class, 12);
    }

    /**
     * Changes from model version 12 -> 13:
     * <ol>
     *   <li>Page URL & mailing lists now in separate extension</li>
     * </ol>
     */
    @Override
    public void migrate(Document doc) throws MigrationException {
        String extensionClassName = "org.eclipse.skalli.model.ext.info.InfoProjectExt"; //$NON-NLS-1$
        MigrationUtils.getOrCreateExtensionNode(doc, extensionClassName);
        MigrationUtils.moveTagToExtension(doc, extensionClassName, "pageUrl"); //$NON-NLS-1$
        MigrationUtils.moveTagToExtension(doc, extensionClassName, "mailingLists"); //$NON-NLS-1$
    }
}
