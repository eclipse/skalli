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
import java.util.List;
import java.util.Map;

import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.testutil.AssertUtils;
import org.eclipse.skalli.testutil.PropertyHelper;
import org.eclipse.skalli.testutil.PropertyHelperUtils;
import org.eclipse.skalli.testutil.TestExtensibleEntityBase;
import org.eclipse.skalli.testutil.TestExtension;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("nls")
public class ExtensionEntityBaseTest {

    @Test
    public void testPropertyDefinitions() throws Exception {
        Map<String, Object> values = PropertyHelperUtils.getValues();
        values.put(TestExtension.PROPERTY_BOOL, true);
        values.put(TestExtension.PROPERTY_STR, "string");
        values.put(TestExtension.PROPERTY_ITEMS, Arrays.asList("item1", "item2", "item3"));
        Map<Class<?>, String[]> requiredProperties = PropertyHelperUtils.getRequiredProperties();
        PropertyHelper.checkPropertyDefinitions(TestExtension.class, requiredProperties, values);
    }

    @Test
    public void testGetSetExtensibleEntity() {
        TestExtensibleEntityBase base = new TestExtensibleEntityBase(PropertyHelperUtils.TEST_UUIDS[0]);
        TestExtension ext = new TestExtension();
        ext.setExtensibleEntity(base);
        Assert.assertEquals(base, ext.getExtensibleEntity());
    }

    @Test
    public void testGetPropertyNames() {
        TestExtensibleEntityBase base = new TestExtensibleEntityBase(PropertyHelperUtils.TEST_UUIDS[0]);
        TestExtension ext = new TestExtension();
        ext.setExtensibleEntity(base);
        AssertUtils.assertEqualsAnyOrder("getPropertyNames",
                CollectionUtils.asSet("parentEntity", "str", "items", "parentEntityId",
                        "uuid", "bool", "deleted", "lastModified", "lastModifiedBy"),
                ext.getPropertyNames());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetProperty() throws Exception {
        TestExtensibleEntityBase base = new TestExtensibleEntityBase(PropertyHelperUtils.TEST_UUIDS[0]);
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

}
