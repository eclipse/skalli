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
package org.eclipse.skalli.core.internal.users;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.Group;
import org.eclipse.skalli.services.extension.DataMigrationBase;
import org.eclipse.skalli.services.extension.MigrationException;
import org.eclipse.skalli.services.extension.MigrationUtils;
import org.w3c.dom.Document;

public class LocalGroupDataMigration19 extends DataMigrationBase {

    public LocalGroupDataMigration19() {
        super(Group.class, 19);
    }

    /**
     * Changes from model version 0->1:
     * <ol>
     *   <li>renamed root element org.eclipse.skalli.common.Group to "group"</li>
     * </ol>
     */
    @SuppressWarnings("nls")
    @Override
    public void migrate(Document doc) throws MigrationException {
        MigrationUtils.renameTag(doc, doc.getDocumentElement(), "entity-group");
    }

    // before the "grand" refactoring, Group was in package org.eclipse.skalli.common!
    @SuppressWarnings("nls")
    @Override
    public boolean handlesType(String entityClassName) {
        return super.handlesType(entityClassName) ||
                StringUtils.equals(entityClassName, "org.eclipse.skalli.common.Group");
    }
}
