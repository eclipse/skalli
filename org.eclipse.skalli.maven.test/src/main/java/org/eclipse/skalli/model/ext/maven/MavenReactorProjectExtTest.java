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

import org.eclipse.skalli.testutil.PropertyTestUtil;
import org.junit.Test;

public class MavenReactorProjectExtTest {

    @Test
    public void testPropertyDefinitions() throws Exception {
        Map<String, Object> values = PropertyTestUtil.getValues();
        values.put(MavenReactorProjectExt.PROPERTY_MAVEN_REACTOR, getReactor());
        Map<Class<?>, String[]> requiredProperties = PropertyTestUtil.getRequiredProperties();
        PropertyTestUtil.checkPropertyDefinitions(MavenReactorProjectExt.class, requiredProperties, values);
    }

    private MavenReactor getReactor() {
        MavenReactor ret = new MavenReactor();
        return ret;
    }

}
