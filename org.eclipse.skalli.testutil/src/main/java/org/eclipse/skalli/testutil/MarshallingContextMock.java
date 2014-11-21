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

import java.util.HashMap;
import java.util.Iterator;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class MarshallingContextMock implements MarshallingContext {
    protected HierarchicalStreamWriter writer;
    protected HashMap map = new HashMap();

    public MarshallingContextMock(HierarchicalStreamWriter writer) {
        this.writer = writer;
    }

    @Override
    public Object get(Object key) {
        return map.get(key);
    }

    @Override
    public Iterator keys() {
        return map.keySet().iterator();
    }

    @Override
    public void put(Object key, Object value) {
        map.put(key, value);
    }

    @Override
    public void convertAnother(Object nextItem) {
    }

    @Override
    public void convertAnother(Object nextItem, Converter converter) {
        converter.marshal(nextItem, writer, this);
    }
}
