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
package org.eclipse.skalli.core.group;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.SortedSet;

import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.services.group.GroupService;
import org.junit.Test;

@SuppressWarnings("nls")
public class GroupResourceTest {

    @Test
    public void testValidate_emptyGroupId() {
        GroupsResource groupResource = new GroupsResource();
        SortedSet<Issue> issues = groupResource.validate(
                new GroupsConfig(Arrays.asList(new GroupConfig("", Arrays.asList("jon")))), "admin");
        assertFalse(issues.isEmpty());
    }

    @Test
    public void testValidate_noAdmin() {
        final String loggedInUser = "admin";

        //no group at all
        GroupsResource groupResource = new GroupsResource();
        SortedSet<Issue> issues = groupResource.validate(new GroupsConfig(), loggedInUser);
        assertFalse(Issue.hasFatalIssues(issues));
        assertFalse(issues.isEmpty());

        //no admin group
        issues = groupResource.validate(
                new GroupsConfig(Arrays.asList(
                        new GroupConfig("dummy", Arrays.asList(loggedInUser)))),
                loggedInUser);
        assertFalse(Issue.hasFatalIssues(issues));
        assertFalse(issues.isEmpty());
    }

    @Test
    public void testValidate_ok() {
        final String loggedInUser = "admin";
        GroupsResource groupResource = new GroupsResource();
        SortedSet<Issue> issues = groupResource.validate(
                new GroupsConfig(Arrays.asList(
                        new GroupConfig(GroupService.ADMIN_GROUP, Arrays.asList(loggedInUser)),
                        new GroupConfig("myDummyGroup", Arrays.asList(loggedInUser)))),
                loggedInUser);
        assertTrue(issues.isEmpty());
    }

    @Test
    public void testValidate_memberEmpty() {
        final String loggedInUser = "admin";
        GroupsResource groupResource = new GroupsResource();
        SortedSet<Issue> issues = groupResource.validate(
                new GroupsConfig(Arrays.asList(new GroupConfig(GroupService.ADMIN_GROUP, Arrays
                        .asList(loggedInUser, "")))),
                loggedInUser);
        assertTrue(Issue.hasFatalIssues(issues));
    }

    @Test
    public void testValidate_groups_without_member() {
        final String loggedInUser = "admin";
        GroupsResource groupResource = new GroupsResource();
        SortedSet<Issue> issues = groupResource.validate(
                new GroupsConfig(Arrays.asList(//
                        new GroupConfig(GroupService.ADMIN_GROUP, Arrays.asList(loggedInUser)), //
                        new GroupConfig("emptyGroup", new ArrayList<String>())
                        )),
                loggedInUser);
        assertFalse(Issue.hasFatalIssues(issues));
        assertFalse(issues.isEmpty());
    }

    @Test
    public void testValidate_groups_doublye() {
        final String loggedInUser = "admin";
        GroupsResource groupResource = new GroupsResource();
        SortedSet<Issue> issues = groupResource.validate(
                new GroupsConfig(Arrays.asList(//
                        new GroupConfig(GroupService.ADMIN_GROUP, Arrays.asList(loggedInUser)), //
                        new GroupConfig("myDummyGroup", Arrays.asList("user1", "user2")),
                        new GroupConfig("myDummyGroup", Arrays.asList("user4", "user6"))
                        )),
                loggedInUser);
        assertTrue(Issue.hasFatalIssues(issues));
    }

}
