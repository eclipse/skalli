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
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang.time.DateFormatUtils;
import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.testutil.AssertUtils;
import org.eclipse.skalli.testutil.PropertyTestUtil;
import org.eclipse.skalli.testutil.TestEntityBase;
import org.eclipse.skalli.testutil.TestEntityBase1;
import org.eclipse.skalli.testutil.TestEntityBase2;
import org.eclipse.skalli.testutil.TestExtensibleEntityBase;
import org.eclipse.skalli.testutil.TestExtension;
import org.eclipse.skalli.testutil.TestUUIDs;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("nls")
public class EntityBaseTest {

    @Test
    public void testPropertyDefinitions() throws Exception {
        Map<String, Object> values = PropertyTestUtil.getValues();
        Map<Class<?>, String[]> requiredProperties = PropertyTestUtil.getRequiredProperties();
        PropertyTestUtil.checkPropertyDefinitions(EntityBase.class, requiredProperties, values);
    }

    @Test
    public void testSetUUIDTwice() {
        TestEntityBase entity = new TestEntityBase();
        entity.setUuid(TestUUIDs.TEST_UUIDS[0]);
        Assert.assertEquals(TestUUIDs.TEST_UUIDS[0], entity.getUuid());
        entity.setUuid(TestUUIDs.TEST_UUIDS[1]);
        Assert.assertEquals(TestUUIDs.TEST_UUIDS[0], entity.getUuid()); // still old UUID!
        entity.setUuid(null);
        Assert.assertEquals(TestUUIDs.TEST_UUIDS[0], entity.getUuid()); // still old UUID!
    }

    @Test
    public void testLastModified() {
        TestEntityBase entity = new TestEntityBase();
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.ENGLISH); //$NON-NLS-1$
        String lastModified = DatatypeConverter.printDateTime(now);
        entity.setLastModified(lastModified);
        Assert.assertEquals(lastModified, entity.getLastModified());
        Assert.assertEquals(now.getTimeInMillis(), entity.getLastModifiedMillis());
        entity.setLastModified(null);
        Assert.assertNull(entity.getLastModified());
        Assert.assertEquals(-1L, entity.getLastModifiedMillis());
        entity.setLastModified("");
        Assert.assertNull(entity.getLastModified());
        Assert.assertEquals(-1L, entity.getLastModifiedMillis());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidLastModified() {
        long now = System.currentTimeMillis();
        TestEntityBase entity = new TestEntityBase();
        entity.setLastModified(DateFormatUtils.ISO_DATE_FORMAT.format(now));
        entity.setLastModified(DateFormatUtils.ISO_DATE_TIME_ZONE_FORMAT.format(now));
        entity.setLastModified(DateFormatUtils.ISO_TIME_FORMAT.format(now));
        entity.setLastModified("foobar");
        entity.setLastModified(Long.toString(now));
    }

    public void testLastModifiedBy() {
        TestEntityBase entity = new TestEntityBase();
        entity.setLastModifiedBy("homer");
        Assert.assertEquals("homer", entity.getLastModifiedBy());
        entity.setLastModifiedBy(null);
        Assert.assertNull(entity.getLastModifiedBy());
        entity.setLastModifiedBy("");
        Assert.assertNull(entity.getLastModifiedBy());
    }

