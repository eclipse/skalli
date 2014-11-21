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
package org.eclipse.skalli.services.extension;

import java.util.Collection;
import java.util.Map;

import org.eclipse.skalli.testutil.BundleManager;
import org.eclipse.skalli.testutil.TestExtension;
import org.eclipse.skalli.testutil.TestExtensionService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.ServiceRegistration;

public class ExtensionServicesTest {

    @SuppressWarnings("rawtypes")
    private ServiceRegistration<ExtensionService> serviceRegistration;

    private TestExtensionService testService;

    @Before
    public void setup() throws Exception {
        testService = new TestExtensionService();
        serviceRegistration = BundleManager.registerService(ExtensionService.class, testService, null);
        Assert.assertNotNull(serviceRegistration);
    }

    @After
    public void tearDown() {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
        }
    }

    @Test
    public void testAll() throws Exception {
        Collection<ExtensionService<?>> all = ExtensionServices.getAll();
        Assert.assertNotNull(all);
        Assert.assertTrue(all.size() > 0);
        Assert.assertTrue(all.contains(testService));
    }

    @Test
    public void testByShortNames() throws Exception {
        Map<String, ExtensionService<?>> byShortNames = ExtensionServices.getByShortNames();
        Assert.assertNotNull(byShortNames);
        Assert.assertTrue(byShortNames.size() > 0);
        Assert.assertTrue(byShortNames.containsKey(testService.getShortName()));
        Assert.assertEquals(testService, byShortNames.get(testService.getShortName()));
    }

    @Test
    public void testByShortName() throws Exception {
        ExtensionService<?> service = ExtensionServices.getByShortName(testService.getShortName());
        Assert.assertEquals(testService, service);
    }

    @Test
    public void testByExtensionClassNames() throws Exception {
        Map<String, ExtensionService<?>> byExtensionClassNames = ExtensionServices.getByExtensionClassNames();
        Assert.assertNotNull(byExtensionClassNames);
        Assert.assertTrue(byExtensionClassNames.size() > 0);
        Assert.assertTrue(byExtensionClassNames.containsKey(testService.getExtensionClass().getName()));
        Assert.assertEquals(testService, byExtensionClassNames.get(testService.getExtensionClass().getName()));
    }

    @Test
    public void testByExtensionClassName() throws Exception {
        ExtensionService<TestExtension> service = ExtensionServices.getByExtensionClassName(testService.getExtensionClass().getName());
        Assert.assertEquals(testService, service);
    }

    @Test
    public void testByExtensionClass() throws Exception {
        ExtensionService<TestExtension> service = ExtensionServices.getByExtensionClass(testService.getExtensionClass());
        Assert.assertEquals(testService, service);
    }

}
