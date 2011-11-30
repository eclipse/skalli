/*******************************************************************************
 * Copyright (c) 2010 - 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.model.ext.maven.internal;

import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.model.ext.AbstractDataMigration;
import org.eclipse.skalli.model.ext.ValidationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class DataMigrationMavenModule extends AbstractDataMigration {

    public DataMigrationMavenModule() {
        super(Project.class, 18);
    }

    /* (non-Javadoc)
     * @see org.eclipse.skalli.model.ext.DataMigration#migrate(org.w3c.dom.Document)
     */
    @Override
    public void migrate(Document doc) throws ValidationException {
        NodeList nodes = doc.getElementsByTagName("org.eclipse.skalli.model.ext.maven.MavenCoordinate"); //$NON-NLS-1$
        for (int i = 0; i < nodes.getLength(); i++) {
            doc.renameNode(nodes.item(i), null, "org.eclipse.skalli.model.ext.maven.MavenModule"); //$NON-NLS-1$
        }
    }

}
