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

/**
 *
 */
@SuppressWarnings("nls")
public class XMLRestWriterTest {

    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"; //$NON-NLS-1$

    private StringWriter writer;
    private RestWriter restWriter;

    @Before
    public void setup() throws Exception {
        writer = new StringWriter();
        restWriter = new XMLRestWriter(writer, "http://example.org", JSONRestWriter.NAMED_ROOT);
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
        assertEquals("<item>x</item><item>4711</item><item>1.0</item><item>true</item>");
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
        assertEquals("<item>42</item><item>-1</item><item>4711</item><item>4711</item><item>1.0</item>"
                + "<item>1.0</item><item>4711</item><item>1</item><item>1.0</item>");
    }

    @Test
    public void testArrayWithKey() throws Exception {
        restWriter
        .key("k").array()
          .value("x")
          .value(4711)
          .value(1.0)
          .value(false)
        .end()
        .flush();
        assertEquals("<k><item>x</item><item>4711</item><item>1.0</item><item>false</item></k>");
    }

    @Test
    public void testCollection() throws Exception {
        restWriter
        .collection("k", "i", Arrays.asList("a", "b"))
        .flush();
        assertEquals("<k><i>a</i><i>b</i></k>");
    }

    @Test
    public void testArrayWithItemKeys() throws Exception {
        restWriter
        .array("e")
          .value("x")
          .value(4711)
          .value(1.0)
          .value(true)
        .end()
        .flush();
        assertEquals("<e>x</e><e>4711</e><e>1.0</e><e>true</e>");
    }

    @Test
    public void testNamedArrayWithItemKeys() throws Exception {
        restWriter
        .array("k", "e")
          .value("x")
          .value(4711)
          .value(1.0)
          .value(true)
        .end()
        .flush();
        assertEquals("<k><e>x</e><e>4711</e><e>1.0</e><e>true</e></k>");
    }

    @Test
    public void testArrayWithKeyAndItemKeys() throws Exception {
        restWriter
        .key("k").array("e")
          .value("x")
          .value(4711)
          .value(1.0)
          .value(true)
        .end()
        .flush();
        assertEquals("<k><e>x</e><e>4711</e><e>1.0</e><e>true</e></k>");
    }

    @Test
    public void testArrayWithKeyAndNullItemKey() throws Exception {
        restWriter
        .array("k", null)
          .value("x")
          .value(4711)
          .value(1.0)
          .value(false)
        .end()
        .flush();
        assertEquals("<k><item>x</item><item>4711</item><item>1.0</item><item>false</item></k>");
    }

    @Test
    public void testArrayNullKeyAndNullItemKey() throws Exception {
        restWriter
        .array(null, null)
          .value("x")
          .value(4711)
          .value(1.0)
          .value(false)
        .end()
        .flush();
        assertEquals("<item>x</item><item>4711</item><item>1.0</item><item>false</item>");
    }

    @Test(expected=IllegalStateException.class)
    public void testArrayInvalidNamedValues() throws Exception {
        restWriter
        .array()
          .pair("foo", "bar");
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
        assertEquals("<item>x</item><item>4711</item><item>1.0</item><item>true</item>");
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
        assertEquals("<a>x</a><b>4711</b><c>1.0</c>");
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
        assertEquals("<a>x</a><b>4711</b><b>1.0</b>");
    }

    @Test
    public void testArrayOverrideGlobalItemKey() throws Exception {
        restWriter
        .array("a")
          .item("b").value("x").end()
        .end()
        .flush();
        assertEquals("<b>x</b>");
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
        assertEquals("<item><x>foo</x><y>4711</y><z>1.0</z><b>true</b></item>");
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
        assertEquals("<item><x>foo</x></item><item><y>4711</y></item><item><z>1.0</z></item><item><b>false</b></item>");
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
        assertEquals("<a><x>foo</x></a><b><y>4711</y></b><c><z>1.0</z></c>");
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
        assertEquals("<e><x>foo</x><y>4711</y><z>1.0</z></e>");
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
        assertEquals("<item><item>x</item><item>4711</item><item>1.0</item></item>");
    }

