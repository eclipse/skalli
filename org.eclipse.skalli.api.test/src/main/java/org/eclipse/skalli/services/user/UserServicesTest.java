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
package org.eclipse.skalli.services.user;

import org.easymock.EasyMock;
import org.eclipse.skalli.services.configuration.ConfigurationService;
import org.eclipse.skalli.testutil.BundleManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleException;

@SuppressWarnings("nls")
public class UserServicesTest {

    private ConfigurationService mockConfig;

    private class TestUSU extends UserServices {
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
        BundleManager.startBundles();
        mockConfig = EasyMock.createMock(ConfigurationService.class);
    }

    @Test
    public void testGetConfiguredUserService() {

        Object[] mocks = new Object[] { mockConfig };

        EasyMock.reset(mocks);

        mockConfig.readCustomization("userStore", UserStoreConfig.class);
        EasyMock.expectLastCall().andReturn(createUserStoreConfig("local", true));

        EasyMock.replay(mocks);

        UserService res = new TestUSU().getConfiguredUserService();
        Assert.assertNotNull(res);

        EasyMock.verify(mocks);
    }

    @Test
    public void testGetConfiguredUserService_withFallback() {

        Object[] mocks = new Object[] { mockConfig };

        EasyMock.reset(mocks);

        mockConfig.readCustomization("userStore", UserStoreConfig.class);
        EasyMock.expectLastCall().andReturn(createUserStoreConfig("foobar", true));

        EasyMock.replay(mocks);

        UserService res = new TestUSU().getConfiguredUserService();
        Assert.assertNotNull(res);

        EasyMock.verify(mocks);
    }

    private UserStoreConfig createUserStoreConfig(String type, boolean useLocalFallback) {
        UserStoreConfig config = new UserStoreConfig();
        config.setType(type);
        config.setUseLocalFallback(useLocalFallback);
        return config;
    }
}