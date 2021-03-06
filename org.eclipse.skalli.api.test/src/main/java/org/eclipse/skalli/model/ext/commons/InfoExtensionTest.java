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
package org.eclipse.skalli.model.ext.commons;

import java.util.LinkedHashSet;
import java.util.Map;

import org.eclipse.skalli.testutil.PropertyTestUtil;
import org.junit.Test;

public class InfoExtensionTest {

    private static final String PAGE_URL = "http://example.org/home"; //$NON-NLS-1$

    @Test
    public void testPropertyDefinitions() throws Exception {
        Map<String, Object> values = PropertyTestUtil.getValues();

        values.put(InfoExtension.PROPERTY_PAGE_URL, PAGE_URL);
        LinkedHashSet<String> mailingLists = new LinkedHashSet<String>();
        mailingLists.add("homer@listserv.springfield.net");
        mailingLists.add("marge@listserv.springfield.net");
        values.put(InfoExtension.PROPERTY_MAILING_LIST, mailingLists);

        Map<Class<?>, String[]> requiredProperties = PropertyTestUtil.getRequiredProperties();

        PropertyTestUtil.checkPropertyDefinitions(InfoExtension.class, requiredProperties, values);

    }
}
