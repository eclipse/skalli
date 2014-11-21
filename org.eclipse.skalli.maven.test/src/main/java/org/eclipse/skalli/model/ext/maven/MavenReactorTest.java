/*******************************************************************************
 * Copyright (c) 2010-2014 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.model.ext.maven;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;
import java.util.TreeSet;

import org.eclipse.skalli.testutil.PropertyTestUtil;
import org.junit.Test;

public class MavenReactorTest {

    @Test
    public void testPropertyDefinitions() throws Exception {
        Map<String, Object> values = PropertyTestUtil.getValues();

        values.put(MavenReactor.PROPERTY_COORDINATE, MavenCoordinateUtil.TEST_COORD);

        TreeSet<MavenModule> modules = new TreeSet<MavenModule>();
        modules.addAll(MavenCoordinateUtil.TEST_MODULES);

        values.put(MavenReactor.PROPERTY_MODULES, modules);

        Map<Class<?>, String[]> requiredProperties = PropertyTestUtil.getRequiredProperties();
        PropertyTestUtil.checkPropertyDefinitions(MavenReactor.class, requiredProperties, values);
    }

    @Test
    public  void testAddModules(){
        MavenReactor r = new MavenReactor();
        r.addModule(new MavenModule("g1", "a1", null));
        assertThat(r.getModules().size(), is(1));


        r.addModule(new MavenModule("g1", "a1", null));
        assertThat(r.getModules().size(), is(1));

        r.addModule(new MavenModule("g1", "a2", null));
        assertThat(r.getModules().size(), is(2));
        assertThat(r.getModules(), hasItem(new MavenModule("g1", "a2", null)));

        r.addModule(new MavenModule("g2", "a1", null));
        assertThat(r.getModules().size(), is(3));
    }

}
