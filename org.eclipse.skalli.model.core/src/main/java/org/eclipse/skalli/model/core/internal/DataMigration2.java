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
package org.eclipse.skalli.model.core.internal;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.model.ext.AbstractDataMigration;

public class DataMigration2 extends AbstractDataMigration {

  public DataMigration2() {
    super(Project.class, 2);
  }

  @Override
  public void migrate(Document doc) {
    NodeList wikiUrlNodes = doc.getElementsByTagName("wikiUrl");
    if (wikiUrlNodes.getLength() > 0) {
      Element wikiUrlElement = (Element) wikiUrlNodes.item(0);
      if (wikiUrlElement != null) {
        String wikiUrl = wikiUrlElement.getTextContent();
        wikiUrlElement.getParentNode().removeChild(wikiUrlElement);

        NodeList projectNodes = doc.getElementsByTagName("org.eclipse.skalli.model.core.Project");
        if (projectNodes.getLength() > 0) {
          Element projectElement = (Element) projectNodes.item(0);
          Element pageUrlElement = doc.createElement("pageUrl");
          pageUrlElement.setTextContent(wikiUrl);
          projectElement.appendChild(pageUrlElement);
        }
      }
    }
  }

}

