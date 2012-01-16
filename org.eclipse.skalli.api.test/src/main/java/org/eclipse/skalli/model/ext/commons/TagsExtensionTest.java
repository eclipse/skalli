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
package org.eclipse.skalli.model.ext.commons;

import java.util.Map;
import java.util.SortedSet;

import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.testutil.PropertyHelper;
import org.eclipse.skalli.testutil.PropertyHelperUtils;
import org.junit.Test;

@SuppressWarnings("nls")
public class TagsExtensionTest {

    @Test
    public void testPropertyDefinitions() throws Exception {
        Map<String, Object> values = PropertyHelperUtils.getValues();

        Map<Class<?>, String[]> requiredProperties = PropertyHelperUtils.getRequiredProperties();
        SortedSet<String> tags = CollectionUtils.asSortedSet("skalli", "osgi", "foo", "bar");
        values.put(TagsExtension.PROPERTY_TAGS, tags);
        PropertyHelper.checkPropertyDefinitions(TagsExtension.class, requiredProperties, values);
    }

}
