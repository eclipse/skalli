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
package org.eclipse.skalli.services.favorites;

import java.util.Map;

import org.eclipse.skalli.commons.UUIDList;
import org.eclipse.skalli.testutil.PropertyTestUtil;
import org.eclipse.skalli.testutil.TestUUIDs;
import org.junit.Test;

@SuppressWarnings("nls")
public class FavoritesTest {

    @Test
    public void testPropertyDefinitions() throws Exception {
        Map<String, Object> values = PropertyTestUtil.getValues();
        values.put(Favorites.PROPERTY_USERID, "homer");
        UUIDList projects = new UUIDList();
        projects.add(TestUUIDs.TEST_UUIDS[0]);
        projects.add(TestUUIDs.TEST_UUIDS[1]);
        projects.add(TestUUIDs.TEST_UUIDS[2]);
        values.put(Favorites.PROPERTY_PROJECTS, projects);
        Map<Class<?>, String[]> requiredProperties = PropertyTestUtil.getRequiredProperties();
        PropertyTestUtil.checkPropertyDefinitions(Favorites.class, requiredProperties, values);
    }

}
