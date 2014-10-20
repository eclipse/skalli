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
package org.eclipse.skalli.core.rest;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

import org.eclipse.skalli.commons.FormatUtils;
import org.eclipse.skalli.services.rest.RestWriter;
import org.eclipse.skalli.testutil.TestUUIDs;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("nls")
public class JSONRestWriterTest {

    private StringWriter writer;
    private RestWriter restWriter;

    @Before
    public void setup() throws Exception {
        writer = new StringWriter();
        restWriter = new JSONRestWriter(writer, "http://example.org");
    }

    @Test
    public void testAnonymousArray() throws Exception {
        restWriter
        .array()
          .value("x")
          .value(4711)
          .value(1.0)
          .value(true)
        .end()
        .flush();
        Assert.assertEquals("[\"x\",4711,1.0,true]", writer.toString());
    }

    @Test
    public void testArrayVariousNumbers() throws Exception {
        restWriter
        .array()
          .value((byte)42)
          .value((short)-1)
          .value((int)4711)
          .value(4711L)
          .value((float)1.0)
          .value(1.0d)
          .value(new BigInteger("4711"))
          .value(new BigDecimal(1.0d))
          .value(1.0)
        .end()
        .flush();
        Assert.assertEquals("[42,-1,4711,4711,1.0,1.0,4711,1,1.0]", writer.toString());
    }

    @Test
    public void testArrayWithKey() throws Exception {
        restWriter
        .set(JSONRestWriter.NAMED_ROOT)
        .key("k").array()
          .value("x")
          .value(4711)
          .value(1.0)
          .value(false)
        .end()
        .flush();
        Assert.assertEquals("\"k\":[\"x\",4711,1.0,false]", writer.toString());
    }

    @Test
    public void testCollection() throws Exception {
        restWriter
        .set(JSONRestWriter.NAMED_ROOT)
        .collection("k", "i", Arrays.asList("a", "b"))
        .flush();
        Assert.assertEquals("\"k\":[\"a\",\"b\"]", writer.toString());
    }

    @Test
    public void testArrayWithItemKeys() throws Exception {
        restWriter
        .array("e")
          .value("x")
          .value(4711)
          .value(1.0)
        .end()
        .flush();
        Assert.assertEquals("[\"x\",4711,1.0]",
                writer.toString());
    }

    @Test
    public void testNamedArrayWithItemKeys() throws Exception {
        restWriter
        .set(JSONRestWriter.NAMED_ROOT)
        .array("k", "e")
          .value("x")
          .value(4711)
          .value(1.0)
        .end()
        .flush();
        Assert.assertEquals("\"k\":[\"x\",4711,1.0]", writer.toString());
    }

    @Test
    public void testArrayWithKeyAndItemKeys() throws Exception {
        restWriter
        .set(JSONRestWriter.NAMED_ROOT)
        .key("k").array("e")
          .value("x")
          .value(4711)
          .value(1.0)
        .end()
        .flush();
        Assert.assertEquals("\"k\":[\"x\",4711,1.0]", writer.toString());
    }

    @Test
    public void testArrayWithKeyAndNullItemKey() throws Exception {
        restWriter
        .set(JSONRestWriter.NAMED_ROOT)
        .array("k", null)
          .value("x")
          .value(4711)
          .value(1.0)
        .end()
        .flush();
        Assert.assertEquals("\"k\":[\"x\",4711,1.0]", writer.toString());
    }

    @Test
    public void testArrayNullKeyAndNullItemKey() throws Exception {
        restWriter
        .array(null, null)
          .value("x")
          .value(4711)
          .value(1.0)
        .end()
        .flush();
        Assert.assertEquals("[\"x\",4711,1.0]", writer.toString());
    }

    @Test
    public void testArrayWithUnnamedItems() throws Exception {
        restWriter
        .array()
          .item().value("x").end()
          .item().value(4711).end()
          .item().value(1.0).end()
          .item().value(true).end()
        .end()
        .flush();
        Assert.assertEquals("[\"x\",4711,1.0,true]",
                writer.toString());
    }

