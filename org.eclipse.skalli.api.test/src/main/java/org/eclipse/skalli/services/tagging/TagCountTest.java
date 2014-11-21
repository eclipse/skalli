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
package org.eclipse.skalli.services.tagging;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

@SuppressWarnings("nls")
public class TagCountTest {

    @Test
    public void testValidTagCounts() throws Exception {
        TagCount t = new TagCount("foobar", 4711);
        assertTagCount(t, "foobar", 4711);
        t = new TagCount("foobar", 0);
        assertTagCount(t, "foobar", 0);
    }

    @Test
    public void testInvalidTagCounts() throws Exception {
        assertInvalidTagCount(null, 4711);
        assertInvalidTagCount("", 4711);
        assertInvalidTagCount("  \t", 4711);
        assertInvalidTagCount("foobar", -1);
    }

    @Test
    public void testCompare() throws Exception {
        assertLess("zzz", 4, "aaa", 3); //sorted by count descending!
        assertLess("aaa", 4, "zzz", 3); //sorted by count descending!
        assertLess("aaa", 3, "zzz", 3); // sorted alphanumerically!
        assertLess("ZZZ", 3, "aaa", 3); // sorted alphanumerically!
        assertLess("4711", 3, "ZZZ", 3); // sorted alphanumerically!
        assertEqualTagCounts("aaa", 3, "aaa", 3);
    }

    @Test
    public void testAsMap() throws Exception {
        List<TagCount> tags = Arrays.asList(
                new TagCount("zzz", 4),
                new TagCount("aaa", 3),
                new TagCount("foo", 1),
                new TagCount("bar", 5));
        Map<String,Integer> map = TagCount.asMap(tags);
        assertNotNull(map);
        assertEquals(4, map.size());
        Iterator<Entry<String,Integer>> it = map.entrySet().iterator();
        assertTagCount(it.next(), "aaa", 3);
        assertTagCount(it.next(), "bar", 5);
        assertTagCount(it.next(), "foo", 1);
        assertTagCount(it.next(), "zzz", 4);
    }

    @Test
    public void testAsMapEmptyCollection() throws Exception {
        Map<String,Integer> map = TagCount.asMap(Collections.<TagCount>emptyList());
        assertNotNull(map);
        assertTrue(map.isEmpty());
        map = TagCount.asMap(null);
        assertNotNull(map);
        assertTrue(map.isEmpty());
    }

    private void assertLess(String name1, int count1, String name2, int count2) {
        TagCount less = new TagCount(name1, count1);
        TagCount greater = new TagCount(name2, count2);
        assertTrue(less.compareTo(greater) < 0);
        assertTrue(greater.compareTo(less) > 0);
    }

    private void assertEqualTagCounts(String name1, int count1, String name2, int count2) {
        TagCount t1 = new TagCount(name1, count1);
        TagCount t2 = new TagCount(name2, count2);
        assertEquals(0, t1.compareTo(t2));
        assertEquals(0, t2.compareTo(t1));
        assertTrue(t1.equals(t2));
        assertTrue(t2.equals(t1));
    }

    private void assertTagCount(TagCount next, String name, int count) {
        assertEquals(name, next.getName());
        assertEquals(count, next.getCount());
    }

    private void assertTagCount(Entry<String,Integer> next, String name, int count) {
        assertEquals(name, next.getKey());
        assertEquals(count, (int)next.getValue());
    }

    private void assertInvalidTagCount(String name, int count) {
        try {
            new TagCount(name, count);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

}

