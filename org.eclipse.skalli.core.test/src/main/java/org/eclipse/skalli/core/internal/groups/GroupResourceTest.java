package org.eclipse.skalli.core.internal.groups;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.skalli.model.ValidationException;
import org.eclipse.skalli.services.group.GroupService;
import org.junit.Before;
import org.junit.Test;

public class GroupResourceTest {

    private static GroupsResource groupResource;

    @Before
    public void before() {
        groupResource = new GroupsResource();
    }

    @Test
    public void testValidate_emptyGroupId() {
        ValidationException exceptions = groupResource.validate(
                new GroupsConfig(Arrays.asList(new GroupConfig("", Arrays.asList("jon")))), "admin");
        assertTrue(exceptions.hasFatalIssues());

    }

    @Test
    public void testValidate_noAdmin() {
        final String loggedInUser = "admin";

        //no group at all
        ValidationException exceptions = groupResource.validate(new GroupsConfig(), loggedInUser);
        assertFalse(exceptions.hasFatalIssues());
        assertTrue(exceptions.hasIssues());

        //no admin group
        exceptions = groupResource.validate(
                new GroupsConfig(Arrays.asList(
                        new GroupConfig("dummy", Arrays.asList(loggedInUser)))),
                loggedInUser);
        assertFalse(exceptions.hasFatalIssues());
        assertTrue(exceptions.hasIssues());
    }

    @Test
    public void testValidate_ok() {
        final String loggedInUser = "admin";

        ValidationException exceptions = groupResource.validate(
                new GroupsConfig(Arrays.asList(
                        new GroupConfig(GroupService.ADMIN_GROUP, Arrays.asList(loggedInUser)),
                        new GroupConfig("myDummyGroup", Arrays.asList(loggedInUser)))),
                loggedInUser);
        assertFalse(exceptions.hasIssues());
    }

    @Test
    public void testValidate_memberEmpty() {
        final String loggedInUser = "admin";

        ValidationException exceptions = groupResource.validate(
                new GroupsConfig(Arrays.asList(new GroupConfig(GroupService.ADMIN_GROUP, Arrays
                        .asList(loggedInUser, "")))),
                loggedInUser);
        assertTrue(exceptions.hasFatalIssues());
    }

    @Test
    public void testValidate_groups_without_member() {
        final String loggedInUser = "admin";

        ValidationException exceptions = groupResource.validate(
                new GroupsConfig(Arrays.asList(//
                        new GroupConfig(GroupService.ADMIN_GROUP, Arrays.asList(loggedInUser)), //
                        new GroupConfig("emptyGroup", new ArrayList<String>())
                        )),
                loggedInUser);
        assertFalse(exceptions.hasFatalIssues());
        assertTrue(exceptions.hasIssues());
    }

    @Test
    public void testValidate_groups_doublye() {
        final String loggedInUser = "admin";

        ValidationException exceptions = groupResource.validate(
                new GroupsConfig(Arrays.asList(//
                        new GroupConfig(GroupService.ADMIN_GROUP, Arrays.asList(loggedInUser)), //
                        new GroupConfig("myDummyGroup", Arrays.asList("user1", "user2")),
                        new GroupConfig("myDummyGroup", Arrays.asList("user4", "user6"))
                        )),
                loggedInUser);
        assertTrue(exceptions.hasFatalIssues());
    }

}