    @Test
    public void testArrayWithInnerNamedObject() throws Exception {
        restWriter
        .array() //define itemKey="item"
          .object("inner") // key "inner" overwrites itemKey!
            .pair("x", "foo")
            .pair("y", 4711)
            .pair("z", 1.0)
          .end()
          .key("other").object() // key "other" overwrites itemKey!
            .pair("x", "foo")
          .end()
          .object() // itemKey is still "other"
              .pair("y", "bar")
          .end()
          .array() // itemKey is still "other", but for the array values it is "item"
              .value("1")
              .value("2")
          .end()
          .array("w") // itemKey is still "other", but for the array values it is "w"
              .value("1")
              .value("2")
          .end()
          .array("k", "w") // key "k" overwrites itemKey , but for the array values it is "w"
              .value("1")
              .value("2")
          .end()
        .end()
        .flush();
        assertEquals("<inner><x>foo</x><y>4711</y><z>1.0</z></inner>"
                + "<other><x>foo</x></other><other><y>bar</y></other>"
                + "<other><item>1</item><item>2</item></other>"
                + "<other><w>1</w><w>2</w></other>"
                + "<k><w>1</w><w>2</w></k>");
    }

    @Test
    public void testArrayInnerNamedObjectIgnoreItemKey() throws Exception {
        restWriter
        .array("outter", "item") //define itemKey="item"
          .object("inner") // object key "inner" overrules itemKey!
            .pair("x", "foo")
            .pair("y", 4711)
            .pair("z", 1.0)
          .end()
          .object() // no object key => use itemKey
            .pair("x", "foo")
            .pair("y", 4711)
            .pair("z", 1.0)
          .end()
        .end()
        .flush();
        assertEquals("<outter><inner><x>foo</x><y>4711</y><z>1.0</z></inner>"
                + "<inner><x>foo</x><y>4711</y><z>1.0</z></inner></outter>");
    }

    @Test
    public void testArrayInnerObjectWithItemKey() throws Exception {
        restWriter
        .array("outter", "inner")
          .object()
            .pair("x", "foo")
            .pair("y", 4711)
            .pair("z", 1.0)
          .end()
        .end()
        .flush();
        assertEquals("<outter><inner><x>foo</x><y>4711</y><z>1.0</z></inner></outter>");
    }

    @Test
    public void testNamedArrayWithAttributeNoItems() throws Exception {
        restWriter
        .array("k", "e")
          .attribute("a", "b")
        .end()
        .flush();
        assertEquals("<k a=\"b\"/>");
    }

    @Test
    public void testNamedArrayWithAttributeAndItems() throws Exception {
        restWriter
        .array("k", "e")
          .attribute("a", "b")
          .attribute("c", "d")
          .value("x")
          .value(4711)
          .value(1.0)
        .end()
        .flush();
        assertEquals("<k a=\"b\" c=\"d\"><e>x</e><e>4711</e><e>1.0</e></k>");
    }

    @Test
    public void testNestedArraysDifferentItemNames() throws Exception {
        restWriter
        .array("k", "a") //renders <k> and itemKey:="a"
            .array("b") //renders <a> and itemKey:= "b"
                .array() // renders <b> and itemKey:="item"
                   .array("c") //renders <item> and itemKey:="c"
                       .value("x") //renders <c>x</c>
                   .end() //renders </item>
                   .array("d") //renders <item> and itemKey:="d"
                       .value("y") //renders <d>y</d>
                   .end() //renders </item>
                .end() //renders </b>
                .array() //renders <b> and itemKey:="item"
                    .value("z") //renders <item>z</item>
                .end() //renders </b>
            .end() //renders </a>
            .array("e") //renders <a> and itemKey:="e"
                .value("w") //renders <e>w</e>
            .end() //renders </a>
        .end() //renders </k>
        .flush();
        assertEquals("<k><a><b><item><c>x</c></item><item><d>y</d></item></b><b><item>z</item></b></a><a><e>w</e></a></k>");
    }

