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
package org.eclipse.skalli.services.extension.rest;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.Request;
import org.restlet.data.Method;
import org.restlet.data.Reference;

@SuppressWarnings("nls")
public class ResourceContextTest {

    @Test
    public void testConstructor() throws Exception {
        Reference resourceRef = new Reference("http", "example.org", 8080, "/foo/bar", "a=foo&b=bar&c", "frag");
        Request request = new Request(Method.GET, resourceRef);
        RequestContext ctx = new RequestContext(request);
        Assert.assertEquals("GET", ctx.getAction());
        Assert.assertEquals("http://example.org:8080", ctx.getHost());
        Assert.assertEquals("/foo/bar", ctx.getPath());
        Assert.assertEquals("a=foo&b=bar&c", ctx.getQueryString());
        Assert.assertEquals("foo", ctx.getQueryAttribute("a"));
        Assert.assertEquals("bar", ctx.getQueryAttribute("b"));
        Assert.assertNull(ctx.getQueryAttribute("c"));
        Assert.assertTrue(ctx.hasQueryAttribute("c"));
        Assert.assertNull(ctx.getQueryAttribute("unknown"));
        Assert.assertFalse(ctx.hasQueryAttribute("unknown"));
        Map<String,String> attr = ctx.getQueryAttributes();
        Assert.assertEquals("foo", attr.get("a"));
        Assert.assertEquals("bar", attr.get("b"));
        Assert.assertNull(attr.get("c"));
        Assert.assertTrue(attr.containsKey("c"));
        Assert.assertNotNull(ctx.getResourceRef());
        Assert.assertEquals("frag", ctx.getResourceRef().getFragment());
        Assert.assertNotNull(ctx.getQueryAsForm());
    }

}
