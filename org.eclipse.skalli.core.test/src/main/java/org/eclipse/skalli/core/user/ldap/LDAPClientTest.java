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
package org.eclipse.skalli.core.user.ldap;

import java.util.HashSet;
import java.util.Set;

import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.easymock.EasyMock;
import org.eclipse.skalli.model.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("nls")
public class LDAPClientTest {

    private static final String USERS_GROUP = "CN=Users,DC=some,DC=corp";

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testSearchUserById_nonExisting() throws Exception {
        LDAPClient client = getLDAPClient();
        DirContext mockContext = MockInitialDirContextFactory.getLatestMockContext();

        EasyMock.reset(mockContext);

        mockContext.close();
        EasyMock.expectLastCall();

        mockContext.search(EasyMock.eq(USERS_GROUP), EasyMock.isA(String.class), EasyMock
                .isA(SearchControls.class));
        EasyMock.expectLastCall().andReturn(new MockNamingEnumeration<SearchResult>());

        EasyMock.replay(mockContext);

        User user = client.searchUserById("non-existing-userid");
        Assert.assertNotNull(user);
        Assert.assertEquals("non-existing-userid", user.getDisplayName());

        EasyMock.verify(mockContext);
    }

    @Test
    public void testSearchUsersById_nonExisting() throws Exception {
        LDAPClient client = getLDAPClient();
        DirContext mockContext = MockInitialDirContextFactory.getLatestMockContext();

        EasyMock.reset(mockContext);

        mockContext.close();
        EasyMock.expectLastCall();

        mockContext.search(EasyMock.eq(USERS_GROUP), EasyMock.isA(String.class), EasyMock
                .isA(SearchControls.class));
        EasyMock.expectLastCall().andReturn(new MockNamingEnumeration<SearchResult>()).anyTimes();

        EasyMock.replay(mockContext);

        Set<String> ids = new HashSet<String>();
        ids.add("id1");
        ids.add("id2");

        Set<User> user = client.searchUsersByIds(ids);
        Assert.assertNotNull(user);
        Assert.assertEquals(user.size(), 2);

        EasyMock.verify(mockContext);
    }

    private LDAPClient getLDAPClient() {
        LDAPConfig config = new LDAPConfig();
        config.setCtxFactory(MockInitialDirContextFactory.class.getName());
        config.setProviderUrl("ldap://thisIsIgnoredInTests");
        config.setUsername("hello");
        config.setPassword("world");
        config.setBaseDN(USERS_GROUP);
        return new LDAPClient(config);
    }

}
