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

import java.util.Map;

import org.eclipse.skalli.testutil.PropertyHelper;
import org.eclipse.skalli.testutil.PropertyHelperUtils;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("nls")
public class UserTest {

    @Test
    public void testPropertyDefinitions() throws Exception {
        Map<String, Object> values = PropertyHelperUtils.getValues();
        values.put(User.PROPERTY_USERID, "homer");
        values.put(User.PROPERTY_FIRSTNAME, "Homer");
        values.put(User.PROPERTY_LASTNAME, "Simpson");
        values.put(User.PROPERTY_EMAIL, "homer@springfield.net");
        values.put(User.PROPERTY_DISPLAY_NAME, "Homer Simpson");

        Map<Class<?>, String[]> requiredProperties = PropertyHelperUtils.getRequiredProperties();

        PropertyHelper.checkPropertyDefinitions(User.class, requiredProperties, values);
    }

    @Test
    public void testCompare() throws Exception {
        User user1 = new User("homer", "Homer", "Simpson", null);
        User user2 = new User("burns", "Mr.", "Burns", null);
        Assert.assertTrue(user1.compareTo(user2) > 0);
        Assert.assertTrue(user2.compareTo(user1) < 0);
        Assert.assertTrue(user1.compareTo(user1) == 0);
        Assert.assertTrue(user2.compareTo(user2) == 0);

        user2 = new User("marge", "Marge", "Simpson", null);
        Assert.assertTrue(user1.compareTo(user2) < 0);
        Assert.assertTrue(user2.compareTo(user1) > 0);

        user2 = new User("homer", "Marge", "Simpson", null);
        Assert.assertTrue(user1.compareTo(user2) < 0);
        Assert.assertTrue(user2.compareTo(user1) > 0);

        user2 = new User("homer1", "Homer", "Simpson", null);
        Assert.assertTrue(user1.compareTo(user2) < 0);
        Assert.assertTrue(user2.compareTo(user1) > 0);

        // EntityBase#compare() compares class names:
        // org.eclipse.skalli.model.User > java.lang.String
        Assert.assertTrue(user1.compareTo("foobar") > 0);
    }

}
