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
package org.eclipse.skalli.common;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleException;

import org.eclipse.skalli.common.configuration.ConfigurationService;
import org.eclipse.skalli.testutil.BundleManager;

@SuppressWarnings("nls")
public class UserServiceUtilTest {

    private ConfigurationService mockConfig;

    private class TestUSU extends UserServiceUtil {
        @Override
        ConfigurationService getConfigService() {
            return mockConfig;
        }

        @Override
        UserService getUserServiceByType(String type) {
            if (type.equals("local")) {
                return EasyMock.createMock(UserService.class);
            } else {
                return null;
            }
        }
    }

    @Before
    public void setup() throws BundleException {
        new BundleManager(this.getClass()).startBundles();
        mockConfig = EasyMock.createMock(ConfigurationService.class);
    }

    @Test
    public void testGetConfiguredUserService() {

        Object[] mocks = new Object[] { mockConfig };

        EasyMock.reset(mocks);

        mockConfig.readString(EasyMock.eq(ConfigKeyUserStore.TYPE));
        EasyMock.expectLastCall().andReturn("local");

        EasyMock.replay(mocks);

        UserService res = new TestUSU().getConfiguredUserService();
        Assert.assertNotNull(res);

        EasyMock.verify(mocks);
    }

    @Test
    public void testGetConfiguredUserService_withFallback() {

        Object[] mocks = new Object[] { mockConfig };

        EasyMock.reset(mocks);

        mockConfig.readString(EasyMock.eq(ConfigKeyUserStore.TYPE));
        EasyMock.expectLastCall().andReturn("gaga");

        mockConfig.readString(EasyMock.eq(ConfigKeyUserStore.USE_LOCAL_FALLBACK));
        EasyMock.expectLastCall().andReturn("true");

        EasyMock.replay(mocks);

        UserService res = new TestUSU().getConfiguredUserService();
        Assert.assertNotNull(res);

        EasyMock.verify(mocks);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetConfiguredUserService_unknown() {

        Object[] mocks = new Object[] { mockConfig };

        EasyMock.reset(mocks);

        mockConfig.readString(EasyMock.eq(ConfigKeyUserStore.TYPE));
        EasyMock.expectLastCall().andReturn("gaga");

        mockConfig.readString(EasyMock.eq(ConfigKeyUserStore.USE_LOCAL_FALLBACK));
        EasyMock.expectLastCall().andReturn("false");

        EasyMock.replay(mocks);

        new TestUSU().getConfiguredUserService();
    }

}
