package org.eclipse.skalli.model.core.internal;

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
