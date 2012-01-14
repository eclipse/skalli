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

import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.services.extension.MigrationException;
import org.eclipse.skalli.services.extension.MigrationUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Renames &lt;maturity&gt; of {@link org.eclipse.skalli.services.projects.Project}
 * to &lt;phase&gt;. If project has  {@link org.eclipse.skalli.model.ext.sap.NGPProjectExt}
 * extension: copies &lt;maturity&gt; as &lt;ngpMaturity&gt;
 *
 */
public class DataMigration8 extends ProjectDataMigration {

    public DataMigration8() {
        super(Project.class, 8);
    }

    /**
     * Changes from model version 8->9:
     * <ol>
     *   <li>tag "maturity" renamed to "phase"</li>
     *   <li>map the previously defined enum values "EXPERIMENTAL" and "INCUBATION"
     *     to "Experimental" and "Incubation"</li>
     * </ol>
     */
    @Override
    public void migrate(Document doc) throws MigrationException {
        MigrationUtils.renameTag(doc, "maturity", "phase");
        NodeList nodes = doc.getElementsByTagName("phase");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);
            String value = element.getTextContent();
            if ("EXPERIMENTAL".equals(value)) {
                value = "Experimental";
            }
            if ("INCUBATION".equals(value)) {
                value = "Incubation";
            }
            element.setTextContent(value);
        }
    }

}
