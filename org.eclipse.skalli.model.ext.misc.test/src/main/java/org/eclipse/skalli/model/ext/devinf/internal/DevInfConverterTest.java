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

import org.eclipse.skalli.model.ext.devinf.DevInfProjectExt;
import org.eclipse.skalli.services.rest.RestWriter;
import org.eclipse.skalli.testutil.RestWriterTestBase;
import org.junit.Test;

@SuppressWarnings("nls")
public class DevInfConverterTest extends RestWriterTestBase {

    @Test
    public void testMarshalBlankExtensionXML() throws Exception {
        DevInfProjectExt devInf = new DevInfProjectExt();
        RestWriter restWriter = getRestWriterXML();
        marshalDevInfExtension(devInf, restWriter);
        assertEqualsXML("<devInf><scmLocations/><javadocs/></devInf>");
    }

    @Test
    public void testMarshalExtensionXML() throws Exception {
        DevInfProjectExt devInf = newDevInfExtension();
        RestWriter restWriter = getRestWriterXML();
        marshalDevInfExtension(devInf, restWriter);
        assertEqualsXML("<devInf>"
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
                + "</devInf>");
    }

    @Test
    public void testMarshalBlankExtensionJSON() throws Exception {
        DevInfProjectExt devInf = new DevInfProjectExt();
        RestWriter restWriter = getRestWriterJSON();
        marshalDevInfExtension(devInf, restWriter);
        assertEqualsJSON("{\"scmLocations\":[],\"javadocs\":[]}");
    }

    @Test
    public void testMarshalExtensionJSON() throws Exception {
        DevInfProjectExt devInf = newDevInfExtension();
        RestWriter restWriter = getRestWriterJSON();
        marshalDevInfExtension(devInf, restWriter);
        assertEqualsJSON("{"
                + "\"bugtrackerUrl\":\"bugs\""
                + ",\"ciUrl\":\"ci\""
                + ",\"metricsUrl\":\"m\""
                + ",\"scmUrl\":\"scm\""
                + ",\"scmLocations\":[\"loc1\",\"loc2\"]"
                + ",\"javadocs\":[\"j1\",\"j2\"]}");
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
}