    @Test
    public void testArrayWithNamedItems() throws Exception {
        restWriter
        .array()
          .item("a").value("x").end()
          .item("b").value(4711).end()
          .item("c").value(1.0).end()
        .end()
        .flush();
        Assert.assertEquals("[\"x\",4711,1.0]", writer.toString());
    }

    @Test
    public void testArrayWithPartiallyNamedItems() throws Exception {
        restWriter
        .array("a")
          .item().value("x").end()
          .item("b").value(4711).end()
          .item().value(1.0).end()
        .end()
        .flush();
        Assert.assertEquals("[\"x\",4711,1.0]",
                writer.toString());
    }

    @Test
    public void testArrayOverrideGlobalItemKey() throws Exception {
        restWriter
        .array("a")
          .item("b").value("x").end()
        .end()
        .flush();
        Assert.assertEquals("[\"x\"]", writer.toString());
    }

    @Test
    public void testArrayWithInnerUnnamedObject() throws Exception {
        restWriter
        .array()
          .object()
            .pair("x", "foo")
            .pair("y", 4711)
            .pair("z", 1.0)
            .pair("b", true)
           .end()
        .end()
        .flush();
        Assert.assertEquals("[{\"x\":\"foo\",\"y\":4711,\"z\":1.0,\"b\":true}]",
                writer.toString());
    }

    @Test
    public void testArrayWithMultipleUnnamedObjects() throws Exception {
        restWriter
        .array()
          .object().pair("x", "foo").end()
          .object().pair("y", 4711).end()
          .object().pair("z", 1.0).end()
          .object().pair("b", false).end()
        .end()
        .flush();
        Assert.assertEquals("[{\"x\":\"foo\"},{\"y\":4711},{\"z\":1.0},{\"b\":false}]",
                writer.toString());
    }

    @Test
    public void testArrayWithMultipleNamedObjects() throws Exception {
        restWriter
        .array()
          .object("a").pair("x", "foo").end()
          .object("b").pair("y", 4711).end()
          .object("c").pair("z", 1.0).end()
        .end()
        .flush();
        Assert.assertEquals("[{\"x\":\"foo\"},{\"y\":4711},{\"z\":1.0}]",
                writer.toString());
    }

    @Test
    public void testArrayWithInnerObjectNamedByItemKey() throws Exception {
        restWriter
        .array()
          .item("e")
            .object()
              .pair("x", "foo")
              .pair("y", 4711)
              .pair("z", 1.0)
             .end()
           .end()
        .end()
        .flush();
        Assert.assertEquals("[{\"x\":\"foo\",\"y\":4711,\"z\":1.0}]",
                writer.toString());
    }

    @Test
    public void testArrayWithInnerUnnamedArray() throws Exception {
        restWriter
        .array()
          .array()
            .value("x")
            .value(4711)
            .value(1.0)
           .end()
        .end()
        .flush();
        Assert.assertEquals("[[\"x\",4711,1.0]]",
                writer.toString());
    }

    @Test
    public void testArrayWithInnerNamedObject() throws Exception {
        restWriter
        .array()
          .object("inner")
            .pair("x", "foo")
            .pair("y", 4711)
            .pair("z", 1.0)
          .end()
        .end()
        .flush();
        Assert.assertEquals("[{\"x\":\"foo\",\"y\":4711,\"z\":1.0}]",
                writer.toString());
    }

    @Test
    public void testArrayInnerNamedObjectIgnoreItemKey() throws Exception {
        restWriter
        .set(JSONRestWriter.NAMED_ROOT)
        .array("outter", "item")
          .object("inner")
            .pair("x", "foo")
            .pair("y", 4711)
            .pair("z", 1.0)
          .end()
        .end()
        .flush();
        Assert.assertEquals("\"outter\":[{\"x\":\"foo\",\"y\":4711,\"z\":1.0}]",
                writer.toString());
    }

    @Test
    public void testArrayInnerObjectWithItemKey() throws Exception {
        restWriter
        .set(JSONRestWriter.NAMED_ROOT)
        .array("outter", "inner")
          .object()
            .pair("x", "foo")
            .pair("y", 4711)
            .pair("z", 1.0)
          .end()
        .end()
        .flush();
        Assert.assertEquals("\"outter\":[{\"x\":\"foo\",\"y\":4711,\"z\":1.0}]",
                writer.toString());
    }

