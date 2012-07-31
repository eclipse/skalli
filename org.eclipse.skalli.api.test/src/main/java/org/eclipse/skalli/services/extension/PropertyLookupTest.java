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
package org.eclipse.skalli.services.extension;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.testutil.BundleManager;
import org.eclipse.skalli.testutil.TestExtension;
import org.eclipse.skalli.testutil.TestExtensionService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.ServiceRegistration;

@SuppressWarnings("nls")
public class PropertyLookupTest {

    @SuppressWarnings("rawtypes")
    private ServiceRegistration<ExtensionService> serviceRegistration;

    @Before
    public void setup() throws Exception {
        serviceRegistration = BundleManager.registerService(ExtensionService.class,  new TestExtensionService(), null);
        Assert.assertNotNull(serviceRegistration);
    }

    @After
    public void tearDown() {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
        }
    }

    @Test
    public void testLookUp() throws Exception {
        Project project = createProject();
        Map<String,Object> props = new HashMap<String,Object>();
        props.put("userId", "hugo");
        PropertyLookup lookup = new PropertyLookup(project, props);
        Assert.assertEquals("bla.blubb", lookup.lookup(Project.PROPERTY_PROJECTID));
        Assert.assertEquals("Blubber", lookup.lookup(Project.PROPERTY_NAME));
        Assert.assertEquals("foobar", lookup.lookup("testext." + TestExtension.PROPERTY_STR));
        Assert.assertEquals("a,b,c", lookup.lookup("testext." + TestExtension.PROPERTY_ITEMS));
        Assert.assertEquals("hugo", lookup.lookup("userId"));
        Assert.assertNull(lookup.lookup(Project.PROPERTY_DESCRIPTION));
        Assert.assertNull(lookup.lookup("testext.abc"));
        Assert.assertNull(lookup.lookup(null));
        Assert.assertNull(lookup.lookup(""));
    }

    @Test
    public void testLookUpNoProject() throws Exception {
        Map<String,Object> props = new HashMap<String,Object>();
        props.put("userId", "hugo");
        PropertyLookup lookup = new PropertyLookup(null, props);
        Assert.assertEquals("hugo", lookup.lookup("userId"));
        Assert.assertNull(lookup.lookup(Project.PROPERTY_PROJECTID));
        Assert.assertNull(lookup.lookup("testext.abc"));
        Assert.assertNull(lookup.lookup(null));
        Assert.assertNull(lookup.lookup(""));
    }

    @Test
    public void testLookUpNoCustomProps() throws Exception {
        Project project = createProject();
        PropertyLookup lookup = new PropertyLookup(project);
        Assert.assertNull(lookup.lookup("userId"));
        Assert.assertEquals("bla.blubb", lookup.lookup(Project.PROPERTY_PROJECTID));
        Assert.assertNull(lookup.lookup("testext.abc"));
        Assert.assertNull(lookup.lookup(null));
        Assert.assertNull(lookup.lookup(""));
    }

    private Project createProject() {
        Project project = new Project("bla.blubb", null, "Blubber");
        TestExtension ext = new TestExtension();
        ext.setStr("foobar");
        ext.addItem("a");
        ext.addItem("b");
        ext.addItem("c");
        project.addExtension(ext);
        return project;
    }
}
