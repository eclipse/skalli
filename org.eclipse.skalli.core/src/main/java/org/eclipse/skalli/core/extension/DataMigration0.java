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
package org.eclipse.skalli.core.extension;

import org.eclipse.skalli.model.Project;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@SuppressWarnings("nls")
public class DataMigration0 extends ProjectDataMigration {

    public DataMigration0() {
        super(Project.class, 0);
    }

    private void addNoComparator(Document doc, NodeList elements) {
        for (int i = 0; i < elements.getLength(); i++) {
            Element node = (Element) elements.item(i);
            Element element = doc.createElement("no-comparator");
            Node firstChild = node.getFirstChild();
            node.insertBefore(element, firstChild);
        }
    }

    @Override
    public void migrate(Document doc) {
        addNoComparator(doc, doc.getElementsByTagName("extensions"));
        addNoComparator(doc, doc.getElementsByTagName("members"));
        addNoComparator(doc, doc.getElementsByTagName("roles"));
    }

}
