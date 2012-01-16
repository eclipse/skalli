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
package org.eclipse.skalli.core.internal.users;

import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("nls")
public class NormalizeUtilTest {

    @Test
    public void test() {
        Assert.assertEquals("a", NormalizeUtil.normalize("á"));
        Assert.assertEquals("a", NormalizeUtil.normalize("â"));
        Assert.assertEquals("i", NormalizeUtil.normalize("ï"));
        Assert.assertEquals("c", NormalizeUtil.normalize("ç"));

        Assert.assertEquals("ae", NormalizeUtil.normalize("ä"));
        Assert.assertEquals("oe", NormalizeUtil.normalize("ö"));
        Assert.assertEquals("ue", NormalizeUtil.normalize("ü"));
        Assert.assertEquals("Ae", NormalizeUtil.normalize("Ä"));
        Assert.assertEquals("Oe", NormalizeUtil.normalize("Ö"));
        Assert.assertEquals("Ue", NormalizeUtil.normalize("Ü"));
        Assert.assertEquals("ss", NormalizeUtil.normalize("ß"));
    }

}
