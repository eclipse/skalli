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
package org.eclipse.skalli.commons;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XMLUtils {

    // no instances, please!
    private XMLUtils() {
    }

    /**
     * Returns a new, empty XML document.
     * @throws ParserConfigurationException  a serious serious configuration error occured.
     */
    public static Document newDocument() throws ParserConfigurationException {
        return getDocumentBuilder().newDocument();
    }

    /**
     * Reads and parses an XML document from a given file.
     *
     * @param file  the file to read and parse.
     * @return  an XML document parsed from the given file.
     *
     * @throws SAXException  if a parsing error occurd.
     * @throws IOException  if an i/o error occured.
     * @throws ParserConfigurationException  a serious serious configuration error occured.
     */
    public static Document documentFromFile(File file) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilder docBuilder = getDocumentBuilder();
        Document doc = docBuilder.parse(file);
        return doc;
    }

    /**
     * Reads and parses an XML document from a given stream.
     *
     * @param in  the stream to read and parse.
     * @return  an XML document parsed from the given stream.
     *
     * @throws SAXException  if a parsing error occurd.
     * @throws IOException  if an i/o error occured.
     * @throws ParserConfigurationException  a serious serious configuration error occured.
     */
    public static Document documentFromStream(InputStream in) throws SAXException, IOException,
            ParserConfigurationException {
        DocumentBuilder docBuilder = getDocumentBuilder();
        Document doc = docBuilder.parse(in);
        return doc;
    }

    /**
     * Reads and parses an XML document from a given string.
     *
     * @param xml  the string to read and parse.
     * @return  an XML document parsed from the given string.
     *
     * @throws SAXException  if a parsing error occurd.
     * @throws IOException  if an i/o error occured.
     * @throws ParserConfigurationException  a serious serious configuration error occured.
     */
    public static Document documentFromString(String xml) throws SAXException, IOException,
            ParserConfigurationException {
        DocumentBuilder docBuilder = getDocumentBuilder();
        Document doc = docBuilder.parse(new InputSource(new StringReader(xml)));
        return doc;
    }

    /**
     * Transforms the given XML document into its textual representation.
     *
     * @param doc  the document to transform.
     * @return  the XML document transformed to a string.
     *
     * @throws TransformerException  if the transformation failed.
     */
    public static String documentToString(Document doc) throws TransformerException {
        StreamResult result = new StreamResult(new StringWriter());
        transform(doc, result);
        String xmlString = result.getWriter().toString();
        return xmlString;
    }

    /**
     * Transforms the given XML document into its textual representation and
     * writes the result to a file.
     *
     * @param doc  the document to transform.
     * @param file  the target file.
     *
     * @throws TransformerException  if the transformation failed.
     * @throws FileNotFoundException  if a folder with the same name exists,
     * or the file cannot be created or opened.
     */
    public static void documentToFile(Document doc, File file) throws TransformerException, FileNotFoundException {
        FileOutputStream fos = new FileOutputStream(file);
        try {
            StreamResult result = new StreamResult(fos);
            transform(doc, result);
        } finally {
            IOUtils.closeQuietly(fos);
        }
    }

    /**
     * Transforms the given XML document into its textual representation and
     * provides the result as input stream.
     *
     * @param doc  the document to transform.
     * @return  an input stream from which the textual representation of the XML document can be read.
     *
     * @throws TransformerException  if the transformation failed.
     */
    public static InputStream documentToStream(Document doc) throws TransformerException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            StreamResult result = new StreamResult(os);
            transform(doc, result);
            return new ByteArrayInputStream(os.toByteArray());
        } finally {
            IOUtils.closeQuietly(os);
        }
    }

    private static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        return docBuilder;
    }

    private static void transform(Document doc, StreamResult result) throws TransformerException {
        DOMSource source = new DOMSource(doc);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
        transformer.transform(source, result);
    }
}
