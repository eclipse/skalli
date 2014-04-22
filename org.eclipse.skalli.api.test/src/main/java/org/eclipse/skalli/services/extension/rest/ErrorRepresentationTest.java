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

import java.text.MessageFormat;

import org.eclipse.skalli.commons.XMLUtils;
import org.eclipse.skalli.testutil.RestWriterTestBase;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Status;

@SuppressWarnings("nls")
public class ErrorRepresentationTest extends RestWriterTestBase {

    @Test
    public void testMarshalXML() throws Exception {
        ErrorRepresentation error = new ErrorRepresentation(getRequestContext(MediaType.TEXT_XML),
                Status.CLIENT_ERROR_NOT_FOUND, "error:notfound", "Unknown Resource");
        error.write(writer);
        assertEqualsXML(MessageFormat.format(
                "<error {0}=\"{1}\" {2}=\"{3}\" {4}=\"{5} http://example.org:8080/schemas/error.xsd\">" +
                "<errorId>error:notfound</errorId><timestamp>{6}</timestamp>" +
                "<message>Unknown Resource</message></error>",
                XMLUtils.XMLNS, RestUtils.API_NAMESPACE, XMLUtils.XMLNS_XSI, XMLUtils.XSI_INSTANCE_NS,
                XMLUtils.XSI_SCHEMA_LOCATION, RestUtils.API_NAMESPACE, error.getTimestamp()));
    }

    @Test
    public void testMarshalXMLDefaultErrorId() throws Exception {
        ErrorRepresentation error = new ErrorRepresentation(getRequestContext(MediaType.TEXT_XML),
                Status.CLIENT_ERROR_NOT_FOUND, null, null);
        error.write(writer);
        assertEqualsXML(MessageFormat.format(
                "<error {0}=\"{1}\" {2}=\"{3}\" {4}=\"{5} http://example.org:8080/schemas/error.xsd\">" +
                "<errorId>rest:/foo/bar:{6}</errorId><timestamp>{7}</timestamp>" +
                "<message>{8} ({9})</message></error>",
                XMLUtils.XMLNS, RestUtils.API_NAMESPACE, XMLUtils.XMLNS_XSI, XMLUtils.XSI_INSTANCE_NS,
                XMLUtils.XSI_SCHEMA_LOCATION, RestUtils.API_NAMESPACE,
                Status.CLIENT_ERROR_NOT_FOUND.getCode(), error.getTimestamp(),
                Status.CLIENT_ERROR_NOT_FOUND.getDescription(), Status.CLIENT_ERROR_NOT_FOUND.getName()));
    }

    @Test
    public void testMarshalJSON() throws Exception {
        ErrorRepresentation error = new ErrorRepresentation(getRequestContext(MediaType.APPLICATION_JSON),
                Status.CLIENT_ERROR_NOT_FOUND, "error:notfound", "Unknown Resource");
        error.write(writer);
        assertEqualsJSON("{\"errorId\":\"error:notfound\"," +
                "\"timestamp\":\"" + error.getTimestamp() + "\"," +
                "\"message\":\"Unknown Resource\"}");
    }

    @Test
    public void testMarshalJSONDefaultErrorId() throws Exception {
        ErrorRepresentation error = new ErrorRepresentation(getRequestContext(MediaType.APPLICATION_JSON),
                Status.CLIENT_ERROR_NOT_FOUND, null, null);
        error.write(writer);
        assertEqualsJSON(MessageFormat.format(
                "'{'\"errorId\":\"rest:/foo/bar:{0}\",\"timestamp\":\"{1}\",\"message\":\"{2} ({3})\"}",
                Status.CLIENT_ERROR_NOT_FOUND.getCode(), error.getTimestamp(),
                Status.CLIENT_ERROR_NOT_FOUND.getDescription(), Status.CLIENT_ERROR_NOT_FOUND.getName()));
    }
}