    @Test
    public void testNestedAnyonmousInnerArray() throws Exception {
        restWriter
        .object("o") //renders <o>
          .attribute("a", 4711)
          .array("p") //itemKey:="p"
            .object() //renders <p>
              .pair("x", "1") //renders <x>1</x>
              .array()
                .link("rel", "href")
              .end()
            .end()
            .object()
              .pair("y", "2")
            .end()
          .end()
        .end() //renders </o>
        .flush();
        assertEquals("<o a=\"4711\"><p><x>1</x><link rel=\"rel\" href=\"href\"/></p><p><y>2</y></p></o>");
    }

    @Test
    public void testUnnamedArrayWithAttributes() throws Exception {
        restWriter
        .array()
          .attribute("a", "b")
          .attribute("x", "y")
        .end()
        .flush();
        assertEquals("<item a=\"b\"/><item x=\"y\"/>");
    }

    @Test
    public void testUnnamedArrayWithItemKeyAndAttributes() throws Exception {
        restWriter
        .array("foo")
          .attribute("a", "b")
          .attribute("x", "y")
        .end()
        .flush();
        assertEquals("<foo a=\"b\"/><foo x=\"y\"/>");
    }

    @Test
    public void testUnnamedArrayWithAttributeAndItems() throws Exception {
        restWriter
        .array("e")
          .attribute("a", "b")
          .attribute("c", "d")
          .value("x")
          .value(4711)
          .value(1.0)
        .end()
        .flush();
        assertEquals("<e a=\"b\"/><e c=\"d\"/><e>x</e><e>4711</e><e>1.0</e>");
    }

    @Test
    public void testAttributeAfterItem() throws Exception {
        restWriter
        .array()
          .value("x")
          .attribute("a", "b")
        .end()
        .flush();
        assertEquals("<item>x</item><item a=\"b\"/>");
    }

    @Test
    public void testAttributeAfterNamedItem() throws Exception {
        restWriter
        .array("k", "e")
          .value("x")
          .attribute("a", "b")
        .end()
        .flush();
        assertEquals("<k><e>x</e><e a=\"b\"/></k>");
    }

    @Test
    public void testAttributeAfterItemOverwriteNames() throws Exception {
        restWriter
        .array("k", "overwritten")
          .item("x").value("x").end()
          .item("y").attribute("a", "b").end()
        .end()
        .flush();
        assertEquals("<k><x>x</x><y a=\"b\"/></k>");
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
        assertEquals("<x>foo</x><y>4711</y><z>1.0</z>");
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
        assertEquals("<a>42</a><b>-1</b><c>4711</c><d>4711</d><e>1.0</e><f>1.0</f><g>4711</g><h>1</h><i>1.0</i>");
    }

    @Test
    public void testEmptyAnonymousObject() throws Exception {
        restWriter
        .object()
        .end()
        .flush();
        assertEquals("");
    }

    @Test
    public void testNamedObject() throws Exception {
        restWriter
        .object("k")
          .pair("x", "foo")
          .pair("y", 4711)
          .pair("z", 1.0)
        .end()
        .flush();
        assertEquals("<k><x>foo</x><y>4711</y><z>1.0</z></k>");
    }

    @Test
    public void testEmptyNamedObject() throws Exception {
        restWriter
        .object("k")
        .end()
        .flush();
        assertEquals("<k/>");
    }

    @Test
    public void testObjectWithKey() throws Exception {
        restWriter
        .key("k").object()
          .pair("x", "foo")
          .pair("y", 4711)
          .pair("z", 1.0)
        .end()
        .flush();
        assertEquals("<k><x>foo</x><y>4711</y><z>1.0</z></k>");
    }

    @Test
    public void testObjectOverrideKey() throws Exception {
        restWriter
        .key("foo").object("bar")
          .pair("x", "foo")
          .pair("y", 4711)
          .pair("z", 1.0)
        .end()
        .flush();
        assertEquals("<bar><x>foo</x><y>4711</y><z>1.0</z></bar>");
    }

