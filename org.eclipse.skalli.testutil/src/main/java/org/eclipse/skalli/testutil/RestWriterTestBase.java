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
package org.eclipse.skalli.testutil;

import java.io.StringReader;
import java.io.StringWriter;

import org.eclipse.skalli.commons.XMLUtils;
import org.eclipse.skalli.services.extension.rest.RestConverter;
import org.eclipse.skalli.services.rest.RequestContext;
import org.eclipse.skalli.services.rest.RestReader;
import org.eclipse.skalli.services.rest.RestService;
import org.eclipse.skalli.services.rest.RestWriter;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.xml.sax.SAXException;

/**
 * Base class for {@link RestConverter} tests.
 */
@SuppressWarnings("nls")
public class RestWriterTestBase {

    protected static final String PROTOCOL = "http";
    protected static final Method ACTION = Method.GET;
    protected static final String HOST = "example.org";
    protected static final int PORT = 8080;
    protected static final String PATH = "/foo/bar";
    protected static final String QUERY = "a=foo&b=bar&c";
    protected static final String FRAGMENT = "frag";

    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n";

    /** Pattern for generating namespace attributes (use with <code>MessageFormat</code> */
    protected static final String ATTRIBUTES_PATTERN =
            "xmlns=\"{0}\" "
            + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
            + "xsi:schemaLocation=\"{0} http://example.org/schemas/{1}.xsd\" "
            + "apiVersion=\"{2}\"";

    protected StringWriter writer;
    protected RestService restService;


    @Before
    public void setup() throws Exception {
        writer = new StringWriter();
        restService = BundleManager.getRequiredService(RestService.class);
    }

    protected RestWriter getRestWriterXML() {
        return restService.getRestWriter(writer, getRequestContext(MediaType.TEXT_XML));
    }

    protected RestWriter getRestWriterJSON() {
        return restService.getRestWriter(writer, getRequestContext(MediaType.APPLICATION_JSON));
    }

    protected RestReader getRestReaderJSON(String json) {
        return restService.getRestReader(new StringReader(json), getRequestContext(MediaType.APPLICATION_JSON));
    }

    protected RequestContext getRequestContext(MediaType mediaType) {
        return getRequestContext(ACTION, PROTOCOL, HOST, PORT, PATH, QUERY, FRAGMENT, mediaType);
    }

    protected RequestContext getRequestContext(Method action, String protocol, String host, int port, String path,
           String query, String fragment, MediaType mediaType) {
        Reference resourceRef = new Reference(protocol, host, port, path, query, fragment);
        resourceRef.addQueryParameter("accept", mediaType.getName());
        Request request = new Request(action, resourceRef);
        return new RequestContext(request);
    }

    protected void assertEqualsXML(String expected) throws Exception {
        String actual = writer.toString();
        try {
            XMLUtils.documentFromString(actual);
        } catch (SAXException e) {
            Assert.fail("Invalid XML: " + actual);
        }
        Assert.assertEquals(XML_HEADER + expected, actual);
    }

    protected void assertEqualsJSON(String expected) throws Exception {
        String actual = writer.toString();
        try {
            new JSONObject(actual);
        } catch (JSONException e) {
            Assert.fail("Invalid JSON: " + e.getMessage());
        }
        Assert.assertEquals(expected, writer.toString());
    }
}
