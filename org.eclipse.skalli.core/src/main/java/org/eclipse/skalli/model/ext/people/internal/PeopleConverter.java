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
package org.eclipse.skalli.model.ext.people.internal;

import org.eclipse.skalli.model.Member;
import org.eclipse.skalli.model.ext.commons.PeopleExtension;
import org.eclipse.skalli.services.extension.rest.RestConverterBase;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class PeopleConverter extends RestConverterBase<PeopleExtension> {

    public static final String API_VERSION = "1.0"; //$NON-NLS-1$
    public static final String NAMESPACE = "http://www.eclipse.org/skalli/2010/API/Extension-People"; //$NON-NLS-1$

    public PeopleConverter(String host) {
        super(PeopleExtension.class, "people", host); //$NON-NLS-1$
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        PeopleExtension ext = (PeopleExtension) source;
        writer.startNode("leads"); //$NON-NLS-1$
        for (Member member : ext.getLeads()) {
            writer.startNode("lead"); //$NON-NLS-1$
            writeNode(writer, "userId", member.getUserID()); //$NON-NLS-1$
            writeUserLink(writer, USER_RELATION, member.getUserID());
            writer.endNode();
        }
        writer.endNode();
        writer.startNode("members"); //$NON-NLS-1$
        for (Member member : ext.getMembers()) {
            writer.startNode("member"); //$NON-NLS-1$
            writeNode(writer, "userId", member.getUserID()); //$NON-NLS-1$
            writeUserLink(writer, USER_RELATION, member.getUserID());
            writer.endNode();
        }
        writer.endNode();

    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        return iterateNodes(null, reader, context);
    }

    private PeopleExtension iterateNodes(PeopleExtension ext, HierarchicalStreamReader reader,
            UnmarshallingContext context) {
        if (ext == null) {
            ext = new PeopleExtension();
        }

        while (reader.hasMoreChildren()) {
            reader.moveDown();

            String field = reader.getNodeName();

            if ("leads".equals(field) && reader.hasMoreChildren()) { //$NON-NLS-1$
                iterateNodes(ext, reader, context);
            }
            else if ("members".equals(field) && reader.hasMoreChildren()) { //$NON-NLS-1$
                iterateNodes(ext, reader, context);
            }
            else if ("lead".equals(field) && reader.hasMoreChildren()) { //$NON-NLS-1$
                ext.addLead(unmarshalProjectMember(reader));
            }
            else if ("member".equals(field) && reader.hasMoreChildren()) { //$NON-NLS-1$
                ext.addMember(unmarshalProjectMember(reader));
            }

            reader.moveUp();
        }

        return ext;
    }

    private Member unmarshalProjectMember(HierarchicalStreamReader reader) {
        Member result = null;
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            String field = reader.getNodeName();
            String value = reader.getValue();
            if ("userId".equals(field)) { //$NON-NLS-1$
                result = new Member(value);
            }
            reader.moveUp();
        }
        return result;
    }

    @Override
    public String getApiVersion() {
        return API_VERSION;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public String getXsdFileName() {
        return "extension-people.xsd"; //$NON-NLS-1$
    }

}
