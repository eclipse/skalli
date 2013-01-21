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
package org.eclipse.skalli.core.persistence;

import java.util.Arrays;

import org.custommonkey.xmlunit.ComparisonController;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceConstants;
import org.custommonkey.xmlunit.DifferenceEngine;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.ElementNameQualifier;
import org.eclipse.skalli.commons.CollectionUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class XMLDiff implements ComparisonController, DifferenceListener {

    private static final int[] IGNORED_DIFFERENCES = CollectionUtils.asSortedArray(new int[] {
            // ignore all namespace, doctype etc stuff
            DifferenceConstants.HAS_DOCTYPE_DECLARATION_ID,
            DifferenceConstants.DOCTYPE_NAME_ID,
            DifferenceConstants.DOCTYPE_PUBLIC_ID_ID,
            DifferenceConstants.DOCTYPE_SYSTEM_ID_ID,
            DifferenceConstants.NODE_TYPE_ID,
            DifferenceConstants.NAMESPACE_PREFIX_ID,
            DifferenceConstants.NAMESPACE_URI_ID,
            DifferenceConstants.SCHEMA_LOCATION_ID,
            DifferenceConstants.NO_NAMESPACE_SCHEMA_LOCATION_ID,

            // ignore ordering of tags
            DifferenceConstants.CHILD_NODELIST_SEQUENCE_ID,

            // ignore all attribute related differences
            DifferenceConstants.ATTR_VALUE_ID,
            DifferenceConstants.ATTR_NAME_NOT_FOUND_ID,
            DifferenceConstants.ATTR_SEQUENCE_ID,
            DifferenceConstants.ATTR_VALUE_EXPLICITLY_SPECIFIED_ID,
            DifferenceConstants.ELEMENT_NUM_ATTRIBUTES_ID,

            // ignpre comments, CDATA sections etc.
            DifferenceConstants.COMMENT_VALUE_ID,
            DifferenceConstants.PROCESSING_INSTRUCTION_TARGET_ID,
            DifferenceConstants.PROCESSING_INSTRUCTION_DATA_ID,
            DifferenceConstants.CDATA_VALUE_ID });

    private boolean identical = true;

    @Override
    public int differenceFound(Difference difference) {
        int result = RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
        if (Arrays.binarySearch(IGNORED_DIFFERENCES, difference.getId()) < 0) {
            identical = false;
            result = RETURN_ACCEPT_DIFFERENCE;
        }
        return result;
    }

    @Override
    public void skippedComparison(Node node, Node node1) {
    }

    @Override
    public boolean haltComparison(Difference difference) {
        return !identical;
    }

    public boolean identical() {
        return identical;
    }

    public static boolean identical(Element newElement, Element oldElement) {
        if (newElement != null && oldElement == null || newElement == null && oldElement != null) {
            return false;
        }
        XMLDiff diff = new XMLDiff();
        DifferenceEngine engine = new DifferenceEngine(diff);
        engine.compare(newElement, oldElement, diff, new ElementNameQualifier());
        return diff.identical();
    }
}
