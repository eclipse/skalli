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
package org.eclipse.skalli.testutil;

import java.io.StringWriter;

import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;

public class StringBufferHierarchicalStreamWriter implements HierarchicalStreamWriter {
    private StringWriter sb;
    private PrettyPrintWriter writer;

    public StringBufferHierarchicalStreamWriter() {
        sb = new StringWriter();
        writer = new PrettyPrintWriter(sb);
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    }

    @Override
    public String toString() {
        return sb.toString();
    }

    @Override
    public void addAttribute(String key, String value) {
        writer.addAttribute(key, value);
    }

    @Override
    public void close() {
        writer.close();
    }

    @Override
    public void endNode() {
        writer.endNode();
    }

    @Override
    public void flush() {
        writer.flush();
    }

    @Override
    public void setValue(String text) {
        writer.setValue(text);
    }

    @Override
    public void startNode(String name) {
        writer.startNode(name);
    }

    @Override
    public HierarchicalStreamWriter underlyingWriter() {
        return writer;
    }
}
