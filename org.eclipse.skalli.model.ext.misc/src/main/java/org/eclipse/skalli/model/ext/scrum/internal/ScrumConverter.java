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
package org.eclipse.skalli.model.ext.scrum.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.skalli.model.Member;
import org.eclipse.skalli.model.ext.scrum.ScrumProjectExt;
import org.eclipse.skalli.services.extension.rest.RestConverterBase;
import org.eclipse.skalli.services.extension.rest.RestException;
import org.eclipse.skalli.services.extension.rest.RestUtils;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

class ScrumConverter extends RestConverterBase<ScrumProjectExt> {

    public static final String API_VERSION = "1.0"; //$NON-NLS-1$
    public static final String NAMESPACE = "http://www.eclipse.org/skalli/2010/API/Extension-Scrum"; //$NON-NLS-1$

    public ScrumConverter() {
        super(ScrumProjectExt.class);
    }

    @SuppressWarnings("nls")
    @Override
    protected void marshal(ScrumProjectExt extension) throws IOException {
        writer.pair("backlogUrl", extension.getBacklogUrl());
        writer.array("scrumMasters", "scrumMaster");
        for (Member member : extension.getScrumMasters()) {
            writer
                .object()
                .pair("userId", member.getUserID())
                .link(USER_RELATION, RestUtils.URL_USERS, member.getUserID())
                .end();
        }
        writer.end();
        writer.array("productOwners", "productOwner");
        for (Member member : extension.getProductOwners()) {
            writer
                .object()
                .pair("userId", member.getUserID())
                .link(USER_RELATION, RestUtils.URL_USERS, member.getUserID())
                .end();
        }
        writer.end();
    }

    @Override
    protected ScrumProjectExt unmarshal() throws RestException, IOException {
        return unmarshal(new ScrumProjectExt());
    }

    @SuppressWarnings("nls")
    private ScrumProjectExt unmarshal(ScrumProjectExt ext) throws RestException, IOException {
        while (reader.hasMore()) {
            if (reader.isKey("backlogUrl")) {
                ext.setBacklogUrl(reader.valueString());
            } else if (reader.isKey("scrumMasters")) {
                ext.getScrumMasters().addAll(readMembers("scrumMaster"));
            } else if (reader.isKey("productOwners")) {
                ext.getProductOwners().addAll(readMembers("productOwner"));
            } else {
                reader.skip();
            }
        }
        return ext;
    }

    private List<Member> readMembers(String itemKey) throws RestException, IOException {
        List<Member> members = new ArrayList<Member>();
        reader.array(itemKey);
        while (reader.hasMore()) {
            Member member = readMember();
            if (member != null) {
                members.add(member);
            }
        }
        reader.end();
        return members;
    }

    private Member readMember() throws RestException, IOException {
        Member member = null;
        reader.object();
        while (reader.hasMore()) {
            if (reader.isKey("userId")) { //$NON-NLS-1$
                member = new Member(reader.valueString());
            } else {
                reader.skip();
            }
        }
        reader.end();
        return member;
    }

    public ScrumConverter(String host) {
        super(ScrumProjectExt.class, "scrum", host);
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        ScrumProjectExt ext = (ScrumProjectExt) source;
        writeNode(writer, "backlogUrl", ext.getBacklogUrl()); //$NON-NLS-1$
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader arg0, UnmarshallingContext arg1) {
        // don't support that yet
        return null;
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
        return "extension-scrum.xsd";
    }
}
