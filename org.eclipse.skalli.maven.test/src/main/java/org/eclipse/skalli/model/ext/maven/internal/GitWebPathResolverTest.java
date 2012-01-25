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
package org.eclipse.skalli.model.ext.maven.internal;

import static org.junit.Assert.*;

import org.junit.Test;

@SuppressWarnings("nls")
public class GitWebPathResolverTest {

    private static final String SCM_LOCATION = "scm:git:git://git.example.org/project.git";
    private static final String GIT_PATTERN = "^scm:git:git://(git\\.example\\.org(:\\d+)?)/(.*\\.git)$";
    private static final String GITWEB_TEMPLATE = "http://${1}:50000/git/?p=${3}";

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testResolveNoMapping() throws Exception {
        new GitWebPathResolver(null, null);
        new GitWebPathResolver("", "");
        new GitWebPathResolver(null, "");
        new GitWebPathResolver("", null);
    }

    @Test
    public void testResolvePathBlank() throws Exception {
        String expectedUrl = "http://git.example.org:50000/git/?p=project.git;a=blob_plain;f=pom.xml;hb=HEAD";
        GitWebPathResolver resolver = new GitWebPathResolver(GIT_PATTERN, GITWEB_TEMPLATE);
        assertTrue(resolver.canResolve(SCM_LOCATION));
        assertEquals(expectedUrl, resolver.resolvePath(SCM_LOCATION, null).toExternalForm());
        assertEquals(expectedUrl, resolver.resolvePath(SCM_LOCATION, "").toExternalForm());
        assertEquals(expectedUrl, resolver.resolvePath(SCM_LOCATION, ".").toExternalForm());
    }

    @Test
    public void testResolvePathNotBlank() throws Exception {
        String expectedUrl = "http://git.example.org:50000/git/?p=project.git;a=blob_plain;f=path/pom.xml;hb=HEAD";
        GitWebPathResolver resolver = new GitWebPathResolver(GIT_PATTERN, GITWEB_TEMPLATE);
        assertTrue(resolver.canResolve(SCM_LOCATION));
        assertEquals(expectedUrl, resolver.resolvePath(SCM_LOCATION, "path").toExternalForm());
        assertEquals(expectedUrl, resolver.resolvePath(SCM_LOCATION, "path/").toExternalForm());
        assertEquals(expectedUrl, resolver.resolvePath(SCM_LOCATION, "path/pom.xml").toExternalForm());
        assertEquals(expectedUrl, resolver.resolvePath(SCM_LOCATION, "/path/pom.xml").toExternalForm());
    }

    @Test(expected = java.net.MalformedURLException.class)
    public void testInvalidTemplate() throws Exception {
        GitWebPathResolver resolver = new GitWebPathResolver(GIT_PATTERN, "foobar");
        resolver.resolvePath(SCM_LOCATION, "path");
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testNoMatch() throws Exception {
        String scmLocation = "scm:p4://tralala";
        GitWebPathResolver resolver = new GitWebPathResolver(GIT_PATTERN, GITWEB_TEMPLATE);
        assertFalse(resolver.canResolve(scmLocation));
        resolver.resolvePath(scmLocation, "path");
    }

    @Test
    public void testInvalidPath() throws Throwable {
        GitWebPathResolver resolver = new GitWebPathResolver(GIT_PATTERN, GITWEB_TEMPLATE);
        assertThrows(resolver, "..", IllegalArgumentException.class);
        assertThrows(resolver, "path\\path", IllegalArgumentException.class);
        assertThrows(resolver, "./path", IllegalArgumentException.class);
        assertThrows(resolver, "path/.", IllegalArgumentException.class);
        assertThrows(resolver, "path/./path", IllegalArgumentException.class);
        assertThrows(resolver, "path/../path", IllegalArgumentException.class);
        assertThrows(resolver, "path/..", IllegalArgumentException.class);
    }

    private void assertThrows(GitWebPathResolver resolver, String path, Class<?> clazz) throws Throwable {
        try {
            resolver.resolvePath(SCM_LOCATION, path);
            fail("exception " + clazz + " expected");
        } catch (Throwable t) {
            if (!clazz.equals(t.getClass())) {
                throw t;
            }
        }
    }
}
