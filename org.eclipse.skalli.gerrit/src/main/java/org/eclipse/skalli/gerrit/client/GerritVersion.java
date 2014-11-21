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
package org.eclipse.skalli.gerrit.client;

import org.apache.commons.lang.StringUtils;


public enum GerritVersion {
    GERRIT_UNKNOWN_VERSION,
    GERRIT_2_0_X, GERRIT_2_1_X, GERRIT_2_1_7, GERRIT_2_1_8, GERRIT_2_2_X,
    GERRIT_2_2_2, GERRIT_2_3_X, GERRIT_2_4_X, GERRIT_2_5_X, GERRIT_2_6_X,
    GERRIT_2_7_X;

    @SuppressWarnings("nls")
    public static GerritVersion asGerritVersion(String s) {
        if (StringUtils.isBlank(s)) {
            return GERRIT_UNKNOWN_VERSION;
        }
        if (s.equals("2.1.7") || s.startsWith("2.1.7.") || s.startsWith("2.1.7-")) {
            return GERRIT_2_1_7;
        }
        if (s.equals("2.1.8") || s.startsWith("2.1.8.") || s.startsWith("2.1.8-")) {
            return GERRIT_2_1_8;
        }
        if (s.equals("2.2.2") || s.startsWith("2.2.2.") || s.startsWith("2.2.2-")) {
            return GERRIT_2_2_2;
        }
        if (s.equals("2.0") || s.startsWith("2.0.") || s.startsWith("2.0-")) {
            return GERRIT_2_0_X;
        }
        if (s.equals("2.1") || s.startsWith("2.1.") || s.startsWith("2.1-")) {
            return GERRIT_2_1_X;
        }
        if (s.equals("2.2") || s.startsWith("2.2.") || s.startsWith("2.2-")) {
            return GERRIT_2_2_X;
        }
        if (s.equals("2.3") || s.startsWith("2.3.") || s.startsWith("2.3-")) {
            return GERRIT_2_3_X;
        }
        if (s.equals("2.4") || s.startsWith("2.4.") || s.startsWith("2.4-")) {
            return GERRIT_2_4_X;
        }
        if (s.equals("2.5") || s.startsWith("2.5.") || s.startsWith("2.5-")) {
            return GERRIT_2_5_X;
        }
        if (s.equals("2.6") || s.startsWith("2.6.") || s.startsWith("2.6-")) {
            return GERRIT_2_6_X;
        }
        if (s.equals("2.7") || s.startsWith("2.7.") || s.startsWith("2.7-")) {
            return GERRIT_2_7_X;
        }
        // fall back to a resonable default version
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