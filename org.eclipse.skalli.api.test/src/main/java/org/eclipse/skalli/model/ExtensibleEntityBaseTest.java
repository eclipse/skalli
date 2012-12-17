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
package org.eclipse.skalli.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.skalli.testutil.AssertUtils;
import org.eclipse.skalli.testutil.PropertyTestUtil;
import org.eclipse.skalli.testutil.TestUUIDs;
import org.eclipse.skalli.testutil.TestExtensibleEntityBase;
import org.eclipse.skalli.testutil.TestExtension;
import org.eclipse.skalli.testutil.TestExtensionEntityBase1;
import org.eclipse.skalli.testutil.TestExtensionEntityBase2;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("nls")
public class ExtensibleEntityBaseTest {

    @Test
    public void testPropertyDefinitions() throws Exception {
        Map<String, Object> values = PropertyTestUtil.getValues();
        Map<Class<?>, String[]> requiredProperties = PropertyTestUtil.getRequiredProperties();
        PropertyTestUtil.checkPropertyDefinitions(TestExtensibleEntityBase.class, requiredProperties, values);
    }

    @Test
    public void testExtension() {
        TestExtensibleEntityBase base = new TestExtensibleEntityBase(TestUUIDs.TEST_UUIDS[0]);
        Assert.assertNull(base.getExtension(TestExtensionEntityBase1.class));
        Assert.assertNull(base.getExtension(null));
        Assert.assertTrue(base.getAllExtensions().isEmpty());

        TestExtensionEntityBase1 ext1 = new TestExtensionEntityBase1();
        TestExtensionEntityBase2 ext2 = new TestExtensionEntityBase2();
        base.addExtension(ext1);
        base.addExtension(ext2);
        Assert.assertNotNull(base.getExtension(TestExtensionEntityBase1.class));
        Assert.assertNotNull(base.getExtension(TestExtensionEntityBase2.class));

        Collection<ExtensionEntityBase> c = base.getAllExtensions();
        Assert.assertEquals(2, c.size());
        Assert.assertTrue(c.contains(ext1));
        Assert.assertTrue(c.contains(ext2));

        base.addExtension(ext1);
        c = base.getAllExtensions();
        Assert.assertEquals(2, c.size());
        Assert.assertTrue(c.contains(ext1));
        Assert.assertTrue(c.contains(ext2));
        Iterator<ExtensionEntityBase> it = c.iterator();
        Assert.assertEquals(ext1, it.next());
        Assert.assertEquals(ext2, it.next());

        base.addExtension(null);
        c = base.getAllExtensions();
        Assert.assertEquals(2, c.size());
        Assert.assertTrue(c.contains(ext1));
        Assert.assertTrue(c.contains(ext2));

        base.removeExtension(TestExtensionEntityBase1.class);
        c = base.getAllExtensions();
        Assert.assertNull(base.getExtension(TestExtensionEntityBase1.class));
        Assert.assertEquals(1, c.size());
        Assert.assertTrue(c.contains(ext2));

        base.removeExtension(TestExtensionEntityBase2.class);
        Assert.assertNull(base.getExtension(TestExtensionEntityBase1.class));
        Assert.assertNull(base.getExtension(TestExtensionEntityBase2.class));
        Assert.assertTrue(base.getAllExtensions().isEmpty());
    }

