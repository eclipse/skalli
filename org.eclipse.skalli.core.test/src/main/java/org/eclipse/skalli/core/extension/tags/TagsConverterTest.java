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
package org.eclipse.skalli.core.extension.tags;

import org.eclipse.skalli.model.ext.commons.TagsExtension;
import org.eclipse.skalli.services.rest.RestWriter;
import org.eclipse.skalli.testutil.RestWriterTestBase;
import org.junit.Test;

@SuppressWarnings("nls")
public class TagsConverterTest extends RestWriterTestBase {

    @Test
    public void testMarshalBlankExtensionXML() throws Exception {
        TagsExtension tags = new TagsExtension();
        RestWriter restWriter = getRestWriterXML();
        marshalTagsExtension(tags, restWriter);
        assertEqualsXML("<tags></tags>");
    }

    @Test
    public void testMarshalXML() throws Exception {
        TagsExtension tags = newTagsExtension();
        RestWriter restWriter = getRestWriterXML();
        marshalTagsExtension(tags, restWriter);
        assertEqualsXML("<tags><tag>a</tag><tag>b</tag><tag>c</tag></tags>");
    }

    @Test
    public void testMarshalBlankExtensionJSON() throws Exception {
        TagsExtension tags = new TagsExtension();
        RestWriter restWriter = getRestWriterJSON();
        marshalTagsExtension(tags, restWriter);
        assertEqualsJSON("{\"items\":[]}");
    }

    @Test
    public void testMarshalJSON() throws Exception {
        TagsExtension tags = newTagsExtension();
        RestWriter restWriter = getRestWriterJSON();
        marshalTagsExtension(tags, restWriter);
        assertEqualsJSON("{\"items\":[\"a\",\"b\",\"c\"]}");
    }

    private TagsExtension newTagsExtension() {
        TagsExtension tags = new TagsExtension();
        tags.addTag("a");
        tags.addTag("b");
        tags.addTag("c");
        return tags;
    }

    private void marshalTagsExtension(TagsExtension tags, RestWriter restWriter) throws Exception {
        TagsConverter converter = new TagsConverter();
        restWriter.object("tags");
        converter.marshal(tags, restWriter);
        restWriter.end();
        restWriter.flush();
    }
}
