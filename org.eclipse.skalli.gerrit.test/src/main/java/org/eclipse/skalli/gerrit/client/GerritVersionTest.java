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
package org.eclipse.skalli.gerrit.client;

import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("nls")
public class GerritVersionTest {

    @Test
    public void testGetVersion() throws Exception {
        Assert.assertEquals(GerritVersion.GERRIT_2_0_X, GerritVersion.asGerritVersion("2.0"));
        Assert.assertEquals(GerritVersion.GERRIT_2_0_X, GerritVersion.asGerritVersion("2.0-rc0"));
        Assert.assertEquals(GerritVersion.GERRIT_2_0_X, GerritVersion.asGerritVersion("2.0.1"));
        Assert.assertEquals(GerritVersion.GERRIT_2_1_X, GerritVersion.asGerritVersion("2.1"));
        Assert.assertEquals(GerritVersion.GERRIT_2_1_X, GerritVersion.asGerritVersion("2.1-xy"));
        Assert.assertEquals(GerritVersion.GERRIT_2_1_X, GerritVersion.asGerritVersion("2.1.27"));;
        Assert.assertEquals(GerritVersion.GERRIT_2_1_8, GerritVersion.asGerritVersion("2.1.8"));
        Assert.assertEquals(GerritVersion.GERRIT_2_1_8, GerritVersion.asGerritVersion("2.1.8.1"));
        Assert.assertEquals(GerritVersion.GERRIT_2_2_X, GerritVersion.asGerritVersion("2.2"));
        Assert.assertEquals(GerritVersion.GERRIT_2_2_X, GerritVersion.asGerritVersion("2.2.5"));
        Assert.assertEquals(GerritVersion.GERRIT_2_2_X, GerritVersion.asGerritVersion("2.2-rc3"));
        Assert.assertEquals(GerritVersion.GERRIT_2_2_2, GerritVersion.asGerritVersion("2.2.2"));
        Assert.assertEquals(GerritVersion.GERRIT_2_2_2, GerritVersion.asGerritVersion("2.2.2.1"));
        Assert.assertEquals(GerritVersion.GERRIT_2_3_X, GerritVersion.asGerritVersion("2.3"));
        Assert.assertEquals(GerritVersion.GERRIT_2_3_X, GerritVersion.asGerritVersion("2.3-abc"));
        Assert.assertEquals(GerritVersion.GERRIT_2_3_X, GerritVersion.asGerritVersion("2.3.12"));
        Assert.assertEquals(GerritVersion.GERRIT_2_4_X, GerritVersion.asGerritVersion("2.4"));
        Assert.assertEquals(GerritVersion.GERRIT_2_4_X, GerritVersion.asGerritVersion("2.4-rc0"));
        Assert.assertEquals(GerritVersion.GERRIT_2_4_X, GerritVersion.asGerritVersion("2.4.47.11"));

        Assert.assertEquals(GerritVersion.GERRIT_UNKNOWN_VERSION, GerritVersion.asGerritVersion(null));
        Assert.assertEquals(GerritVersion.GERRIT_UNKNOWN_VERSION, GerritVersion.asGerritVersion(""));
        Assert.assertEquals(GerritVersion.GERRIT_2_4_X, GerritVersion.asGerritVersion("7.0"));
        Assert.assertEquals(GerritVersion.GERRIT_2_4_X, GerritVersion.asGerritVersion("foobar"));
    }

    @Test
    public void testSupports() throws Exception {
        Assert.assertFalse(GerritVersion.GERRIT_2_1_8.supports(GerritFeature.LS_GROUPS));
        Assert.assertTrue(GerritVersion.GERRIT_2_2_2.supports(GerritFeature.LS_GROUPS));
        Assert.assertFalse(GerritVersion.GERRIT_2_2_2.supports(GerritFeature.LS_PROJECTS_ALL_ATTR));
        Assert.assertTrue(GerritVersion.GERRIT_2_3_X.supports(GerritFeature.LS_PROJECTS_ALL_ATTR));
        Assert.assertTrue(GerritVersion.GERRIT_2_4_X.supports(GerritFeature.LS_PROJECTS_ALL_ATTR));
        Assert.assertFalse(GerritVersion.GERRIT_2_5_X.supports(GerritFeature.SUPPORTS_REST));
        Assert.assertTrue(GerritVersion.GERRIT_2_6_X.supports(GerritFeature.SUPPORTS_REST));
        Assert.assertFalse(GerritVersion.GERRIT_2_7_X.supports(GerritFeature.CREATE_PROJECT_MAX_SIZE));
        Assert.assertTrue(GerritVersion.GERRIT_2_8_X.supports(GerritFeature.CREATE_PROJECT_MAX_SIZE));
        Assert.assertFalse(GerritVersion.GERRIT_2_8_X.supports(GerritFeature.CREATE_PROJECT_PLUGIN_CONFIG));
        Assert.assertTrue(GerritVersion.GERRIT_2_9_X.supports(GerritFeature.CREATE_PROJECT_PLUGIN_CONFIG));
    }
}
