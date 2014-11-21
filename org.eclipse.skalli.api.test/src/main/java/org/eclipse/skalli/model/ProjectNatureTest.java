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
package org.eclipse.skalli.model;

import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("nls")
public class ProjectNatureTest {

    @Test
    public void testValueOf() {
        Assert.assertEquals(ProjectNature.PROJECT, ProjectNature.valueOf("PROJECT"));
        Assert.assertEquals(ProjectNature.COMPONENT, ProjectNature.valueOf("COMPONENT"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueOf_invalid() {
        ProjectNature.valueOf("foobar");
    }

    @Test
    public void testValues() {
        Assert.assertEquals(ProjectNature.PROJECT, ProjectNature.values()[0]);
        Assert.assertEquals(ProjectNature.COMPONENT, ProjectNature.values()[1]);
    }
}