    @Test
    public void testNamedArrayWithAttributeNoItems() throws Exception {
        restWriter
        .set(JSONRestWriter.NAMED_ROOT)
        .array("k", "e")
          .attribute("a", "b")
        .end()
        .flush();
        Assert.assertEquals("\"k\":[{\"a\":\"b\"}]",
                writer.toString());
    }

    @Test
    public void testNamedArrayWithAttributeAndItems() throws Exception {
        restWriter
        .set(JSONRestWriter.NAMED_ROOT)
        .array("k", "e")
          .attribute("a", "b")
          .attribute("c", "d")
          .value("x")
          .value(4711)
          .value(1.0)
        .end()
        .flush();
        Assert.assertEquals("\"k\":[{\"a\":\"b\"},{\"c\":\"d\"},\"x\",4711,1.0]",
                writer.toString());
    }

    @Test
    public void testUnnamedArrayWithAttribute() throws Exception {
        restWriter
        .array()
          .attribute("a", "b")
        .end()
        .flush();
        Assert.assertEquals("[{\"a\":\"b\"}]", writer.toString());
    }

    @Test
    public void testAttributeAfterItems() throws Exception {
        restWriter
        .array()
          .value("x")
          .attribute("a", "b")
        .end()
        .flush();
        Assert.assertEquals("[\"x\",{\"a\":\"b\"}]", writer.toString());
    }

    @Test
    public void testAnonymousObject() throws Exception {
        restWriter
        .object()
          .pair("x", "foo")
          .pair("y", 4711)
          .pair("z", 1.0)
        .end()
        .flush();
        Assert.assertEquals("{\"x\":\"foo\",\"y\":4711,\"z\":1.0}",
                writer.toString());
    }

    @Test
    public void testObjectVariousNumbers() throws Exception {
        restWriter
        .object()
          .pair("a", (byte)42)
          .pair("b", (short)-1)
          .pair("c", (int)4711)
          .pair("d", 4711L)
          .pair("e", (float)1.0)
          .pair("f", 1.0d)
          .pair("g", new BigInteger("4711"))
          .pair("h", new BigDecimal(1.0d))
          .pair("i", 1.0)
        .end()
        .flush();
        Assert.assertEquals("{\"a\":42,\"b\":-1,\"c\":4711,\"d\":4711,\"e\":1.0,\"f\":1.0,\"g\":4711,\"h\":1,\"i\":1.0}",
                writer.toString());
    }

    @Test
    public void testEmptyAnonymousObject() throws Exception {
        restWriter
        .object()
        .end()
        .flush();
        Assert.assertEquals("{}", writer.toString());
    }


    @Test
    public void testNamedObject() throws Exception {
        restWriter
        .set(JSONRestWriter.NAMED_ROOT)
        .object("items")
          .pair("x", "foo")
          .pair("y", 4711)
          .pair("z", 1.0)
        .end()
        .flush();
        Assert.assertEquals("\"items\":{\"x\":\"foo\",\"y\":4711,\"z\":1.0}",
                writer.toString());
    }

    @Test
    public void testEmptyNamedObject() throws Exception {
        restWriter
        .set(JSONRestWriter.NAMED_ROOT)
        .object("k")
        .end()
        .flush();
        Assert.assertEquals("\"k\":{}", writer.toString());
    }

    @Test
    public void testObjectWithKey() throws Exception {
        restWriter
        .set(JSONRestWriter.NAMED_ROOT)
        .key("items").object()
          .pair("x", "foo")
          .pair("y", 4711)
          .pair("z", 1.0)
        .end()
        .flush();
        Assert.assertEquals("\"items\":{\"x\":\"foo\",\"y\":4711,\"z\":1.0}",
                writer.toString());
    }

    @Test
    public void testObjectOverrideKey() throws Exception {
        restWriter
        .set(JSONRestWriter.NAMED_ROOT)
        .key("foo").object("bar")
          .pair("x", "foo")
          .pair("y", 4711)
          .pair("z", 1.0)
        .end()
        .flush();
        Assert.assertEquals("\"bar\":{\"x\":\"foo\",\"y\":4711,\"z\":1.0}",
                writer.toString());
    }

