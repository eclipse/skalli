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
package org.eclipse.skalli.model.ext.devinf.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.skalli.model.ext.devinf.DevInfProjectExt;
import org.eclipse.skalli.services.rest.RestReader;
import org.eclipse.skalli.services.rest.RestWriter;
import org.eclipse.skalli.testutil.AssertUtils;
import org.eclipse.skalli.testutil.RestWriterTestBase;
import org.junit.Test;

@SuppressWarnings("nls")
public class DevInfConverterTest extends RestWriterTestBase {

    private static final String INITIAL_DEVINF_EXTENSION_XML = "<devInf><scmLocations/><javadocs/></devInf>";
    private static final String DEVINF_EXTENSION_XML = "<devInf>"
            + "<bugtrackerUrl>bugs</bugtrackerUrl>"
            + "<ciUrl>ci</ciUrl>"
            + "<metricsUrl>m</metricsUrl>"
            + "<scmUrl>scm</scmUrl>"
            + "<scmLocations>"
            + "<scmLocation>loc1</scmLocation>"
            + "<scmLocation>loc2</scmLocation>"
            + "</scmLocations>"
            + "<javadocs>"
            + "<javadoc>j1</javadoc>"
            + "<javadoc>j2</javadoc>"
            + "</javadocs>"
            + "</devInf>";
    private static final String INITIAL_DEVINF_EXTENSION_JSON = "{\"scmLocations\":[],\"javadocs\":[]}";
    private static final String DEVINF_EXTENSION_JSON = "{"
            + "\"bugtrackerUrl\":\"bugs\""
            + ",\"ciUrl\":\"ci\""
            + ",\"metricsUrl\":\"m\""
            + ",\"scmUrl\":\"scm\""
            + ",\"scmLocations\":[\"loc1\",\"loc2\"]"
            + ",\"javadocs\":[\"j1\",\"j2\"]}";
    private static final String DEVINF_EXTENSION_UNKNOWN_ATTR_JSON = "{"
            + "\"ignore\":true,"
            + "\"bugtrackerUrl\":\"bugs\","
            + "\"scmLocations\":[\"loc1\",\"loc2\"],"
            + "\"scmUrl\":\"scm\","
            + "\"unknown\":\"yes\","
            + "\"javadocs\":[],"
            + "\"whatever\":4711}";

    @Test
    public void testMarshalBlankExtensionXML() throws Exception {
        DevInfProjectExt devInf = new DevInfProjectExt();
        RestWriter restWriter = getRestWriterXML();
        marshalDevInfExtension(devInf, restWriter);
        assertEqualsXML(INITIAL_DEVINF_EXTENSION_XML);
    }

    @Test
    public void testMarshalExtensionXML() throws Exception {
        DevInfProjectExt devInf = newDevInfExtension();
        RestWriter restWriter = getRestWriterXML();
        marshalDevInfExtension(devInf, restWriter);
        assertEqualsXML(DEVINF_EXTENSION_XML);
    }

    @Test
    public void testMarshalBlankExtensionJSON() throws Exception {
        DevInfProjectExt devInf = new DevInfProjectExt();
        RestWriter restWriter = getRestWriterJSON();
        marshalDevInfExtension(devInf, restWriter);
        assertEqualsJSON(INITIAL_DEVINF_EXTENSION_JSON);
    }

    @Test
    public void testMarshalExtensionJSON() throws Exception {
        DevInfProjectExt devInf = newDevInfExtension();
        RestWriter restWriter = getRestWriterJSON();
        marshalDevInfExtension(devInf, restWriter);
        assertEqualsJSON(DEVINF_EXTENSION_JSON);
    }

    @Test
    public void testUnmarshallInitialJSON() throws Exception {
        RestReader restReader = getRestReaderJSON(INITIAL_DEVINF_EXTENSION_JSON);
        DevInfProjectExt devinf = unmarshalDevInfExtension(restReader);
        assertEquals("", devinf.getBugtrackerUrl());
        assertEquals("", devinf.getCiUrl());
        assertEquals("", devinf.getMetricsUrl());
        assertEquals("", devinf.getScmUrl());
        assertTrue(devinf.getScmLocations().isEmpty());
        assertTrue(devinf.getJavadocs().isEmpty());
    }

    @Test
    public void testUnmarshallJSON() throws Exception {
        RestReader restReader = getRestReaderJSON(DEVINF_EXTENSION_JSON);
        DevInfProjectExt devinf = unmarshalDevInfExtension(restReader);
        assertEquals("bugs", devinf.getBugtrackerUrl());
        assertEquals("ci", devinf.getCiUrl());
        assertEquals("m", devinf.getMetricsUrl());
        assertEquals("scm", devinf.getScmUrl());
        AssertUtils.assertEquals("getScmLocations", devinf.getScmLocations(), "loc1", "loc2");
        AssertUtils.assertEquals("getJavadocs", devinf.getJavadocs(), "j1", "j2");
    }

    @Test
    public void testUnmarshallIgnoreUnknownAttributesJSON() throws Exception {
        RestReader restReader = getRestReaderJSON(DEVINF_EXTENSION_UNKNOWN_ATTR_JSON);
        DevInfProjectExt devinf = unmarshalDevInfExtension(restReader);
        assertEquals("bugs", devinf.getBugtrackerUrl());
        assertEquals("", devinf.getCiUrl());
        assertEquals("", devinf.getMetricsUrl());
        assertEquals("scm", devinf.getScmUrl());
        AssertUtils.assertEquals("getScmLocations", devinf.getScmLocations(), "loc1", "loc2");
        assertTrue(devinf.getJavadocs().isEmpty());
    }

    private DevInfProjectExt newDevInfExtension() {
        DevInfProjectExt devInf = new DevInfProjectExt();
        devInf.setBugtrackerUrl("bugs");
        devInf.setCiUrl("ci");
        devInf.setMetricsUrl("m");
        devInf.setScmUrl("scm");
        devInf.addScmLocation("loc1");
        devInf.addScmLocation("loc2");
        devInf.addJavadoc("j1");
        devInf.addJavadoc("j2");
        return devInf;
    }

    private void marshalDevInfExtension(DevInfProjectExt devInf, RestWriter restWriter) throws Exception {
        DevInfConverter converter = new DevInfConverter();
        restWriter.object("devInf");
        converter.marshal(devInf, restWriter);
        restWriter.end();
        restWriter.flush();
    }

    private DevInfProjectExt unmarshalDevInfExtension(RestReader restReader) throws Exception {
        DevInfConverter converter = new DevInfConverter();
        restReader.object();
        DevInfProjectExt devinf = converter.unmarshal(restReader);
        restReader.end();
        return devinf;
    }
}
