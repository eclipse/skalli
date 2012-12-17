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
import org.eclipse.skalli.testutil.PropertyTestUtil;
import org.junit.Test;

@SuppressWarnings("nls")
public class TagsExtensionTest {

    @Test
    public void testPropertyDefinitions() throws Exception {
        Map<String, Object> values = PropertyTestUtil.getValues();

        Map<Class<?>, String[]> requiredProperties = PropertyTestUtil.getRequiredProperties();
        SortedSet<String> tags = CollectionUtils.asSortedSet("skalli", "osgi", "foo", "bar");
        values.put(TagsExtension.PROPERTY_TAGS, tags);
        PropertyTestUtil.checkPropertyDefinitions(TagsExtension.class, requiredProperties, values);
    }

}
