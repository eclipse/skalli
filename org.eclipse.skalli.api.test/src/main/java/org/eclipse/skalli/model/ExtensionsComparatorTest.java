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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.skalli.testutil.TestExtension;
import org.eclipse.skalli.testutil.TestExtension1;
import org.junit.Test;

public class ExtensionsComparatorTest {

    @Test
    public void testCompare() throws Exception {
        TestExtension ext1 = new TestExtension();
        TestExtension1 ext2 = new TestExtension1();
        ExtensionsComparator c = new ExtensionsComparator();
        assertEquals(0, c.compare(ext1, ext1));
        assertEquals(0, c.compare(ext2, ext2));
        assertTrue(c.compare(ext1, ext2) < 0);
        assertTrue(c.compare(ext2, ext1) > 0);
        assertTrue(c.compare(ext1, null) < 0);
        assertTrue(c.compare(null, ext1) > 0);
        assertTrue(c.compare(null, null) == 0);
    }

}
