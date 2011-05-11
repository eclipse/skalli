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
package org.eclipse.skalli.common.util;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.eclipse.skalli.testutil.PropertyHelperUtils;
import org.eclipse.skalli.testutil.TestEntityBase1;
import org.eclipse.skalli.testutil.TestExtension;
import org.eclipse.skalli.testutil.TestExtension1;

public class XMLUtilsTest {

    private static final String XML = "<bla><hello>world</hello><blubb>noop</blubb></bla>";

    @Test
    public void testDocumentFromString() throws Exception {
        Document res = XMLUtils.documentFromString(XML);
        Assert.assertNotNull(res);
        Assert.assertEquals(1, res.getElementsByTagName("hello").getLength());
        Assert.assertEquals(1, res.getElementsByTagName("blubb").getLength());
    }

    @Test
    public void testDocumentFromStream() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(XML.getBytes());
        Document res = XMLUtils.documentFromStream(in);
        Assert.assertNotNull(res);
        Assert.assertEquals(1, res.getElementsByTagName("hello").getLength());
        Assert.assertEquals(1, res.getElementsByTagName("blubb").getLength());
    }

    @Test
    public void testDocumentFromFile() throws Exception {
        File tmpFile = null;
        try {
            tmpFile = File.createTempFile("testDocumentFromFile", ".xml");
            FileUtils.writeStringToFile(tmpFile, XML);

            Document res = XMLUtils.documentFromFile(tmpFile);
            Assert.assertNotNull(res);
            Assert.assertEquals(1, res.getElementsByTagName("hello").getLength());
            Assert.assertEquals(1, res.getElementsByTagName("blubb").getLength());
        } finally {
            if (tmpFile != null) {
                tmpFile.delete();
            }
        }
    }

    private Document createDocument() throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        Element root = doc.createElement("hello");
        doc.appendChild(root);
        return doc;
    }

    @Test
    public void testDocumentToString() throws Exception {
        Document doc = createDocument();
        String res = XMLUtils.documentToString(doc);
        Assert.assertNotNull(res);
        Assert.assertTrue(res.contains("<hello"));
    }

    @Test
    public void testDocumentToFile() throws Exception {
        File tmpFile = null;
        try {
            tmpFile = File.createTempFile("testDocumentToString", ".xml");
            Assert.assertTrue(tmpFile.length() == 0);

            Document doc = createDocument();
            XMLUtils.documentToFile(doc, tmpFile);
            Assert.assertTrue(tmpFile.exists());
            Assert.assertTrue(tmpFile.length() > 0);
        } finally {
            if (tmpFile != null) {
                tmpFile.delete();
            }
        }
    }

    private static final String ROOT_TAG = TestEntityBase1.class.getName();
    private static final String PROPERTY_STR = "str";
    private static final String PROPERTY_STR_VALUE = "value";
    private static final String PROPERTY_STRSET = "strset";
    private static final String TESTEXTENSION_TAG = TestExtension.class.getName();
    private static final String TESTEXTENSION1_TAG = TestExtension1.class.getName();

    private static boolean EXISTS = true;
    private static boolean NOT_EXISTS = false;
    private static boolean COPY = true;
    private static boolean MOVE = false;
    private static boolean TO_EXISTING_EXTENSION = true;
    private static boolean TO_NON_EXISTING_EXTENSION = false;
    private static boolean WITH_RENAME = true;
    private static boolean WITHOUT_RENAME = false;
    private static boolean ATTRIBUTE_OF_EXTENSION = true;
    private static boolean ATTRIBUTE_OF_ENTITY = false;
    private static boolean WITHOUT_COMPARATOR = true;
    private static boolean WITH_COMPARATOR = false;

    @Test
    public void testMoveToExistingExtension() throws Exception {
        assertCopyOrMove(MOVE, TO_EXISTING_EXTENSION, WITHOUT_RENAME);
    }

    @Test
    public void testMoveToNonExistingExtension() throws Exception {
        assertCopyOrMove(MOVE, TO_NON_EXISTING_EXTENSION, WITHOUT_RENAME);
    }

    @Test
    public void testMoveToExistingExtensionAndRename() throws Exception {
        assertCopyOrMove(MOVE, TO_EXISTING_EXTENSION, WITH_RENAME);
    }

    @Test
    public void testMoveToNonExistingExtensionAndRename() throws Exception {
        assertCopyOrMove(MOVE, TO_NON_EXISTING_EXTENSION, WITH_RENAME);
    }

    @Test
    public void testCopyToExistingExtension() throws Exception {
        assertCopyOrMove(COPY, TO_EXISTING_EXTENSION, WITHOUT_RENAME);
    }

    @Test
    public void testCopyToNonExistingExtension() throws Exception {
        assertCopyOrMove(COPY, TO_NON_EXISTING_EXTENSION, WITHOUT_RENAME);
    }

    @Test
    public void testCopyToExistingExtensionAndRename() throws Exception {
        assertCopyOrMove(COPY, TO_EXISTING_EXTENSION, WITH_RENAME);
    }

    @Test
    public void testCopyToNonExistingExtensionAndRename() throws Exception {
        assertCopyOrMove(COPY, TO_NON_EXISTING_EXTENSION, WITH_RENAME);
    }

    @Test
    public void testMoveFromExtensionToExistingExtension() throws Exception {
        assertMoveExtensionToExtension(TO_EXISTING_EXTENSION, WITHOUT_RENAME);
    }

    @Test
    public void testMoveFromExtensionToNonExistingExtension() throws Exception {
        assertMoveExtensionToExtension(TO_NON_EXISTING_EXTENSION, WITHOUT_RENAME);
    }

    @Test
    public void testMoveFromExtensionToExistingExtensionWithRename() throws Exception {
        assertMoveExtensionToExtension(TO_EXISTING_EXTENSION, WITH_RENAME);
    }

    @Test
    public void testMoveFromExtensionToNonExistingExtensionWithRename() throws Exception {
        assertMoveExtensionToExtension(TO_NON_EXISTING_EXTENSION, WITH_RENAME);
    }

    @Test
    public void testRenameTag() throws Exception {
        assertRenamed(ATTRIBUTE_OF_ENTITY);
    }

    @Test
    public void testRenameExtensionTag() throws Exception {
        assertRenamed(ATTRIBUTE_OF_EXTENSION);
    }

    @Test
    public void testMigrateStringToStringSet() throws Exception {
        assertStringSet(ATTRIBUTE_OF_ENTITY, WITH_COMPARATOR);
    }

    @Test
    public void testMigrateStringToStringSetNoComparator() throws Exception {
        assertStringSet(ATTRIBUTE_OF_ENTITY, WITHOUT_COMPARATOR);
    }

    @Test
    public void testMigrateExtensionStringToStringSet() throws Exception {
        assertStringSet(ATTRIBUTE_OF_EXTENSION, WITH_COMPARATOR);
    }

    @Test
    public void testMigrateExtensionStringToStringSetNoComparator() throws Exception {
        assertStringSet(ATTRIBUTE_OF_EXTENSION, WITHOUT_COMPARATOR);
    }

    private void assertElement(Element extensionsNode, String tagName) {
        assertNotNull(extensionsNode);
        assertEquals(tagName, extensionsNode.getNodeName());
    }

    private void assertElementsByTagName(Document doc, String tagName, boolean exists) {
        NodeList nodes = doc.getElementsByTagName(tagName);
        assertNotNull(nodes);
        assertEquals(exists ? 1 : 0, nodes.getLength());
    }

    private void assertExtensionAttribute(Document doc, String extensionName, String tagName, String value,
            boolean exists) {
        NodeList nodes = doc.getElementsByTagName(extensionName);
        assertEquals(1, nodes.getLength());
        Element extensionElement = (Element) nodes.item(0);
        assertNotNull(extensionElement);
        boolean hasTag = false;
        boolean hasValue = false;
        NodeList children = extensionElement.getChildNodes();
        for (int i = 0; i < children.getLength(); ++i) {
            Node child = children.item(i);
            if (tagName.equals(child.getNodeName())) {
                assertFalse(hasTag);
                if (value != null) {
                    hasValue = value.equals(child.getTextContent());
                }
                hasTag = true;
                break;
            }
        }
        if (exists) {
            assertTrue(hasTag);
            assertTrue(hasValue);
        } else {
            assertFalse(hasTag);
        }
    }

    private void assertCopyOrMove(boolean copy, boolean extensionExists, boolean rename) throws Exception {
        StringBuilder xml = new StringBuilder();
        beginXml(xml);
        beginNode(xml, ROOT_TAG);
        appendNode(xml, PROPERTY_STR, PROPERTY_STR_VALUE);
        if (extensionExists) {
            beginNode(xml, XMLUtils.EXTENSIONS_TAGNAME);
            appendTestExtension(xml);
            endNode(xml, XMLUtils.EXTENSIONS_TAGNAME);
        }
        endNode(xml, ROOT_TAG);

        Document doc = XMLUtils.documentFromString(xml.toString());
        if (rename) {
            if (copy) {
                XMLUtils.copyTagToExtension(doc, TESTEXTENSION_TAG, PROPERTY_STR, PROPERTY_STRSET);
            } else {
                XMLUtils.moveTagToExtension(doc, TESTEXTENSION_TAG, PROPERTY_STR, PROPERTY_STRSET);
            }
        } else {
            if (copy) {
                XMLUtils.copyTagToExtension(doc, TESTEXTENSION_TAG, PROPERTY_STR);
            } else {
                XMLUtils.moveTagToExtension(doc, TESTEXTENSION_TAG, PROPERTY_STR);
            }
        }
        assertExtensionAttribute(doc, ROOT_TAG, PROPERTY_STR, PROPERTY_STR_VALUE, copy ? EXISTS : NOT_EXISTS);
        assertExtensionAttribute(doc, TESTEXTENSION_TAG, rename ? PROPERTY_STRSET : PROPERTY_STR, PROPERTY_STR_VALUE,
                EXISTS);
    }

    private void assertMoveExtensionToExtension(boolean extensionExists, boolean rename) throws Exception {
        StringBuilder xml = new StringBuilder();
        beginXml(xml);
        beginNode(xml, ROOT_TAG);
        beginNode(xml, XMLUtils.EXTENSIONS_TAGNAME);
        appendTestExtension(xml);
        if (extensionExists) {
            beginNode(xml, TESTEXTENSION1_TAG);
            endNode(xml, TESTEXTENSION1_TAG);
        }
        endNode(xml, XMLUtils.EXTENSIONS_TAGNAME);
        endNode(xml, ROOT_TAG);

        Document doc = XMLUtils.documentFromString(xml.toString());
        XMLUtils.moveTagToExtension(doc, TESTEXTENSION_TAG, TESTEXTENSION1_TAG, PROPERTY_STR, rename ? PROPERTY_STRSET
                : PROPERTY_STR);
        assertExtensionAttribute(doc, TESTEXTENSION_TAG, PROPERTY_STR, null, NOT_EXISTS);
        assertExtensionAttribute(doc, TESTEXTENSION_TAG, PROPERTY_STRSET, null, NOT_EXISTS);
        assertExtensionAttribute(doc, TESTEXTENSION1_TAG, rename ? PROPERTY_STRSET : PROPERTY_STR, PROPERTY_STR_VALUE,
                EXISTS);
    }

    private void assertRenamed(boolean inExtension) throws Exception {
        StringBuilder xml = new StringBuilder();
        beginXml(xml);
        beginNode(xml, ROOT_TAG);
        if (inExtension) {
            beginNode(xml, XMLUtils.EXTENSIONS_TAGNAME);
            appendTestExtension(xml);
            endNode(xml, XMLUtils.EXTENSIONS_TAGNAME);
        } else {
            appendNode(xml, PROPERTY_STR, PROPERTY_STR_VALUE);
        }
        endNode(xml, ROOT_TAG);

        Document doc = XMLUtils.documentFromString(xml.toString());
        if (inExtension) {
            XMLUtils.renameTag(doc, TESTEXTENSION_TAG, PROPERTY_STR, PROPERTY_STRSET);
        } else {
            XMLUtils.renameTag(doc, PROPERTY_STR, PROPERTY_STRSET);
        }
        assertElementsByTagName(doc, PROPERTY_STR, NOT_EXISTS);
        assertElementsByTagName(doc, PROPERTY_STRSET, EXISTS);
    }

    private void assertStringSet(boolean inExtension, boolean noComparator) throws Exception {
        StringBuilder xml = new StringBuilder();
        beginXml(xml);
        beginNode(xml, ROOT_TAG);
        if (inExtension) {
            beginNode(xml, XMLUtils.EXTENSIONS_TAGNAME);
            appendTestExtension(xml);
            endNode(xml, XMLUtils.EXTENSIONS_TAGNAME);
        } else {
            appendNode(xml, PROPERTY_STR, PROPERTY_STR_VALUE);
        }
        endNode(xml, ROOT_TAG);

        Document doc = XMLUtils.documentFromString(xml.toString());
        if (inExtension) {
            XMLUtils.migrateStringToStringSet(doc, TESTEXTENSION_TAG, PROPERTY_STR, PROPERTY_STRSET, noComparator);
        } else {
            XMLUtils.migrateStringToStringSet(doc, PROPERTY_STR, PROPERTY_STRSET, noComparator);
        }
        assertElementsByTagName(doc, PROPERTY_STR, NOT_EXISTS);
        assertElementsByTagName(doc, PROPERTY_STRSET, EXISTS);
        Element setElement = (Element) doc.getElementsByTagName(PROPERTY_STRSET).item(0);
        assertNotNull(setElement);
        Node parent = setElement.getParentNode();
        assertNotNull(parent);
        assertEquals(inExtension ? TESTEXTENSION_TAG : ROOT_TAG, parent.getNodeName());
        NodeList setItems = setElement.getChildNodes();
        assertEquals(noComparator ? 2 : 1, setItems.getLength());
        if (noComparator) {
            assertEquals(XMLUtils.NO_COMPARATOR_TAGNAME, setItems.item(0).getNodeName());
        }
        assertEquals("string", setItems.item(noComparator ? 1 : 0).getNodeName());
        assertEquals(PROPERTY_STR_VALUE, setItems.item(noComparator ? 1 : 0).getTextContent());
    }

    private void beginXml(StringBuilder xml) {
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
    }

    private void beginNode(StringBuilder xml, String tagName) {
        xml.append("<").append(tagName).append(">");
    }

    private void endNode(StringBuilder xml, String tagName) {
        xml.append("</").append(tagName).append(">");
    }

    private void appendNode(StringBuilder xml, String tagName, Object value) {
        beginNode(xml, tagName);
        xml.append(value.toString());
        endNode(xml, tagName);
    }

    private void appendTestExtension(StringBuilder xml) {
        beginNode(xml, TESTEXTENSION_TAG);
        appendNode(xml, TestExtension.PROPERTY_BOOL, Boolean.TRUE);
        appendNode(xml, TestExtension.PROPERTY_STR, PROPERTY_STR_VALUE);
        appendNode(xml, TestExtension.PROPERTY_DELETED, Boolean.FALSE);
        appendNode(xml, TestExtension.PROPERTY_UUID, PropertyHelperUtils.TEST_UUIDS[0]);
        endNode(xml, TESTEXTENSION_TAG);
    }

}
