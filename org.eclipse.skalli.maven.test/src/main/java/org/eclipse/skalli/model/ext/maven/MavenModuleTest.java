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
package org.eclipse.skalli.model.ext.maven;

import static org.eclipse.skalli.model.ext.maven.MavenCoordinateUtil.TEST_COORD;
import static org.junit.Assert.*;

import org.eclipse.skalli.testutil.AssertUtils;
import org.junit.Test;

@SuppressWarnings("nls")
public class MavenModuleTest {

    @Test
    public void testConstructor() throws Exception {
        MavenCoordinate c =  new MavenModule(MavenCoordinateUtil.GROUPID,
                MavenCoordinateUtil.ARTIFACT, MavenCoordinateUtil.PACKAGING);
        assertEquals(MavenCoordinateUtil.GROUPID, c.getGroupId());
        assertEquals(MavenCoordinateUtil.ARTIFACT, c.getArtefactId());
        assertEquals(MavenCoordinateUtil.PACKAGING, c.getPackaging());
    }

    @Test
    public void testConstructor_defaultPackaing() throws Exception {
        MavenCoordinate c =  new MavenModule(MavenCoordinateUtil.GROUPID,
                MavenCoordinateUtil.ARTIFACT, null);
        assertEquals(MavenCoordinateUtil.GROUPID, c.getGroupId());
        assertEquals(MavenCoordinateUtil.ARTIFACT, c.getArtefactId());
        assertEquals("jar", c.getPackaging());
    }


    @Test
    public void testCopyConstructor() throws Exception {
        MavenModule original = new MavenModule(TEST_COORD);
        original.addVersion("1.0");
        MavenModule clone = new MavenModule(original);

        // ensure that original and clone are equal
        assertEquals(original, clone);

        // now change some attributes
        clone.setGroupId("com.example.test");
        clone.setArtefactId("foobar");
        clone.setPackaging("zip");
        clone.addVersion("2.0");

        // check that original is untouched
        assertEquals(MavenCoordinateUtil.GROUPID, original.getGroupId());
        assertEquals(MavenCoordinateUtil.ARTIFACT, original.getArtefactId());
        assertEquals(MavenCoordinateUtil.PACKAGING, original.getPackaging());
        AssertUtils.assertEquals("getVersions()", original.getVersions(), "1.0");
    }

    @Test
    public void testBasics() {
        MavenModule clone = new MavenModule(TEST_COORD);
        assertEquals(MavenCoordinateUtil.GROUPID, clone.getGroupId());
        assertEquals(MavenCoordinateUtil.ARTIFACT, clone.getArtefactId());
        assertEquals(MavenCoordinateUtil.PACKAGING, clone.getPackaging());
        assertTrue(clone.getVersions().isEmpty());
        clone.addVersion("2.0");
        clone.addVersion("0.1");
        clone.addVersion("1.0");
        AssertUtils.assertEquals("getVersions()", clone.getVersions(), "0.1", "1.0", "2.0");
        clone.removeVersion("1.0");
        AssertUtils.assertEquals("getVersions()", clone.getVersions(), "0.1", "2.0");
        assertTrue(clone.hasVersion("0.1"));
    }

    @Test
    public void testCompareAndEquals() throws Exception {
        MavenModule left = TEST_COORD;
        MavenModule right = new MavenModule("com.example.test", MavenCoordinateUtil.ARTIFACT,
                MavenCoordinateUtil.PACKAGING);
        assertGreater(left, right);

        right = new MavenModule(MavenCoordinateUtil.GROUPID, "b", MavenCoordinateUtil.PACKAGING);
        assertLower(left, right);

        right = new MavenModule(MavenCoordinateUtil.GROUPID, MavenCoordinateUtil.ARTIFACT, "eclipse-plugin");
        assertGreater(left, right);

        right = new MavenModule(null, MavenCoordinateUtil.ARTIFACT, MavenCoordinateUtil.PACKAGING);
        assertGreater(left, right);

        right = new MavenModule(MavenCoordinateUtil.GROUPID, null, MavenCoordinateUtil.PACKAGING);
        assertGreater(left, right);

        right = new MavenModule(MavenCoordinateUtil.GROUPID, MavenCoordinateUtil.ARTIFACT, null);
        assertEqual(left, right);

        right = new MavenModule(MavenCoordinateUtil.GROUPID.toUpperCase(), MavenCoordinateUtil.ARTIFACT,
                MavenCoordinateUtil.PACKAGING);
        assertTrue(left.compareTo(right) != 0);
        assertFalse(left.equals(right));
        assertFalse(right.equals(left));

        right = left;
        assertEqual(left, right);
    }

    @Test
    public void testCompareWithVersions() throws Exception {
        MavenModule left = new MavenModule(TEST_COORD);
        left.addVersion("1.0");
        left.addVersion("2.0");
        MavenModule right = new MavenModule(TEST_COORD);
        right.addVersion("1.0");
        right.addVersion("1.5");
        right.addVersion("2.0");
        assertGreater(left, right);
        left.addVersion("1.5");
        assertEquals(left, right);
    }

    @Test
    public void testMultipleVersions() throws Exception {
        MavenModule mm = new MavenModule(TEST_COORD);
        mm.addVersion("1.0.0");
        assertEquals(1, mm.getVersions().size());
        mm.addVersion("2.0.0");
        assertEquals(2, mm.getVersions().size());
        mm.addVersion("2.0.0");
        assertEquals(2, mm.getVersions().size());
    }


    private void assertGreater(MavenModule left, MavenModule right) {
        assertTrue(left.compareTo(right) > 0);
        assertTrue(right.compareTo(left) < 0);
        assertFalse(left.equals(right));
        assertFalse(right.equals(left));
    }

    private void assertLower(MavenModule left, MavenModule right) {
        assertGreater(right, left);
    }

    private void assertEqual(MavenModule left, MavenModule right) {
        assertTrue(right.compareTo(left) == 0);
        assertTrue(left.compareTo(right) == 0);
        assertTrue(left.equals(right));
        assertTrue(right.equals(left));
    }
}
