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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.skalli.model.ExtensibleEntityBase;
import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.testutil.AssertUtils;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("nls")
public class EntityHelperTest {

    private static class TestEntity extends ExtensionEntityBase {
        String string = null;
        List<Object> list = null;
        Set<Object> set = null;
    }

    private static class TestEntityWithVariousCollections extends ExtensionEntityBase {
        List<String> list = null;
        Set<String> set = null;
        SortedSet<String> set1 = null;
        Map<String, String> map1 = null;
        SortedMap<String, String> map2 = null;
        ArrayList<String> list1 = null;
        TreeSet<String> set2 = null;
        HashSet<String> set3 = null;
        LinkedHashSet<String> set4 = null;
        HashMap<String, String> map3 = null;
        Vector<String> vector = null;
        ConcurrentLinkedQueue<String> queue = null;

        public void fillCollections() {
            list = new ArrayList<String>(Arrays.asList("foo", "", "bar", "", "", "hugo"));
            set = new HashSet<String>(list);
            set1 = new TreeSet<String>(list);
            map1 = new HashMap<String, String>();
            for (int i = 0; i < list.size(); ++i) {
                map1.put(Integer.toString(i), list.get(i));
            }
            map2 = new TreeMap<String, String>();
            for (int i = 0; i < list.size(); ++i) {
                map2.put(Integer.toString(i), list.get(i));
            }
            vector = new Vector<String>(list);
        }
    }

    private static class TestExtensibleEntity extends ExtensibleEntityBase {
        String string = null;
        List<Object> list = null;
        Set<Object> set = null;
    }

    @Test
    public void testReplaceNullFields_String() {
        TestEntity entity = new TestEntity();
        Assert.assertNull(entity.string);
        EntityHelper.normalize(entity);
        Assert.assertNotNull(entity.string);
        Assert.assertEquals("", entity.string);

        entity.string = ":-)";
        EntityHelper.normalize(entity);
        Assert.assertEquals(":-)", entity.string);
    }

    @Test
    public void testReplaceNullFields_List() {
        TestEntity entity = new TestEntity();
        Assert.assertNull(entity.list);
        EntityHelper.normalize(entity);
        Assert.assertNotNull(entity.list);
        if (entity.list != null) {
            Assert.assertEquals(0, entity.list.size());
            entity.list.add(new Object());
            EntityHelper.normalize(entity);
            Assert.assertEquals(1, entity.list.size());
        }
    }

    @Test
    public void testReplaceNullFields_Set() {
        TestEntity entity = new TestEntity();
        Assert.assertNull(entity.set);
        EntityHelper.normalize(entity);
        Assert.assertNotNull(entity.set);
        if (entity.set != null) {
            Assert.assertEquals(0, entity.set.size());

            entity.set.add(new Object());
            EntityHelper.normalize(entity);
            Assert.assertEquals(1, entity.set.size());
        }
    }

    @Test
    public void testReplaceNullFields_InstanceTypes() {
        TestEntityWithVariousCollections entity = new TestEntityWithVariousCollections();
        EntityHelper.normalize(entity);
        assertCollectionTypes(entity);
    }

    @Test
    public void testReplaceNullFields_Extensible() {
        TestEntity entity = new TestEntity();
        TestExtensibleEntity extensibleEntity = new TestExtensibleEntity();
        extensibleEntity.addExtension(entity);

        Assert.assertNull(extensibleEntity.set);
        Assert.assertNull(extensibleEntity.list);
        Assert.assertNull(extensibleEntity.string);
        Assert.assertNull(entity.set);
        Assert.assertNull(entity.list);
        Assert.assertNull(entity.string);
        EntityHelper.normalize(extensibleEntity);
        Assert.assertNotNull(extensibleEntity.set);
        Assert.assertNotNull(extensibleEntity.list);
        Assert.assertNotNull(extensibleEntity.string);
        Assert.assertNotNull(entity.set);
        Assert.assertNotNull(entity.list);
        Assert.assertNotNull(entity.string);
    }

    @Test
    public void testNormalize() {
        TestEntityWithVariousCollections entity = new TestEntityWithVariousCollections();
        entity.fillCollections();
        EntityHelper.normalize(entity);

        assertCollectionTypes(entity);
        AssertUtils.assertEquals("list", entity.list, "foo", "bar", "hugo");
        AssertUtils.assertEquals("set", entity.set, "foo", "bar", "hugo");
        AssertUtils.assertEquals("map1", entity.map1.values(), "bar", "foo", "hugo");
        AssertUtils.assertEquals("map2", entity.map2.values(), "foo", "bar", "hugo");
        AssertUtils.assertEquals("vector", entity.vector, "foo", "bar", "hugo");

    }

    private void assertCollectionTypes(TestEntityWithVariousCollections entity) {
        Assert.assertEquals(ArrayList.class, entity.list.getClass());
        Assert.assertEquals(HashSet.class, entity.set.getClass());
        Assert.assertEquals(TreeSet.class, entity.set1.getClass());
        Assert.assertEquals(HashMap.class, entity.map1.getClass());
        Assert.assertEquals(TreeMap.class, entity.map2.getClass());
        Assert.assertEquals(ArrayList.class, entity.list1.getClass());
        Assert.assertEquals(TreeSet.class, entity.set2.getClass());
        Assert.assertEquals(HashSet.class, entity.set3.getClass());
        Assert.assertEquals(LinkedHashSet.class, entity.set4.getClass());
        Assert.assertEquals(HashMap.class, entity.map3.getClass());
        Assert.assertEquals(Vector.class, entity.vector.getClass());
        Assert.assertEquals(ConcurrentLinkedQueue.class, entity.queue.getClass());
    }
}
