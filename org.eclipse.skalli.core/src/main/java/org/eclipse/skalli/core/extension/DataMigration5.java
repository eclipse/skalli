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

@SuppressWarnings("nls")
public class DataMigration5 extends ProjectDataMigration {

    public DataMigration5() {
        super(Project.class, 5);
    }

    @Override
    public void migrate(Document doc) throws MigrationException {
        MigrationUtils.migrateStringToStringSet(doc, "mailingList", "mailingLists", false);
        MigrationUtils.migrateStringToStringSet(doc, "scmLocation", "scmLocations", false);
    }

}
