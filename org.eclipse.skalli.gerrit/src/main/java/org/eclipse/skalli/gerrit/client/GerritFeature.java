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

public enum GerritFeature {
    REF_RIGHTS_TABLE,                   // REF_RIGTHS table is available
    PROJECTS_TABLE,                     // PROJECTS table is available
    LS_PROJECTS_TYPE_ATTR,              // ls-projects --type attribute available
    LS_PROJECTS_ALL_ATTR,               // ls-projects --all attribute available
    LS_GROUPS,                          // ls-groups command available
    LS_GROUPS_PROJECT_ATTR,             // ls-groups --project attribute available
    LS_GROUPS_VISIBLE_TO_ALL_ATTR,      // ls-groups --visible-to-all attribute available
    LS_GROUPS_TYPE_ATTR,                // ls-groups --type attribute available
    LS_GROUPS_USER_ATTR,                // ls-groups --user attribute available
    RENAME_GROUP,                       // rename-group command available
    CREATE_PROJECT_SUGGEST_PARENT,      // create-project --suggest-parents attribute available
    ACCOUNT_CHECK_OBSOLETE,             // account check for --member options of create-group obsolete
    SUPPORTS_REST,                      // supports new HTTP-based REST API
    CREATE_PROJECT_MAX_SIZE,            // create-project supports --max-object-size-limit
    CREATE_PROJECT_PLUGIN_CONFIG        // create-project supports --plugin-config
}