/*******************************************************************************
 * Copyright (c) 2010 - 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.model.ext.maven.recommendedupdatesites;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.skalli.testutil.PropertyHelper;
import org.eclipse.skalli.testutil.PropertyHelperUtils;
import org.junit.Test;

/**
 *
 */
public class RecommendedUpdateSitesTest {
    final UUID TEST_PROJECT_UUID = PropertyHelperUtils.TEST_UUIDS[0];

    @Test
    public void testPropertyDefinitions() throws Exception {
        Map<String, Object> values = PropertyHelperUtils.getValues();
        values.put(RecommendedUpdateSites.PROPERTY_ID, "test_updatesite");
        values.put(RecommendedUpdateSites.PROPERTY_USERID, "jon");
        values.put(RecommendedUpdateSites.PROPERTY_NAME, "Test Updatesite");
        values.put(RecommendedUpdateSites.PROPERTY_SHORT_NAME, "alias");
        values.put(RecommendedUpdateSites.PROPERTY_DESCIPTION, "This test Updatesite ist for test purpose.");


        List<UpdateSite> updateSites = new ArrayList<UpdateSite>();
        updateSites.add(createUpdateSite("1"));
        updateSites.add(createUpdateSite("2"));
        values.put(RecommendedUpdateSites.PROPERTY_UPDATESITES, updateSites );

        Map<Class<?>, String[]> requiredProperties = Collections.emptyMap();
        PropertyHelper.checkPropertyDefinitions(RecommendedUpdateSites.class, requiredProperties, values);
    }

    /**
     * @return
     */
    private UpdateSite createUpdateSite(String id) {
        UpdateSite us = new UpdateSite();
        us.setName("name"+id);
        us.setGroupId("com.example.recommendedupdatesites."+id);
        us.setArtifactId("com.example.recommendedupdatesites.detail."+id);
        us.setDescription("description for "+ us.getName());
        us.setProjectUUID(TEST_PROJECT_UUID);
        return us;
    }

}
