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
package org.eclipse.skalli.commons;

import java.util.UUID;


import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class UUIDListConverter implements Converter {

    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") Class type) {
        return UUIDList.class.isAssignableFrom(type);
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        UUIDList uuids = (UUIDList) source;
        for (UUID uuid : uuids) {
            writer.startNode("uuid"); //$NON-NLS-1$
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
            if ("uuid".equals(name)) { //$NON-NLS-1$
                uuids.add(UUID.fromString(value));
            }
            reader.moveUp();
        }
        return uuids;
    }

}
