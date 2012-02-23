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
import java.util.TreeSet;

import org.eclipse.skalli.testutil.PropertyHelper;
import org.eclipse.skalli.testutil.PropertyHelperUtils;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("nls")
public class ProjectTest {

    @Test
    public void testPropertyDefinitions() throws Exception {
        Map<String, Object> values = PropertyHelperUtils.getValues();
        values.put(Project.PROPERTY_PROJECTID, "eclipse.skalli");
        values.put(Project.PROPERTY_TEMPLATEID, "default");
        values.put(Project.PROPERTY_PARENT_PROJECT, null);
        values.put(Project.PROPERTY_NAME, "Skalli");
        values.put(Project.PROPERTY_DESCRIPTION_FORMAT, "text");
        values.put(Project.PROPERTY_DESCRIPTION, "The goal of the project is to build an extensible" +
                " system for organizing projects.");
        TreeSet<String> tags = new TreeSet<String>();
        tags.add("springfield");
        tags.add("cartoon");
        values.put(Project.PROPERTY_LOGO_URL, "http://example.org/logo");
        values.put(Project.PROPERTY_PHASE, "Gone Crazy!");
        values.put(Project.PROPERTY_REGISTERED, System.currentTimeMillis());
        values.put(Project.PROPERTY_SHORT_NAME, "ECLSKA");
        Map<Class<?>, String[]> requiredProperties = PropertyHelperUtils.getRequiredProperties();
        PropertyHelper.checkPropertyDefinitions(Project.class, requiredProperties, values);
    }

    @Test
    public void testGetOrConstructShortName() {
        Project p = new Project();
        p.setProjectId("hello.world");

        p.setName("A simple Test Project");
        Assert.assertEquals("AsTP", p.getOrConstructShortName());

        p.setName("A very simple Test Project with an extremely long name that exceeds the limits");
        Assert.assertEquals("AvsTPwaeln", p.getOrConstructShortName());

        p.setName("A simple - Test Project");
        Assert.assertEquals("AsTP", p.getOrConstructShortName());

        p.setName("Project ()");
        Assert.assertEquals("Project", p.getOrConstructShortName());

        p.setName("TestProject");
        Assert.assertEquals("TestProjec", p.getOrConstructShortName());

        p.setName("Test(Project");
        Assert.assertEquals("TestProjec", p.getOrConstructShortName());

        p.setName("Test-Project");
        Assert.assertEquals("TestProjec", p.getOrConstructShortName());

        p.setName("$$()");
        Assert.assertEquals("helloworld", p.getOrConstructShortName());
    }
}
