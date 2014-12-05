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

import java.util.Arrays;

import javax.xml.transform.TransformerException;

import org.custommonkey.xmlunit.ComparisonController;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceConstants;
import org.custommonkey.xmlunit.DifferenceEngine;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.XMLUnit;
import org.eclipse.skalli.commons.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

@SuppressWarnings("nls")
public class XMLDiffUtil {

    public static class DiffOptions implements ComparisonController, DifferenceListener {

        private int[] list = new int[8];
        private int size = 0;

        private boolean identical = true;

        public DiffOptions(int...options) {
            append(options);
        }

        public DiffOptions(DiffOptions base, int...options) {
            append(base.list);
            append(options);
        }

        public void append(int... options) {
            if (options != null && options.length > 0) {
                if (size + options.length > list.length) {
                    int[] grown = new int[list.length + options.length];
                    System.arraycopy(list, 0, grown, 0, list.length);
                    list = grown;
                }
                System.arraycopy(list, size, options, 0, options.length);
                size += options.length;
                Arrays.sort(list);
            }
        }

        @Override
        public int differenceFound(Difference difference) {
            int result = RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
            if (Arrays.binarySearch(list, difference.getId()) < 0) {
                identical = false;
                result = RETURN_ACCEPT_DIFFERENCE;
            }
            return result;
        }

        @Override
        public void skippedComparison(Node control, Node test) {
        }

        @Override
        public boolean haltComparison(Difference afterDifference) {
            return !identical;
        }

        public boolean identical() {
            return identical;
        }
    }

    /**
     * Ignore comments, processing instructions and CDATA sections.
     */
    public static final DiffOptions DEFAULT_DIFF_OPTIONS = new DiffOptions(
            DifferenceConstants.COMMENT_VALUE_ID,
            DifferenceConstants.PROCESSING_INSTRUCTION_TARGET_ID,
            DifferenceConstants.PROCESSING_INSTRUCTION_DATA_ID,
            DifferenceConstants.CDATA_VALUE_ID);

    /**
     * Asserts that the given {@link Document documents} are {@link Diff#similar() similiar}
     * and {@link Diff#identical() identical}.
     *
     * @param expected  the expected document.
     * @param actual  the actual document produced by the test.
     */
    public static void assertEquals(Document expected, Document actual) {
        XMLUnit.setIgnoreWhitespace(true);
        DifferenceEngine engine = new DifferenceEngine(new DiffOptions(DEFAULT_DIFF_OPTIONS,
                DifferenceConstants.CHILD_NODELIST_SEQUENCE_ID));
        Diff diff = new Diff(expected, actual, engine);
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
