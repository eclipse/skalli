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

import java.util.Map;

import org.eclipse.skalli.testutil.PropertyTestUtil;
import org.junit.Test;

@SuppressWarnings("nls")
public class MemberTest {

    @Test
    public void testPropertyDefinitions() throws Exception {
        Map<String, Object> values = PropertyTestUtil.getValues();
        values.put(Member.PROPERTY_USERID, "homer");

        Map<Class<?>, String[]> requiredProperties = PropertyTestUtil.getRequiredProperties();
        requiredProperties.put(Member.class, new String[] { Member.PROPERTY_USERID });

        PropertyTestUtil.checkPropertyDefinitions(Member.class, requiredProperties, values);
    }

}
