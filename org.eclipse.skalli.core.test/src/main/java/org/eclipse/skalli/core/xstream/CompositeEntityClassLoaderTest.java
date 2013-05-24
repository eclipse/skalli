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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("nls")
public class CompositeEntityClassLoaderTest {

    private TestClassLoader classLoader1;
    private TestClassLoader classLoader2;
    private Set<ClassLoader> classLoaders;
    private CompositeEntityClassLoader cecl;

    private static class TestClassLoader extends ClassLoader {

        private final String knownClass;

        public TestClassLoader(String knownClass) {
            this.knownClass = knownClass;
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            if (knownClass.equals(name)) {
                return Object.class;
            } else {
                throw new ClassNotFoundException();
            }
        }

        @Override
        public InputStream getResourceAsStream(String name) {
            if (knownClass.equals(name)) {
                return new ByteArrayInputStream(new byte[0]);
            } else {
                return null;
            }
        }

    }

    @Before
    public void setup() {
        classLoader1 = new TestClassLoader("foo");
        classLoader2 = new TestClassLoader("bar");
        classLoaders = new HashSet<ClassLoader>(2);
        classLoaders.add(classLoader1);
        classLoaders.add(classLoader2);
        cecl = new CompositeEntityClassLoader(classLoaders);
    }

    @Test
    public void testLoadClass() throws Exception {
        Assert.assertNotNull(cecl.loadClass("foo"));
        Assert.assertNotNull(cecl.loadClass("bar"));
    }

    @Test(expected = ClassNotFoundException.class)
    public void testLoadClass_notFound() throws Exception {
        cecl.loadClass("bla");
    }

    @Test
    public void testGetResourceAsStream() throws Exception {
        Assert.assertNotNull(cecl.getResourceAsStream("foo"));
        Assert.assertNotNull(cecl.getResourceAsStream("bar"));
        Assert.assertNull(cecl.getResourceAsStream("bla"));
    }

}
