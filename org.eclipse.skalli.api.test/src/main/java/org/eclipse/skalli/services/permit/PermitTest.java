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
package org.eclipse.skalli.services.permit;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.skalli.services.permit.Permit.Level;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("nls")
public class PermitTest {

    private static final Permit[] PERMITS = new Permit[] {
        Permit.valueOf("ALLOW GET /projects"),
        Permit.valueOf("ALLOW GET /"),
        Permit.valueOf("FORBID GET /projects"),
        Permit.valueOf("ALLOW GET /**"),
        Permit.valueOf("ALLOW GET /projects/foobar"),
        Permit.valueOf("FORBID PUT /projects/foobar"),
        Permit.valueOf("FORBID GET /projects/foobar"),
        Permit.valueOf("FORBID PUT /projects/**/props"),
        Permit.valueOf("ALLOW PUT /projects/**/props/prop"),
        Permit.valueOf("FORBID PUT /projects/**/props/prop"),
        Permit.valueOf("ALLOW PUT /projects/**/props/**"),
        Permit.valueOf("FORBID DELETE /projects/foobar"),
        Permit.valueOf("FORBID POST /projects/foobar"),
        Permit.valueOf("FORBID XYZ /projects/foobar"),
        Permit.valueOf("ALLOW PUT /projects/foobar/props/prop"),
        Permit.valueOf("FORBID PUT /projects/foobar/props/prop"),
        Permit.valueOf("ALLOW GET /projects/foobar/props/prop"),
        Permit.valueOf("ALLOW POST /projects/foobar/props/prop"),
        Permit.valueOf("ALLOW DELETE /projects/foobar/props/prop"),
        Permit.valueOf("ALLOW PUT /projects/**/props/prop"),
        Permit.valueOf("ALLOW GET /projects/foobar/a/b/c"),
        Permit.valueOf("ALLOW ** /projects/foobar/a/b/c"),
        Permit.valueOf("FORBID ** /projects/foobar/a/b/c"),
        Permit.valueOf("FORBID GET /projects/foobar/a/b/c"),
        Permit.valueOf("10 GET /projects/foobar/a/b/c")
    };

    private static final List<Permit> EXPECTED_SORTED_PERMITS = Arrays.asList(
        Permit.valueOf("10 GET /projects/foobar/a/b/c"),
        Permit.valueOf("FORBID ** /projects/foobar/a/b/c"),
        Permit.valueOf("ALLOW DELETE /projects/foobar/props/prop"),
        Permit.valueOf("ALLOW GET /projects/foobar/props/prop"),
        Permit.valueOf("ALLOW POST /projects/foobar/props/prop"),
        Permit.valueOf("FORBID PUT /projects/foobar/props/prop"),
        Permit.valueOf("ALLOW PUT /projects/**/props/prop"),
        Permit.valueOf("ALLOW PUT /projects/**/props/**"),
        Permit.valueOf("FORBID PUT /projects/**/props"),
        Permit.valueOf("FORBID DELETE /projects/foobar"),
        Permit.valueOf("FORBID GET /projects/foobar"),
        Permit.valueOf("FORBID POST /projects/foobar"),
        Permit.valueOf("FORBID PUT /projects/foobar"),
        Permit.valueOf("FORBID XYZ /projects/foobar"),
        Permit.valueOf("FORBID GET /projects"),
        Permit.valueOf("ALLOW GET /**"),
        Permit.valueOf("ALLOW GET /")
    );

    private static final PermitSet SORTED_PERMITS = new PermitSet(PERMITS);


