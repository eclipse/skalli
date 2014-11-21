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
package org.eclipse.skalli.services.entity;

import java.util.Collection;
import java.util.Map;

import org.eclipse.skalli.testutil.BundleManager;
import org.eclipse.skalli.testutil.TestExtensibleEntityBase;
import org.eclipse.skalli.testutil.TestExtensibleEntityEntityService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.ServiceRegistration;

public class EntityServicesTest {

    @SuppressWarnings("rawtypes")
    private ServiceRegistration<EntityService> serviceRegistration;

    private TestExtensibleEntityEntityService testService;

    @Before
    public void setup() throws Exception {
        testService = new TestExtensibleEntityEntityService(0);
        serviceRegistration = BundleManager.registerService(EntityService.class, testService, null);
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
        Collection<EntityService<?>> all = EntityServices.getAll();
        Assert.assertNotNull(all);
        Assert.assertTrue(all.size() > 0);
        Assert.assertTrue(all.contains(testService));
    }

    @Test
    public void testByEntityClassNames() throws Exception {
        Map<String, EntityService<?>> byEntityClassNames = EntityServices.getByEntityClassNames();
        Assert.assertNotNull(byEntityClassNames);
        Assert.assertTrue(byEntityClassNames.size() > 0);
        Assert.assertTrue(byEntityClassNames.containsKey(testService.getEntityClass().getName()));
        Assert.assertEquals(testService, byEntityClassNames.get(testService.getEntityClass().getName()));
    }

    @Test
    public void testByEntityClassName() throws Exception {
        EntityService<TestExtensibleEntityBase> service = EntityServices.getByEntityClassName(testService.getEntityClass().getName());
        Assert.assertEquals(testService, service);
    }

    @Test
    public void testByEntityClass() throws Exception {
        EntityService<TestExtensibleEntityBase> service = EntityServices.getByEntityClass(testService.getEntityClass());
        Assert.assertEquals(testService, service);
    }
}
