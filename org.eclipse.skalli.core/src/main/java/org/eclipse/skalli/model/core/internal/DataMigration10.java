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

public class DataMigration10 extends ProjectDataMigration {

    public DataMigration10() {
        super(Project.class, 10);
    }

    /**
     * Changes from model version 10->11:
     * <ol>
     *   <li>renamed parentProject -> parentEntityId</li>
     * </ol>
     */
    @Override
    public void migrate(Document doc) throws MigrationException {
        MigrationUtils.renameTag(doc, "parentProject", "parentEntityId");
    }
}
