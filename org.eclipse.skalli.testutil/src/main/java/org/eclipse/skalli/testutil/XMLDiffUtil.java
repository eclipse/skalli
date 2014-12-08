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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.xml.transform.TransformerException;

import org.custommonkey.xmlunit.ComparisonController;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.DifferenceConstants;
import org.custommonkey.xmlunit.DifferenceEngine;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.XMLUnit;
import org.eclipse.skalli.commons.XMLUtils;
import org.w3c.dom.Document;

/**
 * Utility for determining the differences between XML documents with {@link XMLUnit}.
 */
@SuppressWarnings("nls")
public class XMLDiffUtil {

    /**
     * Combination of diff options for ignoring comments, processing instructions
     * and CDATA sections.
     */
    public static final XMLDiffOptions DEFAULT_DIFF_OPTIONS = new XMLDiffOptions(
            DifferenceConstants.COMMENT_VALUE_ID,
            DifferenceConstants.PROCESSING_INSTRUCTION_TARGET_ID,
            DifferenceConstants.PROCESSING_INSTRUCTION_DATA_ID,
            DifferenceConstants.CDATA_VALUE_ID);

    /**
     * Asserts that the given {@link Document documents} are {@link Diff#similar() similiar}
     * and {@link Diff#identical() identical}. Applies only {@link #DEFAULT_DIFF_OPTIONS default diff options}.
     *
     * @param expected  the expected document.
     * @param actual  the actual document produced by the test.
     */
    public static void assertEquals(Document expected, Document actual) {
        assertEquals(expected, actual, DEFAULT_DIFF_OPTIONS);
    }

    /**
     * Asserts that the given {@link Document documents} are {@link Diff#similar() similiar}
     * and {@link Diff#identical() identical}. Applies the given diff options.
     *
     * @param expected  the expected document.
     * @param actual  the actual document produced by the test.
     * @param diffOptions  the diff options to apply.
     */
    public static void assertEquals(Document expected, Document actual, XMLDiffOptions diffOptions) {
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        DifferenceEngine engine = new DifferenceEngine(new XMLDiffOptions(diffOptions));
        Diff diff = new Diff(expected, actual, engine);
        diff.overrideDifferenceListener(diffOptions);
        assertSimilar(expected, actual, diff);
        assertIdentical(expected, actual, diff);
    }

    /**
     * Asserts that the given {@link Document documents} are {@link Diff#similar() similiar}
     * and {@link Diff#identical() identical}.
     *
     * @param expected  the expected document.
     * @param actual  the actual document produced by the test.
     * @param diffListener  the difference listener to apply.
     */
    public static void assertEquals(Document expected, Document actual, DifferenceListener diffListener) {
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        Diff diff = new Diff(expected, actual);
        diff.overrideDifferenceListener(diffListener);
        assertSimilar(expected, actual, diff);
        assertIdentical(expected, actual, diff);
    }

    /**
     * Asserts that the given {@link Document documents} are {@link Diff#similar() similiar}
     * according to the given {@link Diff}.
     *
     * @param expected  the expected document.
     * @param actual  the actual document produced by the test.
     * @param diff  the {@link Diff} of the documents.
     */
    public static void assertSimilar(Document expected, Document actual, Diff diff) {
        assertTrue(detailsToString(expected, actual, diff), diff.similar());
    }

    /**
     * Asserts that the given {@link Document documents} are {@link Diff#identical() identical}
     * according to the given {@link Diff}.
     *
     * @param expected  the expected document.
     * @param actual  the actual document produced by the test.
     * @param diff  the {@link Diff} of the documents.
     */
    public static void assertIdentical(Document expected, Document actual, Diff diff) {
        assertTrue(detailsToString(expected, actual, diff), diff.identical());
    }

    /**
     * Renders a diff report for the given  {@link Document documents}.
     *
     * @param expected  the expected document.
     * @param actual  the actual document produced by the test.
     * @param diff  the {@link Diff} of the documents.
     * @return  the difference of the documents, as well as the string representations
     * of both documents.
     */
    public static String detailsToString(Document expected, Document actual, Diff diff) {
        StringBuffer sb = new StringBuffer();
        diff.appendMessage(sb);

        try {
            if (expected != null) {
                sb.append("\nExpected:\n");
                sb.append(XMLUtils.documentToString(expected)).append("\n");
            }
            if (actual != null) {
                sb.append("\nActual:\n");
                sb.append(XMLUtils.documentToString(actual)).append("\n");
            }
        } catch (TransformerException e) {
            fail("Failed to render an XML document:" + e.getMessageAndLocation());
        }
        return sb.toString();
    }
}