    @Test
    public void testObjectWithInnerObject() throws Exception {
        restWriter
        .key("outter").object()
          .key("inner").object()
            .pair("x", "foo")
            .pair("y", 4711)
            .pair("z", 1.0)
          .end()
        .end()
        .flush();
        assertEquals("<outter><inner><x>foo</x><y>4711</y><z>1.0</z></inner></outter>");
    }

    @Test
    public void testObjectWithAttributeAndValue() throws Exception {
        restWriter
        .object("k")
          .attribute("a", "b")
          .attribute("c", 4711)
          .attribute("d", 1.0)
          .attribute("e", true)
          .attribute("f", TestUUIDs.TEST_UUIDS[0])
          .pair("g", "b")
          .pair("h", 4711)
          .pair("i", 1.0)
          .pair("j", true)
          .pair("k", TestUUIDs.TEST_UUIDS[0])
          .value("foobar")
        .end()
        .flush();
        assertEquals("<k a=\"b\" c=\"4711\" d=\"1.0\" e=\"true\" f=\""
                + TestUUIDs.TEST_UUIDS[0].toString()
                + "\">"
                + "<g>b</g><h>4711</h><i>1.0</i><j>true</j><k>"
                + TestUUIDs.TEST_UUIDS[0].toString()
                + "</k>foobar</k>");
    }

    @Test
    public void testObjectWithNumberAttributes() throws Exception {
        restWriter
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
        assertEquals("<k a=\"42\" b=\"-1\" c=\"4711\" d=\"4711\" e=\"1.0\" f=\"1.0\" g=\"4711\" h=\"1\" i=\"1.0\"/>");
    }

    public void testUnnamedObjectWithValue() throws Exception {
        restWriter
        .object()
        .value("x")
      .end()
      .flush();
       assertEquals("<value>x</value>");
    }

    @Test(expected=IllegalStateException.class)
    public void testObjectWithSecondValue() throws Exception {
        restWriter
        .object()
          .value("x")
          .value("y");
    }

    @Test(expected=IllegalStateException.class)
    public void testNamedObjectWithSecondValue() throws Exception {
        restWriter
        .object("k")
          .value("x")
          .value("y");
    }

    @Test
    public void testUnnamedObjectWithAttributes() throws Exception {
        restWriter
        .object()
          .attribute("a", "b")
          .attribute("x", "y")
        .end()
        .flush();
        assertEquals("<a>b</a><x>y</x>");
    }

    @Test(expected=IllegalStateException.class)
    public void testUnnamedObjectWithAttributeFollowingValue() throws Exception {
        restWriter
        .object()
          .value("foobar")
          .attribute("a", "b");
    }

    @Test(expected=IllegalStateException.class)
    public void testObjectWithAttributeFollowingValue() throws Exception {
        restWriter
        .object("k")
          .value("foobar")
          .attribute("a", "b");
    }

    @Test(expected=IllegalStateException.class)
    public void testObjectFollowingValue() throws Exception {
        restWriter
        .object()
          .value("x")
          .object();
    }

    @Test(expected=IllegalStateException.class)
    public void testArrayFollowingValue() throws Exception {
        restWriter
        .object()
          .value("x")
          .array();
    }

    @Test(expected=IllegalStateException.class)
    public void testItemFollowingValue() throws Exception {
        restWriter
        .object()
          .value("x")
          .item();
    }

    @Test
    public void testObjectWithLinks() throws Exception {
        restWriter
        .object()
          .link("a", "b")
          .link("c", "d")
        .end()
        .flush();
        assertEquals("<link rel=\"a\" href=\"b\"/><link rel=\"c\" href=\"d\"/>");
    }

    @Test
    public void testArrayWithLinks() throws Exception {
        restWriter
        .array()
          .link("a", "b")
          .link("c", "d")
        .end()
        .flush();
        assertEquals("<link rel=\"a\" href=\"b\"/><link rel=\"c\" href=\"d\"/>");
    }

