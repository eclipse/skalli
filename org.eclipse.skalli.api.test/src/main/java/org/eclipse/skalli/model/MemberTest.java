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

import org.eclipse.skalli.testutil.PropertyHelper;
import org.eclipse.skalli.testutil.PropertyHelperUtils;
import org.junit.Test;

@SuppressWarnings("nls")
public class MemberTest {

    @Test
    public void testPropertyDefinitions() throws Exception {
        Map<String, Object> values = PropertyHelperUtils.getValues();
        values.put(Member.PROPERTY_USERID, "homer");

        Map<Class<?>, String[]> requiredProperties = PropertyHelperUtils.getRequiredProperties();
        requiredProperties.put(Member.class, new String[] { Member.PROPERTY_USERID });

        PropertyHelper.checkPropertyDefinitions(Member.class, requiredProperties, values);
    }

}
