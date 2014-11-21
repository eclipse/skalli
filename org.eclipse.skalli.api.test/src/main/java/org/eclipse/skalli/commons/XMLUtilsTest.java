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
package org.eclipse.skalli.commons;

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

@SuppressWarnings("nls")
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
        ByteArrayInputStream in = new ByteArrayInputStream(XML.getBytes("UTF-8"));
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
}