    @Test
    public void testInheritedExtension() {
        TestExtensibleEntityBase base = new TestExtensibleEntityBase(TestUUIDs.TEST_UUIDS[0]);
        TestExtensibleEntityBase parent = new TestExtensibleEntityBase(TestUUIDs.TEST_UUIDS[1]);
        TestExtensibleEntityBase parentParent = new TestExtensibleEntityBase(TestUUIDs.TEST_UUIDS[2]);

        TestExtension ext1 = new TestExtension();
        ext1.setExtensibleEntity(base);
        ext1.setBool(true);
        ext1.setStr("Homer");
        List<String> list1 = Arrays.asList("A", "B");
        ext1.addItem(list1.get(0));
        ext1.addItem(list1.get(1));
        base.addExtension(ext1);

        TestExtension ext2 = new TestExtension();
        ext2.setExtensibleEntity(parent);
        ext2.setBool(false);
        ext2.setStr("Marge");
        List<String> list2 = Arrays.asList("C", "D");
        ext2.addItem(list2.get(0));
        ext2.addItem(list2.get(1));
        parent.addExtension(ext2);

        TestExtension ext3 = new TestExtension();
        ext3.setExtensibleEntity(parent);
        ext3.setBool(false);
        ext3.setStr("Lisa");
        List<String> list3 = Arrays.asList("E", "F");
        ext3.addItem(list3.get(0));
        ext3.addItem(list3.get(1));
        parentParent.addExtension(ext3);

        base.setParentEntity(parent);
        parent.setParentEntity(parentParent);

        // TestExtension.class is not inherited => expect ext1
        Assert.assertFalse(base.isInherited(TestExtension.class));
        Assert.assertFalse(parent.isInherited(TestExtension.class));
        TestExtension ext = base.getExtension(TestExtension.class);
        Assert.assertNotNull(ext);
        Assert.assertEquals(ext1, ext);
        Assert.assertEquals("Homer", ext.getStr());
        AssertUtils.assertEquals("", list1, ext.getItems());

        // setInherited(TestExtension.class) => expect ext2 from parent
        base.setInherited(TestExtension.class, true);
        Assert.assertTrue(base.isInherited(TestExtension.class));
        Assert.assertFalse(parent.isInherited(TestExtension.class));
        ext = base.getExtension(TestExtension.class);
        Assert.assertNotNull(ext);
        Assert.assertEquals(ext2, ext);
        Assert.assertEquals("Marge", ext.getStr());
        AssertUtils.assertEquals("", list2, ext.getItems());

        // remove ext2 from parent => expect null
        parent.removeExtension(TestExtension.class);
        Assert.assertTrue(base.isInherited(TestExtension.class));
        Assert.assertFalse(parent.isInherited(TestExtension.class));
        Assert.assertNull(base.getExtension(TestExtension.class));

        // switch parent to inheritance, too => expect ext3 from parentParent
        parent.setInherited(TestExtension.class, true);
        Assert.assertTrue(base.isInherited(TestExtension.class));
        Assert.assertTrue(parent.isInherited(TestExtension.class));
        ext = base.getExtension(TestExtension.class);
        Assert.assertNotNull(ext);
        Assert.assertEquals(ext3, ext);
        Assert.assertEquals("Lisa", ext.getStr());
        AssertUtils.assertEquals("", list3, ext.getItems());

        // remove ext3 from parentParent => expect null
        parentParent.removeExtension(TestExtension.class);
        Assert.assertTrue(base.isInherited(TestExtension.class));
        Assert.assertTrue(parent.isInherited(TestExtension.class));
        Assert.assertNull(base.getExtension(TestExtension.class));

        // add ext2 to parent again => inheritance switched off => expect ext2
        parent.addExtension(ext2);
        Assert.assertTrue(base.isInherited(TestExtension.class));
        Assert.assertFalse(parent.isInherited(TestExtension.class));
        ext = base.getExtension(TestExtension.class);
        Assert.assertNotNull(ext);
        Assert.assertEquals(ext2, ext);
        Assert.assertEquals("Marge", ext.getStr());
        AssertUtils.assertEquals("", list2, ext.getItems());

        // switch off inheritance => expect null
        base.setInherited(TestExtension.class, false);
        Assert.assertFalse(base.isInherited(TestExtension.class));
        Assert.assertFalse(parent.isInherited(TestExtension.class));
        Assert.assertNull(base.getExtension(TestExtension.class));

        // add ext1 to base again => expect ext1
        base.addExtension(ext1);
        Assert.assertFalse(base.isInherited(TestExtension.class));
        Assert.assertFalse(parent.isInherited(TestExtension.class));
        ext = base.getExtension(TestExtension.class);
        Assert.assertNotNull(ext);
        Assert.assertEquals(ext1, ext);
        Assert.assertEquals("Homer", ext.getStr());
        AssertUtils.assertEquals("", list1, ext.getItems());

        // switch parent to inheritance, again => expect ext1
        parent.setInherited(TestExtension.class, true);
        Assert.assertFalse(base.isInherited(TestExtension.class));
        Assert.assertTrue(parent.isInherited(TestExtension.class));
        ext = base.getExtension(TestExtension.class);
        Assert.assertNotNull(ext);
        Assert.assertEquals(ext1, ext);
        Assert.assertEquals("Homer", ext.getStr());
        AssertUtils.assertEquals("", list1, ext.getItems());
    }
}
