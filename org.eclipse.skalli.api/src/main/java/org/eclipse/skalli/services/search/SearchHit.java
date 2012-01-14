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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.skalli.model.EntityBase;

public final class SearchHit<T extends EntityBase> {

    private T entity;
    private Map<String, List<String>> storedValues;
    private Map<String, List<String>> highlightedValues;
    private Float score;

    public SearchHit(T entity, Map<String, List<String>> storedValues, Map<String, List<String>> highlightedValues) {
       this(entity,storedValues, null, highlightedValues);
   }

    public SearchHit(T entity, Map<String, List<String>> storedValues, Float score, Map<String, List<String>> highlightedValues) {
        this.entity = entity;
        this.storedValues = storedValues;
        this.highlightedValues = highlightedValues;
        this.score= score;
    }

    public Float getScore() {
        return score;
    }

    public String getValue(String key, boolean highlighted) {
        List<String> values = getMultiValue(key, highlighted);
        if (values != null && values.size() > 0) {
            return values.get(0);
        } else {
            return null;
        }
    }

    public Map<String, String> getSingleValuesHighlighted() {
        Map<String, String> ret = new HashMap<String, String>();
        for (Entry<String, List<String>> entry : highlightedValues.entrySet()) {
            ret.put(entry.getKey(), entry.getValue().get(0));
        }
        return ret;
    }

    public Map<String, String> getSingleValues() {
        Map<String, String> ret = new HashMap<String, String>();
        for (Entry<String, List<String>> entry : storedValues.entrySet()) {
            ret.put(entry.getKey(), entry.getValue().get(0));
        }
        return ret;
    }

    public Map<String, List<String>> getMultiValues() {
        return storedValues;
    }

    public Map<String, List<String>> getMultiValuesHighlighted() {
        return highlightedValues;
    }

    public List<String> getMultiValue(String key, boolean highlighted) {
        List<String> values;
        if (highlighted) {
            values = highlightedValues.get(key);
        } else {
            values = storedValues.get(key);
        }
        return values;
    }

    public T getEntity() {
        return entity;
    }

}
