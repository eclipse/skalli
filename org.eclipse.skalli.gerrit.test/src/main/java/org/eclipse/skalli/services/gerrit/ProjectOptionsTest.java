/*******************************************************************************
 * Copyright (c) 2010-2015 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.services.gerrit;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.eclipse.skalli.testutil.AssertUtils;
import org.junit.Test;

@SuppressWarnings("nls")
public class ProjectOptionsTest {

    @Test
    public void testBasics() throws Exception {
        ProjectOptions options = new ProjectOptions();
        options.setName("skalli");
        options.setBranches(Arrays.asList("foo", "bar"));
        options.setOwners(Arrays.asList("c", "a", "b"));
        options.setParent("foo bar");
        options.setPermissionsOnly(true);
        options.setDescription("test project");
        options.setSubmitType(SubmitType.REBASE_IF_NECESSARY);
        options.setUseContributorAgreements(InheritableBoolean.TRUE);
        options.setUseSignedOffBy(InheritableBoolean.INHERIT);
        options.setRequiredChangeId(InheritableBoolean.TRUE);
        options.setUseContentMerge(InheritableBoolean.FALSE);
        options.setCreateEmptyCommit(true);
        options.setMaxObjectSizeLimit("1m");
        options.putPluginConfig("x", "c", "d");
        options.putPluginConfig("y", "a", "d");
        options.putPluginConfig("x", "a", "b");

        // put some plugin entries with null or blank arguments
        options.putPluginConfig(null, "a", "b");
        options.putPluginConfig("", "a", "b");
        options.putPluginConfig("x", null, "b");
        options.putPluginConfig("x", "", "b");
        options.putPluginConfig("x", "a", null);
        options.putPluginConfig("x", "a", "");

        assertEquals("skalli", options.getName());
        AssertUtils.assertEquals("getBranches", options.getBranches(), "bar", "foo");
        AssertUtils.assertEquals("getOwners", options.getOwners(), "a", "b", "c");
        assertEquals("foo bar", options.getParent());
        assertTrue(options.isPermissionsOnly());
        assertEquals("test project", options.getDescription());
        assertEquals(SubmitType.REBASE_IF_NECESSARY, options.getSubmitType());
        assertEquals(InheritableBoolean.TRUE, options.getUseContributorAgreements());
        assertEquals(InheritableBoolean.INHERIT, options.getUseSignedOffBy());
        assertEquals(InheritableBoolean.TRUE, options.getRequiredChangeId());
        assertEquals(InheritableBoolean.FALSE, options.getUseContentMerge());
        assertTrue(options.isCreateEmptyCommit());
        assertEquals("1m", options.getMaxObjectSizeLimit());
        AssertUtils.assertEquals("getPluginConfigKeys", options.getPluginConfigKeys(), "x", "y");
        assertEquals("b", options.getPluginConfig("x").get("a"));
        assertEquals("d", options.getPluginConfig("x").get("c"));
        assertEquals("d", options.getPluginConfig("y").get("a"));
        assertNull(options.getPluginConfig("z"));
        assertNull(options.getPluginConfig("x").get("foo"));
        assertNull(options.getPluginConfig(null));
        assertNull(options.getPluginConfig(""));
        assertEquals("b", options.getPluginConfigValue("x", "a"));
        assertEquals("d", options.getPluginConfigValue("x", "c"));
        assertEquals("d", options.getPluginConfigValue("y", "a"));
        assertNull(options.getPluginConfigValue("z", "a"));
        assertNull(options.getPluginConfigValue("x", "foo"));
        assertNull(options.getPluginConfigValue("x", null));
        assertNull(options.getPluginConfigValue("x", ""));
        assertNull(options.getPluginConfigValue(null, "a"));
        assertNull(options.getPluginConfigValue("", "a"));
    }

}
