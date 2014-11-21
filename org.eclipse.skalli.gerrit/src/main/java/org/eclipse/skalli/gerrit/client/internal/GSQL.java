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
package org.eclipse.skalli.gerrit.client.internal;

/**
 * Wrapper for GSQL specifics that we need to use unless there is an enriched Gerrit API
 */
public final class GSQL {

    private GSQL() {

    }

    enum Tables {
        ACCOUNTS,
        ACCOUNT_AGREEMENTS,
        ACCOUNT_DIFF_PREFERENCES,
        ACCOUNT_EXTERNAL_IDS,
        ACCOUNT_GROUPS,
        ACCOUNT_GROUP_AGREEMENTS,
        ACCOUNT_GROUP_MEMBERS,
        ACCOUNT_GROUP_MEMBERS_AUDIT,
        ACCOUNT_GROUP_NAMES,
        ACCOUNT_PATCH_REVIEWS,
        ACCOUNT_PROJECT_WATCHES,
        ACCOUNT_SSH_KEYS,
        APPROVAL_CATEGORIES,
        APPROVAL_CATEGORY_VALUES,
        CHANGES,
        CHANGE_MESSAGES,
        CONTRIBUTOR_AGREEMENTS,
        PATCH_COMMENTS,
        PATCH_SETS,
        PATCH_SET_ANCESTORS,
        PATCH_SET_APPROVALS,
        PROJECTS,
        REF_RIGHTS,
        SCHEMA_VERSION,
        STARRED_CHANGES,
        SYSTEM_CONFIG,
        TRACKING_IDS;

        @Override
        public String toString() {
            return name();
        }
    }

    enum ResultFormat {
        PRETTY, JSON
    }

}
