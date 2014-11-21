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
package org.eclipse.skalli.core.permit;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class PermitsConfigConverter implements Converter {

    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") Class type) {
        return type.equals(PermitsConfig.class);
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        PermitsConfig config = (PermitsConfig)source;
        int pos = 0;
        List<PermitConfig> permits = config.getPermits();
        for (PermitConfig permit : permits) {
            permit.setPos(pos);
            writer.startNode("permit"); //$NON-NLS-1$
            context.convertAnother(permit);
            writer.endNode();
            ++pos;
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        PermitsConfig config = new PermitsConfig();
        List<PermitConfig> permits = new ArrayList<PermitConfig>();
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            String field = reader.getNodeName();
            if ("permit".equals(field)) { //$NON-NLS-1$
                PermitConfig permit = (PermitConfig)context.convertAnother(context.currentObject(), PermitConfig.class);
                if (permit != null) {
                    permits.add(permit);
                }
            }
            reader.moveUp();
        }
        config.setPermits(permits);
        return config;
    }

}
