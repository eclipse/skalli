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
package org.eclipse.skalli.core.xstream;

import java.util.ArrayList;

import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.ExtensionsMap;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converter for marshaling/unmarshaling {@link ExtensionsMap}s with XStream.
 */
public class ExtensionsMapConverter implements Converter {

    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") Class type) {
        return ExtensionsMap.class.isAssignableFrom(type);
    }

    /**
     * Marshals extensions stored in the given {@link ExtensionsMap} with a guaranteed
     * stable ordering, i.e. by comparing the names of extension classes.
     */
    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        ExtensionsMap map = (ExtensionsMap) source;
        ArrayList<ExtensionEntityBase> sortedList = new ArrayList<ExtensionEntityBase>(map.getAllExtensions());
        context.convertAnother(sortedList);
    }

    /**
     * Unmarshals extensions and returns an <code>ExtensionsMap</code>.
     * Extensions that cannot be resolved, e.g. because there is no corresponding
     * extension class available, are ignored.
     */
    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        ExtensionsMap map = new ExtensionsMap();
        ArrayList<?> sortedList = (ArrayList<?>) context.convertAnother(map, ArrayList.class);
        for (Object entry : sortedList) {
            if (entry != null) {
                map.putExtension((ExtensionEntityBase) entry);
            }
        }
        return map;
    }

}
