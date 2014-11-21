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
package org.eclipse.skalli.core.configuration;

import static org.junit.Assert.*;

import org.easymock.EasyMock;
import org.eclipse.skalli.services.configuration.ConfigSection;
import org.eclipse.skalli.services.configuration.EventConfigUpdate;
import org.eclipse.skalli.services.event.EventService;
import org.eclipse.skalli.testutil.HashMapStorageService;
import org.junit.Test;
import org.restlet.resource.ServerResource;

@SuppressWarnings("nls")
public class ConfigurationComponentTest {

    private static class TestConfig {
        public String prop1;
        public String prop2;
        public TestConfig() {
        }
    }

    private static class TestConfig1 {
        public TestConfig1() {
        }
    }

    private static class TestConfigSection implements ConfigSection<TestConfig> {
        public static final String STORAGE_KEY = "configSection"; //$NON-NLS-1$

        @Override
        public String getStorageKey() {
            return STORAGE_KEY;
        }
        @Override
        public Class<TestConfig> getConfigClass() {
            return TestConfig.class;
        }
        @Override
        public String[] getResourcePaths() {
            return null;
        }
        @Override
        public Class<? extends ServerResource> getServerResource(String resourePath) {
            return null;
        }
    }

    @Test
    public void testWriteReadConfiguration() throws Exception {
        EventService mockEventService = EasyMock.createMock(EventService.class);
        EasyMock.reset(mockEventService);
        EasyMock.makeThreadSafe(mockEventService, true);
        mockEventService.fireEvent(EasyMock.isA(EventConfigUpdate.class));
        EasyMock.expectLastCall().times(2); // bindConfigSection + writeConfiguration
        EasyMock.replay(mockEventService);

        ConfigurationComponent cc = new ConfigurationComponent(HashMapStorageService.class.getName());
        cc.bindStorageService(new HashMapStorageService());
        cc.bindEventService(mockEventService);
        TestConfigSection configSection1 = new TestConfigSection();
        cc.bindConfigSection(configSection1);
        assertEquals(configSection1, cc.getConfigSection(TestConfigSection.STORAGE_KEY));
        assertEquals(configSection1, cc.getConfigSection(TestConfig.class));
        assertNull(cc.getConfigSection(TestConfig1.class));

        assertNull(cc.readConfiguration(TestConfig.class));
        assertNull(cc.readConfiguration(TestConfig1.class));

        TestConfig config = new TestConfig();
        config.prop1 = "Hello";
        config.prop2 = "World";
        cc.writeConfiguration(config);
        TestConfig res = cc.readConfiguration(TestConfig.class);
        assertNotNull(res);
        assertEquals(config.prop1, res.prop1);
        assertEquals(config.prop2, res.prop2);

        // no ConfigSection for TestConfig1 => TestConfig1 is not stored!
        cc.writeConfiguration(new TestConfig1());
        assertNull(cc.readConfiguration(TestConfig1.class));

        EasyMock.verify(mockEventService);
    }
}
