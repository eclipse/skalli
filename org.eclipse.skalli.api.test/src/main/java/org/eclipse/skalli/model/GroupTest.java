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

import java.util.Collections;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;

import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.model.Group;
import org.eclipse.skalli.testutil.PropertyHelper;
import org.eclipse.skalli.testutil.PropertyHelperUtils;
import org.junit.Test;

@SuppressWarnings("nls")
public class GroupTest {

    @Test
    public void testPropertyDefinitions() throws Exception {
        Map<String, Object> values = PropertyHelperUtils.getValues();
        values.put(Group.PROPERTY_GROUP_ID, "simpsons");
        TreeSet<String> members = new TreeSet<String>();
        members.add("homer");
        members.add("bart");
        values.put(Group.PROPERTY_GROUP_MEMBERS, members);
        values.put(EntityBase.PROPERTY_UUID, UUID.fromString("e4d78581-08da-4f04-8a90-a7dac41f6247"));
        values.put(EntityBase.PROPERTY_DELETED, Boolean.FALSE);

        Map<Class<?>, String[]> requiredProperties = Collections.emptyMap();
        PropertyHelper.checkPropertyDefinitions(Group.class, requiredProperties, values);
    }

}
