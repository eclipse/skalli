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
package org.eclipse.skalli.model.ext.mapping;

import org.eclipse.skalli.model.ext.linkgroups.OrderableGroup;
import org.junit.Assert;
import org.junit.Test;

public class OrderableGroupTest {

    @Test
    @SuppressWarnings("nls")
    public void testOrderableGroup() throws Exception {
        OrderableGroup<String> orderableGroup = new OrderableGroup<String>();
        Assert.assertTrue(orderableGroup.getItems().isEmpty());

        Assert.assertTrue(orderableGroup.add("a"));
        Assert.assertTrue(orderableGroup.add("b"));
        Assert.assertTrue(orderableGroup.add("c"));
        Assert.assertEquals(3, orderableGroup.getItems().size());

        Assert.assertFalse(orderableGroup.add("c"));

        Assert.assertTrue(orderableGroup.remove("c"));
        Assert.assertEquals(2, orderableGroup.getItems().size());

        Assert.assertTrue(orderableGroup.moveUp("b"));
        Assert.assertFalse(orderableGroup.moveUp("b"));

        Assert.assertTrue(orderableGroup.moveDown("b"));
        Assert.assertFalse(orderableGroup.moveDown("b"));

        Assert.assertTrue(orderableGroup.hasItem("a"));
        Assert.assertTrue(orderableGroup.hasItem("b"));
        Assert.assertFalse(orderableGroup.hasItem("c"));
    }
}
