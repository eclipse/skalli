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

import java.util.Arrays;

import org.custommonkey.xmlunit.ComparisonController;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceListener;
import org.w3c.dom.Node;

public class XMLDiffOptions implements ComparisonController, DifferenceListener {

    private int[] list = new int[8];
    private int size = 0;

    private boolean identical = true;

    public XMLDiffOptions(int...options) {
        append(options);
    }

    public XMLDiffOptions(XMLDiffOptions base, int...options) {
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