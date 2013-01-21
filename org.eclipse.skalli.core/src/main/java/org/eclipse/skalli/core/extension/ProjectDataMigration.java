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

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.services.extension.DataMigrationBase;

public abstract class ProjectDataMigration extends DataMigrationBase {

    public ProjectDataMigration(Class<? extends EntityBase> migratingClass, int fromVersion) {
        super(migratingClass, fromVersion);
    }

    // before the "grand" refactoring, Group was in package org.eclipse.skalli.common!
    @SuppressWarnings("nls")
    @Override
    public boolean handlesType(String entityClassName) {
        return super.handlesType(entityClassName) ||
                StringUtils.equals(entityClassName, "org.eclipse.skalli.model.core.Project");
    }

}
