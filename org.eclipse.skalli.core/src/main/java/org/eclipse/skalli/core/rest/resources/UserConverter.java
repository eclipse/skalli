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
package org.eclipse.skalli.core.rest.resources;

import org.eclipse.skalli.model.User;
import org.eclipse.skalli.services.extension.rest.RestConverterBase;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

class UserConverter extends RestConverterBase<User> {

    public static final String API_VERSION = "1.0"; //$NON-NLS-1$
    public static final String NAMESPACE = "http://www.eclipse.org/skalli/2010/API"; //$NON-NLS-1$

    public UserConverter(String host) {
        super(User.class, "user", host); //$NON-NLS-1$
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        User user = (User) source;
        marshalNSAttributes(writer);
        marshalApiVersion(writer);
        writeUserLink(writer, SELF_RELATION, user.getUserId());
        writeNode(writer, "userId", user.getUserId()); //$NON-NLS-1$
        writeNode(writer, "firstname", user.getFirstname()); //$NON-NLS-1$
        writeNode(writer, "lastname", user.getLastname()); //$NON-NLS-1$
        writeNode(writer, "email", user.getEmail()); //$NON-NLS-1$
        writeNode(writer, "phone", user.getTelephone()); //$NON-NLS-1$
        writeNode(writer, "mobile", user.getMobile()); //$NON-NLS-1$
        writeNode(writer, "sip", user.getSip()); //$NON-NLS-1$
        writeNode(writer, "company", user.getCompany()); //$NON-NLS-1$
        writeNode(writer, "department", user.getDepartment()); //$NON-NLS-1$
        writeNode(writer, "location", user.getLocation()); //$NON-NLS-1$
        writeNode(writer, "room", user.getRoom()); //$NON-NLS-1$
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
        return "user.xsd"; //$NON-NLS-1$
    }
}
