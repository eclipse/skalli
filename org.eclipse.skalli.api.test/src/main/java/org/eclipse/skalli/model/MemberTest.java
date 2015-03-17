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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Map;

import org.eclipse.skalli.testutil.PropertyTestUtil;
import org.junit.Test;

@SuppressWarnings("nls")
public class MemberTest {

    @Test
    public void testPropertyDefinitions() throws Exception {
        Map<String, Object> values = PropertyTestUtil.getValues();
        values.put(Member.PROPERTY_USERID, "homer");

        Map<Class<?>, String[]> requiredProperties = PropertyTestUtil.getRequiredProperties();
        requiredProperties.put(Member.class, new String[] { Member.PROPERTY_USERID });

        PropertyTestUtil.checkPropertyDefinitions(Member.class, requiredProperties, values);
    }

    @Test
    public void testUserIdToLowercase() throws Exception {
        assertEquals("a", new Member("a").getUserID());
        assertEquals("a", new Member("A").getUserID());
        assertNull(new Member(null).getUserID());
        Member member = new Member(null);
        member.setUserID("A");
        assertEquals("a", member.getUserID());
    }

    @Test
    public void testCompare() throws Exception {
        assertCompareTo(0, "a", "a");
        assertCompareTo(0, "a", "A");
        assertCompareTo(0, "A", "a");
        assertCompareTo(0, "A", "A");
        assertCompareTo(-1, "a", "b");
        assertCompareTo(-1, "a", "B");
        assertCompareTo(-1, "A", "b");
        assertCompareTo(-1, "A", "B");
        assertCompareTo(1, "b", "a");
        assertCompareTo(1, "b", "A");
        assertCompareTo(1, "B", "a");
        assertCompareTo(1, "B", "A");
        assertCompareTo(-1, null, "a");
        assertCompareTo(-1, null, "A");
        assertCompareTo(1, "a", null);
        assertCompareTo(1, "A", null);
        assertCompareTo(0, null, null);
    }

    private void assertCompareTo(int expected, String left, String right) {
        assertEquals(expected, new Member(left).compareTo(new Member(right)));
    }
}
