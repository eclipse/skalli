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

import org.eclipse.skalli.model.Project;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@SuppressWarnings("nls")
public class DataMigration3 extends ProjectDataMigration {

    public DataMigration3() {
        super(Project.class, 3);
    }

    @Override
    public void migrate(Document doc) {
        NodeList mailingListNodes = doc.getElementsByTagName("mailingList");
        if (mailingListNodes.getLength() > 0) {
            Element oldMailingListElement = (Element) mailingListNodes.item(0);
            if (oldMailingListElement != null) {
                String mailingList = oldMailingListElement.getTextContent();
                oldMailingListElement.getParentNode().removeChild(oldMailingListElement);

                NodeList projectNodes = doc.getElementsByTagName("org.eclipse.skalli.model.core.Project");
                if (projectNodes.getLength() > 0) {
                    Element projectElement = (Element) projectNodes.item(0);
                    Element newMailingListElement = doc.createElement("mailingList");
                    newMailingListElement.setTextContent(mailingList);
                    projectElement.appendChild(newMailingListElement);
                }
            }
        }
    }
}
