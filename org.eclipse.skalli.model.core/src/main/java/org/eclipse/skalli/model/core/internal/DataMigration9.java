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

public class DataMigration9 extends AbstractDataMigration {

  public DataMigration9() {
    super(Project.class, 9);
  }

  /**
   * Changes from model version 9->10:
   * <ol>
   *   <li>do no longer use class name of project template as id, but
   *     {@link org.eclipse.skalli.model.core.ProjectTemplate#getId()}</li>
   * </ol>
   */
  @Override
  public void migrate(Document doc) {
    NodeList nodes = doc.getElementsByTagName("projectTemplateId");
    for (int i = 0; i < nodes.getLength(); i++) {
      Element element = (Element)nodes.item(i);
      String oldValue = element.getTextContent();
      if ("org.eclipse.skalli.model.core.DefaultProjectTemplate".equals(oldValue)) {
        element.setTextContent("default");
      } else if ("org.eclipse.skalli.model.ext.sap.internal.NGPProjectTemplate".equals(oldValue)) {
        element.setTextContent("com.sap.ngp");
      }
    }
  }

}

