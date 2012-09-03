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
package org.eclipse.skalli.core.services.permit;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.skalli.services.permit.Permit;
import org.eclipse.skalli.testutil.PropertyHelperUtils;
import org.junit.Test;

@SuppressWarnings("nls")
public class PermitsConfigTest {

    @Test
    public void testEmptyPermits() throws Exception {
        PermitsConfig permits = new PermitsConfig();
        assertNotNull(permits.getPermits());
        assertTrue(permits.getPermits().isEmpty());
    }

    @Test
    public void testAdd() throws Exception {
        PermitsConfig permits = new PermitsConfig();

        PermitConfig permit = getPermit(PropertyHelperUtils.TEST_UUIDS[0], -1, null);
        permits.add(permit);
        assertEquals(1, permits.getPermits().size());
        assertEquals(permit, permits.get(PropertyHelperUtils.TEST_UUIDS[0]));

        permit = getPermit(PropertyHelperUtils.TEST_UUIDS[0], -1, "/projects");
        permits.add(permit);
        assertEquals(1, permits.getPermits().size());
        assertEquals(permit, permits.get(PropertyHelperUtils.TEST_UUIDS[0]));
        assertEquals("/projects", permits.get(PropertyHelperUtils.TEST_UUIDS[0]).getPath());

        permit = getPermit(PropertyHelperUtils.TEST_UUIDS[1], -1, null);
        permits.add(permit);
        assertEquals(2, permits.getPermits().size());
        assertEquals(PropertyHelperUtils.TEST_UUIDS[0], permits.getPermits().get(0).getUuid());
        assertEquals(PropertyHelperUtils.TEST_UUIDS[1], permits.getPermits().get(1).getUuid());

        permit = getPermit(PropertyHelperUtils.TEST_UUIDS[1], 0, null);
        permits.add(permit);
        assertEquals(2, permits.getPermits().size());
        assertEquals(PropertyHelperUtils.TEST_UUIDS[1], permits.getPermits().get(0).getUuid());
        assertEquals(PropertyHelperUtils.TEST_UUIDS[0], permits.getPermits().get(1).getUuid());

        permit = getPermit(PropertyHelperUtils.TEST_UUIDS[2], 1, null);
        permits.add(permit);
        assertEquals(3, permits.getPermits().size());
        assertEquals(PropertyHelperUtils.TEST_UUIDS[1], permits.getPermits().get(0).getUuid());
        assertEquals(PropertyHelperUtils.TEST_UUIDS[2], permits.getPermits().get(1).getUuid());
        assertEquals(PropertyHelperUtils.TEST_UUIDS[0], permits.getPermits().get(2).getUuid());
    }

    @Test
    public void testGet() throws Exception {
        PermitsConfig permits = new PermitsConfig();
        PermitConfig permit = getPermit(PropertyHelperUtils.TEST_UUIDS[0], -1, null);
        permits.add(permit);
        assertEquals(permit, permits.get(PropertyHelperUtils.TEST_UUIDS[0]));
        assertNull(permits.get(PropertyHelperUtils.TEST_UUIDS[1]));
        assertNull(permits.get(null));
    }

