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
package org.eclipse.skalli.services.search;

public class PagingInfo {

    private final int start;
    private final int count;

    /**
     * Creates a paging info. Adjusts the given parameters if necessary:
     * <ol>
     * <li>the sum of <tt>start</tt> and </tt>count</tt> is lower or equal {@link Integer#MAX_VALUE}</li>
     * <li><tt>start</tt> is at greater equal zero</li>
     * <li><tt>count</tt> lower zero is interpreted as the maximum possible number of page elements</li>
     * </ol>
     *
     * @param start  the first page element.
     * @param count  the number of page elements.
     */
    public PagingInfo(int start, int count) {
        this.start = start < 0 ? 0 : start;
        int maxCount = Integer.MAX_VALUE - this.start;
        this.count = count < 0? maxCount : Math.min(count, maxCount);
    }

    public int getStart() {
        return start;
    }

    public int getCount() {
        return count;
    }

}
