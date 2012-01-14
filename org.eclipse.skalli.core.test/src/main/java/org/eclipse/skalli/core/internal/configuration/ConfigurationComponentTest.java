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
package org.eclipse.skalli.core.internal.configuration;

import org.easymock.EasyMock;
import org.eclipse.skalli.services.configuration.ConfigKey;
import org.eclipse.skalli.services.configuration.ConfigTransaction;
import org.eclipse.skalli.services.event.EventCustomizingUpdate;
import org.eclipse.skalli.services.event.EventService;
import org.eclipse.skalli.testutil.HashMapStorageService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("nls")
public class ConfigurationComponentTest {

    private ConfigurationComponent cc;

    ConfigKey testConfigKey1 = new ConfigKey() {
        @Override
        public boolean isEncrypted() {
            return false;
        }

        @Override
        public String getKey() {
            return "key1";
        }

        @Override
        public String getDefaultValue() {
            return "default1";
        }
    };
    ConfigKey testConfigKeyInt = new ConfigKey() {
        @Override
        public boolean isEncrypted() {
            return false;
        }

        @Override
        public String getKey() {
            return "keyInt";
        }

        @Override
        public String getDefaultValue() {
            return "42";
        }
    };
    ConfigKey testConfigKeyIntNoDefault = new ConfigKey() {
        @Override
        public boolean isEncrypted() {
            return false;
        }

        @Override
        public String getKey() {
            return "keyIntNoDefault";
        }

        @Override
        public String getDefaultValue() {
            return null;
        }
    };
    ConfigKey testConfigKeyBoolean = new ConfigKey() {
        @Override
        public boolean isEncrypted() {
            return false;
        }

        @Override
        public String getKey() {
            return "keyBool";
        }

        @Override
        public String getDefaultValue() {
            return "false";
        }
    };

    @Before
    public void setup() {
        cc = createConfigurationComponent();
    }

    private ConfigurationComponent createConfigurationComponent() {
        ConfigurationComponent cc = new ConfigurationComponent(HashMapStorageService.class.getName());
        cc.bindStorageService(new HashMapStorageService());
        return cc;
    }

    @Test
    public void testCustomization() throws Exception {
        EventService mockEventService = EasyMock.createMock(EventService.class);

        EasyMock.reset(mockEventService);

        mockEventService.fireEvent(EasyMock.isA(EventCustomizingUpdate.class));

        EasyMock.replay(mockEventService);

        CustomizationData c1 = new CustomizationData();
        c1.prop1 = "Hello";
        c1.prop2 = "World";

        ConfigurationComponent ccOrig = createConfigurationComponent();
        ccOrig.bindEventService(mockEventService);

        ccOrig.writeCustomization("key1", c1);
        EasyMock.verify(mockEventService);
        CustomizationData res = ccOrig.readCustomization("key1", CustomizationData.class);
        Assert.assertNotNull(res);
        Assert.assertEquals(c1.prop1, res.prop1);
        Assert.assertEquals(c1.prop2, res.prop2);
    }

    @Test
    public void testRWString() throws Exception {
        String res1 = cc.readString(testConfigKey1);
        Assert.assertEquals("default1", res1);

        ConfigTransaction tx = cc.startTransaction();
        cc.writeString(tx, testConfigKey1, "value1");
        cc.commit(tx);

        String res2 = cc.readString(testConfigKey1);
        Assert.assertEquals("value1", res2);
    }

    @Test
    public void testRWInteger() throws Exception {
        // define an integer entry
        ConfigTransaction tx = cc.startTransaction();
        cc.writeInteger(tx, testConfigKeyInt, 2);
        cc.commit(tx);
        Assert.assertEquals(2, (int)cc.readInteger(testConfigKeyInt));

        // remove the integer entry: so we expect readInteger to return
        // the default value again!
        tx = cc.startTransaction();
        cc.writeInteger(tx, testConfigKeyInt, null);
        cc.commit(tx);
        Assert.assertEquals(42, (int)cc.readInteger(testConfigKeyInt));
    }

    @Test
    public void testRWIntegerNoDefault() throws Exception {
        // define an integer entry
        ConfigTransaction tx = cc.startTransaction();
        cc.writeInteger(tx, testConfigKeyIntNoDefault, 2);
        cc.commit(tx);
        Assert.assertEquals(2, (int)cc.readInteger(testConfigKeyIntNoDefault));

        // remove the integer entry: so we expect readInteger to return
        // the default value again!
        tx = cc.startTransaction();
        cc.writeInteger(tx, testConfigKeyIntNoDefault, null);
        cc.commit(tx);
        Assert.assertNull(cc.readInteger(testConfigKeyIntNoDefault));
    }

    @Test
    public void testRWBoolean() throws Exception {
        ConfigTransaction tx = cc.startTransaction();
        cc.writeBoolean(tx, testConfigKeyBoolean, true);
        cc.commit(tx);
        Assert.assertEquals(true, (boolean)cc.readBoolean(testConfigKeyBoolean));

        tx = cc.startTransaction();
        cc.writeBoolean(tx, testConfigKeyBoolean, false);
        cc.commit(tx);
        Assert.assertEquals(false, (boolean)cc.readBoolean(testConfigKeyBoolean));

        tx = cc.startTransaction();
        cc.writeBoolean(tx, testConfigKeyBoolean, null);
        cc.commit(tx);
        Assert.assertEquals(false, (boolean)cc.readBoolean(testConfigKeyBoolean));
    }

    @Test
    public void testRWMultipleInOneTransaction() throws Exception {
        ConfigTransaction tx = cc.startTransaction();
        cc.writeString(tx, testConfigKey1, "value1");
        cc.writeBoolean(tx, testConfigKeyBoolean, true);
        cc.writeInteger(tx, testConfigKeyInt, null);
        cc.writeInteger(tx, testConfigKeyIntNoDefault, 2);
        cc.commit(tx);

        Assert.assertEquals(true, (boolean)cc.readBoolean(testConfigKeyBoolean));
        Assert.assertEquals("value1", cc.readString(testConfigKey1));
        Assert.assertEquals(42, (int)cc.readInteger(testConfigKeyInt)); // default value!
        Assert.assertEquals(2, (int)cc.readInteger(testConfigKeyIntNoDefault));
    }

    @Test
    public void testTransactionOrder() throws Exception {
        ConfigTransaction tx = cc.startTransaction();
        cc.writeInteger(tx, testConfigKeyIntNoDefault, 2);
        cc.writeInteger(tx, testConfigKeyIntNoDefault, null);
        cc.commit(tx);
        Assert.assertNull(cc.readInteger(testConfigKeyIntNoDefault));

        tx = cc.startTransaction();
        cc.writeInteger(tx, testConfigKeyIntNoDefault, null);
        cc.writeInteger(tx, testConfigKeyIntNoDefault, 2);
        cc.commit(tx);
        Assert.assertEquals(2, (int)cc.readInteger(testConfigKeyIntNoDefault));
    }

}