    @Test
    public void testSetResetParentEntity() {
        TestEntityBase1 entity1 = new TestEntityBase1();
        entity1.setUuid(TestUUIDs.TEST_UUIDS[0]);
        TestEntityBase2 entity2 = new TestEntityBase2();
        entity2.setUuid(TestUUIDs.TEST_UUIDS[1]);
        entity1.setParentEntity(entity2);
        Assert.assertEquals(entity2, entity1.getParentEntity());
        Assert.assertEquals(TestUUIDs.TEST_UUIDS[1], entity1.getParentEntityId());
        entity1.setParentEntity(null);
        Assert.assertNull(entity1.getParentEntity());
        Assert.assertNull(entity1.getParentEntityId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetParentWithMissingUUID() {
        TestEntityBase1 entity1 = new TestEntityBase1();
        entity1.setUuid(TestUUIDs.TEST_UUIDS[0]);
        TestEntityBase2 entity2 = new TestEntityBase2();
        entity1.setParentEntity(entity2);
    }

    @Test
    public void testGetChildren() throws Exception {
        EntityBase parent = new TestEntityBase(TestUUIDs.TEST_UUIDS[0]);
        EntityBase child1 = new TestEntityBase(TestUUIDs.TEST_UUIDS[1]);
        EntityBase child2 = new TestEntityBase(TestUUIDs.TEST_UUIDS[2]);
        EntityBase child3 = new TestEntityBase(TestUUIDs.TEST_UUIDS[3]);
        child1.setParentEntity(parent);
        child1.setNextSibling(child2);
        child2.setParentEntity(parent);
        child2.setNextSibling(child3);
        child3.setParentEntity(parent);
        parent.setFirstChild(child1);
        Assert.assertNull(parent.getNextSibling());
        Assert.assertNull(parent.getParentEntity());
        Assert.assertEquals(child1, parent.getFirstChild());
        Assert.assertEquals(parent, child1.getParentEntity());
        Assert.assertEquals(child2, child1.getNextSibling());
        Assert.assertEquals(parent, child2.getParentEntity());
        Assert.assertEquals(child3, child2.getNextSibling());
        Assert.assertNull(child3.getNextSibling());
        Assert.assertEquals(parent, child3.getParentEntity());
        List<EntityBase> expected = Arrays.asList(child1, child2, child3);
        AssertUtils.assertEquals("getChildren()", expected, parent.getChildren());
        parent.getChildren().set(0, new TestEntityBase(TestUUIDs.TEST_UUIDS[4]));
        AssertUtils.assertEquals("getChildren()", expected, parent.getChildren());
    }

    @Test
    public void testNoChildren() throws Exception {
        EntityBase parent = new TestEntityBase(TestUUIDs.TEST_UUIDS[0]);
        Assert.assertNull(parent.getFirstChild());
        Assert.assertNotNull(parent.getChildren());
        Assert.assertTrue(parent.getChildren().isEmpty());
    }

    @Test
    public void testGetPropertyNames() {
        TestExtensibleEntityBase base = new TestExtensibleEntityBase(TestUUIDs.TEST_UUIDS[0]);
        TestExtension ext = new TestExtension();
        ext.setExtensibleEntity(base);
        AssertUtils.assertEqualsAnyOrder("getPropertyNames",
                CollectionUtils.asSet("parentEntity", "str", "items", "parentEntityId",
                        "uuid", "bool", "deleted", "lastModified", "lastModifiedBy",
                        "firstChild", "nextSibling"),
                ext.getPropertyNames());
    }

    @Test
    public void testGetProperty() throws Exception {
        TestExtensibleEntityBase base = new TestExtensibleEntityBase(TestUUIDs.TEST_UUIDS[0]);
        TestExtension extBase = new TestExtension();
        extBase.setExtensibleEntity(base);
        extBase.setBool(true);
        extBase.setStr("Homer");
        List<String> list = Arrays.asList("Marge", "Lisa", "Bart");
        extBase.setItems(list);
        Assert.assertEquals(Boolean.TRUE, (Boolean) extBase.getProperty(TestExtension.PROPERTY_BOOL));
        Assert.assertEquals("Homer", (String) extBase.getProperty(TestExtension.PROPERTY_STR));
        AssertUtils.assertEquals("List", list, (List<String>) extBase.getProperty(TestExtension.PROPERTY_ITEMS));
    }

    @Test
    public void testGetPropertyWithNullValue() throws Exception {
        TestExtensibleEntityBase base = new TestExtensibleEntityBase(TestUUIDs.TEST_UUIDS[0]);
        TestExtension extBase = new TestExtension();
        extBase.setExtensibleEntity(base);
        extBase.setStr(null);
        Assert.assertNull(extBase.getProperty(TestExtension.PROPERTY_STR));
    }

    @Test(expected = NoSuchPropertyException.class)
    public void testGetUnknownProperty() throws Exception {
        TestExtensibleEntityBase base = new TestExtensibleEntityBase(TestUUIDs.TEST_UUIDS[0]);
        base.getProperty("foobar");
    }

    @Test
    public void testSetProperty() throws Exception {
        TestExtensibleEntityBase base = new TestExtensibleEntityBase(TestUUIDs.TEST_UUIDS[0]);
        TestExtension extBase = new TestExtension();
        extBase.setExtensibleEntity(base);
        extBase.setProperty(TestExtension.PROPERTY_BOOL, true);
        extBase.setProperty(TestExtension.PROPERTY_STR, "Homer");
        List<String> list = Arrays.asList("Marge", "Lisa", "Bart");
        extBase.setProperty(TestExtension.PROPERTY_ITEMS, list);
        Assert.assertEquals(true, extBase.isBool());
        Assert.assertEquals("Homer", extBase.getStr());
        AssertUtils.assertEquals("List", list, extBase.getItems());
    }

    @Test
    public void testNullValue() throws Exception {
        TestExtensibleEntityBase base = new TestExtensibleEntityBase(TestUUIDs.TEST_UUIDS[0]);
        TestExtension extBase = new TestExtension();
        extBase.setExtensibleEntity(base);
        extBase.setProperty(TestExtension.PROPERTY_STR, "Homer");
        Assert.assertNotNull(extBase.getStr());
        extBase.setProperty(TestExtension.PROPERTY_STR, null);
        Assert.assertNull(extBase.getStr());
    }

    @Test(expected = NoSuchPropertyException.class)
    public void testSetUnknownProperty() throws Exception {
        TestExtensibleEntityBase base = new TestExtensibleEntityBase(TestUUIDs.TEST_UUIDS[0]);
        base.setProperty("foo", "bar");
    }

    @Test(expected = PropertyUpdateException.class)
    public void testSetPrimitivePropertyToNull() throws Exception {
        TestExtensibleEntityBase base = new TestExtensibleEntityBase(TestUUIDs.TEST_UUIDS[0]);
        TestExtension extBase = new TestExtension();
        extBase.setExtensibleEntity(base);
        extBase.setProperty(TestExtension.PROPERTY_BOOL, null);
    }

    @Test(expected = PropertyUpdateException.class)
    public void testSetValueWithIncompatibleType() throws Exception {
        TestExtensibleEntityBase base = new TestExtensibleEntityBase(TestUUIDs.TEST_UUIDS[0]);
        TestExtension extBase = new TestExtension();
        extBase.setExtensibleEntity(base);
        extBase.setProperty(TestExtension.PROPERTY_BOOL, new String[]{});
    }

    @Test(expected = PropertyUpdateException.class)
    public void testSetterThrowsException() throws Exception {
        TestExtensibleEntityBase base = new TestExtensibleEntityBase(TestUUIDs.TEST_UUIDS[0]);
        TestExtension extBase = new TestExtension();
        extBase.setExtensibleEntity(base);
        extBase.setProperty(TestExtension.PROPERTY_ITEMS, null);
    }

    @Test
    public void testToString() {
        TestEntityBase entity = new TestEntityBase();
        entity.setUuid(TestUUIDs.TEST_UUIDS[0]);
        Assert.assertEquals(TestUUIDs.TEST_UUIDS[0].toString(), entity.toString());
    }

    @Test
    public void testHashCode() {
        TestEntityBase entity = new TestEntityBase();
        entity.setUuid(TestUUIDs.TEST_UUIDS[0]);
        Assert.assertEquals(TestUUIDs.TEST_UUIDS[0].hashCode(), entity.hashCode());
    }

    @Test
    public void testEquals() {
        TestEntityBase entity1 = new TestEntityBase();
        entity1.setUuid(TestUUIDs.TEST_UUIDS[0]);
        TestEntityBase entity2 = new TestEntityBase();
        entity2.setUuid(TestUUIDs.TEST_UUIDS[0]);
        TestEntityBase entity3 = new TestEntityBase();
        entity3.setUuid(TestUUIDs.TEST_UUIDS[1]);

        Assert.assertTrue(entity1.equals(entity1));

        Assert.assertTrue(entity1.equals(entity2));
        Assert.assertTrue(entity2.equals(entity1));

        Assert.assertFalse(entity1.equals(entity3));
        Assert.assertFalse(entity3.equals(entity1));

        Assert.assertFalse(entity1.equals(TestUUIDs.TEST_UUIDS[1]));
    }
}
