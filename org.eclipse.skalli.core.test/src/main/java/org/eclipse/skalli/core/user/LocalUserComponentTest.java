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
package org.eclipse.skalli.core.user;

import java.util.List;
import java.util.Set;

import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.user.UserService;
import org.eclipse.skalli.testutil.BundleManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Constants;

@SuppressWarnings("nls")
public class LocalUserComponentTest {

    private UserService userService;
    private List<User> users;

    private static final String FILTER =
            "(&(" + Constants.OBJECTCLASS + "=" + UserService.class.getName() + ")" + "(userService.type=local))";

    @Before
    public void setup() throws Exception {
        BundleManager.startBundles();
        userService = Services.getService(UserService.class, FILTER);
        Assert.assertNotNull("local user service not found", userService);
        users = userService.getUsers();
        Assert.assertEquals(5, users.size());
    }

    @Test
    public void testGetUserById() {
        for (User user : users) {
            Assert.assertEquals(user, userService.getUserById(user.getUserId()));
        }
        Assert.assertEquals(new User("anonymous"), userService.getUserById("anonymous"));
    }

    @Test
    public void testGetUsersById() {
        Set<User> userSet = userService.getUsersById(CollectionUtils.asSet("gh", "lc", "jw"));
        Assert.assertTrue(userSet.contains(userService.getUserById("gh")));
        Assert.assertTrue(userSet.contains(userService.getUserById("lc")));
        Assert.assertTrue(userSet.contains(userService.getUserById("jw")));

        userSet = userService.getUsersById(CollectionUtils.asSet("gh", "unknown"));
        Assert.assertTrue(userSet.contains(userService.getUserById("gh")));
        Assert.assertTrue(userSet.contains(new User("unknown")));

        userSet = userService.getUsersById(null);
        Assert.assertEquals(0, userSet.size());
    }

    @Test
    public void testFindUser() {
        User userGregHouse = userService.getUserById("gh");

        List<User> findResult = userService.findUser("Gregory House");
        Assert.assertEquals(1, findResult.size());
        Assert.assertEquals(userGregHouse, findResult.get(0));

        findResult = userService.findUser("Greg House");
        Assert.assertEquals(1, findResult.size());
        Assert.assertEquals(userGregHouse, findResult.get(0));

        findResult = userService.findUser("grEg HoUse"); // case-insensitive!
        Assert.assertEquals(1, findResult.size());
        Assert.assertEquals(userGregHouse, findResult.get(0));

        findResult = userService.findUser("Gregory");
        Assert.assertEquals(1, findResult.size());
        Assert.assertEquals(userGregHouse, findResult.get(0));

        findResult = userService.findUser("Greg");
        Assert.assertEquals(1, findResult.size());
        Assert.assertEquals(userGregHouse, findResult.get(0));

        findResult = userService.findUser("House");
        Assert.assertEquals(1, findResult.size());
        Assert.assertEquals(userGregHouse, findResult.get(0));

        findResult = userService.findUser("House, Greg");
        Assert.assertEquals(1, findResult.size());
        Assert.assertEquals(userGregHouse, findResult.get(0));

        findResult = userService.findUser("Greg House, M.D.");
        Assert.assertEquals(1, findResult.size());
        Assert.assertEquals(userGregHouse, findResult.get(0));

        findResult = userService.findUser("Dr. Greg House");
        Assert.assertEquals(1, findResult.size());
        Assert.assertEquals(userGregHouse, findResult.get(0));

        findResult = userService.findUser("Greg 'MasterOfCuddy' House");
        Assert.assertEquals(1, findResult.size());
        Assert.assertEquals(userGregHouse, findResult.get(0));

        findResult = userService.findUser("gh");
        Assert.assertEquals(1, findResult.size());
        Assert.assertEquals(userGregHouse, findResult.get(0));

        findResult = userService.findUser("greg.house@princeton-plainsborough.com");
        Assert.assertEquals(1, findResult.size());
        Assert.assertEquals(userGregHouse, findResult.get(0));

        findResult = userService.findUser("diagnost");
        Assert.assertEquals(1, findResult.size());
        Assert.assertEquals(userGregHouse, findResult.get(0));

        findResult = userService.findUser("");
        Assert.assertEquals(0, findResult.size());
        findResult = userService.findUser(null);
        Assert.assertEquals(0, findResult.size());
    }
}
