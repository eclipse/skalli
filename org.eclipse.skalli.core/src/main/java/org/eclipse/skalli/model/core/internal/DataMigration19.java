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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DataMigration19 extends ProjectDataMigration {

    public DataMigration19() {
        super(Project.class, 19);
    }

    /**
     * Changes from model version 19->20:
     * <ol>
     *   <li>people extension: renamed ProjectMember -> member</li>
     *   <li>scrum extension: renamed ProjectMember -> member</li>
     *   <li>org.eclipse.skalli.common.LinkGroup -> linkgroup</li>
     *   <li>org.eclipse.skalli.model.ext.Link -> link</li>
     *   <li>moved tags -> TagsExtension</li>
     * </ol>
     */
    @SuppressWarnings("nls")
    @Override
    public void migrate(Document doc) throws MigrationException {
        MigrationUtils.renameTag(doc, doc.getDocumentElement(), "entity-project");
        MigrationUtils.renameAllTags(doc, "org.eclipse.skalli.model.ext.info.InfoProjectExt", "entity-info");
        MigrationUtils.renameAllTags(doc, "org.eclipse.skalli.model.ext.people.PeopleProjectExt", "entity-people");
        MigrationUtils.renameAllTags(doc,"org.eclipse.skalli.model.core.ProjectMember", "member");
        MigrationUtils.renameAllTags(doc,"org.eclipse.skalli.common.LinkGroup", "linkGroup");
        MigrationUtils.renameAllTags(doc,"org.eclipse.skalli.model.ext.Link", "link");
        MigrationUtils.moveTagToExtension(doc, "entity-tags", "tags");
        Element inheritedElement = MigrationUtils.getElementOfEntity(doc, "inheritedExtensions");
        if (inheritedElement != null) {
            NodeList nodes = inheritedElement.getChildNodes();
            if (nodes != null) {
                for (int i = 0; i < nodes.getLength(); i++) {
                    Node node = nodes.item(i);
                    if ("string".equals(node.getNodeName())) { //$NON-NLS-1$
                        String value = node.getTextContent();
                        if ("org.eclipse.skalli.model.ext.info.InfoProjectExt".equals(value)) {
                            node.setTextContent("entity-info");
                        } else if ("org.eclipse.skalli.model.ext.people.PeopleProjectExt".equals(value)) {
                            node.setTextContent("entity-people");
                        } else if ("org.eclipse.skalli.model.ext.devinf.DevInfProjectExt".equals(value)) {
                            node.setTextContent("entity-devInf");
                        } else if ("org.eclipse.skalli.model.ext.linkgroups.LinkGroupsProjectExt".equals(value)) {
                            node.setTextContent("entity-linkGroups");
                        } else if ("org.eclipse.skalli.model.ext.misc.RelatedProjectsExt".equals(value)) {
                            node.setTextContent("entity-relatedProjects");
                        } else if ("org.eclipse.skalli.model.ext.misc.ReviewProjectExt".equals(value)) {
                            node.setTextContent("entity-reviews");
                        } else if ("org.eclipse.skalli.model.ext.scrum.ScrumProjectExt".equals(value)) {
                            node.setTextContent("entity-scrum");
                        }
                    }
                }
            }
        }
    }
}
