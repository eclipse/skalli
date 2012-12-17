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
package org.eclipse.skalli.model.ext.linkgroups;

import java.util.LinkedHashSet;
import java.util.Map;

import org.eclipse.skalli.commons.Link;
import org.eclipse.skalli.testutil.PropertyTestUtil;
import org.junit.Test;

@SuppressWarnings("nls")
public class LinkGroupsProjectExtTest {

    private static final String BASE_URL = "http://links.example.org/";

    @Test
    public void testPropertyDefinitions() throws Exception {
        Map<String, Object> values = PropertyTestUtil.getValues();
        LinkedHashSet<LinkGroup> linkGroups = new LinkedHashSet<LinkGroup>();

        LinkGroup linkGroup1 = new LinkGroup();
        linkGroup1.setCaption("Group1");
        linkGroup1.add(new Link(BASE_URL + "1.1", "1.1"));
        linkGroup1.add(new Link(BASE_URL + "1.2", "1.2"));
        linkGroups.add(linkGroup1);

        LinkGroup linkGroup2 = new LinkGroup();
        linkGroup2.setCaption("Group2");
        linkGroup2.add(new Link(BASE_URL + "2.1", "2.1"));
        linkGroup2.add(new Link(BASE_URL + "2.2", "2.2"));
        linkGroups.add(linkGroup2);

        values.put(LinkGroupsProjectExt.PROPERTY_LINKGROUPS, linkGroups);
        Map<Class<?>, String[]> requiredProperties = PropertyTestUtil.getRequiredProperties();

        PropertyTestUtil.checkPropertyDefinitions(LinkGroupsProjectExt.class, requiredProperties, values);
    }

}
