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
package org.eclipse.skalli.core.xstream;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.mapper.MapperWrapper;

/**
 * Specialized XStream for our needs: MapperWrapperIgnoreUnknownElements to
 * ignore unknown fields and inner classes.
 */
public class IgnoreUnknownElementsXStream extends XStream {

    private IgnoreUnknownElementsMapperWrapper wrapper;

    private IgnoreUnknownElementsXStream() {
    }

    @Override
    protected MapperWrapper wrapMapper(MapperWrapper next) {
        if (wrapper == null) {
            wrapper = new IgnoreUnknownElementsMapperWrapper(next);
        }
        return wrapper;
    }

    static public XStream getXStreamInstance(Set<? extends Converter> converters, Set<ClassLoader> entityClassLoaders,
            Map<String, Class<?>> aliases) {
        XStream xstream = new IgnoreUnknownElementsXStream();
        if (converters != null) {
            for (Converter converter : converters) {
                xstream.registerConverter(converter);
            }
        }
        if (entityClassLoaders != null) {
            xstream.setClassLoader(new CompositeEntityClassLoader(entityClassLoaders));
        }
        if (aliases != null) {
            for (Entry<String, Class<?>> entry : aliases.entrySet()) {
                xstream.alias(entry.getKey(), entry.getValue());
            }
        }
        return xstream;
    }

}