    @Test
    public void testRemove() throws Exception {
        PermitsConfig permits = new PermitsConfig();
        assertNull(permits.remove(PropertyHelperUtils.TEST_UUIDS[0]));
        permits.add(getPermit(PropertyHelperUtils.TEST_UUIDS[0], -1, null));
        permits.add(getPermit(PropertyHelperUtils.TEST_UUIDS[1], -1, null));
        permits.add(getPermit(PropertyHelperUtils.TEST_UUIDS[2], -1, null));
        assertEquals(3, permits.getPermits().size());
        assertEquals(PropertyHelperUtils.TEST_UUIDS[0], permits.getPermits().get(0).getUuid());
        assertEquals(PropertyHelperUtils.TEST_UUIDS[1], permits.getPermits().get(1).getUuid());
        assertEquals(PropertyHelperUtils.TEST_UUIDS[2], permits.getPermits().get(2).getUuid());
        PermitConfig stored = permits.remove(PropertyHelperUtils.TEST_UUIDS[1]);
        assertEquals(2, permits.getPermits().size());
        assertEquals(PropertyHelperUtils.TEST_UUIDS[1], stored.getUuid());
        assertEquals(PropertyHelperUtils.TEST_UUIDS[0], permits.getPermits().get(0).getUuid());
        assertEquals(PropertyHelperUtils.TEST_UUIDS[2], permits.getPermits().get(1).getUuid());
        stored = permits.remove(PropertyHelperUtils.TEST_UUIDS[1]);
        assertEquals(2, permits.getPermits().size());
        assertNull(stored);
        assertEquals(PropertyHelperUtils.TEST_UUIDS[0], permits.getPermits().get(0).getUuid());
        assertEquals(PropertyHelperUtils.TEST_UUIDS[2], permits.getPermits().get(1).getUuid());
        stored = permits.remove(PropertyHelperUtils.TEST_UUIDS[2]);
        assertEquals(PropertyHelperUtils.TEST_UUIDS[2], stored.getUuid());
        assertNull(permits.remove(PropertyHelperUtils.TEST_UUIDS[10]));
        stored = permits.remove(PropertyHelperUtils.TEST_UUIDS[0]);
        assertEquals(PropertyHelperUtils.TEST_UUIDS[0], stored.getUuid());
        assertTrue(permits.getPermits().isEmpty());
        assertNull(permits.remove(PropertyHelperUtils.TEST_UUIDS[0]));
        assertNull(permits.remove(null));
    }

    @Test
    public void testSet() throws Exception {
        PermitsConfig permits = new PermitsConfig();
        permits.add(getPermit(PropertyHelperUtils.TEST_UUIDS[0], -1, null));
        permits.add(getPermit(PropertyHelperUtils.TEST_UUIDS[1], -5, null)); // all negatives are treated as pso undefined
        permits.add(getPermit(PropertyHelperUtils.TEST_UUIDS[2], -1, null));
        assertEquals(PropertyHelperUtils.TEST_UUIDS[1], permits.getPermits().get(1).getUuid());

        List<PermitConfig> c = Arrays.asList(
                getPermit(PropertyHelperUtils.TEST_UUIDS[3], -1, null),
                getPermit(PropertyHelperUtils.TEST_UUIDS[4], -1, null),
                getPermit(PropertyHelperUtils.TEST_UUIDS[5], 0, null),
                getPermit(PropertyHelperUtils.TEST_UUIDS[1], 2, null));
        permits.setPermits(c);
        assertEquals(4, permits.getPermits().size());
        assertEquals(PropertyHelperUtils.TEST_UUIDS[5], permits.getPermits().get(0).getUuid());
        assertEquals(PropertyHelperUtils.TEST_UUIDS[3], permits.getPermits().get(1).getUuid());
        assertEquals(PropertyHelperUtils.TEST_UUIDS[1], permits.getPermits().get(2).getUuid());
        assertEquals(PropertyHelperUtils.TEST_UUIDS[4], permits.getPermits().get(3).getUuid());

        List<PermitConfig> d = Arrays.asList(
                getPermit(PropertyHelperUtils.TEST_UUIDS[5], 0, null),
                getPermit(PropertyHelperUtils.TEST_UUIDS[1], 2, null)); // index out of bounds => add at end
        permits.setPermits(d);
        assertEquals(2, permits.getPermits().size());
        assertEquals(PropertyHelperUtils.TEST_UUIDS[5], permits.getPermits().get(0).getUuid());
        assertEquals(PropertyHelperUtils.TEST_UUIDS[1], permits.getPermits().get(1).getUuid());

        List<PermitConfig> e = Arrays.asList(
                getPermit(PropertyHelperUtils.TEST_UUIDS[5], 0, null),
                getPermit(PropertyHelperUtils.TEST_UUIDS[1], 0, null));
        permits.setPermits(e);
        assertEquals(2, permits.getPermits().size());
        assertEquals(PropertyHelperUtils.TEST_UUIDS[1], permits.getPermits().get(0).getUuid());
        assertEquals(PropertyHelperUtils.TEST_UUIDS[5], permits.getPermits().get(1).getUuid());

        List<PermitConfig> empty = Collections.emptyList();
        permits.setPermits(empty);
        assertEquals(0, permits.getPermits().size());
        permits.setPermits(c);
        assertEquals(4, permits.getPermits().size());
        permits.setPermits(null);
        assertEquals(0, permits.getPermits().size());
    }

