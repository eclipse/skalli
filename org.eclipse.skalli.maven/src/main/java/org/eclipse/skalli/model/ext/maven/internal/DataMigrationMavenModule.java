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
package org.eclipse.skalli.model.ext.maven.internal;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.services.extension.DataMigrationBase;
import org.eclipse.skalli.services.extension.MigrationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class DataMigrationMavenModule extends DataMigrationBase {

    public DataMigrationMavenModule() {
        super(Project.class, 18);
    }

    /* (non-Javadoc)
     * @see org.eclipse.skalli.model.ext.DataMigration#migrate(org.w3c.dom.Document)
     */
    @Override
    public void migrate(Document doc) throws MigrationException {
        NodeList nodes = doc.getElementsByTagName("org.eclipse.skalli.model.ext.maven.MavenCoordinate"); //$NON-NLS-1$
        for (int i = 0; i < nodes.getLength(); i++) {
            doc.renameNode(nodes.item(i), null, "org.eclipse.skalli.model.ext.maven.MavenModule"); //$NON-NLS-1$
        }
    }

    // before the "grand" refactoring, Group was in package org.eclipse.skalli.common!
    @SuppressWarnings("nls")
    @Override
    public boolean handlesType(String entityClassName) {
        return super.handlesType(entityClassName) ||
                StringUtils.equals(entityClassName, "org.eclipse.skalli.model.core.Project");
    }
}
