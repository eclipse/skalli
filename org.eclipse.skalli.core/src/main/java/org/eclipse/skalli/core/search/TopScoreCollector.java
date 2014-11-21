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

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopDocsCollector;

public abstract class TopScoreCollector extends TopDocsCollector<ScoreDoc> {

    ScoreDoc pqTop;
    int docBase = 0;
    Scorer scorer;

    TopScoreCollector(int numHits) {
        super(new SearchHitQueue(numHits, true));
        pqTop = pq.top();
    }

    @Override
    protected TopDocs newTopDocs(ScoreDoc[] results, int start) {
        if (results == null) {
            return EMPTY_TOPDOCS;
        }

        float maxScore = Float.NaN;
        if (start == 0) {
            maxScore = results[0].score;
        } else {
            for (int i = pq.size(); i > 1; i--) {
                pq.pop();
            }
            maxScore = pq.pop().score;
        }

        return new TopDocs(totalHits, results, maxScore);
    }

    @Override
    public void setNextReader(IndexReader reader, int base) {
        docBase = base;
    }

    @Override
    public void setScorer(Scorer scorer) throws IOException {
        this.scorer = scorer;
    }
}
