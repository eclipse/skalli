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
package org.eclipse.skalli.services.tagging;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;

/**
 * Helper class to report the number of occurences of a given tag.
 */
public class TagCount implements Comparable<TagCount> {

    private String name;
    private int count;

    /**
     * Creates a <code>TagCounty</code> instance with the given tag name
     * and number of occurences.
     *
     * @param name  the name of the tag, never <code>null</code> or blank.
     * @param count  the number of occurences of the tag, or zero.
     *
     */
    public TagCount(String name, int count) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("tag name must not be blank");
        }
        if (count < 0) {
            throw new IllegalArgumentException("tag count must be a positive number");
        }
        this.name = name;
        this.count = count;
    }

    /**
     * Returns the tag name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the number of occurences of the tag.
     * @return
     */
    public int getCount() {
        return count;
    }

    /**
     * Converts the given collection of <code>TagCount</code> values into a
     * map of tag name/number pairs sorted alphanumerically by tag name.
     *
     * @param entries  the collection of tag counts to convert.
     * @return the resulting map, or an empty map.
     */
    public static SortedMap<String,Integer> asMap(Collection<TagCount> entries) {
        TreeMap<String,Integer> map = new TreeMap<String,Integer>();
        if (entries != null) {
            for (TagCount entry: entries) {
                map.put(entry.getName(), entry.getCount());
            }
        }
        return map;
    }

    @Override
    public int hashCode() {
        int result = 31 + count;
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof TagCount) {
            return compareTo((TagCount)o) == 0;
        }
        return false;
    }

    /**
     * This method sorts first by decreasing {@link #getCount() tag count},
     * then alphanumerically by {@link #getName() tag name}.
     */
    @Override
    public int compareTo(TagCount o) {
        // sort by reverse order of count
        int result = Integer.signum(o.count - count);
        if (result == 0) {
            // then alphanumerically
            result = name.compareTo(o.name);
        }
        return result;
    }

    @Override
    public String toString() {
        return MessageFormat.format("{0} ({1})", name, count); //$NON-NLS-1$
    }
}
