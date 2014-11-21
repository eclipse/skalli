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
package org.eclipse.skalli.commons;

import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("nls")
public class UUIDUtilsTest {

    @Test
    public void testIsUUID() {
        Assert.assertTrue(UUIDUtils.isUUID("5856b08a-0f87-4d91-b007-ac367ced247a"));
        Assert.assertFalse(UUIDUtils.isUUID("foobar"));
        Assert.assertFalse(UUIDUtils.isUUID(Long.toString(System.currentTimeMillis())));
        Assert.assertFalse(UUIDUtils.isUUID(null));
        Assert.assertFalse(UUIDUtils.isUUID(""));
    }

    @Test
    public void testAsUUID() {
        Assert.assertNotNull(UUIDUtils.asUUID("5856b08a-0f87-4d91-b007-ac367ced247a"));
        Assert.assertEquals("5856b08a-0f87-4d91-b007-ac367ced247a",
                UUIDUtils.asUUID("5856b08a-0f87-4d91-b007-ac367ced247a").toString());

        Assert.assertNotNull(UUIDUtils.asUUID("foobar"));
        Assert.assertFalse("foobar".equals(UUIDUtils.asUUID("foobar").toString()));

        Assert.assertNotNull(UUIDUtils.asUUID(null));
        Assert.assertNotNull(UUIDUtils.asUUID(""));
    }
}
