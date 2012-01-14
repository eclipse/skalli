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
package org.eclipse.skalli.commons;

import java.util.Iterator;
import java.util.SortedSet;

public class ComparatorUtils {

    public static boolean equals(Object o1, Object o2) {
        if (o1 == o2) {
            return true;
        }
        if (o1 == null) {
            if (o2 != null) {
                return false;
            }
        }
        return o1.equals(o2);
    }

    /**
     * Compares the given objects by comparing their string representations by evaluating
     * <code>o1.toString().compareTo(o2.toString())</code>.
     * This method accepts <code>null</code> pointers for both arguments and <code>null</code>
     * is always lower than any non-null argument.
     */
    public static int compareAsStrings(Object o1, Object o2) {
        int result = 0;
        boolean thisDefined = o1 != null;
        boolean otherDefined = o2 != null;
        if (thisDefined) {
            result = otherDefined ? o1.toString().compareTo(o2.toString()) : 1;
        } else {
            result = otherDefined ? -1 : 0;
        }
        return result;
    }

    /**
     * Compares instances of {@link Comparable} by evaluating <code>o1.compareTo(o2)</code>.
     * This method accepts <code>null</code> pointers for both arguments and <code>null</code>
     * is always lower than any non-null argument.
     */
    public static <T extends Comparable<? super T>> int compare(T o1, T o2) {
        int result = 0;
        boolean thisDefined = o1 != null;
        boolean otherDefined = o2 != null;
        if (thisDefined) {
            result = otherDefined ? o1.compareTo(o2) : 1;
        } else {
            result = otherDefined ? -1 : 0;
        }
        return result;
    }

    /**
     * Compares instances of {@link SortedSet} by comparing their elements in the order given by
     * their iterators.
     * This method accepts <code>null</code> pointers for both arguments and <code>null</code>
     * is always lower than any non-null argument. An empty set is always lower then any
     * non-empty set. Non-empty sets are compared element by element where the first non-equal
     * element decides the comparision. The {@link #compare(Comparable, Comparable)} method
     * is used to compare elements, i.e. <code>null</code> elements in either of the sets
     * are supported.
     */
    public static <T extends Comparable<? super T>> int compare(SortedSet<T> o1, SortedSet<T> o2) {
        int result = 0;
        boolean thisDefined = o1 != null;
        boolean otherDefined = o2 != null;
        if (thisDefined) {
            if (otherDefined) {
                Iterator<T> it1 = o1.iterator();
                Iterator<T> it2 = o2.iterator();
                while (result == 0 && it1.hasNext() && it2.hasNext()) {
                    result = compare(it1.next(), it2.next());
                }
                if (result == 0) {
                    result = it1.hasNext()? 1 : (it2.hasNext()? -1 : 0);
                }
            } else {
                result = 1;
            }
        } else {
            result = otherDefined ? -1 : 0;
        }
        return result;
    }
}