    @Test
    public void testObjectWithInnerObject() throws Exception {
        restWriter
        .set(JSONRestWriter.NAMED_ROOT)
        .key("outter").object()
          .key("inner").object()
            .pair("x", "foo")
            .pair("y", 4711)
            .pair("z", 1.0)
          .end()
        .end()
        .flush();
        Assert.assertEquals("\"outter\":{\"inner\":{\"x\":\"foo\",\"y\":4711,\"z\":1.0}}",
                writer.toString());
    }

    @Test
    public void testObjectWithAttributesAndValue() throws Exception {
        restWriter
        .set(JSONRestWriter.NAMED_ROOT)
        .object("k")
          .attribute("a", "b")
          .attribute("c", 4711)
          .attribute("d", 1.0)
          .attribute("e", true)
          .attribute("f", TestUUIDs.TEST_UUIDS[0])
          .value("foobar")
        .end()
        .flush();
        Assert.assertEquals("\"k\":{\"a\":\"b\",\"c\":4711,\"d\":1.0,\"e\":true,\"f\":\""
                + TestUUIDs.TEST_UUIDS[0].toString() + "\",\"value\":\"foobar\"}",
                writer.toString());
    }

    @Test
    public void testObjectWithNumberAttributes() throws Exception {
        restWriter
        .set(JSONRestWriter.NAMED_ROOT)
        .object("k")
          .attribute("a", (byte)42)
          .attribute("b", (short)-1)
          .attribute("c", (int)4711)
          .attribute("d", 4711L)
          .attribute("e", (float)1.0)
          .attribute("f", 1.0d)
          .attribute("g", new BigInteger("4711"))
          .attribute("h", new BigDecimal(1.0d))
          .attribute("i", 1.0)
        .end()
        .flush();
        Assert.assertEquals("\"k\":{\"a\":42,\"b\":-1,\"c\":4711,\"d\":4711,\"e\":1.0,"
                + "\"f\":1.0,\"g\":4711,\"h\":1,\"i\":1.0}",
                writer.toString());
    }

    @Test
    public void testUnnamedObjectWithValue() throws Exception {
        restWriter
        .object()
          .value("x")
        .end()
        .flush();
        Assert.assertEquals("{\"value\":\"x\"}", writer.toString());
    }

    @Test(expected=IllegalStateException.class)
    public void testObjectWithSecondValue() throws Exception {
        restWriter
        .object()
          .value("x")
          .value("y");
    }

    @Test
    public void testUnnamedObjectWithAttribute() throws Exception {
        restWriter
        .object()
          .attribute("a", "b")
        .end()
        .flush();
        Assert.assertEquals("{\"a\":\"b\"}", writer.toString());
    }

    @Test(expected=IllegalStateException.class)
    public void testObjectWithAttributeFollowingValue() throws Exception {
        restWriter
        .object()
          .value("foobar")
          .attribute("a", "b");
    }

    @Test
    public void testObjectWithLinks() throws Exception {
        restWriter
        .object()
          .link("a", "b")
          .link("c", "d")
        .end()
        .flush();
        Assert.assertEquals("{\"link\":{\"rel\":\"a\",\"href\":\"b\"},\"link\":{\"rel\":\"c\",\"href\":\"d\"}}",
                writer.toString());
    }

    @Test
    public void testLinks() throws Exception {
        restWriter
        .set(JSONRestWriter.NAMED_ROOT)
        .links()
          .link("a", "b")
          .link("c", "d")
          .link("e", "1", "2")
          .link("f", restWriter.hrefOf("1", "2"))
          .set(RestWriter.RELATIVE_LINKS)
          .link("g", "1", "2")
          .link("h", restWriter.hrefOf("1", "2"))
        .end()
        .flush();
        Assert.assertEquals("\"links\":[{\"rel\":\"a\",\"href\":\"b\"},"
                + "{\"rel\":\"c\",\"href\":\"d\"},"
                + "{\"rel\":\"e\",\"href\":\"http://example.org/1/2\"},"
                + "{\"rel\":\"f\",\"href\":\"http://example.org/1/2\"},"
                + "{\"rel\":\"g\",\"href\":\"1/2\"},"
                + "{\"rel\":\"h\",\"href\":\"1/2\"}]",
                writer.toString());
    }

