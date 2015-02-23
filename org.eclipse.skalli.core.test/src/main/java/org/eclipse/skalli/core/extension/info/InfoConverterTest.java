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
package org.eclipse.skalli.core.extension.info;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.skalli.model.ext.commons.InfoExtension;
import org.eclipse.skalli.services.rest.RestReader;
import org.eclipse.skalli.services.rest.RestWriter;
import org.eclipse.skalli.testutil.AssertUtils;
import org.eclipse.skalli.testutil.RestWriterTestBase;
import org.junit.Test;

@SuppressWarnings("nls")
public class InfoConverterTest extends RestWriterTestBase {

    private static final String INITIAL_INFO_EXTENSION_XML = "<info><mailingLists/></info>";
    private static final String INFO_EXTENSION_XML = "<info><homepage>foobar</homepage>"
            + "<mailingLists><mailingList>a</mailingList><mailingList>b</mailingList></mailingLists></info>";
    private static final String INITIAL_INFO_EXTENSION_JSON = "{\"mailingLists\":[]}";
    private static final String INFO_EXTENSION_JSON = "{\"homepage\":\"foobar\",\"mailingLists\":[\"a\",\"b\"]}";

    @Test
    public void testMarshalBlankExtensionXML() throws Exception {
        InfoExtension info = new InfoExtension();
        RestWriter restWriter = getRestWriterXML();
        marshalInfoExtension(info, restWriter);
        assertEqualsXML(INITIAL_INFO_EXTENSION_XML);
    }

    @Test
    public void testMarshalXML() throws Exception {
        InfoExtension info = newInfoExtension();
        RestWriter restWriter = getRestWriterXML();
        marshalInfoExtension(info, restWriter);
        assertEqualsXML(INFO_EXTENSION_XML);
    }

    @Test
    public void testMarshalBlankExtensionJSON() throws Exception {
        InfoExtension info = new InfoExtension();
        RestWriter restWriter = getRestWriterJSON();
        marshalInfoExtension(info, restWriter);
        assertEqualsJSON(INITIAL_INFO_EXTENSION_JSON);
    }

    @Test
    public void testMarshalJSON() throws Exception {
        InfoExtension info = newInfoExtension();
        RestWriter restWriter = getRestWriterJSON();
        marshalInfoExtension(info, restWriter);
        assertEqualsJSON(INFO_EXTENSION_JSON);
    }

    @Test
    public void testUnmarshallInitialJSON() throws Exception {
        RestReader restReader = getRestReaderJSON(INITIAL_INFO_EXTENSION_JSON);
        InfoExtension info = unmarshalInfoExtension(restReader);
        assertEquals("", info.getPageUrl());
        assertTrue(info.getMailingLists().isEmpty());
    }

    @Test
    public void testUnmarshallJSON() throws Exception {
        RestReader restReader = getRestReaderJSON(INFO_EXTENSION_JSON);
        InfoExtension info = unmarshalInfoExtension(restReader);
        assertEquals("foobar", info.getPageUrl());
        AssertUtils.assertEquals("getMailingLists", info.getMailingLists(), "a", "b");
    }

    private InfoExtension newInfoExtension() {
        InfoExtension info = new InfoExtension();
        info.setPageUrl("foobar");
        info.addMailingList("a");
        info.addMailingList("b");
        return info;
    }

    private void marshalInfoExtension(InfoExtension info, RestWriter restWriter) throws Exception {
        InfoConverter converter = new InfoConverter();
        restWriter.object("info");
        converter.marshal(info, restWriter);
        restWriter.end();
        restWriter.flush();
    }

    private InfoExtension unmarshalInfoExtension(RestReader restReader) throws Exception {
        InfoConverter converter = new InfoConverter();
        restReader.object();
        InfoExtension info = converter.unmarshal(restReader);
        restReader.end();
        return info;
    }
}
