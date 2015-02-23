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

import static org.junit.Assert.assertTrue;

import org.eclipse.skalli.model.ext.commons.TagsExtension;
import org.eclipse.skalli.services.rest.RestReader;
import org.eclipse.skalli.services.rest.RestWriter;
import org.eclipse.skalli.testutil.AssertUtils;
import org.eclipse.skalli.testutil.RestWriterTestBase;
import org.junit.Test;

@SuppressWarnings("nls")
public class TagsConverterTest extends RestWriterTestBase {

    private static final String INITIAL_TAGS_EXTENSION_XML = "<tags></tags>";
    private static final String TAGS_EXTENSION_XML = "<tags><tag>a</tag><tag>b</tag><tag>c</tag></tags>";
    private static final String INITIAL_TAGS_EXTENSION_JSON = "{\"items\":[]}";
    private static final String TAGS_EXTENSION_JSON = "{\"items\":[\"a\",\"b\",\"c\"]}";

    @Test
    public void testMarshalBlankExtensionXML() throws Exception {
        TagsExtension tags = new TagsExtension();
        RestWriter restWriter = getRestWriterXML();
        marshalTagsExtension(tags, restWriter);
        assertEqualsXML(INITIAL_TAGS_EXTENSION_XML);
    }

    @Test
    public void testMarshalXML() throws Exception {
        TagsExtension tags = newTagsExtension();
        RestWriter restWriter = getRestWriterXML();
        marshalTagsExtension(tags, restWriter);
        assertEqualsXML(TAGS_EXTENSION_XML);
    }

    @Test
    public void testMarshalBlankExtensionJSON() throws Exception {
        TagsExtension tags = new TagsExtension();
        RestWriter restWriter = getRestWriterJSON();
        marshalTagsExtension(tags, restWriter);
        assertEqualsJSON(INITIAL_TAGS_EXTENSION_JSON);
    }

    @Test
    public void testMarshalJSON() throws Exception {
        TagsExtension tags = newTagsExtension();
        RestWriter restWriter = getRestWriterJSON();
        marshalTagsExtension(tags, restWriter);
        assertEqualsJSON(TAGS_EXTENSION_JSON);
    }

    @Test
    public void testUnmarshallInitialJSON() throws Exception {
        RestReader restReader = getRestReaderJSON(INITIAL_TAGS_EXTENSION_JSON);
        TagsExtension tags = unmarshalTagsExtension(restReader);
        assertTrue(tags.getTags().isEmpty());
    }

    @Test
    public void testUnmarshallJSON() throws Exception {
        RestReader restReader = getRestReaderJSON(TAGS_EXTENSION_JSON);
        TagsExtension tags = unmarshalTagsExtension(restReader);
        AssertUtils.assertEquals("getTags", tags.getTags(), "a", "b", "c");
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

    private TagsExtension unmarshalTagsExtension(RestReader restReader) throws Exception {
        TagsConverter converter = new TagsConverter();
        restReader.object();
        TagsExtension tags = converter.unmarshal(restReader);
        restReader.end();
        return tags;
    }
}