    @Test
    public void testLinks() throws Exception {
        restWriter
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
        assertEquals("<link rel=\"a\" href=\"b\"/><link rel=\"c\" href=\"d\"/>"
                + "<link rel=\"e\" href=\"http://example.org/1/2\"/>"
                + "<link rel=\"f\" href=\"http://example.org/1/2\"/>"
                + "<link rel=\"g\" href=\"1/2\"/>"
                + "<link rel=\"h\" href=\"1/2\"/>");
    }

    @Test
    public void testTimestamp() throws Exception {
        long now = System.currentTimeMillis();
        String timestamp = FormatUtils.formatUTC(now);
        restWriter
        .timestamp("timestamp", now)
        .flush();
        assertEquals("<timestamp millis=\"" + now + "\">" + timestamp + "</timestamp>");
    }

    @Test
    public void testArrayWithItemKeyInnerLinks() throws Exception {
        restWriter
        .array("a")
          .link("a", "b")
          .link("c", "d")
        .end()
        .flush();
        assertEquals("<link rel=\"a\" href=\"b\"/><link rel=\"c\" href=\"d\"/>");
    }

    @Test
    public void testNamedArrayWithLinks() throws Exception {
        restWriter
        .key("k").array()
          .link("a", "b")
          .link("c", "d")
        .end()
        .flush();
        assertEquals("<k><link rel=\"a\" href=\"b\"/><link rel=\"c\" href=\"d\"/></k>");
    }

    @Test
    public void testObjectWithInnerArray() throws Exception {
        restWriter
        .key("outter").object()
          .key("inner").array()
            .value("x")
            .value(4711)
            .value(1.0)
          .end()
        .end()
        .flush();
        assertEquals("<outter><inner><item>x</item><item>4711</item><item>1.0</item></inner></outter>");
    }

    @Test
    public void testObjectWithInnerArrayWithNamedItems() throws Exception {
        restWriter
        .key("outter").object()
          .key("inner").array("e")
            .value("x")
            .value(4711)
            .value(1.0)
          .end()
        .end()
        .flush();
        assertEquals("<outter><inner><e>x</e><e>4711</e><e>1.0</e></inner></outter>");
    }

    @Test
    public void testObjectWithAnonymousInnerArray() throws Exception {
        restWriter
        .key("outter").object()
          .array()
            .value("x")
            .value(4711)
            .value(1.0)
          .end()
        .end()
        .flush();
        assertEquals("<outter><item>x</item><item>4711</item><item>1.0</item></outter>");
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
        .object("k")
          .pair("x", (String)null)
        .end()
        .flush();
        assertEquals("<k></k>");
    }


    @Test
    public void testValueWithNullValue() throws Exception {
        restWriter
        .set(RestWriter.ALL_MEMBERS)
        .object("k")
          .pair("x", (String)null)
        .end()
        .flush();
        assertEquals("<k><x/></k>");
    }

    @Test
    public void testSurpressBlankValue() throws Exception {
        restWriter
        .object("k")
          .pair("x", "")
        .end()
        .flush();
        assertEquals("<k></k>");
    }

    @Test
    public void testValueWithBlankValue() throws Exception {
        restWriter
        .set(RestWriter.ALL_MEMBERS)
        .object("k")
          .pair("x", "")
        .end()
        .flush();
        assertEquals("<k><x></x></k>");
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
    public void testComplexArray() throws Exception {
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
        assertEquals("<links>"
                + "<link rel=\"browse\" href=\"A\">foobar</link>"
                + "<inner><item>a</item><item>4711</item><item>1.0</item><e>b</e>"
                + "<e><foo>bar</foo></e><e>hugo</e></inner>"
                + "<ooo xmlns:xsi=\"something\" a=\"b\" c=\"d\"><x>y</x></ooo>"
                + "</links>");
    }

    private void assertEquals(String expected) throws Exception {
        Assert.assertEquals(XML_HEADER + expected, writer.toString());
    }
}
