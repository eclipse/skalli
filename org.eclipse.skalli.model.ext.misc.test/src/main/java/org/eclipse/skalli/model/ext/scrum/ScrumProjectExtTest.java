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
package org.eclipse.skalli.model.ext.scrum;

import java.util.Map;
import java.util.TreeSet;

import org.eclipse.skalli.model.Member;
import org.eclipse.skalli.testutil.PropertyTestUtil;
import org.junit.Test;

public class ScrumProjectExtTest {
    @Test
    public void testPropertyDefinitions() throws Exception {
        Map<String, Object> values = PropertyTestUtil.getValues();
        values.put(ScrumProjectExt.PROPERTY_BACKLOG_URL, "http://devinf.example.org/skalli/backlog");
        TreeSet<Member> scrumMasters = new TreeSet<Member>();
        scrumMasters.add(new Member("homer"));
        scrumMasters.add(new Member("marge"));
        values.put(ScrumProjectExt.PROPERTY_SCRUM_MASTERS, scrumMasters);
        TreeSet<Member> productOwners = new TreeSet<Member>();
        productOwners.add(new Member("matt"));
        values.put(ScrumProjectExt.PROPERTY_PRODUCT_OWNERS, productOwners);
        Map<Class<?>, String[]> requiredProperties = PropertyTestUtil.getRequiredProperties();
        PropertyTestUtil.checkPropertyDefinitions(ScrumProjectExt.class, requiredProperties, values);
    }
}
