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
package org.eclipse.skalli.model.ext.maven;

import java.util.Map;
import java.util.TreeSet;

import org.junit.Test;

import org.eclipse.skalli.model.ext.maven.MavenModule;
import org.eclipse.skalli.model.ext.maven.MavenReactor;
import org.eclipse.skalli.testutil.PropertyHelper;
import org.eclipse.skalli.testutil.PropertyHelperUtils;

public class MavenReactorTest {

    @Test
    public void testPropertyDefinitions() throws Exception {
        Map<String, Object> values = PropertyHelperUtils.getValues();

        values.put(MavenReactor.PROPERTY_COORDINATE, MavenCoordinateUtil.TEST_COORD);

        TreeSet<MavenModule> modules = new TreeSet<MavenModule>();
        modules.addAll(MavenCoordinateUtil.TEST_MODULES);

        values.put(MavenReactor.PROPERTY_MODULES, modules);

        Map<Class<?>, String[]> requiredProperties = PropertyHelperUtils.getRequiredProperties();
        PropertyHelper.checkPropertyDefinitions(MavenReactor.class, requiredProperties, values);
    }

}
