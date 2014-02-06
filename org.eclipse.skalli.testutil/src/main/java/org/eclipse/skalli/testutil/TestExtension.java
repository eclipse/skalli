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
package org.eclipse.skalli.testutil;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.PropertyName;

@SuppressWarnings("nls")
public class TestExtension extends ExtensionEntityBase {

    @PropertyName
    public static final String PROPERTY_BOOL = "bool";

    @PropertyName
    public static final String PROPERTY_STR = "str";

    @PropertyName
    public static final String PROPERTY_ITEMS = "items";

    private boolean bool;
    private String str = "";
    private ArrayList<String> items = new ArrayList<String>();
    private String unpublished;

    public TestExtension() {
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

    // getter with no corresponding @PropertyName constant
    public String getUnpublished() {
        return unpublished;
    }

    public void setUnpublished(String unpublished) {
        this.unpublished = unpublished;
    }

    public static boolean assertEquals(TestExtension o1, TestExtension o2) {
        boolean equals = true;
        equals &= o1.isBool() == o2.isBool();
        equals &= o1.getStr().equals(o2.getStr());
        for (int i = 0; i < o1.getItems().size(); ++i) {
            equals &= o1.getItems().get(i).equals(o2.getItems().get(i));
        }
        return equals;
    }
}
