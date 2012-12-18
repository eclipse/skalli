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
package org.eclipse.skalli.model;

import static org.junit.Assert.*;

import java.util.Map;

import org.eclipse.skalli.testutil.PropertyTestUtil;
import org.eclipse.skalli.testutil.TestUUIDs;
import org.junit.Test;

@SuppressWarnings("nls")
public class UserTest {

    @Test
    public void testPropertyDefinitions() throws Exception {
        Map<String, Object> values = PropertyTestUtil.getValues();
        values.put(User.PROPERTY_USERID, "homer");
        values.put(User.PROPERTY_FIRSTNAME, "Homer");
        values.put(User.PROPERTY_LASTNAME, "Simpson");
        values.put(User.PROPERTY_EMAIL, "homer@springfield.net");
        values.put(User.PROPERTY_DISPLAY_NAME, "Homer Simpson");
        values.put(User.PROPERTY_TELEPHONE, "5551234");
        values.put(User.PROPERTY_MOBILE, "5555678");
        values.put(User.PROPERTY_ROOM, "reactor control");
        values.put(User.PROPERTY_LOCATION, "Springfield");
        values.put(User.PROPERTY_DEPARTMENT, "reactor stuff");
        values.put(User.PROPERTY_COMPANY, "Springfield Nuclear Power Plant");
        values.put(User.PROPERTY_SIP, "homer@springfield.net");

        Map<Class<?>, String[]> requiredProperties = PropertyTestUtil.getRequiredProperties();

        PropertyTestUtil.checkPropertyDefinitions(User.class, requiredProperties, values);
    }

    @Test
    public void testUnknown() throws Exception {
        User unknown = new User("homer");
        assertTrue(unknown.isUnknown());
        assertEquals(User.UNKNOWN, unknown.getFirstname());
        assertEquals(User.UNKNOWN, unknown.getLastname());
        assertEquals(User.UNKNOWN, unknown.getEmail());
        assertFalse(unknown.hasFirstname());
        assertFalse(unknown.hasLastname());
        assertFalse(unknown.hasEmail());

        User user = new User("homer", "Homer", "Simpson", "homer@springfield.net");
        assertFalse(user.isUnknown());
        assertTrue(user.hasFirstname());
        assertTrue(user.hasLastname());
        assertTrue(user.hasEmail());

        user = new User("homer", "Homer", "Simpson", null);
        assertFalse(user.isUnknown());
        assertTrue(user.hasFirstname());
        assertTrue(user.hasLastname());
        assertFalse(user.hasEmail());

        user = new User("homer", "Homer", "Simpson", User.UNKNOWN);
        assertFalse(user.isUnknown());
        assertTrue(user.hasFirstname());
        assertTrue(user.hasLastname());
        assertFalse(user.hasEmail());

        user = new User("homer", null, "Simpson", null);
        assertFalse(user.isUnknown());
        assertFalse(user.hasFirstname());
        assertTrue(user.hasLastname());
        assertFalse(user.hasEmail());

        user = new User("homer", "Homer", null, null);
        assertFalse(user.isUnknown());
        assertTrue(user.hasFirstname());
        assertFalse(user.hasLastname());
        assertFalse(user.hasEmail());

        user = new User("homer", "Homer", null, User.UNKNOWN);
        assertFalse(user.isUnknown());
        assertTrue(user.hasFirstname());
        assertFalse(user.hasLastname());
        assertFalse(user.hasEmail());

        user = new User("homer", "Homer", User.UNKNOWN, null);
        assertFalse(user.isUnknown());
        assertTrue(user.hasFirstname());
        assertFalse(user.hasLastname());
        assertFalse(user.hasEmail());

        user = new User("homer", null, null, "homer@springfield.net");
        assertFalse(user.isUnknown());
        assertFalse(user.hasFirstname());
        assertFalse(user.hasLastname());
        assertTrue(user.hasEmail());

        user = new User("homer", User.UNKNOWN, User.UNKNOWN, "homer@springfield.net");
        assertFalse(user.isUnknown());
        assertFalse(user.hasFirstname());
        assertFalse(user.hasLastname());
        assertTrue(user.hasEmail());

        user = new User();
        assertFalse(user.isUnknown());
        assertFalse(unknown.hasFirstname());
        assertFalse(unknown.hasLastname());
        assertFalse(unknown.hasEmail());
    }

    @Test
    public void testGetDisplayName() throws Exception {
        User user = new User("homer");
        assertEquals("homer", user.getDisplayName());

        user = new User("homer", "Homer", "Simpson", "homer@springfield.net");
        assertEquals("Homer Simpson", user.getDisplayName());

        user = new User("homer", "Homer", "Simpson", null);
        assertEquals("Homer Simpson", user.getDisplayName());

        user = new User("homer", "Homer", null, "homer@springfield.net");
        assertEquals("Homer", user.getDisplayName());

        user = new User("homer", null, "Simpson", "homer@springfield.net");
        assertEquals("Simpson", user.getDisplayName());

        user = new User("homer", null, null, "homer@springfield.net");
        assertEquals("homer@springfield.net", user.getDisplayName());

        user = new User("homer", User.UNKNOWN, User.UNKNOWN, "homer@springfield.net");
        assertEquals("homer@springfield.net", user.getDisplayName());

        user = new User();
        user.setUuid(TestUUIDs.TEST_UUIDS[0]);
        assertEquals(TestUUIDs.TEST_UUIDS[0].toString(), user.getDisplayName());
    }

    @Test
    public void testCompare() throws Exception {
        User user1 = new User("homer", "Homer", "Simpson", null);
        User user2 = new User("burns", "Mr.", "Burns", null);
        assertTrue(user1.compareTo(user2) > 0);
        assertTrue(user2.compareTo(user1) < 0);
        assertTrue(user1.compareTo(user1) == 0);
        assertTrue(user2.compareTo(user2) == 0);

        user2 = new User("marge", "Marge", "Simpson", null);
        assertTrue(user1.compareTo(user2) < 0);
        assertTrue(user2.compareTo(user1) > 0);

        user2 = new User("homer", "Marge", "Simpson", null);
        assertTrue(user1.compareTo(user2) < 0);
        assertTrue(user2.compareTo(user1) > 0);

        user2 = new User("homer1", "Homer", "Simpson", null);
        assertTrue(user1.compareTo(user2) < 0);
        assertTrue(user2.compareTo(user1) > 0);

        // EntityBase#compare() compares class names:
        // org.eclipse.skalli.model.User > java.lang.String
        assertTrue(user1.compareTo("foobar") > 0);
    }

}
