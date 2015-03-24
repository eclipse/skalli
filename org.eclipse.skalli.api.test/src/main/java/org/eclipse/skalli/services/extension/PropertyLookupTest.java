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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.testutil.AssertUtils;
import org.eclipse.skalli.testutil.BundleManager;
import org.eclipse.skalli.testutil.TestExtension;
import org.eclipse.skalli.testutil.TestExtension1;
import org.eclipse.skalli.testutil.TestExtensionService;
import org.eclipse.skalli.testutil.TestExtensionService1;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.ServiceRegistration;

@SuppressWarnings("nls")
public class PropertyLookupTest {

    private List<ServiceRegistration<?>> serviceRegistrations = new ArrayList<ServiceRegistration<?>>();

    @Before
    public void setup() throws Exception {
        serviceRegistrations.add(BundleManager.registerService(ExtensionService.class,  new TestExtensionService(), null));
        serviceRegistrations.add(BundleManager.registerService(ExtensionService.class,  new TestExtensionService1(), null));
        Assert.assertEquals(2, serviceRegistrations.size());
    }

    @After
    public void tearDown() {
        for (ServiceRegistration<?> serviceRegistration : serviceRegistrations) {
            serviceRegistration.unregister();
        }
    }

    private static final String PREFIX = "testext.";
    private static final String PREFIX1 = "testext1.";
    private static final String USER_PREFIX = "user.";

    @Test
    public void testMapMethods() throws Exception {
        Project project = createProject();
        Map<String,Object> props = new HashMap<String,Object>();
        props.put("userId", "hugo");
        PropertyLookup lookup = new PropertyLookup(project, props);

        Assert.assertFalse(lookup.isEmpty());
        Set<String> expectedKeys = CollectionUtils.asSet(
                EntityBase.PROPERTY_UUID, EntityBase.PROPERTY_DELETED, EntityBase.PROPERTY_PARENT_ENTITY,
                EntityBase.PROPERTY_PARENT_ENTITY_ID, EntityBase.PROPERTY_LAST_MODIFIED,
                EntityBase.PROPERTY_LAST_MODIFIED_BY, EntityBase.PROPERTY_FIRST_CHILD,
                EntityBase.PROPERTY_NEXT_SIBLING,
                Project.PROPERTY_PROJECTID, Project.PROPERTY_NAME, Project.PROPERTY_SHORT_NAME,
                Project.PROPERTY_DESCRIPTION_FORMAT, Project.PROPERTY_DESCRIPTION, Project.PROPERTY_TEMPLATEID,
                Project.PROPERTY_LOGO_URL, Project.PROPERTY_PHASE,
                Project.PROPERTY_REGISTERED,
                PREFIX + EntityBase.PROPERTY_UUID, PREFIX + EntityBase.PROPERTY_DELETED,
                PREFIX + EntityBase.PROPERTY_PARENT_ENTITY, PREFIX + EntityBase.PROPERTY_PARENT_ENTITY_ID,
                PREFIX + EntityBase.PROPERTY_LAST_MODIFIED, PREFIX + EntityBase.PROPERTY_LAST_MODIFIED_BY,
                PREFIX + EntityBase.PROPERTY_FIRST_CHILD, PREFIX + EntityBase.PROPERTY_NEXT_SIBLING,
                PREFIX + TestExtension.PROPERTY_BOOL, PREFIX + TestExtension.PROPERTY_ITEMS,
                PREFIX + TestExtension.PROPERTY_STR,
                PREFIX1 + EntityBase.PROPERTY_UUID, PREFIX1 + EntityBase.PROPERTY_DELETED,
                PREFIX1 + EntityBase.PROPERTY_PARENT_ENTITY, PREFIX1 + EntityBase.PROPERTY_PARENT_ENTITY_ID,
                PREFIX1 + EntityBase.PROPERTY_LAST_MODIFIED, PREFIX1 + EntityBase.PROPERTY_LAST_MODIFIED_BY,
                PREFIX1 + EntityBase.PROPERTY_FIRST_CHILD, PREFIX1 + EntityBase.PROPERTY_NEXT_SIBLING,
                PREFIX1 + TestExtension.PROPERTY_BOOL, PREFIX1 + TestExtension.PROPERTY_ITEMS,
                PREFIX1 + TestExtension.PROPERTY_STR,
                "userId");
        Assert.assertEquals(expectedKeys.size(), lookup.size());
        AssertUtils.assertEqualsAnyOrder("keySet", expectedKeys, lookup.keySet());

        Assert.assertEquals("hugo", lookup.get("userId"));
        assertProjectLookup(lookup);
    }

    @Test
    public void testPutAll() {
        Project project = createProject();
        User user = createUser();
        Map<String,Object> props = new HashMap<String,Object>();
        props.put(User.PROPERTY_USERID, user.getUserId());
        PropertyLookup lookup = new PropertyLookup(props);
        lookup.putAllProperties(project, "");
        lookup.putAllProperties(user, USER_PREFIX);

        assertProjectLookup(lookup);
        Assert.assertEquals(user.getUserId(), lookup.get(User.PROPERTY_USERID));
        Assert.assertEquals(user.getUserId(), lookup.get(USER_PREFIX + User.PROPERTY_USERID));
        Assert.assertEquals(user.getFirstname(), lookup.get(USER_PREFIX + User.PROPERTY_FIRSTNAME));
        Assert.assertEquals(user.getLastname(), lookup.get(USER_PREFIX + User.PROPERTY_LASTNAME));
        Assert.assertEquals(user.getEmail(), lookup.get(USER_PREFIX + User.PROPERTY_EMAIL));
    }

    @SuppressWarnings("unchecked")
    private void assertProjectLookup(PropertyLookup lookup) {
        Assert.assertEquals("bla.blubb", lookup.get(Project.PROPERTY_PROJECTID));
        Assert.assertEquals("Blubber", lookup.get(Project.PROPERTY_NAME));
        Assert.assertEquals("text", lookup.get(Project.PROPERTY_DESCRIPTION_FORMAT));
        Assert.assertEquals("foobar", lookup.get(PREFIX + TestExtension.PROPERTY_STR));
        Assert.assertEquals("hubab", lookup.get(PREFIX1 + TestExtension.PROPERTY_STR));
        AssertUtils.assertEquals("get", (List<String>)lookup.get(PREFIX + TestExtension.PROPERTY_ITEMS), "a", "b", "c");
        AssertUtils.assertEquals("get", (List<String>)lookup.get(PREFIX + TestExtension.PROPERTY_ITEMS), "a", "b", "c");
        Assert.assertTrue(((List<String>)lookup.get(PREFIX1 + TestExtension1.PROPERTY_ITEMS)).isEmpty());
        Assert.assertNull(lookup.get(Project.PROPERTY_DESCRIPTION));
        Assert.assertNull(lookup.get("testext.abc"));
        Assert.assertNull(lookup.get(null));
        Assert.assertNull(lookup.get(""));
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
        TestExtension1 ext1 = new TestExtension1();
        ext1.setStr("hubab");
        project.addExtension(ext1);
        return project;
    }

    private User createUser() {
        return new User("homer", "Homer", "Simpson", "homer@springfield.org");
    }
}
