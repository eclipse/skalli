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

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompositeEntityClassLoader extends ClassLoader {

    private static final Logger LOG = LoggerFactory.getLogger(CompositeEntityClassLoader.class);

    private final Set<ClassLoader> classLoaders = new HashSet<ClassLoader>(0);

    public CompositeEntityClassLoader(Set<ClassLoader> classLoaders) {
        this.classLoaders.addAll(classLoaders);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> ret = null;
        for (ClassLoader classLoader : classLoaders) {
            try {
                ret = classLoader.loadClass(name);
                if (ret != null) {
                    return ret;
                }
            } catch (ClassNotFoundException e) {
                // ingore, cuz that's the point of this composite class loader!
                LOG.debug(MessageFormat.format("Class {0} not found by class loader {1}", name, classLoader));
            }
        }
        throw new ClassNotFoundException("Class not found: " + name);
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        for (ClassLoader classLoader : classLoaders) {
            InputStream ret = null;
            ret = classLoader.getResourceAsStream(name);
            if (ret != null) {
                return ret;
            }
        }
        return null;
    }

}
