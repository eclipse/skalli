/*******************************************************************************
 * Copyright (c) 2010, 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/edl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.example.skalli.ext.helloworld.internal;

import java.io.IOException;

import org.eclipse.skalli.services.extension.rest.RestConverterBase;
import org.example.skalli.model.ext.helloworld.HelloWorldProjectExt;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

class HelloWorldConverter extends RestConverterBase<HelloWorldProjectExt> {

    public static final String API_VERSION = "1.0"; //$NON-NLS-1$
    public static final String NAMESPACE = "http://www.eclipse.org/skalli/2010/API/Extension-HelloWorld"; //$NON-NLS-1$

    public HelloWorldConverter() {
        super(HelloWorldProjectExt.class);
    }

    @SuppressWarnings("nls")
    @Override
    protected void marshal(HelloWorldProjectExt ext) throws IOException {
        writer.pair("name", ext.getName());
        writer.collection("friends", "friend", ext.getFriends());
    }

    @Deprecated
    public HelloWorldConverter(String host) {
        super(HelloWorldProjectExt.class, "helloWorld", host);
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
        return null;
    }

    @Deprecated
    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        HelloWorldProjectExt ext = (HelloWorldProjectExt) source;
        writeNode(writer, "name", ext.getName());
        writeFriends(writer, ext);
        writer.endNode();

    }

    @Deprecated
    private void writeFriends(HierarchicalStreamWriter writer, HelloWorldProjectExt ext) {
        writer.startNode("friends");
        for (String friend : ext.getFriends()) {
            writeNode(writer, "friend", friend); //$NON-NLS-1
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        //    HelloWorldProjectExt ext = new HelloWorldProjectExt();
        //    while (reader.hasMoreChildren()) {
        //      reader.moveDown();
        //      String field = reader.getNodeName();
        //      if ("name".equals(field) && reader.hasMoreChildren()) { //$NON-NLS-1$
        //        String name = reader.getValue();
        //        ext.setName(name);
        //      } else if ("friends".equals(field) && reader.hasMoreChildren()) { //$NON-NLS-1$
        //        iterateFriends(ext, reader);
        //      }
        //      reader.moveUp();
        //    }
        //
        //    return ext;
        //TODO
        return new HelloWorldProjectExt();
    }

    //  private void iterateFriends(HelloWorldProjectExt ext, HierarchicalStreamReader reader) {
    //    if (reader.hasMoreChildren()) {
    //      reader.moveDown();
    //      String field = reader.getNodeName();
    //      if ("friend".equals(field)) {
    //        String friend = reader.getValue();
    //        ext.addFriend(friend);
    //      }
    //      iterateFriends(ext, reader);
    //      reader.moveUp();
    //    }
    //  }
}
