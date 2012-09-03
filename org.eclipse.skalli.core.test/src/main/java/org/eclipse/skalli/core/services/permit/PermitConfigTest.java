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

import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.services.permit.Permit;
import org.eclipse.skalli.testutil.PropertyHelperUtils;
import org.junit.Test;

@SuppressWarnings("nls")
public class PermitConfigTest {

    @Test
    public void testProperties() throws Exception {
        PermitConfig config = new PermitConfig();
        assertNull(config.getUuid());
        assertNull(config.getType());
        assertNull(config.getAction());
        assertNull(config.getOwner());
        assertNull(config.getPath());
        assertEquals(0, config.getLevel());
        assertEquals(0, config.getPos());
        assertFalse(config.isOverride());
        config.setUuid(PropertyHelperUtils.TEST_UUIDS[0]);
        assertEquals(PropertyHelperUtils.TEST_UUIDS[0], config.getUuid());
        config.setType(PermitsConfig.GROUP_PERMIT);
        assertEquals(PermitsConfig.GROUP_PERMIT, config.getType());
        config.setAction(Permit.ACTION_GET);
        assertEquals(Permit.ACTION_GET, config.getAction());
        config.setOwner("hugo");
        assertEquals("hugo", config.getOwner());
        config.setPath("/projects/${project}");
        assertEquals("/projects/${project}", config.getPath());
        config.setLevel(1);
        assertEquals(1, config.getLevel());
        config.setOverride(true);
        assertTrue(config.isOverride());
        Permit permit = config.asPermit();
        assertEquals(Permit.ACTION_GET, permit.getAction());
        assertEquals("/projects/${project}", permit.getPath());
        assertEquals(1, config.getLevel());
        permit = config.asPermit(CollectionUtils.asMap("project", "skalli"));
        assertEquals("/projects/skalli", permit.getPath());
    }

    @Test
    public void testEquals() throws Exception {
        PermitConfig config1 = new PermitConfig();
        PermitConfig config2 = new PermitConfig();
        config1.setUuid(PropertyHelperUtils.TEST_UUIDS[0]);
        config2.setUuid(PropertyHelperUtils.TEST_UUIDS[0]);
        assertEquals(config1, config2);
        assertEquals(config2, config1);
        assertFalse(config1.equals(null));
        assertFalse(config1.equals(PropertyHelperUtils.TEST_UUIDS[0]));
        config2.setUuid(PropertyHelperUtils.TEST_UUIDS[1]);
        assertFalse(config1.equals(config2));
        assertFalse(config2.equals(config1));

        config1.setUuid(null);
        config2.setUuid(null);
        config1.setType(PermitsConfig.GROUP_PERMIT);
        config2.setType(PermitsConfig.GROUP_PERMIT);
        assertEquals(config1, config2);
        assertEquals(config2, config1);
        config2.setType(PermitsConfig.ROLE_PERMIT);
        assertFalse(config1.equals(config2));
        assertFalse(config2.equals(config1));
        config2.setType(null);
        assertFalse(config1.equals(config2));
        assertFalse(config2.equals(config1));
        config2.setType(PermitsConfig.GROUP_PERMIT);

        config1.setAction(Permit.ACTION_GET);
        config2.setAction(Permit.ACTION_GET);
        assertEquals(config1, config2);
        assertEquals(config2, config1);
        config2.setAction(Permit.ACTION_PUT);
        assertFalse(config1.equals(config2));
        assertFalse(config2.equals(config1));
        config2.setAction(null);
        assertFalse(config1.equals(config2));
        assertFalse(config2.equals(config1));
        config2.setAction(Permit.ACTION_GET);

        config1.setPath("/");
        config2.setPath("/");
        assertEquals(config1, config2);
        assertEquals(config2, config1);
        config2.setPath("/projects");
        assertFalse(config1.equals(config2));
        assertFalse(config2.equals(config1));
        config2.setPath(null);
        assertFalse(config1.equals(config2));
        assertFalse(config2.equals(config1));
        config2.setPath("/");

        config1.setLevel(1);
        config2.setLevel(1);
        assertEquals(config1, config2);
        assertEquals(config2, config1);
        config2.setLevel(0);
        assertFalse(config1.equals(config2));
        assertFalse(config2.equals(config1));
        config2.setLevel(1);

        config1.setOwner("hugo");
        config2.setOwner("hugo");
        assertEquals(config1, config2);
        assertEquals(config2, config1);
        config2.setOwner("foo");
        assertFalse(config1.equals(config2));
        assertFalse(config2.equals(config1));
    }

}
