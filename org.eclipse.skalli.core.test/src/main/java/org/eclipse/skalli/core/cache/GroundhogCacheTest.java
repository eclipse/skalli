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
package org.eclipse.skalli.core.cache;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.skalli.core.cache.GroundhogCache;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("nls")
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
public class GroundhogCacheTest {

    static class TestGroundhogCache extends GroundhogCache<String, String> {
        long time;

        public TestGroundhogCache(int maxSize) {
            super(maxSize);
        }

        @Override
        Calendar getCalendar() {
            Calendar cal = super.getCalendar();
            cal.setTimeInMillis(time);
            return cal;
        }

    }

    @Test
    public void testGroundhogEffect_accessAll() {
        TestGroundhogCache cache = new TestGroundhogCache(100);
        cache.time = new Date().getTime();

        // put an entry into the cache
        cache.put("key1", "value1");
        Assert.assertEquals(1, cache.values().size());
        Assert.assertEquals("value1", cache.get("key1"));

        // now change the date and verify there is nothing in anymore
        cache.time += 24 * 60 * 60 * 1000;
        Assert.assertEquals(0, cache.values().size());
    }

    @Test
    public void testGroundhogEffect_accessSingle() {
        TestGroundhogCache cache = new TestGroundhogCache(100);
        cache.time = new Date().getTime();

        // put an entry into the cache
        cache.put("key1", "value1");
        Assert.assertEquals(1, cache.values().size());
        Assert.assertEquals("value1", cache.get("key1"));

        // now change the date and verify there is nothing in anymore
        cache.time += 24 * 60 * 60 * 1000;
        Assert.assertNull(cache.get("key1"));
    }

    @Test
    public void testLRUEffect_write() {
        TestGroundhogCache cache = new TestGroundhogCache(2);
        cache.time = new Date().getTime();

        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.put("key3", "value3");
        Assert.assertEquals(2, cache.values().size());
        Assert.assertEquals("value2", cache.get("key2"));
        Assert.assertEquals("value3", cache.get("key3"));
        Assert.assertNull(cache.get("key1"));
    }

    @Test
    public void testLRUEffect_read() {
        TestGroundhogCache cache = new TestGroundhogCache(2);
        cache.time = new Date().getTime();

        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.get("key1");
        cache.put("key3", "value3");
        Assert.assertEquals(2, cache.values().size());
        Assert.assertEquals("value1", cache.get("key1"));
        Assert.assertEquals("value3", cache.get("key3"));
        Assert.assertNull(cache.get("key2"));
    }

}
