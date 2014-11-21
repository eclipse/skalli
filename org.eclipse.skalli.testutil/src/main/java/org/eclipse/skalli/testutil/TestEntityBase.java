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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.model.PropertyName;
import org.junit.Assert;

@SuppressWarnings("nls")
public class TestEntityBase extends EntityBase {

    @PropertyName
    public static final String PROPERTY_BOOL = "bool";

    @PropertyName
    public static final String PROPERTY_STR = "str";

    @PropertyName
    public static final String PROPERTY_ITEMS = "items";

    private boolean bool;
    private String str = "";
    private ArrayList<String> items = new ArrayList<String>();

    public TestEntityBase() {
    }

    public TestEntityBase(UUID uuid) {
        setUuid(uuid);
    }

    public TestEntityBase(UUID uuid, UUID parentEntityId) {
        setUuid(uuid);
        setParentEntityId(parentEntityId);
    }

    public boolean isBool() {
        return bool;
    }

    public void setBool(boolean bool) {
        this.bool = bool;
    }

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> list) {
        items = new ArrayList<String>(list);
    }

    public void addItem(String item) {
        items.add(item);
    }

    public void removeItem(String item) {
        items.remove(item);
    }

    public boolean hasItem(String item) {
        return getItems().contains(item);
    }

    public static void assertEquals(EntityBase o1, EntityBase o2) {
        if (o1 == null) {
            Assert.assertNull("o1==0, but o2!=null", o2);
            return;
        }
        if (o2 == null) {
            Assert.fail("o2==null, but o1!=null");
        }
        Assert.assertTrue(o1 instanceof TestEntityBase);
        Assert.assertTrue(o2 instanceof TestEntityBase);
        Assert.assertEquals(o1, o2);
        Assert.assertEquals(((TestEntityBase)o1).isBool(), ((TestEntityBase)o2).isBool());
        Assert.assertEquals(((TestEntityBase)o1).getStr(), ((TestEntityBase)o2).getStr());
        AssertUtils.assertEquals("getItems", ((TestEntityBase)o1).getItems(), ((TestEntityBase)o2).getItems());
    }
}
