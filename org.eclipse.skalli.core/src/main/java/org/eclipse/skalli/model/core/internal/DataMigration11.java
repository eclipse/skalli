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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@SuppressWarnings("nls")
public class DataMigration11 extends ProjectDataMigration {
    private static final Logger LOG = LoggerFactory.getLogger(DataMigration11.class);

    public DataMigration11() {
        super(Project.class, 11);
    }

    /**
     * Changes from model version 11 -> 12:
     * <ol>
     *   <li>Project members now in separate collections</li>
     * </ol>
     */
    @Override
    public void migrate(Document doc) {
        Map<String, String> roleCache = new HashMap<String, String>();

        List<String> members = new LinkedList<String>();
        List<String> leads = new LinkedList<String>();
        List<String> productOwners = new LinkedList<String>();
        List<String> scrumMasters = new LinkedList<String>();

        // reading old project members and their roles
        NodeList nodes = doc.getElementsByTagName("org.eclipse.skalli.model.core.ProjectMember");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element member = (Element) nodes.item(i);
            String userId = member.getElementsByTagName("userID").item(0).getTextContent();
            LOG.debug("Reading User '" + userId + "' for Migration.");

            NodeList roles = member.getElementsByTagName("roles").item(0).getChildNodes();
            for (int j = 0; j < roles.getLength(); j++) {
                Node roleNode = (Node) roles.item(j);
                if (roleNode instanceof Element && !roleNode.getNodeName().equals("no-comparator")) {
                    Element roleElement = (Element) roleNode;
                    String role = null;
                    if (StringUtils.isBlank(roleElement.getAttribute("reference"))) {
                        role = roleElement.getElementsByTagName("technicalName").item(0).getTextContent();
                        roleCache.put(roleElement.getNodeName(), role);
                    } else {
                        role = roleCache.get(roleElement.getNodeName());
                    }
                    LOG.debug("User '" + userId + "' has role '" + role + "'.");
                    if (role.equals("projectmember")) {
                        members.add(userId);
                    } else if (role.equals("projectlead")) {
                        leads.add(userId);
                    } else if (role.equals("scrummaster")) {
                        scrumMasters.add(userId);
                    } else if (role.equals("productowner")) {
                        productOwners.add(userId);
                    } else {
                        throw new RuntimeException("unknown role: " + role);
                    }
                }
            }
        }

        // remove current "members" section
        Node membersNode = doc.getElementsByTagName("members").item(0);
        if (membersNode == null) {
            throw new RuntimeException(doc.toString());
        }
        Node projectNode = membersNode.getParentNode();
        projectNode.removeChild(membersNode);

        // add (new) members
        addPeopleSection(doc, projectNode, "members", members);

        // add leads
        addPeopleSection(doc, projectNode, "leads", leads);

        // add scrum people
        if (scrumMasters.size() > 0 || productOwners.size() > 0) {
            Node scrumExt = doc.getElementsByTagName("org.eclipse.skalli.model.ext.scrum.ScrumProjectExt").item(0);

            if (scrumExt == null) {
                LOG.warn("there were scrum people, but no scrum extension.");
            } else {
                // add scrum masters
                addPeopleSection(doc, scrumExt, "scrumMasters", scrumMasters);

                // add product owners
                addPeopleSection(doc, scrumExt, "productOwners", productOwners);
            }
        }
    }

    private void addPeopleSection(Document doc, Node parentNode, String name, List<String> people) {
        Element peopleElement = doc.createElement(name);
        addPeople(doc, peopleElement, people);
        parentNode.appendChild(peopleElement);
    }

    private void addPeople(Document doc, Element element, List<String> people) {
        Element setEntry = doc.createElement("no-comparator");
        element.appendChild(setEntry);
        for (String userId : people) {
            Element memberElement = doc.createElement("org.eclipse.skalli.model.core.ProjectMember");
            element.appendChild(memberElement);

            Element userIdElement = doc.createElement("userID");
            memberElement.appendChild(userIdElement);
            userIdElement.setTextContent(userId);
        }
    }

}