    @Test
    public void testTimestamp() throws Exception {
        long now = System.currentTimeMillis();
        String timestamp = FormatUtils.formatUTC(now);
        restWriter
        .set(JSONRestWriter.NAMED_ROOT)
        .timestamp("timestamp", now)
        .flush();
        Assert.assertEquals("\"timestamp\":{\"millis\":" + now + ",\"value\":\"" + timestamp + "\"}" ,
                writer.toString());
    }

    @Test
    public void testObjectWithInnerArray() throws Exception {
        restWriter
        .set(JSONRestWriter.NAMED_ROOT)
        .key("outter").object()
          .key("inner").array()
            .value("x")
            .value(4711)
            .value(1.0)
          .end()
        .end()
        .flush();
        Assert.assertEquals("\"outter\":{\"inner\":[\"x\",4711,1.0]}",
                writer.toString());
    }

    @Test
    public void testObjectWithAnonymousInnerArray() throws Exception {
        restWriter
        .set(JSONRestWriter.NAMED_ROOT)
        .key("outter").object()
          .array()
            .value("x")
            .value(4711)
            .value(1.0)
          .end()
        .end()
        .flush();
        Assert.assertEquals("\"outter\":{[\"x\",4711,1.0]}",
                writer.toString());
    }

    @Test
    public void testObjectWithInnerArrayIgnoreNamedItems() throws Exception {
        restWriter
        .set(JSONRestWriter.NAMED_ROOT)
        .key("outter").object()
          .key("inner").array("e")
            .value("x")
            .value(4711)
            .value(1.0)
          .end()
        .end()
        .flush();
        Assert.assertEquals("\"outter\":{\"inner\":[\"x\",4711,1.0]}",
                writer.toString());
    }

    @Test(expected=IllegalStateException.class)
    public void testValueWithNullKey() throws Exception {
        restWriter
        .object("k")
          .pair(null, "x");
    }

    @Test
    public void testSurpressNullValue() throws Exception {
        restWriter
        .set(JSONRestWriter.NAMED_ROOT)
        .object("k")
          .pair("x", (String)null)
          .pair("y", "z")
        .end()
        .flush();
        Assert.assertEquals("\"k\":{\"y\":\"z\"}", writer.toString());
    }

    @Test
    public void testValueWithNullValue() throws Exception {
        restWriter
        .set(JSONRestWriter.NAMED_ROOT | RestWriter.ALL_MEMBERS)
        .object("k")
          .pair("x", (String)null)
          .pair("y", "z")
        .end()
        .flush();
        Assert.assertEquals("\"k\":{\"x\":null,\"y\":\"z\"}", writer.toString());
    }

    @Test
    public void testSurpressBlankValue() throws Exception {
        restWriter
        .set(JSONRestWriter.NAMED_ROOT)
        .object("k")
          .pair("x", "")
          .pair("y", "z")
        .end()
        .flush();
        Assert.assertEquals("\"k\":{\"y\":\"z\"}", writer.toString());
    }

    @Test
    public void testValueWithBlankValue() throws Exception {
        restWriter
        .set(JSONRestWriter.NAMED_ROOT | RestWriter.ALL_MEMBERS)
        .object("k")
          .pair("x", "")
          .pair("y", "z")
        .end()
        .flush();
        Assert.assertEquals("\"k\":{\"x\":\"\",\"y\":\"z\"}", writer.toString());
    }

    @Test(expected=IllegalStateException.class)
    public void testObjectMissingEnd() throws Exception {
        restWriter
        .object("k")
          .pair("x", "y")
        .flush();
    }

    @Test(expected=IllegalStateException.class)
    public void testArrayMissingEnd() throws Exception {
        restWriter
        .array("k", "e")
          .value("x")
        .flush();
    }

    @Test(expected=IllegalStateException.class)
    public void testItemMissingEnd() throws Exception {
        restWriter
        .array("k")
          .item().value("x")
        .end()
        .flush();
    }

