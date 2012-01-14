/*******************************************************************************
 * Copyright (c) 2010 - 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.model.ext.maven;

import java.util.Comparator;

import org.apache.commons.lang.StringUtils;

/**
 *  A "simplified" comparator for Maven versions.
 *
 *  The version string is split at '.' and '-' characters.
 *  Parts that are numbers are compared numerically. All other parts are compared like strings.
 */
class MavenVersionsComparator implements Comparator<String> {

    enum SortOrder {
        ASCENDING,
        DESCENDING
    }

    private SortOrder order = SortOrder.ASCENDING;

    public MavenVersionsComparator() {
        this(SortOrder.ASCENDING);
    }

    public MavenVersionsComparator(SortOrder order) {
        this.order = order;
    }

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(String version1, String version2) {
        if (SortOrder.DESCENDING.equals(this.order)) {
            return -1 * compareLatestAsLowest(version1, version2);
        } else {
            return compareLatestAsLowest(version1, version2);
        }
    };

    public int compareLatestAsLowest(String version1, String version2) {
        if (version1.equals(version2)) {
            return 0;
        }

        String[] v1Parts = StringUtils.split(version1, ".-");
        String[] v2Parts = StringUtils.split(version2, ".-");

        int minLength = Math.min(v1Parts.length, v2Parts.length);

        for (int i = 0; i < minLength; i++) {
            String str1 = v1Parts[i];
            String str2 = v2Parts[i];
            try {
                int val1 = Integer.valueOf(str1);
                int val2 = Integer.valueOf(str2);
                int result = val1 < val2 ? -1 : ((val1 == val2) ? 0 : +1);
                if (result != 0) {
                    return result;
                }
            } catch (NumberFormatException e) {
                int result = str1.compareTo(str2);
                if (result != 0) {
                    return result;
                }
            }
        }

        if (version1.length() == version2.length()) {
            return 0;
        } else if (version1.length() < version2.length()) {
            return -1;
        } else {
            return +1;
        }

    }
}
