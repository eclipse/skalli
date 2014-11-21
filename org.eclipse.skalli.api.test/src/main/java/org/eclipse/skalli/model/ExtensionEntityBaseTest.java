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
package org.eclipse.skalli.model;

import java.util.Arrays;
import java.util.Map;

import org.eclipse.skalli.testutil.PropertyTestUtil;
import org.eclipse.skalli.testutil.TestExtensibleEntityBase;
import org.eclipse.skalli.testutil.TestExtension;
import org.eclipse.skalli.testutil.TestUUIDs;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("nls")
public class ExtensionEntityBaseTest {

    @Test
    public void testPropertyDefinitions() throws Exception {
        Map<String, Object> values = PropertyTestUtil.getValues();
        values.put(TestExtension.PROPERTY_BOOL, true);
        values.put(TestExtension.PROPERTY_STR, "string");
        values.put(TestExtension.PROPERTY_ITEMS, Arrays.asList("item1", "item2", "item3"));
        Map<Class<?>, String[]> requiredProperties = PropertyTestUtil.getRequiredProperties();
        PropertyTestUtil.checkPropertyDefinitions(TestExtension.class, requiredProperties, values);
    }

    @Test
    public void testGetSetExtensibleEntity() {
        TestExtensibleEntityBase base = new TestExtensibleEntityBase(TestUUIDs.TEST_UUIDS[0]);
        TestExtension ext = new TestExtension();
        ext.setExtensibleEntity(base);
        Assert.assertEquals(base, ext.getExtensibleEntity());
    }
}
