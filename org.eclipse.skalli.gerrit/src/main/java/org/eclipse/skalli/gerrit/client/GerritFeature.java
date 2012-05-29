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
    CREATE_PROJECT_SUGGEST_PARENT       // create-project --suggest-parents attribute available
}