    @Test(expected=IllegalStateException.class)
    public void testInvalidObjectFollowingFinalState() throws Exception {
        restWriter
        .object("k")
        .end()
        .object("k");
    }

    @Test(expected=IllegalStateException.class)
    public void testInvalidArrayFollowingFinalState() throws Exception {
        restWriter
        .object("k")
        .end()
        .array("k", "e");
    }

    @Test(expected=IllegalStateException.class)
    public void testInvalidNamedValueFollowingFinalState() throws Exception {
        restWriter
        .object("k")
        .end()
        .pair("x", "y");
    }

    @Test(expected=IllegalStateException.class)
    public void testInvalidValueFollowingFinalState() throws Exception {
        restWriter
        .object("k")
        .end()
        .value("x");
    }

    @Test(expected=IllegalStateException.class)
    public void testInvalidAttributeFollowingFinalState() throws Exception {
        restWriter
        .object("k")
        .end()
        .attribute("x", "y");
    }

    @Test(expected=IllegalStateException.class)
    public void testRedundantEnd() throws Exception {
        restWriter
        .object("k")
        .end()
        .end();
    }

    @Test(expected=IllegalStateException.class)
    public void testEndInInitialState() throws Exception {
        restWriter
        .end();
    }

    @Test(expected=IllegalStateException.class)
    public void testUnnamedValueInitialState() throws Exception {
        restWriter
        .value("x");
    }

    @Test(expected=IllegalStateException.class)
    public void testNamedValueInitialState() throws Exception {
        restWriter
        .pair("x", "y");
    }

    @Test
    public void testNamespaceAttributes() throws Exception {
        restWriter
          .set(JSONRestWriter.NAMESPACE_ATTRIBUTES)
          .object()
          .namespace("xmlns:xsi", "foo")
          .attribute("a", "b")
          .pair("x", "y")
          .value("foobar")
        .end()
        .flush();
        Assert.assertEquals("{\"xmlns:xsi\":\"foo\",\"a\":\"b\",\"x\":\"y\",\"value\":\"foobar\"}",
              writer.toString());
    }

    @Test
    public void testPrefixedAttributes() throws Exception {
        StringWriter writer = new StringWriter();
        RestWriter restWriter = new JSONRestWriter(writer, "host",
                JSONRestWriter.PREFIXED_ATTRIBUTES | JSONRestWriter.NAMESPACE_ATTRIBUTES);
        restWriter
          .object()
          .namespace("xmlns:xsi", "foo")
          .attribute("a", "b")
          .pair("x", "y")
          .value("foobar")
        .end()
        .flush();
        Assert.assertEquals("{\"@xmlns:xsi\":\"foo\",\"@a\":\"b\",\"x\":\"y\",\"value\":\"foobar\"}",
              writer.toString());
    }

    @Test
    public void testComplexArray() throws Exception {
        StringWriter writer = new StringWriter();
        RestWriter restWriter = new JSONRestWriter(writer, "host",
                JSONRestWriter.NAMESPACE_ATTRIBUTES | JSONRestWriter.NAMED_ROOT);
        restWriter
        .array("links", "link")
          .item()
            .object()
              .attribute("rel", "browse")
              .attribute("href", "A")
              .value("foobar")
            .end()
          .end()
          .key("inner")
          .array()
            .item().value("a").end()
            .key("ignore").value(4711)
            .value(1.0d)
            .item("e").value("b").end()
            .object().pair("foo", "bar").end()
            .value("hugo")
          .end()
          .object("ooo")
            .namespace("xmlns:xsi", "something")
            .attribute("a", "b")
            .attribute("c", "d")
            .pair("x", "y")
          .end()
        .end()
        .flush();
        Assert.assertEquals("\"links\":["
            + "{\"rel\":\"browse\",\"href\":\"A\",\"value\":\"foobar\"},"
            + "[\"a\",4711,1.0,\"b\",{\"foo\":\"bar\"},\"hugo\"],"
            + "{\"xmlns:xsi\":\"something\",\"a\":\"b\",\"c\":\"d\",\"x\":\"y\"}]",
            writer.toString());
    }
}
