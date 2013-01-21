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
package org.eclipse.skalli.core.search;

import org.eclipse.skalli.core.search.LuceneUtil;
import org.junit.Assert;
import org.junit.Test;

public class LuceneUtilTest {

    private static final String ELL = LuceneUtil.FRAGMENTS_SEPARATOR;

    @SuppressWarnings("nls")
    @Test
    public void testWithEllipsis() throws Exception {
        Assert.assertEquals("foobar", LuceneUtil.withEllipsis(new String[] { "foobar" }, "foobar"));
        Assert.assertEquals("<em>foobar</em>", LuceneUtil.withEllipsis(new String[] { "<em>foobar</em>" }, "foobar"));
        Assert.assertEquals(ELL + " foobar " + ELL,
                LuceneUtil.withEllipsis(new String[] { "foobar" }, "XfoobarY"));
        Assert.assertEquals("foo " + ELL, LuceneUtil.withEllipsis(new String[] { "foo" }, "foo bar"));
        Assert.assertEquals(ELL + " bar", LuceneUtil.withEllipsis(new String[] { "bar" }, "foo bar"));
        Assert.assertEquals("<em>foo</em> " + ELL, LuceneUtil.withEllipsis(new String[] { "<em>foo</em>" }, "foo bar"));
        Assert.assertEquals(ELL + " <em>bar</em>", LuceneUtil.withEllipsis(new String[] { "<em>bar</em>" }, "foo bar"));
        Assert.assertEquals(ELL + " foo " + ELL + " bar " + ELL,
                LuceneUtil.withEllipsis(new String[] { "foo", "bar" }, "abc foo bar xyz"));
        Assert.assertEquals(ELL + " foo " + ELL + " bar " + ELL,
                LuceneUtil.withEllipsis(new String[] { "foo", "bar" }, "abc foo uvw bar xyz"));
        Assert.assertEquals(ELL + " <em>foo</em> " + ELL + " <em>bar</em> " + ELL,
                LuceneUtil.withEllipsis(new String[] { "<em>foo</em>", "<em>bar</em>" }, "abc foo uvw bar xyz"));
        Assert.assertEquals("abc " + ELL + " <em>foo</em> " + ELL + " <em>bar</em> " + ELL,
                LuceneUtil.withEllipsis(new String[] { "abc", "<em>foo</em>", "<em>bar</em>" }, "abc foo uvw bar xyz"));
        Assert.assertEquals(ELL + " <em>foo</em> " + ELL + " <em>bar</em> " + ELL + " xyz",
                LuceneUtil.withEllipsis(new String[] { "<em>foo</em>", "<em>bar</em>", "xyz" }, "abc foo uvw bar xyz"));

        Assert.assertEquals("", LuceneUtil.withEllipsis(new String[] { "" }, ""));
    }

}
