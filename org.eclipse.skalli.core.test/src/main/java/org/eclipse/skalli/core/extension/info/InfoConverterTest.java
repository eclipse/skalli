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

import org.eclipse.skalli.model.ext.commons.InfoExtension;
import org.eclipse.skalli.services.rest.RestWriter;
import org.eclipse.skalli.testutil.RestWriterTestBase;
import org.junit.Test;

@SuppressWarnings("nls")
public class InfoConverterTest extends RestWriterTestBase {

    @Test
    public void testMarshalBlankExtensionXML() throws Exception {
        InfoExtension info = new InfoExtension();
        RestWriter restWriter = getRestWriterXML();
        marshalInfoExtension(info, restWriter);
        assertEqualsXML("<info><mailingLists/></info>");
    }

    @Test
    public void testMarshalXML() throws Exception {
        InfoExtension info = newInfoExtension();
        RestWriter restWriter = getRestWriterXML();
        marshalInfoExtension(info, restWriter);
        assertEqualsXML("<info><homepage>foobar</homepage>"
                + "<mailingLists><mailingList>a</mailingList><mailingList>b</mailingList></mailingLists></info>");
    }

    @Test
    public void testMarshalBlankExtensionJSON() throws Exception {
        InfoExtension info = new InfoExtension();
        RestWriter restWriter = getRestWriterJSON();
        marshalInfoExtension(info, restWriter);
        assertEqualsJSON("{\"mailingLists\":[]}");
    }

    @Test
    public void testMarshalJSON() throws Exception {
        InfoExtension info = newInfoExtension();
        RestWriter restWriter = getRestWriterJSON();
        marshalInfoExtension(info, restWriter);
        assertEqualsJSON("{\"homepage\":\"foobar\",\"mailingLists\":[\"a\",\"b\"]}");
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
}
