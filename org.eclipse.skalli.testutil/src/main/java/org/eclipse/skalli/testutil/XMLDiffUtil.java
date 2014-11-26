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
package org.eclipse.skalli.testutil;

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Assert;
import org.w3c.dom.Document;

@SuppressWarnings("nls")
public class XMLDiffUtil {

    /**
     * Asserts that the given {@link Document documents} are equal.
     */
    public static void assertEquals(Document docLeft, Document docRight, boolean ignoreWhitespace) throws Exception {
        XMLUnit.setIgnoreWhitespace(ignoreWhitespace);
        Diff diff = XMLUnit.compareXML(docRight, docLeft);
        Assert.assertTrue(detailsToString(diff, docRight, docLeft), diff.similar());
        Assert.assertTrue(detailsToString(diff, docRight, docLeft), diff.identical());
    }

    protected static String detailsToString(Diff diff, Document controlDoc, Document modifiedDoc) throws Exception {
        StringBuffer sb = new StringBuffer();
        diff.appendMessage(sb);

        if (controlDoc != null) {
            sb.append("\nControl document:\n");
            sb.append(toString(controlDoc)).append("\n");
        }
        if (modifiedDoc != null) {
            sb.append("\nModified document:\n");
            sb.append(toString(modifiedDoc)).append("\n");
        }

        return sb.toString();
    }

    protected static String toString(Document doc) throws Exception {
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        StringWriter writer = new StringWriter();
        Result result = new StreamResult(writer);
        Source source = new DOMSource(doc);
        transformer.transform(source, result);
        writer.close();

        return writer.toString();
    }

    protected static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        return docBuilder;
    }
}
