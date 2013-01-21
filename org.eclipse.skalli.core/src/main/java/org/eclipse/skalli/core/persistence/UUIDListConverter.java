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
package org.eclipse.skalli.core.persistence;

import java.util.UUID;

import org.eclipse.skalli.commons.UUIDList;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class UUIDListConverter implements Converter {

    @Override
    public boolean canConvert(Class type) {
        return UUIDList.class.isAssignableFrom(type);
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        UUIDList uuids = (UUIDList) source;
        for (UUID uuid : uuids) {
            writer.startNode("uuid");
            writer.setValue(uuid.toString());
            writer.endNode();
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        UUIDList uuids = new UUIDList();
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            String name = reader.getNodeName();
            String value = reader.getValue();
            if ("uuid".equals(name)) {
                uuids.add(UUID.fromString(value));
            }
            reader.moveUp();
        }
        return uuids;
    }

}
