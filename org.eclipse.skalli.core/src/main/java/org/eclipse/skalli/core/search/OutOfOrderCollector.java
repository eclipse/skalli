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
package org.eclipse.skalli.core.search;

import java.io.IOException;

class OutOfOrderCollector extends TopScoreCollector {
    OutOfOrderCollector(int numHits) {
        super(numHits);
    }

    @Override
    public void collect(int doc) throws IOException {
        float score = scorer.score();

        assert !Float.isNaN(score);

        totalHits++;
        doc += docBase;
        if (score < pqTop.score || (score == pqTop.score && doc > pqTop.doc)) {
            return;
        }
        pqTop.doc = doc;
        pqTop.score = score;
        pqTop = pq.updateTop();
    }

    @Override
    public boolean acceptsDocsOutOfOrder() {
        return true;
    }
}
