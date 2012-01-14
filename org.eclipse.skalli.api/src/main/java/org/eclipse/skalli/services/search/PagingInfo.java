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

    public PagingInfo(int start, int count) {
        super();
        this.start = start;
        this.count = count;
    }

    public int getStart() {
        return start;
    }

    public int getCount() {
        return count;
    }

}
