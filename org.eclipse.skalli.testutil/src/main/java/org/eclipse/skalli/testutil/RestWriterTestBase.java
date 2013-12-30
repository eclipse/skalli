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
package org.eclipse.skalli.testutil;

import java.io.StringWriter;

import org.eclipse.skalli.services.extension.rest.RestConverter;
import org.eclipse.skalli.services.rest.RestService;
import org.eclipse.skalli.services.rest.RestWriter;
import org.junit.Assert;
import org.junit.Before;
import org.restlet.data.MediaType;

/**
 * Base class for {@link RestConverter} tests.
 */
@SuppressWarnings("nls")
public class RestWriterTestBase {

    protected static final String HOST = "http://example.org";

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
        return restService.getRestWriter(MediaType.TEXT_XML, writer, HOST);
    }

    protected RestWriter getRestWriterJSON() {
        return restService.getRestWriter(MediaType.APPLICATION_JSON, writer, HOST);
    }

    protected void assertEqualsXML(String expected) throws Exception {
        Assert.assertEquals(XML_HEADER + expected, writer.toString());
    }

    protected void assertEqualsJSON(String expected) throws Exception {
        Assert.assertEquals(expected, writer.toString());
    }
}
