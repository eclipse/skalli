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
import org.eclipse.skalli.services.extension.DataMigrationBase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DataMigration1 extends DataMigrationBase {

    public DataMigration1() {
        super(Project.class, 1);
    }

    private void remove(NodeList elements) {
        for (int i = 0; i < elements.getLength(); i++) {
            Node item = elements.item(i);
            item.getParentNode().removeChild(item);
        }
    }

    private void removeGAVs(NodeList elements) {
        for (int i = 0; i < elements.getLength(); i++) {
            Element node = (Element) elements.item(i);
            remove(node.getElementsByTagName("artifactID")); //$NON-NLS-1$
            remove(node.getElementsByTagName("groupID")); //$NON-NLS-1$
            remove(node.getElementsByTagName("version")); //$NON-NLS-1$
            remove(node.getElementsByTagName("id")); //$NON-NLS-1$
        }
    }

    @Override
    public void migrate(Document doc) {
        removeGAVs(doc.getElementsByTagName(MavenProjectExt.class.getName()));
    }

    // before the "grand" refactoring, Group was in package org.eclipse.skalli.common!
    @SuppressWarnings("nls")
    @Override
    public boolean handlesType(String entityClassName) {
        return super.handlesType(entityClassName) ||
                StringUtils.equals(entityClassName, "org.eclipse.skalli.model.core.Project");
    }

}
