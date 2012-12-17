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
package org.eclipse.skalli.model.ext.devinf;

import java.util.LinkedHashSet;
import java.util.Map;

import org.eclipse.skalli.testutil.PropertyTestUtil;
import org.junit.Test;

public class DevInfProjectExtTest {

    private static final String URL = "http://devinf.example.org/";

    @Test
    public void testPropertyDefinitions() throws Exception {
        Map<String, Object> values = PropertyTestUtil.getValues();
        values.put(DevInfProjectExt.PROPERTY_SCM_URL, URL + "scm");
        values.put(DevInfProjectExt.PROPERTY_BUGTRACKER_URL, URL + "bugtracker");
        values.put(DevInfProjectExt.PROPERTY_CI_URL, URL + "ci");
        values.put(DevInfProjectExt.PROPERTY_METRICS_URL, URL + "metrics");
        values.put(DevInfProjectExt.PROPERTY_REVIEW_URL, URL + "review");
        LinkedHashSet<String> scmLocations = new LinkedHashSet<String>();
        scmLocations.add("scm:repo:" + URL + "repo1");
        scmLocations.add("scm:repo:" + URL + "repo2");
        values.put(DevInfProjectExt.PROPERTY_SCM_LOCATIONS, scmLocations);
        LinkedHashSet<String> javadocs = new LinkedHashSet<String>();
        javadocs.add(URL + "javadoc1");
        javadocs.add(URL + "javadoc2");
        javadocs.add(URL + "javadoc3");
        values.put(DevInfProjectExt.PROPERTY_JAVADOCS_URL, javadocs);
        Map<Class<?>, String[]> requiredProperties = PropertyTestUtil.getRequiredProperties();

        PropertyTestUtil.checkPropertyDefinitions(DevInfProjectExt.class, requiredProperties, values);
    }

}
