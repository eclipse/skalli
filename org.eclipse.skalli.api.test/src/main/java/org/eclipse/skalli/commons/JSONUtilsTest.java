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
package org.eclipse.skalli.commons;

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

@SuppressWarnings("nls")
public class JSONUtilsTest {

    private static final String TEST_JSON =
            "{\"a\":{\"x\":123,\"b\":{\"c\":\"foo\",\"d\":4711,\"e\":[\"item\"],\"f\":\"42\"}},\"y\":\"bar\"}";
    private static final JsonObject TEST_OBJECT = JSONUtils.jsonObjectFromString(TEST_JSON);

    @Test
    public void testGetValue() throws Exception {
        JsonElement elem = JSONUtils.getValue(TEST_OBJECT, "a.b.c");
        assertStringValue(elem, "foo");
        elem = JSONUtils.getValue(TEST_OBJECT, "a.b.d");
        assertIntegerValue(elem, 4711);
        elem = JSONUtils.getValue(TEST_OBJECT, "a.b.e");
        assertTrue(elem.isJsonArray());
        assertEquals("item", elem.getAsJsonArray().get(0).getAsString());
        elem = JSONUtils.getValue(TEST_OBJECT, "a.b.f");
        assertStringValue(elem, "42");
        elem = JSONUtils.getValue(TEST_OBJECT, "a.x");
        assertIntegerValue(elem, 123);
        elem = JSONUtils.getValue(TEST_OBJECT, "a.b");
        assertTrue(elem.isJsonObject());
        elem = JSONUtils.getValue(TEST_OBJECT, "y");
        assertStringValue(elem, "bar");
        elem = JSONUtils.getValue(TEST_OBJECT, "a");
        assertTrue(elem.isJsonObject());
        elem = JSONUtils.getValue(TEST_OBJECT, "a.");
        assertTrue(elem.isJsonObject());

        assertEquals(TEST_OBJECT, JSONUtils.getValue(TEST_OBJECT, ""));
        assertEquals(TEST_OBJECT, JSONUtils.getValue(TEST_OBJECT, null));

        assertNull(JSONUtils.getValue(TEST_OBJECT, "a.b.x"));
        assertNull(JSONUtils.getValue(TEST_OBJECT, "z"));
        assertNull(JSONUtils.getValue(TEST_OBJECT, ".a"));
    }

    @Test
    public void testGetPrimitive() throws Exception {
        JsonPrimitive elem = JSONUtils.getPrimitive(TEST_OBJECT, "a.b.c");
        assertStringValue(elem, "foo");
        elem = JSONUtils.getPrimitive(TEST_OBJECT, "a.b.d");
        assertIntegerValue(elem, 4711);
        assertNull(JSONUtils.getPrimitive(TEST_OBJECT, "a.b.e"));
        elem = JSONUtils.getPrimitive(TEST_OBJECT, "a.b.f");
        assertStringValue(elem, "42");
        elem = JSONUtils.getPrimitive(TEST_OBJECT, "a.x");
        assertIntegerValue(elem, 123);
        assertNull(JSONUtils.getPrimitive(TEST_OBJECT, "a.b"));
        elem = JSONUtils.getPrimitive(TEST_OBJECT, "y");
        assertStringValue(elem, "bar");
        assertNull(JSONUtils.getPrimitive(TEST_OBJECT, "a"));
        assertNull(JSONUtils.getPrimitive(TEST_OBJECT, "a."));
        assertNull(JSONUtils.getPrimitive(TEST_OBJECT, ""));
        assertNull(JSONUtils.getPrimitive(TEST_OBJECT, null));
        assertNull(JSONUtils.getPrimitive(TEST_OBJECT, "a.b.x"));
        assertNull(JSONUtils.getPrimitive(TEST_OBJECT, "z"));
        assertNull(JSONUtils.getPrimitive(TEST_OBJECT, ".a"));
    }

    @Test
    public void testGetString() throws Exception {
        String s = JSONUtils.getString(TEST_JSON, "a.b.c");
        assertEquals("foo", s);
        s = JSONUtils.getString(TEST_JSON, "a.b.d");
        assertEquals("4711", s);
        assertNull(JSONUtils.getString(TEST_JSON, "a.b.e"));
        s = JSONUtils.getString(TEST_JSON, "a.b.f");
        assertEquals("42", s);
        s = JSONUtils.getString(TEST_JSON, "a.x");
        assertEquals("123", s);
        assertNull(JSONUtils.getString(TEST_JSON, "a.b"));
        s = JSONUtils.getString(TEST_JSON, "y");
        assertEquals("bar", s);
        assertNull(JSONUtils.getString(TEST_JSON, "a"));
        assertNull(JSONUtils.getString(TEST_JSON, "a."));
        assertNull(JSONUtils.getString(TEST_JSON, ""));
        assertNull(JSONUtils.getString(TEST_JSON, null));
        assertNull(JSONUtils.getString(TEST_JSON, "a.b.x"));
        assertNull(JSONUtils.getString(TEST_JSON, "z"));
        assertNull(JSONUtils.getString(TEST_JSON, ".a"));
    }

    @Test
    public void testGetInteger() throws Exception {
        Integer i = JSONUtils.getInteger(TEST_JSON, "a.b.c");
        assertNull(i);
        i = JSONUtils.getInteger(TEST_JSON, "a.b.d");
        assertEquals(4711, i.intValue());
        assertNull(JSONUtils.getInteger(TEST_JSON, "a.b.e"));
        i = JSONUtils.getInteger(TEST_JSON, "a.b.f");
        assertEquals(42, i.intValue());
        i = JSONUtils.getInteger(TEST_JSON, "a.x");
        assertEquals(123, i.intValue());
        assertNull(JSONUtils.getInteger(TEST_JSON, "a.b"));
        assertNull(JSONUtils.getInteger(TEST_JSON, "y"));
        assertNull(JSONUtils.getInteger(TEST_JSON, "a"));
        assertNull(JSONUtils.getInteger(TEST_JSON, "a."));
        assertNull(JSONUtils.getInteger(TEST_JSON, ""));
        assertNull(JSONUtils.getInteger(TEST_JSON, null));
        assertNull(JSONUtils.getInteger(TEST_JSON, "a.b.x"));
        assertNull(JSONUtils.getInteger(TEST_JSON, "z"));
        assertNull(JSONUtils.getInteger(TEST_JSON, ".a"));
    }

    private void assertPrimitiveValue(JsonElement elem) {
        assertNotNull(elem);
        assertTrue(elem.isJsonPrimitive());
    }

    private void assertStringValue(JsonElement elem, String value) {
        assertPrimitiveValue(elem);
        assertTrue(((JsonPrimitive)elem).isString());
        assertEquals(value, ((JsonPrimitive)elem).getAsString());
    }

    private void assertIntegerValue(JsonElement elem, int value) {
        assertPrimitiveValue(elem);
        assertTrue(((JsonPrimitive)elem).isNumber());
        assertEquals(value, ((JsonPrimitive)elem).getAsInt());
    }

}
