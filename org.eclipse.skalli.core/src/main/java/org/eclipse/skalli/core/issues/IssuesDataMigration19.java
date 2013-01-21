
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
package org.eclipse.skalli.core.issues;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.services.extension.DataMigrationBase;
import org.eclipse.skalli.services.extension.MigrationException;
import org.eclipse.skalli.services.extension.MigrationUtils;
import org.eclipse.skalli.services.issues.Issues;
import org.eclipse.skalli.services.issues.IssuesService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class IssuesDataMigration19 extends DataMigrationBase {

    public IssuesDataMigration19() {
        super(Issues.class, 19);
    }

    /**
     * Changes from model version 18->19:
     * <ol>
     *   <li>renamed root element org.eclipse.skalli.model.ext.Issues to "issues"</li>
     *   <li>renamed root element org.eclipse.skalli.model.ext.Issue to "issue"</li>
     * </ol>
     */
    @SuppressWarnings("nls")
    @Override
    public void migrate(Document doc) throws MigrationException {
        MigrationUtils.renameTag(doc, doc.getDocumentElement(), "entity-issues");
        MigrationUtils.renameAllTags(doc, "org.eclipse.skalli.model.ext.Issue", "issue");
        MigrationUtils.removeAllTags(doc, "extension");
        NodeList nodes = doc.getElementsByTagName("issuer");
        for (int i=0; i<nodes.getLength(); ++i) {
            Element elem = (Element) nodes.item(i);
            elem.setTextContent(IssuesService.class.getName());
        }
        Element staleElem = MigrationUtils.getChild(doc.getDocumentElement(), "stale");
        if (staleElem == null) {
            staleElem = doc.createElement("stale");
            doc.getDocumentElement().appendChild(staleElem);
        }
        staleElem.setTextContent("true");
    }

    // before the "grand" refactoring, Group was in package org.eclipse.skalli.common!
    @SuppressWarnings("nls")
    @Override
    public boolean handlesType(String entityClassName) {
        return super.handlesType(entityClassName) ||
                StringUtils.equals(entityClassName, "org.eclipse.skalli.model.ext.Issues");
    }
}
