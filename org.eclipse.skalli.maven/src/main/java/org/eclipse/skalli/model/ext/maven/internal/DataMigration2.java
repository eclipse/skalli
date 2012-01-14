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
package org.eclipse.skalli.model.ext.maven.internal;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.ext.maven.MavenProjectExt;
import org.eclipse.skalli.model.ext.maven.MavenReactorProjectExt;
import org.eclipse.skalli.services.extension.DataMigrationBase;
import org.eclipse.skalli.services.extension.MigrationException;
import org.eclipse.skalli.services.extension.MigrationUtils;
import org.w3c.dom.Document;

public class DataMigration2 extends DataMigrationBase {

    private static final String TAG_NAME = "mavenReactor"; //$NON-NLS-1$

    public DataMigration2() {
        super(Project.class, 1);
    }

    @Override
    public void migrate(Document doc) throws MigrationException {
        String sourceExtClassName = MavenProjectExt.class.getName();
        String targetExtClassName = MavenReactorProjectExt.class.getName();
        MigrationUtils.moveTagToExtension(doc, sourceExtClassName, targetExtClassName, TAG_NAME, TAG_NAME);
    }

    // before the "grand" refactoring, Group was in package org.eclipse.skalli.common!
    @SuppressWarnings("nls")
    @Override
    public boolean handlesType(String entityClassName) {
        return super.handlesType(entityClassName) ||
                StringUtils.equals(entityClassName, "org.eclipse.skalli.model.core.Project");
    }
}
