/*******************************************************************************
 * Copyright (c) 2010-2015 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.core.rest;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.UUID;

import org.apache.commons.lang.time.DateFormatUtils;
import org.eclipse.skalli.commons.URLUtils;
import org.eclipse.skalli.testutil.AssertUtils;
import org.junit.Test;

@SuppressWarnings("nls")
public class JSONRestReaderTest {

    @Test
    public void testObjectVariousValues() throws Exception {
        UUID uuid = UUID.randomUUID();
        URL url = URLUtils.stringToURL("http://localhost:8080/path");
        long now  = (System.currentTimeMillis() / 1000) * 1000; // cut off milliseconds
        JSONRestReader json = getRestReader("{\"a\":\"string\",\"b\":4711,\"c\":3.14,\"d\":true,"
                + "\"e\":[\"x\",\"y\"],\"f\":\"\",\"g\":{\"x\":\"string\"},\"h\":\"" + uuid.toString()  + "\","
                + "\"i\":\"" + url.toExternalForm() + "\","
                + "\"j\":\"" + DateFormatUtils.formatUTC(now, "yyyy-MM-dd'T'HH:mm:ss'Z'") + "\"}");
        json.object();
            assertTrue(json.hasMore());
            assertTrue(json.isKey());
            assertTrue(json.isKeyAnyOf("b","a","c"));
            assertFalse(json.isKeyAnyOf("x","y","z"));
            assertEquals("a", json.key());
            assertFalse(json.isKey());
            assertEquals("string", json.valueString());
            assertTrue(json.hasMore());
            assertEquals("b", json.key());
            assertEquals(4711L, json.valueLong());
            assertTrue(json.hasMore());
            assertEquals("c", json.key());
            assertEquals(3.14d, json.valueDouble(), 0.1d);
            assertTrue(json.hasMore());
            assertEquals("d", json.key());
            assertTrue(json.valueBoolean());
            assertTrue(json.hasMore());
            assertEquals("e", json.key());
            assertTrue(json.isArray());
            json.array();
                assertTrue(json.hasMore());
                assertEquals("x", json.valueString());
                assertTrue(json.hasMore());
                assertEquals("y", json.valueString());
                assertFalse(json.hasMore());
            json.end();
            assertTrue(json.hasMore());
            assertEquals("f", json.key());
            assertEquals("", json.valueString());
            assertTrue(json.hasMore());
            assertEquals("g", json.key());
            assertTrue(json.isObject());
            json.object();
                assertTrue(json.hasMore());
                assertEquals("x", json.key());
                assertEquals("string", json.valueString());
                assertFalse(json.hasMore());
            json.end();
            assertTrue(json.hasMore());
            assertEquals("h", json.key());
            assertEquals(uuid, json.valueUUID());
            assertTrue(json.hasMore());
            assertEquals("i", json.key());
            assertEquals(url, json.valueURL());
            assertTrue(json.hasMore());
            assertEquals("j", json.key());
            assertEquals(now, json.valueDatetime().getTimeInMillis());
            assertFalse(json.hasMore());
        json.end();
    }

    @Test
    public void testIsKeyIsValue() throws Exception {
        JSONRestReader json = getRestReader("{\"a\":\"string\",\"value\":\"b\",\"c\":4711}");
        json.object();
        assertTrue(json.isKey());
        assertTrue(json.isKey("a"));
        assertFalse(json.isValue());
        assertEquals("a", json.key());
        assertFalse(json.isKey());
        assertTrue(json.isValue());
        assertEquals("string", json.valueString());
        assertTrue(json.isKey());
        assertTrue(json.isKey("value"));
        assertTrue(json.isValue()); // attribute with the special name "value"!
        assertEquals("b", json.valueString());
        assertTrue(json.isKey());
        assertTrue(json.isKey("c"));
        assertFalse(json.isValue());
        assertEquals("c", json.key());
        assertFalse(json.isKey());
        assertTrue(json.isValue());
        assertEquals(4711L, json.valueLong());
        json.end();
    }

    @Test
    public void testStringValue() throws Exception {
        JSONRestReader json = getRestReader("{\"a\":\"string\",\"b\":4711,\"c\":3.14,\"d\":true,\"e\":null}");
        json.object();
            assertEquals("a", json.key());
            assertEquals("string", json.valueString());
            assertEquals("b", json.key());
            assertEquals("4711", json.valueString());
            assertEquals("c", json.key());
            assertEquals("3.14", json.valueString());
            assertEquals("d", json.key());
            assertEquals("true", json.valueString());
            assertEquals("e", json.key());
            assertNull(json.valueString());
        json.end();
    }

    @Test
    public void testValueIgoreKeys() throws Exception {
        JSONRestReader json = getRestReader("{\"a\":\"string\",\"b\":4711,\"c\":3.14,\"d\":true,\"e\":null}");
        json.object();
            assertEquals("string", json.valueString());
            assertEquals(4711, json.valueLong());
            assertEquals(3.14, json.valueDouble(), 0.1);
            assertEquals(true, json.valueBoolean());
            assertNull(json.valueString());
        json.end();
    }

    @Test(expected=IllegalStateException.class)
    public void testStringValueOfArray() throws Exception {
        JSONRestReader json = getRestReader("\"a\":[\"x\",\"y\"]");
        json.object();
            assertEquals("a", json.key());
            json.valueString();
    }

    @Test(expected=IllegalStateException.class)
    public void testStringValueOfObject() throws Exception {
        JSONRestReader json = getRestReader("\"a\":{\"x\":\"y\"}");
        json.object();
            assertEquals("a", json.key());
            json.valueString();
    }

    public void testValueLongFromString() throws Exception {
        JSONRestReader json = getRestReader("\"a\":\"4711\",\"b\":\"1.0\"");
        json.object();
            assertEquals("a", json.key());
            assertEquals(4711, json.valueLong());
            assertEquals("b", json.key());
            assertEquals(1, json.valueLong());
        json.end();
    }

    @Test(expected=NumberFormatException.class)
    public void testValueLongOfString() throws Exception {
        JSONRestReader json = getRestReader("{\"a\":\"string\"}");
        json.object();
            assertEquals("a", json.key());
            json.valueLong();
    }

    @Test(expected=NumberFormatException.class)
    public void testValueLongOfFloat() throws Exception {
        JSONRestReader json = getRestReader("{\"a\":3.14}");
        json.object();
            assertEquals("a", json.key());
            json.valueLong();
    }

    public void testValueDoubleFromString() throws Exception {
        JSONRestReader json = getRestReader("\"a\":\"4711\",\"b\":\"1.0\"");
        json.object();
            assertEquals("a", json.key());
            assertEquals(4711.0d, json.valueDouble(), 0.1d);
            assertEquals("b", json.key());
            assertEquals(1.0d, json.valueDouble(), 0.1d);
        json.end();
    }

    @Test(expected=NumberFormatException.class)
    public void testValueDoubleOfString() throws Exception {
        JSONRestReader json = getRestReader("{\"a\":\"string\"}");
        json.object();
            assertEquals("a", json.key());
            json.valueDouble();
    }

    @Test
    public void testCollection() throws Exception {
        JSONRestReader json = getRestReader("{\"a\":[\"string\",4711,1.0,true,null]}");
        json.object();
            assertEquals("a", json.key());
            AssertUtils.assertEquals("collection()", json.collection("foobar"),
                    "string", "4711", "1.0", "true", null);
        json.end();
    }

    @Test
    public void testAttributes() throws Exception {
        UUID uuid = UUID.randomUUID();
        URL url = URLUtils.stringToURL("http://localhost:8080/path");
        long now  = (System.currentTimeMillis() / 1000) * 1000; // cut off milliseconds
        JSONRestReader json = getRestReader("{\"a\":\"string\",\"b\":4711,\"c\":3.14,\"d\":true,"
                + "\"e\":\"" + uuid.toString()  + "\","
                + "\"f\":\"" + url.toExternalForm() + "\","
                + "\"g\":\"" + DateFormatUtils.formatUTC(now, "yyyy-MM-dd'T'HH:mm:ss'Z'") + "\"}");
        assertAttributes(json, uuid, url, now);
    }

    @Test
    public void testPrefixedAttributes() throws Exception {
        UUID uuid = UUID.randomUUID();
        URL url = URLUtils.stringToURL("http://localhost:8080/path");
        long now  = (System.currentTimeMillis() / 1000) * 1000; // cut off milliseconds
        JSONRestReader json = getRestReader("{\"@a\":\"string\",\"@b\":4711,\"@c\":3.14,\"@d\":true,"
                + "\"@e\":\"" + uuid.toString()  + "\","
                + "\"@f\":\"" + url.toExternalForm() + "\","
                + "\"@g\":\"" + DateFormatUtils.formatUTC(now, "yyyy-MM-dd'T'HH:mm:ss'Z'") + "\"}");
        json.set(JSONRestReader.PREFIXED_ATTRIBUTES);
        assertAttributes(json, uuid, url, now);
    }

    @Test(expected=IllegalStateException.class)
    public void testUnexpectedEnd() throws Exception {
        JSONRestReader json = getRestReader("{\"a\":\"string\"}");
        json.object();
            assertEquals("a", json.key());
            assertEquals("string", json.valueString());
        json.end();
        json.end(); // unexpected
    }

    @Test(expected=IllegalStateException.class)
    public void testStillInInitialState() throws Exception {
        JSONRestReader json = getRestReader("{}");
        json.end(); // unexpected
    }

    @Test(expected=IllegalStateException.class)
    public void testFinalStateObject() throws Exception {
        JSONRestReader json = getRestReader("{}");
        json.object();
        json.end();
        assertFalse(json.isObject());
        json.object();
    }

    @Test(expected=IllegalStateException.class)
    public void testFinalStateArray() throws Exception {
        JSONRestReader json = getRestReader("{}");
        json.object();
        json.end();
        assertFalse(json.isArray());
        json.array();
    }

    @Test(expected=IllegalStateException.class)
    public void testFinalStateKey() throws Exception {
        JSONRestReader json = getRestReader("{}");
        json.object();
        json.end();
        assertFalse(json.isKey());
        json.key();
    }

    @Test(expected=IllegalStateException.class)
    public void testFinalStateValue() throws Exception {
        JSONRestReader json = getRestReader("{}");
        json.object();
        json.end();
        json.valueString();
    }

    @Test(expected=IllegalStateException.class)
    public void testFinalStateSkip() throws Exception {
        JSONRestReader json = getRestReader("{}");
        json.object();
        json.end();
        json.skip();
    }

    @Test
    public void testSkipArrayElement() throws Exception {
        JSONRestReader json = getRestReader("{\"a\":[\"skipped\",\"string\"]}");
        json.object();
            assertEquals("a", json.key());
            json.array();
                assertTrue(json.hasMore());
                json.skip();
                assertTrue(json.hasMore());
                assertEquals("string", json.valueString());
            json.end();
        json.end();
    }

    @Test
    public void testSkipObjectAttribute() throws Exception {
        JSONRestReader json = getRestReader("{\"a\":{\"skipped\":\"skipped\",\"y\":\"z\"}}");
        json.object();
            assertEquals("a", json.key());
            json.object();
                assertTrue(json.hasMore());
                json.skip();
                assertTrue(json.hasMore());
                assertEquals("y", json.key());
                assertEquals("z", json.valueString());
            json.end();
        json.end();
    }

    @Test
    public void testSkipObjectValue() throws Exception {
        JSONRestReader json = getRestReader("{\"a\":{\"x\":\"skipped\",\"y\":\"z\"}}");
        json.object();
            assertEquals("a", json.key());
            json.object();
                assertTrue(json.hasMore());
                assertEquals("x", json.key());
                json.skip();
                assertTrue(json.hasMore());
                assertEquals("y", json.key());
                assertEquals("z", json.valueString());
            json.end();
        json.end();
    }

    @Test
    public void testSkipKeySequence() throws Exception {
        JSONRestReader json = getRestReader("{\"a\":\"b\",\"c\":\"d\"}");
        json.object();
            assertTrue(json.isKey());
            assertTrue(json.isKeyAnyOf("a"));
            assertEquals("b", json.attributeString());
            assertTrue(json.isKey());
            assertTrue(json.isKeyAnyOf("c"));
            assertEquals("d", json.attributeString());
        json.end();
    }

    @Test
    public void testSkipKeyArray() throws Exception {
        JSONRestReader json = getRestReader("{\"a\":[\"x\"]}");
        json.object();
            assertTrue(json.isKey());
            assertTrue(json.isKeyAnyOf("a"));
            json.array();
              assertEquals("x", json.valueString());
            json.end();
        json.end();
    }

    @Test
    public void testSkipKeyObject() throws Exception {
        JSONRestReader json = getRestReader("{\"a\":{\"x\":\"y\"}}");
        json.object();
            assertTrue(json.isKey());
            assertTrue(json.isKeyAnyOf("a"));
            json.object();
            assertEquals("x", json.key());
            assertEquals("y", json.valueString());
            json.end();
        json.end();
    }

    @Test(expected=IllegalStateException.class)
    public void testImplicitSkipValue() throws Exception {
        JSONRestReader json = getRestReader("{\"a\":\"b\",\"c\":\"d\"}");
        json.object();
            assertEquals("a", json.key());
            assertEquals("c", json.key());
    }

    @Test
    public void testExplicitSkipValue() throws Exception {
        JSONRestReader json = getRestReader("{\"a\":\"b\",\"c\":\"d\"}");
        json.object();
            assertEquals("a", json.key());
            json.skip();
            assertEquals("c", json.key());
            json.skip();
        json.end();
    }

    @Test
    public void testSkipSequence() throws Exception {
        JSONRestReader json = getRestReader("{\"a\":\"b\",\"c\":\"d\",\"e\":\"f\",\"g\":\"h\"}");
        json.object();
            assertTrue(json.isKey());
            assertTrue(json.isKeyAnyOf("a"));
            assertEquals("b", json.attributeString()); // skip the key
            assertTrue(json.isKey());
            assertTrue(json.isKeyAnyOf("c"));
            json.skip(); // skip key and value
            assertTrue(json.isKey());
            assertTrue(json.isKeyAnyOf("e"));
            assertEquals("e", json.key());
            json.skip(); // skip the value
            assertTrue(json.isKey());
            assertTrue(json.isKeyAnyOf("g"));
            assertEquals("g", json.key());
            assertEquals("h", json.valueString());
        json.end();
    }

    @Test
    public void testCheckSameKeyTwice() throws Exception {
        JSONRestReader json = getRestReader("{\"a\":{}}");
        json.object();
            assertTrue(json.isKey());
            assertTrue(json.isKey());
            assertEquals("a", json.key());
            assertFalse(json.isKey());
            json.skip(); // skip the array value
        json.end();
    }

    @Test(expected=IllegalStateException.class)
    public void testReadSameKeyTwice() throws Exception {
        JSONRestReader json = getRestReader("{\"a\":{}}");
        json.object();
            assertTrue(json.isKey());
            assertTrue(json.isKeyAnyOf("a"));
            assertEquals("a", json.key());
            assertFalse(json.isKey());
            assertFalse(json.isKeyAnyOf("a"));
            json.key();
    }

    @Test(expected=IllegalStateException.class)
    public void testExpectValueTwice() throws Exception {
        JSONRestReader json = getRestReader("{\"a\":\"b\"}");
        json.object();
            assertTrue(json.isKey());
            assertTrue(json.isKeyAnyOf("a"));
            assertEquals("a", json.key());
            assertEquals("b", json.valueString());
            json.valueString();
    }

    @Test
    public void testNonExecutePrefix() throws Exception {
        JSONRestReader json = getRestReader(")]}'\n{\"a\":\"string\"}");
        json.object();
            assertEquals("a", json.key());
            assertEquals("string", json.valueString());
        json.end();
    }

    @Test(expected=IOException.class)
    public void testStrictNonExecutePrefix() throws Exception {
        JSONRestReader json = getRestReader(")]}'\n{\"a\":\"string\"}");
        json.set(JSONRestReader.STRICT);
        json.object();
    }

    @Test
    public void testLineComments() throws Exception {
        JSONRestReader json = getRestReader("{\"a\":\"string\",\n # line comment \n\"c\":4711 /* c-style comment */}");
        json.object();
            assertEquals("a", json.key());
            assertEquals("string", json.valueString());
            assertEquals("c", json.key());
            assertEquals(4711, json.valueLong());
        json.end();
    }

    @Test(expected=IOException.class)
    public void testStrictLineComments() throws Exception {
        JSONRestReader json = getRestReader("{\"a\":\"string\",\n # line comment \n\"c\":4711 /* c-style comment */}");
        json.set(JSONRestReader.STRICT);
        json.object();
        assertEquals("a", json.key());
        assertEquals("string", json.valueString());
        json.key();
    }

    @Test
    public void testUnquotedKey() throws Exception {
        JSONRestReader json = getRestReader("{a:\"string\"}");
        json.object();
            assertEquals("a", json.key());
            assertEquals("string", json.valueString());
        json.end();
    }

    @Test(expected=IOException.class)
    public void testStrictUnquotedKey() throws Exception {
        JSONRestReader json = getRestReader("{a:\"string\"}");
        json.set(JSONRestReader.STRICT);
        json.object();
        json.key();
    }

    @Test
    public void testUnquotedString() throws Exception {
        JSONRestReader json = getRestReader("{\"a\":  string  ,\"b\":4711}");
        json.object();
            assertEquals("a", json.key());
            assertEquals("string", json.valueString()); // enclosing whitspace is ignored
            assertEquals("b", json.key());
            assertEquals(4711, json.valueLong());
        json.end();
    }

    @Test(expected=IOException.class)
    public void testStrictUnquotedString() throws Exception {
        JSONRestReader json = getRestReader("{\"a\":string}");
        json.set(JSONRestReader.STRICT);
        json.object();
        assertEquals("a", json.key());
        json.valueString();
    }

    @Test
    public void testSingleQuotedKeys() throws Exception {
        JSONRestReader json = getRestReader("{'a':\"string\"}");
        json.object();
            assertEquals("a", json.key());
            assertEquals("string", json.valueString());
        json.end();
    }

    @Test(expected=IOException.class)
    public void testStrictSingleQuotedKeys() throws Exception {
        JSONRestReader json = getRestReader("{'a':\"string\"}");
        json.set(JSONRestReader.STRICT);
        json.object();
        json.key();
    }

    @Test
    public void testSingleQuotedString() throws Exception {
        JSONRestReader json = getRestReader("{\"a\":'  string  ',\"b\":4711}");
        json.object();
            assertEquals("a", json.key());
            assertEquals("  string  ", json.valueString());
            assertEquals("b", json.key());
            assertEquals(4711, json.valueLong());
        json.end();
    }

    @Test(expected=IOException.class)
    public void testStrictSingleQuotedString() throws Exception {
        JSONRestReader json = getRestReader("{\"a\":'string'}");
        json.set(JSONRestReader.STRICT);
        json.object();
        assertEquals("a", json.key());
        json.valueString();
    }

    @Test
    public void testAlternativeSeparatorsObject() throws Exception {
        JSONRestReader json = getRestReader("{\"a\"=>\"string\";\"b\"=4711}");
        json.object();
            assertEquals("a", json.key());
            assertEquals("string", json.valueString());
            assertEquals("b", json.key());
            assertEquals(4711, json.valueLong());
        json.end();
    }

    @Test(expected=IOException.class)
    public void testStrictSeparatorObject1() throws Exception {
        JSONRestReader json = getRestReader("{\"a\"=\"string\"}");
        json.set(JSONRestReader.STRICT);
        json.object();
        assertEquals("a", json.key());
        json.valueString();
    }

    @Test(expected=IOException.class)
    public void testStrictSeparatorObject2() throws Exception {
        JSONRestReader json = getRestReader("{\"a\"=>\"string\"}");
        json.set(JSONRestReader.STRICT);
        json.object();
        assertEquals("a", json.key());
        json.valueString();
    }

    @Test(expected=IOException.class)
    public void testStrictSeparatorObject3() throws Exception {
        JSONRestReader json = getRestReader("{\"a\":\"string\";\"b\":4711}");
        json.set(JSONRestReader.STRICT);
        json.object();
        assertEquals("a", json.key());
        assertEquals("string", json.valueString());
        json.key();
    }

    @Test
    public void testAlternativeSeparatorsArray() throws Exception {
        JSONRestReader json = getRestReader("[\"a\";\"b\"]");
        json.array();
            assertEquals("a", json.valueString());
            assertEquals("b", json.valueString());
        json.end();
    }

    @Test(expected=IOException.class)
    public void testStrictSeparatorArray() throws Exception {
        JSONRestReader json = getRestReader("[\"a\";\"b\"]");
        json.set(JSONRestReader.STRICT);
        json.array();
        assertEquals("a", json.valueString());
        json.valueString();
    }

    @Test
    public void testArrayWithNullValues() throws Exception {
        JSONRestReader json = getRestReader("[\"a\",null,,,\"b\"]");
        json.array();
            assertEquals("a", json.valueString());
            assertNull(json.valueString());
            assertNull(json.valueString());
            assertNull(json.valueString());
            assertEquals("b", json.valueString());
        json.end();
    }

    @Test(expected=IOException.class)
    public void testStrictArrayWithNullValues() throws Exception {
        JSONRestReader json = getRestReader("[\"a\",,\"b\"]");
        json.set(JSONRestReader.STRICT);
        json.array();
         assertEquals("a", json.valueString());
         json.valueString();
    }

    private void assertAttributes(JSONRestReader json, UUID uuid, URL url, long now) throws Exception {
        json.object();
            assertEquals("a", json.key());
            assertEquals("string", json.attributeString());
            assertEquals("b", json.key());
            assertEquals(4711L, json.attributeLong());
            assertEquals("c", json.key());
            assertEquals(3.14d, json.attributeDouble(), 0.1d);
            assertEquals("d", json.key());
            assertTrue(json.attributeBoolean());
            assertEquals("e", json.key());
            assertEquals(uuid, json.attributeUUID());
            assertEquals("f", json.key());
            assertEquals(url, json.attributeURL());
            assertEquals("g", json.key());
            assertEquals(now, json.attributeDatetime().getTimeInMillis());
        json.end();
    }

    private JSONRestReader getRestReader(String s) {
        StringReader reader = new StringReader(s);
        JSONRestReader json = new JSONRestReader(reader);
        return json;
    }
}
