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

import java.util.TreeSet;

import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.ExtensionsMap;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ExtensionsMapConverter implements Converter {

    @Override
    public boolean canConvert(Class type) {
        return ExtensionsMap.class.isAssignableFrom(type);
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        ExtensionsMap map = (ExtensionsMap) source;
        TreeSet<ExtensionEntityBase> set = new TreeSet<ExtensionEntityBase>(map.getAllExtensions());
        context.convertAnother(set);
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        ExtensionsMap map = new ExtensionsMap();
        TreeSet<?> set = (TreeSet<?>) context.convertAnother(map, TreeSet.class);
        for (Object o : set) {
            map.putExtension((ExtensionEntityBase) o);
        }
        return map;
    }

}
