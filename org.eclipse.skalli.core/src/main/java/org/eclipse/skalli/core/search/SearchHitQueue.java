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
package org.eclipse.skalli.core.search;

import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.util.PriorityQueue;

final class SearchHitQueue extends PriorityQueue<ScoreDoc> {

    private boolean prePopulate;

    SearchHitQueue(int size, boolean prePopulate) {
        this.prePopulate = prePopulate;
        initialize(size);
    }

    @Override
    protected ScoreDoc getSentinelObject() {
        return !prePopulate ? null : new ScoreDoc(Integer.MAX_VALUE, Float.NEGATIVE_INFINITY);
    }

    @Override
    protected boolean lessThan(ScoreDoc hitA, ScoreDoc hitB) {
        if (hitA.score == hitB.score) {
            return hitA.doc > hitB.doc;
        } else {
            return hitA.score < hitB.score;
        }
    }
}
