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
package org.eclipse.skalli.services.permit;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.skalli.services.permit.Permit.Level;
import org.junit.Test;

@SuppressWarnings("nls")
public class PermitSetTest {

    private static final Permit[] PERMITS = {
            new Permit(Level.ALLOW, Permit.ACTION_GET, "/"),
            new Permit(Level.FORBID, Permit.ACTION_GET, "/"),
            new Permit(Level.ALLOW, Permit.ACTION_PUT, "/"),
            new Permit(Level.ALLOW, Permit.ACTION_PUT, "/foobar")};

    @Test
    public void testAdd() throws Exception {
        PermitSet set = new PermitSet();
        assertEquals(0, set.size());
        assertNotNull(set.permits());

        boolean modified = set.add(PERMITS[0]);
        assertTrue(modified);
        assertEquals(1, set.size());
        assertTrue(set.contains(PERMITS[0]));
        assertEquals(PERMITS[0], set.iterator().next());
        assertPermitSet(set, PERMITS[0]);

        // assert that add() replaces permits with same action and
        // path (permits[0] replaced by permits[1]!)
        modified = set.add(PERMITS[1]);
        assertTrue(modified);
        assertPermitSet(set, PERMITS[1]);

        // assert that add() does not replace permits with different action
        modified = set.add(PERMITS[2]);
        assertTrue(modified);
        assertPermitSet(set, PERMITS[1], PERMITS[2]);

        // assert that add() does not replace permits with different paths
        modified = set.add(PERMITS[3]);
        assertTrue(modified);
        assertPermitSet(set, PERMITS[3], PERMITS[1], PERMITS[2]);

        modified = set.add(null);
        assertFalse(modified);
        assertPermitSet(set, PERMITS[3], PERMITS[1], PERMITS[2]);
    }

    @Test
    public void testAddAll() throws Exception {
        PermitSet set = new PermitSet();
        set.add(PERMITS[2]);

        PermitSet set1 = new PermitSet(PERMITS[1], PERMITS[3]);
        assertPermitSet(set1, PERMITS[3], PERMITS[1]);
        boolean modified = set.addAll(set1);
        assertTrue(modified);
        assertPermitSet(set, PERMITS[3], PERMITS[1], PERMITS[2]);

        // add same set twice => modified = false
        modified = set.addAll(set1);
        assertFalse(modified);
        assertPermitSet(set, PERMITS[3], PERMITS[1], PERMITS[2]);

        PermitSet set2 = new PermitSet(Arrays.asList(PERMITS[0], PERMITS[3]));
        modified = set.addAll(set2);
        assertTrue(modified);
        assertPermitSet(set, PERMITS[3], PERMITS[0], PERMITS[2]);

        modified = set.addAll(Arrays.asList(PERMITS[0], PERMITS[3]));
        assertFalse(modified);
        assertPermitSet(set, PERMITS[3], PERMITS[0], PERMITS[2]);

        modified = set.addAll((PermitSet)null);
        assertFalse(modified);
        assertPermitSet(set, PERMITS[3], PERMITS[0], PERMITS[2]);

        modified = set.addAll((Collection<Permit>)null);
        assertFalse(modified);
        assertPermitSet(set, PERMITS[3], PERMITS[0], PERMITS[2]);

        modified = set.addAll(new ArrayList<Permit>());
        assertFalse(modified);
        assertPermitSet(set, PERMITS[3], PERMITS[0], PERMITS[2]);
    }

    @Test
    public void testRemove() throws Exception {
        PermitSet set = new PermitSet(PERMITS);
        assertPermitSet(set, PERMITS[3], PERMITS[1], PERMITS[2]);

        boolean modified = set.remove(PERMITS[1]);
        assertTrue(modified);
        assertPermitSet(set, PERMITS[3], PERMITS[2]);

        // remove uses Permit.equals(), which only checks action
        // and path => removing PERMITS[0] will actually remove PERMITS[1]
        set.add(PERMITS[1]);
        assertPermitSet(set, PERMITS[3], PERMITS[1], PERMITS[2]);
        modified = set.remove(PERMITS[0]);
        assertTrue(modified);
        assertPermitSet(set, PERMITS[3], PERMITS[2]);

        modified = set.remove(null);
        assertFalse(modified);
        assertPermitSet(set, PERMITS[3], PERMITS[2]);

        modified = set.remove("foobar");
        assertFalse(modified);
        assertPermitSet(set, PERMITS[3], PERMITS[2]);

        modified = set.remove(PERMITS[1]);
        assertFalse(modified);
    }

    @Test
    public void testRemoveAll() throws Exception {
        PermitSet set = new PermitSet(PERMITS);
        assertPermitSet(set, PERMITS[3], PERMITS[1], PERMITS[2]);

        boolean modified = set.removeAll(Arrays.asList(PERMITS[3], PERMITS[1]));
        assertTrue(modified);
        assertPermitSet(set, PERMITS[2]);

        // removeAll uses Permit.equals(), which only checks action
        // and path => removing PERMITS[0] will actually remove PERMITS[1]
        set.add(PERMITS[1]);
        assertPermitSet(set, PERMITS[1], PERMITS[2]);
        modified = set.removeAll(Arrays.asList(PERMITS[0]));
        assertTrue(modified);
        assertPermitSet(set, PERMITS[2]);

        modified = set.removeAll(Collections.emptyList());
        assertFalse(modified);
        assertPermitSet(set, PERMITS[2]);

        modified = set.removeAll(null);
        assertFalse(modified);
        assertPermitSet(set, PERMITS[2]);

        modified = set.removeAll(Arrays.asList("foo", "bar"));
        assertFalse(modified);
        assertPermitSet(set, PERMITS[2]);

        modified = set.removeAll(Arrays.asList(PERMITS[3], PERMITS[1]));
        assertFalse(modified);

        modified = set.removeAll(Arrays.asList(PERMITS[2]));
        assertTrue(modified);
        assertTrue(set.isEmpty());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRetainAll() throws Exception {
        PermitSet set = new PermitSet(PERMITS);
        set.retainAll(Arrays.asList(PERMITS[2]));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnmodifiableGetPermits() {
        PermitSet set = new PermitSet(PERMITS);
        Set<Permit> permits = set.permits();
        permits.add(PERMITS[3]);
    }

    private void assertPermitSet(PermitSet set, Permit...expected) {
        assertEquals(expected.length, set.size());
        int i = 0;
        Iterator<Permit> it = set.iterator();
        while (it.hasNext()) {
            Permit next = it.next();
            assertEquals(expected[i].getLevel(), next.getLevel());
            assertEquals(expected[i].getPath(), next.getPath());
            assertEquals(expected[i].getAction(), next.getAction());
            ++i;
        }
    }
}
