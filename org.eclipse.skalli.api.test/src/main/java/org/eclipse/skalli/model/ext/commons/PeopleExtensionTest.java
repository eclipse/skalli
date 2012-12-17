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
package org.eclipse.skalli.model.ext.commons;

import java.util.Map;
import java.util.TreeSet;

import org.eclipse.skalli.model.Member;
import org.eclipse.skalli.testutil.PropertyTestUtil;
import org.junit.Test;

@SuppressWarnings("nls")
public class PeopleExtensionTest {
    @Test
    public void testPropertyDefinitions() throws Exception {
        Map<String, Object> values = PropertyTestUtil.getValues();

        TreeSet<Member> members = new TreeSet<Member>();
        Member HOMER = new Member("homer");
        Member MARGE = new Member("marge");
        members.add(HOMER);
        members.add(MARGE);
        members.add(new Member("maggy"));
        members.add(new Member("bart"));
        members.add(new Member("lisa"));
        values.put(PeopleExtension.PROPERTY_MEMBERS, members);
        TreeSet<Member> leads = new TreeSet<Member>();
        leads.add(HOMER);
        leads.add(MARGE);
        values.put(PeopleExtension.PROPERTY_LEADS, leads);

        Map<Class<?>, String[]> requiredProperties = PropertyTestUtil.getRequiredProperties();

        PropertyTestUtil.checkPropertyDefinitions(PeopleExtension.class, requiredProperties, values);
    }

}
