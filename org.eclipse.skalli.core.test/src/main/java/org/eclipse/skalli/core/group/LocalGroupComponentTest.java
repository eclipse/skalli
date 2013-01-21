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
package org.eclipse.skalli.core.group;

import java.util.List;

import org.eclipse.skalli.model.Group;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.group.GroupService;
import org.eclipse.skalli.testutil.BundleManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Constants;

@SuppressWarnings("nls")
public class LocalGroupComponentTest {

    private GroupService groupService;
    private List<Group> groups;

    private static final String FILTER =
            "(&(" + Constants.OBJECTCLASS + "=" + GroupService.class.getName() + ")" + "(groupService.type=local))";

    @Before
    public void setup() throws Exception {
        BundleManager.startBundles();
        groupService = Services.getService(GroupService.class, FILTER);
        Assert.assertNotNull("local group service not found", groupService);
        groups = groupService.getGroups();
        Assert.assertEquals(4, groups.size());
    }

    @Test
    public void testIsAdministrator() {
        Assert.assertTrue(groupService.isAdministrator("lc"));
        Assert.assertFalse(groupService.isAdministrator("gh"));
        Assert.assertFalse(groupService.isAdministrator("unknown"));
    }

    @Test
    public void testIsMemberOfGroup() {
        Assert.assertTrue(groupService.isMemberOfGroup("gh", "doctors"));
        Assert.assertFalse(groupService.isMemberOfGroup("lc", "doctors"));
        Assert.assertFalse(groupService.isMemberOfGroup("unknown", "doctors"));
    }

    @Test
    public void testGetGroups() throws Exception {
        List<Group> groups = groupService.getGroups("lc");
        Assert.assertEquals(1, groups.size());
        Assert.assertEquals(GroupService.ADMIN_GROUP, groups.get(0).getGroupId());

        groups = groupService.getGroups("ua");
        Assert.assertEquals(2, groups.size());

        //there is no order in the groups
        int scrumMasterGroupId = "scrummaster".equals(groups.get(0).getGroupId()) ? 0 : 1;
        int expertGroupId = scrumMasterGroupId == 0 ? 1 : 0;
        Assert.assertEquals("scrummaster", groups.get(scrumMasterGroupId).getGroupId());
        Assert.assertEquals("expert", groups.get(expertGroupId).getGroupId());
    }
}
