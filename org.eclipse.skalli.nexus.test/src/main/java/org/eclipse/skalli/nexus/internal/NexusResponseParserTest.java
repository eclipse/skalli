/*******************************************************************************
 * Copyright (c) 2010 - 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.nexus.internal;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.skalli.commons.XMLUtils;
import org.eclipse.skalli.nexus.NexusClientException;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

@SuppressWarnings("nls")
public class NexusResponseParserTest {

    private Element getElement(String xml) throws SAXException, IOException, ParserConfigurationException {
        Document doc = XMLUtils.documentFromString(xml);
        return doc.getDocumentElement();
    }

    @Test
    public void testFindNode() throws SAXException, IOException, ParserConfigurationException, NexusClientException {
        Element rootElement = getElement("<root><from>0</from></root>");
        Node node = NexusResponseParser.findNode(rootElement, "from");
        assertThat(node.getNodeName(), is("from"));
    }

    @Test
    public void testFindNode_caseSensitive() throws SAXException, IOException, ParserConfigurationException,
            NexusClientException {
        Element rootElement = getElement("<root><from>0</from></root>");
        assertThat(NexusResponseParser.findNode(rootElement, "From"), nullValue());
    }

    @Test
    public void testFindNode_MoreThanOnce() throws SAXException, IOException, ParserConfigurationException,
            NexusClientException {
        Element rootElement = getElement("<root><foo>0</foo><foo>1</foo></root>");
        try {
            NexusResponseParser.findNode(rootElement, "foo");
            fail("NexusClientException expected, but not thrown");
        } catch (NexusClientException e) {
            assertThat(e.getMessage(), containsString("2"));
            assertThat(e.getMessage(), containsString("foo"));
            assertThat(e.getMessage(), containsString("root"));
        }
    }

    @Test
    public void testFindNode_NotAvailaible() throws SAXException, IOException, ParserConfigurationException,
            NexusClientException {
        Element rootElement = getElement("<root></root>");
        assertThat(NexusResponseParser.findNode(rootElement, "foo"), nullValue());
    }

    @Test
    public void testGetNodeTextContent() throws SAXException, IOException, ParserConfigurationException,
            NexusClientException {
        Element rootElement = getElement("<root><foo>bar</foo></root>");
        assertThat(NexusResponseParser.getNodeTextContent(rootElement, "foo"), is("bar"));
    }

    @Test
    public void testGetNodeTextContent_emptyContent() throws SAXException, IOException, ParserConfigurationException,
            NexusClientException {
        Element rootElement = getElement("<root><foo></foo></root>");
        assertThat(NexusResponseParser.getNodeTextContent(rootElement, "foo"), is(""));
    }

    @Test
    public void testGetNodeTextContent_blankContent() throws SAXException, IOException, ParserConfigurationException,
            NexusClientException {
        Element rootElement = getElement("<root><foo>   </foo></root>");
        assertThat(NexusResponseParser.getNodeTextContent(rootElement, "foo"), is("   "));
    }

    @Test
    public void testGetNodeTextContent_NodeNotExisting() throws SAXException, IOException,
            ParserConfigurationException, NexusClientException {
        Element rootElement = getElement("<root></root>");
        assertThat(NexusResponseParser.getNodeTextContent(rootElement, "foo"), nullValue());
    }

    @Test
    public void testGetNodeTextContentAsInt() throws SAXException, IOException, ParserConfigurationException,
            NexusClientException {
        Element rootElement = getElement("<root><foo>4711</foo></root>");
        assertThat(NexusResponseParser.getNodeTextContentAsInt(rootElement, "foo", 0), is(4711));
    }

    @Test
    public void testGetNodeTextContentAsInt_negativeValue() throws SAXException, IOException,
            ParserConfigurationException, NexusClientException {
        Element rootElement = getElement("<root><foo>0</foo></root>");
        assertThat(NexusResponseParser.getNodeTextContentAsInt(rootElement, "foo",0), is(0));
    }

    @Test
    public void testGetNodeTextContentAsInt_invalidInt() throws SAXException, IOException,
            ParserConfigurationException, NexusClientException {
        try {

            Element rootElement = getElement("<root><foo>bar4711</foo></root>");
            NexusResponseParser.getNodeTextContentAsInt(rootElement, "foo",0);
            fail("NexusClientException expected.");
        } catch (NexusClientException e) {
           assertThat(e.getMessage(), containsString("bar4711"));
           assertThat(e.getMessage(), containsString("not an Integer"));
        }


    }

    @Test
    public void testGetNodeTextContentAsBoolean_true() throws SAXException, IOException, ParserConfigurationException,
            NexusClientException {
        Element rootElement = getElement("<root><foo>true</foo></root>");
        assertThat(NexusResponseParser.getNodeTextContentAsBoolean(rootElement, "foo"), is(true));
    }

    @Test
    public void testGetNodeTextContentAsBoolean_true1() throws SAXException, IOException, ParserConfigurationException,
            NexusClientException {
        Element rootElement = getElement("<root><foo>TRUE</foo></root>");
        assertThat(NexusResponseParser.getNodeTextContentAsBoolean(rootElement, "foo"), is(true));
    }

    @Test
    public void testGetNodeTextContentAsBoolean_true2() throws SAXException, IOException, ParserConfigurationException,
            NexusClientException {
        Element rootElement = getElement("<root><foo>True</foo></root>");
        assertThat(NexusResponseParser.getNodeTextContentAsBoolean(rootElement, "foo"), is(true));
    }

    @Test
    public void testGetNodeTextContentAsBoolean_false() throws SAXException, IOException, ParserConfigurationException,
            NexusClientException {
        Element rootElement = getElement("<root><foo>false</foo></root>");
        assertThat(NexusResponseParser.getNodeTextContentAsBoolean(rootElement, "foo"), is(false));
    }

    @Test
    public void testGetNodeTextContentAsBoolean_false_1() throws SAXException, IOException,
            ParserConfigurationException, NexusClientException {
        Element rootElement = getElement("<root><foo>bar</foo></root>");
        assertThat(NexusResponseParser.getNodeTextContentAsBoolean(rootElement, "foo"), is(false));
    }

    @Test
    public void testGetNodeTextContentAsURL() throws SAXException, IOException, ParserConfigurationException,
            NexusClientException
    {
        Element rootElement = getElement("<root><foo>https://host.example.org/</foo></root>");
        assertThat(NexusResponseParser.getNodeTextContentAsURL(rootElement, "foo"), is(new URL(
                "https://host.example.org/")));
    }

    @Test
    public void testGetNodeTextContentAsURL_emptyUrl() throws SAXException, IOException, ParserConfigurationException,
            NexusClientException
    {
        Element rootElement = getElement("<root><foo>  </foo></root>");
        assertThat(NexusResponseParser.getNodeTextContentAsURL(rootElement, "foo"), nullValue());
    }

    @Test
    public void testGetNodeTextContentAsURL_invalid() throws SAXException, IOException, ParserConfigurationException,
            NexusClientException
    {
        Element rootElement = getElement("<root><foo>notAUrlString</foo></root>");
        try {

            NexusResponseParser.getNodeTextContentAsURL(rootElement, "foo");
            fail("Exception expected");
        } catch (NexusClientException e) {
            assertThat(e.getMessage(), containsString("not a valid URL"));
        }
    }

}
