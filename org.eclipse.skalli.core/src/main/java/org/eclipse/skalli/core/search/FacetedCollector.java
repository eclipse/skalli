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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;

public class FacetedCollector extends OutOfOrderCollector {

    private Map<String, String[]> fieldCaches;

    private Map<String, Map<String, Integer>> facetsMap;

    public FacetedCollector(String[] fields, IndexReader reader, int numHits) throws IOException {
        super(/*reader, sort, */numHits);

        this.facetsMap = new HashMap<String, Map<String, Integer>>();
        this.fieldCaches = new HashMap<String, String[]>();

        for (String field : fields) {
            facetsMap.put(field, new HashMap<String, Integer>());
            fieldCaches.put(field, FieldCache.DEFAULT.getStrings(reader, field));
        }
    }

    @Override
    public void collect(int doc) throws IOException {
        super.collect(doc);

        for (Entry<String, Map<String, Integer>> e : facetsMap.entrySet()) {
            Map<String, Integer> values = e.getValue();
            String value = fieldCaches.get(e.getKey())[doc];

            if (value != null) {
                Integer count = values.get(value);

                if (count == null) {
                    values.put(value, 1);
                } else {
                    values.put(value, count + 1);
                }
            }
        }
    }

    public Map<String, Map<String, Integer>> getFacetsMap() {
        return facetsMap;
    }

}
