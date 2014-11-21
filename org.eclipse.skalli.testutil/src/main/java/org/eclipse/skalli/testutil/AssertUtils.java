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

import java.util.Collection;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.UUID;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.skalli.model.Project;
import org.junit.Assert;

@SuppressWarnings("nls")
public class AssertUtils {

    public static void assertEquals(String message, String[][] collection, String[]... values) {
        Assert.assertEquals(message + "[size]", values.length, collection.length);
        for (int i = 0; i < collection.length; ++i) {
            for (int j = 0; j < 4; ++j) {
                Assert.assertEquals(message + "[" + i + "," + j + "]", collection[i][j], values[i][j]);
            }
        }
    }

    public static <T> void assertEquals(String message, Collection<T> collection, T... values) {
        Assert.assertEquals(message + "[size]", values.length, collection.size());
        int i = 0;
        for (T o : collection) {
            Assert.assertEquals(message + "[" + i + "]", values[i], o);
            ++i;
        }
    }

    public static void assertEquals(String message, SortedMap<?, ?> map, String... values) {
        Assert.assertEquals(message + "[size]", values.length, map.size());
        int i = 0;
        for (Object o : map.keySet()) {
            Assert.assertEquals(message + "[" + i + "]", values[i], o.toString());
            ++i;
        }
    }

    public static <T> void assertEquals(String message, Collection<T> collection1, Collection<T> collection2) {
        Assert.assertEquals(message + "[size]", collection1.size(), collection2.size());
        Iterator<T> it1 = collection1.iterator();
        Iterator<T> it2 = collection2.iterator();
        while (it1.hasNext()) {
            T next1 = it1.next();
            T next2 = it2.next();
            Assert.assertTrue(message + "[" + next1 + "equals(" + next2 + ")]", next1.equals(next2));
        }
    }

    public static <T> void assertEqualsAnyOrder(String message, Collection<T> collection1, Collection<T> collection2) {
        Assert.assertEquals(message + "[size]", collection1.size(), collection2.size());
        Iterator<T> it1 = collection1.iterator();
        while (it1.hasNext()) {
            T next1 = it1.next();
            Assert.assertTrue(message + "[" + next1 + " found]", collection2.contains(next1));
        }
    }

    public static void assertEquals(Collection<Project> projects, UUID...uuids) {
        Assert.assertNotNull(projects);
        Assert.assertEquals(uuids.length, projects.size());
        Iterator<Project> it = projects.iterator();
        for (UUID uuid: uuids) {
            Assert.assertEquals(uuid, it.next().getUuid());
        }
    }

    public static void assertEqualsAnyOrder(Collection<Project> projects, UUID...uuids) {
        Assert.assertNotNull(projects);
        Assert.assertEquals(uuids.length, projects.size());
        for (Project project: projects) {
            Assert.assertTrue(ArrayUtils.contains(uuids, project.getUuid()));
        }
    }

}
