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
