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
package org.eclipse.skalli.services.rest;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Reference;

@SuppressWarnings("nls")
public class RequestContextTest {

    private Reference resourceRef;

    @Before
    public void setup() throws Exception {
        resourceRef = new Reference("http", "example.org", 8080, "/foo/bar", "a=foo&b=bar&c", "frag");

    }

    @Test
    public void testConstructor() throws Exception {
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

    @Test
    public void testAcceptHeader() throws Exception {
        Request request = new Request(Method.GET, resourceRef);
        List<Preference<MediaType>> prefs = request.getClientInfo().getAcceptedMediaTypes();
        prefs.add(new Preference<MediaType>(MediaType.TEXT_XML, 0.2f));
        prefs.add(new Preference<MediaType>(MediaType.APPLICATION_JSON, 0.7f));
        prefs.add(new Preference<MediaType>(MediaType.TEXT_HTML, 0.1f));
        RequestContext ctx = new RequestContext(request);
        Assert.assertEquals(MediaType.APPLICATION_JSON, ctx.getMediaType());
        Assert.assertTrue(ctx.isJSON());
        Assert.assertFalse(ctx.isXML());
    }

    @Test
    public void testAcceptQuery() throws Exception {
        resourceRef.addQueryParameter("accept", "application/json");
        Request request = new Request(Method.GET, resourceRef);
        RequestContext ctx = new RequestContext(request);
        Assert.assertEquals(MediaType.APPLICATION_JSON, ctx.getMediaType());
        Assert.assertTrue(ctx.isJSON());
        Assert.assertFalse(ctx.isXML());
    }

    @Test
    public void testAcceptDefault() throws Exception {
        Request request = new Request(Method.GET, resourceRef);
        RequestContext ctx = new RequestContext(request);
        Assert.assertEquals(MediaType.TEXT_XML, ctx.getMediaType());
        Assert.assertFalse(ctx.isJSON());
        Assert.assertTrue(ctx.isXML());
    }

}