    @Test
    public void testGetByTypeOrOwner() throws Exception {
        PermitsConfig permits = new PermitsConfig();
        permits.add(getPermit(PropertyHelperUtils.TEST_UUIDS[0], PermitsConfig.GLOBAL_PERMIT, null));
        permits.add(getPermit(PropertyHelperUtils.TEST_UUIDS[1], PermitsConfig.ROLE_PERMIT, "c"));
        permits.add(getPermit(PropertyHelperUtils.TEST_UUIDS[2], PermitsConfig.GROUP_PERMIT, "foo"));
        permits.add(getPermit(PropertyHelperUtils.TEST_UUIDS[3], PermitsConfig.USER_PERMIT, "hugo"));
        permits.add(getPermit(PropertyHelperUtils.TEST_UUIDS[4], PermitsConfig.ROLE_PERMIT, "b"));
        permits.add(getPermit(PropertyHelperUtils.TEST_UUIDS[5], PermitsConfig.USER_PERMIT, "hugo"));
        permits.add(getPermit(PropertyHelperUtils.TEST_UUIDS[6], PermitsConfig.GLOBAL_PERMIT, null));
        permits.add(getPermit(PropertyHelperUtils.TEST_UUIDS[7], PermitsConfig.ROLE_PERMIT, "c"));
        permits.add(getPermit(PropertyHelperUtils.TEST_UUIDS[8], PermitsConfig.ROLE_PERMIT, "c"));
        permits.add(getPermit(PropertyHelperUtils.TEST_UUIDS[9], PermitsConfig.GLOBAL_PERMIT, null));
        permits.add(getPermit(PropertyHelperUtils.TEST_UUIDS[10], "something", "whatever"));

        Map<String, List<PermitConfig>> byType = permits.getByType();
        assertNotNull(byType);
        assertEquals(5, byType.size());
        assertNotNull(byType.get(PermitsConfig.GLOBAL_PERMIT));
        assertEquals(3, byType.get(PermitsConfig.GLOBAL_PERMIT).size());
        assertNotNull(byType.get(PermitsConfig.ROLE_PERMIT));
        assertEquals(4, byType.get(PermitsConfig.ROLE_PERMIT).size());
        assertNotNull(byType.get(PermitsConfig.GROUP_PERMIT));
        assertEquals(1, byType.get(PermitsConfig.GROUP_PERMIT).size());
        assertNotNull(byType.get(PermitsConfig.USER_PERMIT));
        assertEquals(2, byType.get(PermitsConfig.USER_PERMIT).size());
        assertNotNull(byType.get("something"));
        assertEquals(1, byType.get("something").size());

        Map<String, List<PermitConfig>> byOwner = permits.getByOwner();
        assertNotNull(byOwner);
        assertEquals(6, byOwner.size());
        assertNotNull(byOwner.get(PermitsConfig.GLOBAL_PERMIT));
        assertEquals(3, byOwner.get(PermitsConfig.GLOBAL_PERMIT).size());
        assertNotNull(byOwner.get("b"));
        assertEquals(1, byOwner.get("b").size());
        assertNotNull(byOwner.get("c"));
        assertEquals(3, byOwner.get("c").size());
        assertNotNull(byOwner.get("foo"));
        assertEquals(1, byOwner.get("foo").size());
        assertNotNull(byOwner.get("hugo"));
        assertEquals(2, byOwner.get("hugo").size());
        assertNotNull(byOwner.get("whatever"));
        assertEquals(1, byOwner.get("whatever").size());
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testUnmodifiableGetPermits() throws Exception {
        PermitsConfig permits = new PermitsConfig();
        permits.getPermits().add(new PermitConfig());
    }

    private PermitConfig getPermit(UUID uuid, int pos, String path) {
        PermitConfig permit = new PermitConfig();
        permit.setUuid(uuid);
        permit.setAction(Permit.ACTION_GET);
        if (path != null) {
            permit.setPath(path);
        }
        permit.setLevel(1);
        permit.setPos(pos);
        return permit;
    }

    private PermitConfig getPermit(UUID uuid, String type, String owner) {
        PermitConfig permit = new PermitConfig();
        permit.setUuid(uuid);
        permit.setAction(Permit.ACTION_GET);
        permit.setPath("/");
        permit.setLevel(1);
        if (type != null) {
            permit.setType(type);
        }
        if (owner != null) {
            permit.setOwner(owner);
        }
        return permit;
    }

}