    @Test
    public void testValueOf() throws Exception {
        assertPermit(1, "GET", "/projects", Permit.valueOf("ALLOW GET /projects"));
        assertPermit(0, "GET", "/projects", Permit.valueOf("FORBID GET /projects"));
        assertPermit(1, "PUT", "/projects", Permit.valueOf("ALLOW PUT /projects"));
        assertPermit(1, "GET", "/projects/foobar", Permit.valueOf("ALLOW GET /projects/foobar"));
        assertPermit(1, "GET", "/projects/**/props", Permit.valueOf("ALLOW GET /projects/**/props"));
        assertPermit(1, "GET", "/", Permit.valueOf("ALLOW GET /"));
        assertPermit(1, "GET", "/**", Permit.valueOf("ALLOW GET **"));
        assertPermit(0, "PUT", "/projects", Permit.valueOf("FORBID PUT projects"));
        assertPermit(0, "PUT", "/projects", Permit.valueOf("0 put /projects"));
        assertPermit(1, "PUT", "/projects", Permit.valueOf("1 put /projects"));
        assertPermit(1, "PUT", "/projects", Permit.valueOf("+1 put /projects"));
        assertPermit(15, "PUT", "/projects", Permit.valueOf("15 put /projects"));
        assertPermit(0, "PUT", "/projects", Permit.valueOf("-1 put /projects"));
        assertPermit(0, "PUT", "/projects", Permit.valueOf("forbid put /projects"));
        assertPermit(1, "GET", "/projects", Permit.valueOf("allow get /projects"));
        assertPermit(0, "XYZ", "/projects", Permit.valueOf("FORBID xyz projects"));
        assertPermit(0, "PUT", "/projects", Permit.valueOf("FORBID  \t  PUT    \n  /projects \t   "));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValueOfIllegalArgument() throws Exception {
        Permit.valueOf("PUT");
        Permit.valueOf("PUT /projects");
        Permit.valueOf("PUT /projects     ");
        Permit.valueOf("PUT /projects\t");
        Permit.valueOf("ALLOW  PUT /projects foobar");
        Permit.valueOf("ALLOW PUT /projects /**");
        Permit.valueOf(null);
        Permit.valueOf("");
        Permit.valueOf("   ");
    }

    @Test(expected=NumberFormatException.class)
    public void testValueOfIllegalLevel() throws Exception {
        Permit.valueOf("foobar GET /projects");
    }

    @Test
    public void testConstructor() throws Exception {
        assertPermit(0, "**", "/", new Permit());
        assertPermit(0, "**", "/", new Permit(-1, null, (String)null));
        assertPermit(0, "**", "/", new Permit(-1, null, (String[])null));
        assertPermit(0, "**", "/", new Permit(-1, null, (String[])null));
        assertPermit(0, "**", "/", new Permit(-1, "", ""));

        assertPermit(1, "GET", "/projects", new Permit(Level.ALLOW, "GET", "/projects"));
        assertPermit(0, "GET", "/projects",  new Permit(Level.FORBID, "GET", "/projects"));
        assertPermit(1, "PUT", "/projects", new Permit(Level.ALLOW, "PUT", "/projects"));
        assertPermit(1, "GET", "/projects/foobar", new Permit(Level.ALLOW, "GET", "/projects/foobar"));
        assertPermit(1, "GET", "/projects/**/props", new Permit(Level.ALLOW, "GET", "/projects/**/props"));
        assertPermit(1, "GET", "/", new Permit(Level.ALLOW, "GET", "/"));
        assertPermit(1, "GET", "/**", new Permit(Level.ALLOW, "GET", "**"));
        assertPermit(0, "PUT", "/projects",  new Permit(Level.FORBID, "PUT", "projects"));
        assertPermit(0, "PUT", "/projects", new Permit(0, "put", "/projects"));
        assertPermit(1, "PUT", "/projects", new Permit(1, "put", "/projects"));
        assertPermit(15, "PUT", "/projects", new Permit(15, "put", "/projects"));
        assertPermit(0, "PUT", "/projects", new Permit(-1, "put", "/projects"));
        assertPermit(0, "PUT", "/projects",  new Permit(Level.FORBID, "put", "/projects"));
        assertPermit(1, "GET", "/projects", new Permit(Level.ALLOW, "get", "/projects"));
        assertPermit(0, "XYZ", "/projects",  new Permit(Level.FORBID, "xyz", "projects"));
    }

    @Test
    public void testGetSegments() throws Exception {
        assertSegments("\nprojects/   foo/ **/bar\t", "projects", "foo", "**", "bar");
        assertSegments("/projects/foo/**/bar", "projects", "foo", "**", "bar");
        assertSegments("/projects", "projects");
        assertSegments("projects", "projects");
        assertSegments("/", new String[]{});
        assertSegments("", new String[]{});
        assertSegments(null, new String[]{});
    }

    private void assertSegments(String path, String...expected) {
        Permit permit = new Permit();
        permit.setPath(path);
        String[] segments = permit.getSegments();
        Assert.assertArrayEquals(expected, segments);
    }

    @Test
    public void testGetSetPath() throws Exception {
        Permit permit = new Permit();
        permit.setPath("\nprojects/   foo/ ** /bar");
        assertEquals("/projects/foo/**/bar", permit.getPath());
    }

    @Test
    public void testPermitSet() throws Exception {
        assertEquals(25, PERMITS.length);
        assertEquals(17, SORTED_PERMITS.size());
        assertEqualsPermits(EXPECTED_SORTED_PERMITS, SORTED_PERMITS);
    }

    @Test
    public void testEquals() throws Exception {
        for (int i = 0, j = PERMITS.length - 1; i < PERMITS.length; ++i, --j) {
            assertEquals(i == j, PERMITS[i].equals(PERMITS[j]));
        }
    }

    @Test
    public void testMatch() throws Exception {
        for (Permit permit : SORTED_PERMITS) {
            assertTrue(Permit.match(SORTED_PERMITS, permit.getLevel(), permit.getAction(), permit.getSegments()));
        }
        assertFalse(matchPermit("ALLOW PUT /")); // implicit FORBID ** /
        assertFalse(matchPermit("ALLOW PUT /**")); // implicit FORBID ** /
        assertFalse(matchPermit("ALLOW PUT /foobar")); // implicit FORBID ** /

        assertTrue(matchPermit("ALLOW GET /")); // ALLOW GET /
        assertTrue(matchPermit("ALLOW GET /foobar")); // ALLOW GET /**
        assertFalse(matchPermit("ALLOW GET /projects")); // FORBID GET /projects

        assertTrue(matchPermit("ALLOW PUT /projects/a/props/b")); // ALLOW PUT /projects/**/props/**
        assertFalse(matchPermit("ALLOW PUT /projects/foobar/props/prop")); // FORBID PUT /projects/foobar/props/prop
        assertTrue(matchPermit("ALLOW PUT /projects/foobar/props/b")); // ALLOW PUT /projects/**/props/**

        assertTrue(matchPermit("9 GET /projects/foobar/a/b/c"));
        assertTrue(matchPermit("10 GET /projects/foobar/a/b/c"));
        assertFalse(matchPermit("11 GET /projects/foobar/a/b/c"));
    }

    private boolean matchPermit(String s) {
        return Permit.match(SORTED_PERMITS, Permit.valueOf(s));
    }

    private void assertPermit(int level, String action, String path, Permit permit) {
        assertEquals(level, permit.getLevel());
        assertEquals(action, permit.getAction());
        assertEquals(path, permit.getPath());
    }

    private void assertEqualsPermits(List<Permit> list, PermitSet set) {
        Assert.assertEquals("size", list.size(), set.size());
        Iterator<Permit> it1 = list.iterator();
        Iterator<Permit> it2 = set.iterator();
        while (it1.hasNext()) {
            Permit next1 = it1.next();
            Permit next2 = it2.next();
            assertThat(next1, IsSamePermit.samePermit(next2));
        }
    }

    private static class IsSamePermit extends BaseMatcher<Permit> {
        private final Permit object;

        public IsSamePermit(Permit object) {
            this.object = object;
        }

        @Override
        public boolean matches(Object arg) {
            if (!(arg instanceof Permit)) {
                return false;
            }
            Permit permit = (Permit) arg;
            return object.getAction().equals(permit.getAction())
                    && object.getPath().equals(permit.getPath())
                    && object.getLevel() == permit.getLevel();
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("equals(").appendValue(object).appendText(")");
        }

        /**
        * Creates a new instance of IsSamePermit
        *
        * @param object The predicate evaluates to true only when the argument is
        * this object.
        */
        @Factory
        public static IsSamePermit samePermit(Permit object) {
            return new IsSamePermit(object);
        }
    }
}