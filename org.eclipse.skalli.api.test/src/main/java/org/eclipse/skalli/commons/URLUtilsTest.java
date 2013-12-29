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
package org.eclipse.skalli.commons;

import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("nls")
public class URLUtilsTest {

    @Test
    public void testStringToURL() throws Exception {
        Assert.assertEquals("http://host:8080/firstsecond",
                URLUtils.stringToURL("http://host:8080/firstsecond").toExternalForm());
        Assert.assertEquals("http://host:8080/first%20second",
                URLUtils.stringToURL("http://host:8080/first%20second").toExternalForm());
        Assert.assertEquals("http://host:8080/first+second",
                URLUtils.stringToURL("http://host:8080/first+second").toExternalForm());
        Assert.assertEquals("http://host:8080/first%20second",
                URLUtils.stringToURL("http://host:8080/first second").toExternalForm());
        Assert.assertEquals("http://host:8080/first%20second/third",
                URLUtils.stringToURL("http://host:8080/first second/third").toExternalForm());
        Assert.assertEquals("http://host:8080/=$%20%25:%C3%A4",
                URLUtils.stringToURL("http://host:8080/=$ %:ä").toExternalForm());
        Assert.assertEquals("http://host:8080/=$%20%25%2520:%C3%A4",
                URLUtils.stringToURL("http://host:8080/=$ %%20:ä").toExternalForm());
        Assert.assertEquals("http://host:8080/path?query",
                URLUtils.stringToURL("http://host:8080/path?query").toExternalForm());
        Assert.assertEquals("http://host:8080/path?a&b&c",
                URLUtils.stringToURL("http://host:8080/path?a&b&c").toExternalForm());
        Assert.assertEquals("http://host:8080/path?ab%20c",
                URLUtils.stringToURL("http://host:8080/path?ab c").toExternalForm());
        Assert.assertEquals("http://host:8080/path?ab%C3%A4c",
                URLUtils.stringToURL("http://host:8080/path?abäc").toExternalForm());
        Assert.assertNull(URLUtils.stringToURL(null));
        Assert.assertNull(URLUtils.stringToURL(""));
    }

    @Test
    public void testRemoveSlashStartEnd() throws Exception {
        Assert.assertNull(URLUtils.removeSlashStartEnd(null));
        Assert.assertEquals("", URLUtils.removeSlashStartEnd(""));
        Assert.assertEquals("", URLUtils.removeSlashStartEnd("/"));
        Assert.assertEquals("", URLUtils.removeSlashStartEnd(" // "));
        Assert.assertEquals("foobar", URLUtils.removeSlashStartEnd("foobar"));
        Assert.assertEquals("foobar", URLUtils.removeSlashStartEnd("/foobar"));
        Assert.assertEquals("foobar", URLUtils.removeSlashStartEnd("foobar/"));
        Assert.assertEquals("foobar", URLUtils.removeSlashStartEnd("/foobar/"));
        Assert.assertEquals("foo/bar", URLUtils.removeSlashStartEnd("/foo/bar/"));
        Assert.assertEquals("foo/bar", URLUtils.removeSlashStartEnd("//foo/bar///"));
        Assert.assertEquals("foo/bar", URLUtils.removeSlashStartEnd(" \t /foo/bar/ \n "));
        Assert.assertEquals("foo\\bar", URLUtils.removeSlashStartEnd(" \t /foo\\bar/ \n "));
        Assert.assertEquals(" foobar ", URLUtils.removeSlashStartEnd(" / foobar / "));
    }

    @Test
    public void testConcat() throws Exception {
        Assert.assertEquals("", URLUtils.concat(null, (Object[])null));
        Assert.assertEquals("", URLUtils.concat(null, (Object[])null));
        Assert.assertEquals("", URLUtils.concat("", (Object[])null));
        Assert.assertEquals("", URLUtils.concat(null, new Object[0]));
        Assert.assertEquals("", URLUtils.concat("", new Object[0]));
        Assert.assertEquals("http://host:8080", URLUtils.concat("http://host:8080", (Object[])null));
        Assert.assertEquals("http://host:8080", URLUtils.concat("http://host:8080/", (Object[])null));
        Assert.assertEquals("http://host:8080", URLUtils.concat("  http://host:8080// \n", (Object[])null));
        Assert.assertEquals("http://host:8080/a", URLUtils.concat("http://host:8080", "a"));
        Assert.assertEquals("http://host:8080", URLUtils.concat("http://host:8080", "/"));
        Assert.assertEquals("http://host:8080", URLUtils.concat("http://host:8080", "/\n/"));
        Assert.assertEquals("http://host:8080/a/b", URLUtils.concat("http://host:8080", "/a/", "b"));
        Assert.assertEquals("http://host:8080/a/b/c", URLUtils.concat("http://host:8080", "\t a/", " \n/b/c/  "));
        Assert.assertEquals("http://host:8080/a/4711", URLUtils.concat("http://host:8080", "a", new Integer(4711)));

    }
}
