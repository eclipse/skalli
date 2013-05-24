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
package org.eclipse.skalli.services;

import org.junit.Assert;
import org.junit.Test;

public class BundlePropertiesTest {

    @Test
    @SuppressWarnings("nls")
    public void testGetProperty() throws Exception {
        // workdir is defined in o.e.g.skalli.parent/pom.xml -> that should overule skalli.properties
        String workdir = BundleProperties.getProperty("workdir");
        Assert.assertTrue(workdir.endsWith("org.eclipse.skalli.testutil"));

        // anotherProperty is defined in skalli.properties
        String anotherProperty = BundleProperties.getProperty("anotherProperty");
        Assert.assertEquals("value", anotherProperty);

        // unknow property should return null
        String unknownProperty = BundleProperties.getProperty("unknownProperty");
        Assert.assertNull(unknownProperty);

        // unknown property with explicit default
        unknownProperty = BundleProperties.getProperty("unknownProperty", "default");
        Assert.assertEquals("default", unknownProperty);

        // known property with explicit default
        anotherProperty = BundleProperties.getProperty("anotherProperty", "default");
        Assert.assertEquals("value", anotherProperty);
    }

}
