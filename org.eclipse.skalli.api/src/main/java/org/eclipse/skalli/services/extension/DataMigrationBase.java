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
package org.eclipse.skalli.services.extension;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.EntityBase;

/**
 * Abstract base class for {@link DataMigration model migrations}.
 * This class implements a couple of utility methods to perform common
 * migration tasks, like renaming an element, or moving an element to a model extension.
 */
public abstract class DataMigrationBase implements DataMigration {

    protected final int fromVersion;
    protected final Class<? extends EntityBase> migratingClass;

    /**
     * Constructs a migration for a model entity with the given model version.
     * @param fromVersion  model version of a model entity to which this
     * migrator should be applied.
     */
    public DataMigrationBase(Class<? extends EntityBase> migratingClass, int fromVersion) {
        this.migratingClass = migratingClass;
        this.fromVersion = fromVersion;
    }

    @Override
    public int getFromVersion() {
        return fromVersion;
    }

    @Override
    public boolean handlesType(String entityClassName) {
        return StringUtils.equals(entityClassName, migratingClass.getName());
    }

    @Override
    public int compareTo(DataMigration o) {
        return Integer.valueOf(fromVersion).compareTo(o.getFromVersion());
    }

}
