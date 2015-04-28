/*******************************************************************************
 * Copyright (c) 2010-2015 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.gerrit.client;

import org.apache.commons.lang.StringUtils;

@SuppressWarnings("nls")
public enum GerritVersion {
    GERRIT_UNKNOWN_VERSION(""),
    GERRIT_2_0_X("2.0"), GERRIT_2_1_7("2.1.7"), GERRIT_2_1_8("2.1.8"), GERRIT_2_1_X("2.1"), GERRIT_2_2_2("2.2.2"),
    GERRIT_2_2_X("2.2"), GERRIT_2_3_X("2.3"), GERRIT_2_4_X("2.4"), GERRIT_2_5_X("2.5"), GERRIT_2_6_X("2.6"),
    GERRIT_2_7_X("2.7"), GERRIT_2_8_X("2.8"), GERRIT_2_9_X("2.9"), GERRIT_2_10_X("2.10"), GERRIT_2_11_X("2.11");

    private String versionString;

    private GerritVersion(String versionString) {
        this.versionString = versionString;
    }

    public static GerritVersion asGerritVersion(String s) {
        if (StringUtils.isBlank(s)) {
            return GERRIT_UNKNOWN_VERSION;
        }
        for (GerritVersion next: values()) {
            if (s.equals(next.versionString) || s.startsWith(next.versionString + ".")
                    || s.startsWith(next.versionString + "-")) {
                return next;
            }
        }
        // fall back to a reasonable default version
        return GERRIT_2_4_X;
    }

    public boolean supports(GerritFeature feature) {
        switch (feature) {
        case REF_RIGHTS_TABLE:
        case PROJECTS_TABLE:
            return compareTo(GERRIT_2_2_X) < 0 ;
        case LS_GROUPS:
        case LS_PROJECTS_TYPE_ATTR:
           return compareTo(GERRIT_2_2_2) >= 0 ;
        case LS_PROJECTS_ALL_ATTR:
        case LS_GROUPS_PROJECT_ATTR:
        case LS_GROUPS_VISIBLE_TO_ALL_ATTR:
        case LS_GROUPS_TYPE_ATTR:
        case LS_GROUPS_USER_ATTR:
        case RENAME_GROUP:
        case CREATE_PROJECT_SUGGEST_PARENT:
            return compareTo(GERRIT_2_3_X) >= 0 ;
        case ACCOUNT_CHECK_OBSOLETE:
            return compareTo(GERRIT_2_1_7) >= 0 ;
        case SUPPORTS_REST:
            return compareTo(GERRIT_2_6_X) >= 0 ;
        default:
            return false;
        }
    }
}